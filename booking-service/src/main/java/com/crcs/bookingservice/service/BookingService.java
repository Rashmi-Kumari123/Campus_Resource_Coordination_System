package com.crcs.bookingservice.service;

import com.crcs.bookingservice.dto.request.CreateBookingRequestDTO;
import com.crcs.bookingservice.dto.request.UpdateBookingStatusRequestDTO;
import com.crcs.bookingservice.dto.response.AvailabilityCheckResponseDTO;
import com.crcs.bookingservice.dto.response.BookingResponseDTO;
import com.crcs.bookingservice.dto.response.PageResponseDTO;
import com.crcs.bookingservice.dto.response.ResourceResponseDTO;
import com.crcs.bookingservice.dto.response.UserProfileResponseDTO;
import com.crcs.bookingservice.model.Booking;
import com.crcs.bookingservice.repository.BookingRepository;
import com.crcs.kafka.producer.KafkaEventProducer;
import com.crcs.common.dto.notification.KafkaEvent;
import com.crcs.common.dto.notification.Notification;
import com.crcs.common.dto.notification.NotificationChannel;
import com.crcs.common.dto.notification.NotificationCommunicationParams;
import com.crcs.common.dto.notification.NotificationData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingService {
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    private final BookingRepository bookingRepository;
    private final ResourceServiceClient resourceServiceClient;
    private final UserServiceClient userServiceClient;
    private final KafkaEventProducer kafkaEventProducer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BookingService(BookingRepository bookingRepository,
                         ResourceServiceClient resourceServiceClient,
                         UserServiceClient userServiceClient,
                         KafkaEventProducer kafkaEventProducer) {
        this.bookingRepository = bookingRepository;
        this.resourceServiceClient = resourceServiceClient;
        this.userServiceClient = userServiceClient;
        this.kafkaEventProducer = kafkaEventProducer;
    }

    /** Placeholder/invalid email domains that must not be used for notifications (e.g. from API docs). */
    private static final List<String> PLACEHOLDER_EMAIL_DOMAINS = List.of("example.com", "example.org");

    private static boolean isPlaceholderEmail(String email) {
        if (email == null || !email.contains("@")) return true;
        String domain = email.substring(email.indexOf('@') + 1).toLowerCase();
        return PLACEHOLDER_EMAIL_DOMAINS.stream().anyMatch(domain::equals);
    }

    public AvailabilityCheckResponseDTO checkAvailability(String resourceId, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
        ResourceResponseDTO resource = resourceServiceClient.getResourceById(resourceId);
        if (resource == null) {
            return new AvailabilityCheckResponseDTO(false, resourceId, startTime, endTime, "Resource not found");
        }

        if (resource.getStatus() != com.crcs.bookingservice.dto.response.ResourceResponseDTO.ResourceStatus.AVAILABLE) {
            return new AvailabilityCheckResponseDTO(false, resourceId, startTime, endTime, "Resource is not available");
        }

        List<Booking> conflictingBookings = bookingRepository.findConflictingBookings(resourceId, startTime, endTime);
        if (!conflictingBookings.isEmpty()) {
            return new AvailabilityCheckResponseDTO(false, resourceId, startTime, endTime, "Resource is already booked for this time slot");
        }

        return new AvailabilityCheckResponseDTO(true, resourceId, startTime, endTime, "Resource is available");
    }

    @Transactional
    public Optional<BookingResponseDTO> createBooking(String userId, CreateBookingRequestDTO request) {
        // Check availability
        AvailabilityCheckResponseDTO availability = checkAvailability(request.getResourceId(), request.getStartTime(), request.getEndTime());
        if (!availability.isAvailable()) {
            logger.warn("Resource not available: {}", availability.getMessage());
            return Optional.empty();
        }

        // Get resource details
        ResourceResponseDTO resource = resourceServiceClient.getResourceById(request.getResourceId());
        if (resource == null) {
            return Optional.empty();
        }

        // Create booking as PENDING (requires FACILITY_MANAGER approval)
        Booking booking = Booking.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .resourceId(request.getResourceId())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(Booking.BookingStatus.PENDING)
                .purpose(request.getPurpose())
                .build();

        booking = bookingRepository.save(booking);

        // Do NOT update resource status until approved
        // Call user-service to retrieve user info required for mail, then publish notification only if we have valid recipient
        UserProfileResponseDTO user = userServiceClient.getUserById(userId);
        if (user != null && StringUtils.isNotBlank(user.getEmail()) && !isPlaceholderEmail(user.getEmail().trim())) {
            String recipientEmail = user.getEmail().trim();
            String contactName = StringUtils.defaultIfBlank(user.getName(), user.getEmail());
            contactName = StringUtils.defaultIfBlank(contactName, "User");
            sendBookingRequestSubmittedNotification(userId, resource.getName(), recipientEmail, contactName);
        } else {
            logger.warn("No valid email for user {} (user not found or placeholder), skipping booking request submitted notification", userId);
        }

        return Optional.of(mapToResponseDTO(booking, resource.getName()));
    }

    public Optional<BookingResponseDTO> getBookingById(String id) {
        return bookingRepository.findById(id)
                .map(booking -> {
                    ResourceResponseDTO resource = resourceServiceClient.getResourceById(booking.getResourceId());
                    return mapToResponseDTO(booking, resource != null ? StringUtils.defaultString(resource.getName(), "Unknown") : "Unknown");
                });
    }

    public PageResponseDTO<BookingResponseDTO> getBookingsByUser(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Booking> bookingPage = bookingRepository.findByUserId(userId, pageable);
        return mapToPageResponseDTO(bookingPage);
    }

    public PageResponseDTO<BookingResponseDTO> getBookingsByResource(String resourceId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Booking> bookingPage = bookingRepository.findByResourceId(resourceId, pageable);
        return mapToPageResponseDTO(bookingPage);
    }

    @Transactional
    public Optional<BookingResponseDTO> updateBookingStatus(String id, UpdateBookingStatusRequestDTO request) {
        return bookingRepository.findById(id)
                .map(booking -> {
                    Booking.BookingStatus oldStatus = booking.getStatus();
                    booking.setStatus(request.getStatus());
                    booking = bookingRepository.save(booking);

                    // Notify on cancellation (resource status kept AVAILABLE for now)
                    if (request.getStatus() == Booking.BookingStatus.CANCELLED && oldStatus != Booking.BookingStatus.CANCELLED) {
                        UserProfileResponseDTO cancelUser = userServiceClient.getUserById(booking.getUserId());
                        if (cancelUser != null && StringUtils.isNotBlank(cancelUser.getEmail()) && !isPlaceholderEmail(cancelUser.getEmail().trim())) {
                            String email = cancelUser.getEmail().trim();
                            String name = StringUtils.defaultIfBlank(cancelUser.getName(), cancelUser.getEmail());
                            sendBookingCancellationNotification(booking.getUserId(), booking.getResourceId(), email, StringUtils.defaultIfBlank(name, "User"));
                        } else {
                            logger.warn("No valid email for user {}, skipping booking cancellation notification", booking.getUserId());
                        }
                    }

                    ResourceResponseDTO resource = resourceServiceClient.getResourceById(booking.getResourceId());
                    return mapToResponseDTO(booking, resource != null ? StringUtils.defaultString(resource.getName(), "Unknown") : "Unknown");
                });
    }

    /**
     * Approve a pending booking (FACILITY_MANAGER only - enforced at API gateway).
     * Sets booking to CONFIRMED. Resource status is left as AVAILABLE for now.
     */
    @Transactional
    public Optional<BookingResponseDTO> approveBooking(String bookingId) {
        return bookingRepository.findById(bookingId)
                .filter(booking -> booking.getStatus() == Booking.BookingStatus.PENDING)
                .map(booking -> {
                    booking.setStatus(Booking.BookingStatus.CONFIRMED);
                    booking = bookingRepository.save(booking);

                    String resName = getResourceName(booking.getResourceId());
                    UserProfileResponseDTO confirmUser = userServiceClient.getUserById(booking.getUserId());
                    if (confirmUser != null && StringUtils.isNotBlank(confirmUser.getEmail()) && !isPlaceholderEmail(confirmUser.getEmail().trim())) {
                        String email = confirmUser.getEmail().trim();
                        String name = StringUtils.defaultIfBlank(confirmUser.getName(), confirmUser.getEmail());
                        sendBookingConfirmationNotification(booking.getUserId(), resName, email, StringUtils.defaultIfBlank(name, "User"));
                    } else {
                        logger.warn("No valid email for user {}, skipping booking confirmation notification", booking.getUserId());
                    }
                    return mapToResponseDTO(booking, resName);
                });
    }

    private String getResourceName(String resourceId) {
        ResourceResponseDTO resource = resourceServiceClient.getResourceById(resourceId);
        return resource != null ? StringUtils.defaultString(resource.getName(), "Unknown") : "Unknown";
    }

    @Transactional
    public boolean cancelBooking(String id, String userId) {
        return bookingRepository.findById(id)
                .filter(booking -> booking.getUserId().equals(userId))
                .map(booking -> {
                    booking.setStatus(Booking.BookingStatus.CANCELLED);
                    bookingRepository.save(booking);
                    UserProfileResponseDTO cancelUser = userServiceClient.getUserById(userId);
                    if (cancelUser != null && StringUtils.isNotBlank(cancelUser.getEmail()) && !isPlaceholderEmail(cancelUser.getEmail().trim())) {
                        String email = cancelUser.getEmail().trim();
                        String name = StringUtils.defaultIfBlank(cancelUser.getName(), cancelUser.getEmail());
                        sendBookingCancellationNotification(userId, booking.getResourceId(), email, StringUtils.defaultIfBlank(name, "User"));
                    } else {
                        logger.warn("No valid email for user {}, skipping booking cancellation notification", userId);
                    }
                    return true;
                })
                .orElse(false);
    }

    /**
     * Publishes booking-request-submitted notification. Caller must retrieve user info from user-service
     * and pass valid recipientEmail/contactName; only then is the event produced.
     */
    private void sendBookingRequestSubmittedNotification(String userId, String resourceName, String recipientEmail, String contactName) {
        try {
            Map<String, String> metadata = new HashMap<>();
            metadata.put("resourceName", resourceName);
            metadata.put("userId", userId);

            NotificationData data = new NotificationData("BOOKING_REQUEST_SUBMITTED", null, null);
            data.setMetadata(metadata);

            NotificationCommunicationParams params = new NotificationCommunicationParams(
                    NotificationChannel.EMAIL,
                    List.of(recipientEmail),
                    contactName != null ? contactName : "User"
            );

            Notification notification = new Notification(data, params);

            KafkaEvent event = new KafkaEvent(
                    UUID.randomUUID().toString(),
                    "CREATE_NOTIFICATION",
                    objectMapper.writeValueAsString(notification)
            );
            event.setUserId(userId);

            kafkaEventProducer.sendEvent("crcs-notification", event);
            logger.info("Produced booking request submitted notification for userId={}, recipient={}", userId, recipientEmail);
        } catch (Exception e) {
            logger.error("Error producing booking request submitted notification", e);
        }
    }

    /**
     * Publishes booking-confirmed notification. Caller must retrieve user info from user-service and pass valid recipientEmail/contactName.
     */
    private void sendBookingConfirmationNotification(String userId, String resourceName, String recipientEmail, String contactName) {
        try {
            Map<String, String> metadata = new HashMap<>();
            metadata.put("resourceName", resourceName);
            metadata.put("userId", userId);

            NotificationData data = new NotificationData("BOOKING_CONFIRMED", null, null);
            data.setMetadata(metadata);

            NotificationCommunicationParams params = new NotificationCommunicationParams(
                    NotificationChannel.EMAIL,
                    List.of(recipientEmail),
                    contactName != null ? contactName : "User"
            );

            Notification notification = new Notification(data, params);

            KafkaEvent event = new KafkaEvent(
                    UUID.randomUUID().toString(),
                    "CREATE_NOTIFICATION",
                    objectMapper.writeValueAsString(notification)
            );
            event.setUserId(userId);

            kafkaEventProducer.sendEvent("crcs-notification", event);
            logger.info("Produced booking confirmation notification for userId={}, recipient={}", userId, recipientEmail);
        } catch (Exception e) {
            logger.error("Error producing booking confirmation notification", e);
        }
    }

    /**
     * Publishes booking-cancelled notification. Caller must retrieve user info from user-service and pass valid recipientEmail/contactName.
     */
    private void sendBookingCancellationNotification(String userId, String resourceId, String recipientEmail, String contactName) {
        try {
            ResourceResponseDTO resource = resourceServiceClient.getResourceById(resourceId);
            Map<String, String> metadata = new HashMap<>();
            metadata.put("resourceName", resource != null ? StringUtils.defaultString(resource.getName(), "Resource") : "Resource");
            metadata.put("userId", userId);

            NotificationData data = new NotificationData("BOOKING_CANCELLED", null, null);
            data.setMetadata(metadata);

            NotificationCommunicationParams params = new NotificationCommunicationParams(
                    NotificationChannel.EMAIL,
                    List.of(recipientEmail),
                    contactName != null ? contactName : "User"
            );

            Notification notification = new Notification(data, params);

            KafkaEvent event = new KafkaEvent(
                    UUID.randomUUID().toString(),
                    "CREATE_NOTIFICATION",
                    objectMapper.writeValueAsString(notification)
            );
            event.setUserId(userId);

            kafkaEventProducer.sendEvent("crcs-notification", event);
            logger.info("Produced booking cancellation notification for userId={}, recipient={}", userId, recipientEmail);
        } catch (Exception e) {
            logger.error("Error producing booking cancellation notification", e);
        }
    }

    private BookingResponseDTO mapToResponseDTO(Booking booking, String resourceName) {
        return BookingResponseDTO.builder()
                .id(booking.getId())
                .userId(booking.getUserId())
                .resourceId(booking.getResourceId())
                .resourceName(resourceName)
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .status(booking.getStatus())
                .purpose(booking.getPurpose())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }

    private PageResponseDTO<BookingResponseDTO> mapToPageResponseDTO(Page<Booking> bookingPage) {
        List<BookingResponseDTO> content = bookingPage.getContent()
                .stream()
                .map(booking -> {
                    ResourceResponseDTO resource = resourceServiceClient.getResourceById(booking.getResourceId());
                    return mapToResponseDTO(booking, resource != null ? StringUtils.defaultString(resource.getName(), "Unknown") : "Unknown");
                })
                .collect(Collectors.toList());

        return new PageResponseDTO<>(
                content,
                bookingPage.getNumber(),
                bookingPage.getSize(),
                bookingPage.getTotalElements(),
                bookingPage.getTotalPages(),
                bookingPage.isLast()
        );
    }
}
