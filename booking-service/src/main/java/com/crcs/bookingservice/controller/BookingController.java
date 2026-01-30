package com.crcs.bookingservice.controller;

import com.crcs.bookingservice.dto.request.AvailabilityCheckRequestDTO;
import com.crcs.bookingservice.dto.request.CreateBookingRequestDTO;
import com.crcs.bookingservice.dto.request.UpdateBookingStatusRequestDTO;
import com.crcs.bookingservice.dto.response.ApiResponseDTO;
import com.crcs.bookingservice.dto.response.AvailabilityCheckResponseDTO;
import com.crcs.bookingservice.dto.response.BookingResponseDTO;
import com.crcs.bookingservice.dto.response.PageResponseDTO;
import com.crcs.bookingservice.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/bookings")
@Tag(name = "Booking Management", description = "APIs for managing resource bookings")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Operation(summary = "Check resource availability", description = "Check if a resource is available for a given time slot")
    @GetMapping("/availability")
    public ResponseEntity<AvailabilityCheckResponseDTO> checkAvailability(
            @RequestParam("resourceId") String resourceId,
            @RequestParam("startTime") java.time.LocalDateTime startTime,
            @RequestParam("endTime") java.time.LocalDateTime endTime) {
        AvailabilityCheckResponseDTO response = bookingService.checkAvailability(resourceId, startTime, endTime);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Check resource availability (POST)", description = "Check if a resource is available for a given time slot via request body")
    @PostMapping("/availability")
    public ResponseEntity<AvailabilityCheckResponseDTO> checkAvailabilityPost(
            @Valid @RequestBody AvailabilityCheckRequestDTO request) {
        AvailabilityCheckResponseDTO response = bookingService.checkAvailability(
                request.getResourceId(), request.getStartTime(), request.getEndTime());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create a new booking", description = "Create a new booking for a resource")
    @PostMapping
    public ResponseEntity<?> createBooking(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateBookingRequestDTO request) {
        Optional<BookingResponseDTO> booking = bookingService.createBooking(userId, request);
        if (booking.isPresent()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(booking.get());
        } else {
            return ResponseEntity.badRequest()
                    .body(new ApiResponseDTO("Resource not available or booking failed"));
        }
    }

    @Operation(summary = "Get booking by ID", description = "Retrieve a specific booking by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> getBookingById(@PathVariable("id") String id) {
        Optional<BookingResponseDTO> booking = bookingService.getBookingById(id);
        if (booking.isPresent()) {
            return ResponseEntity.ok(booking.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get user bookings", description = "Retrieve all bookings for a specific user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<PageResponseDTO<BookingResponseDTO>> getBookingsByUser(
            @PathVariable("userId") String userId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        PageResponseDTO<BookingResponseDTO> bookings = bookingService.getBookingsByUser(userId, page, size);
        return ResponseEntity.ok(bookings);
    }

    @Operation(summary = "Get resource bookings", description = "Retrieve all bookings for a specific resource")
    @GetMapping("/resource/{resourceId}")
    public ResponseEntity<PageResponseDTO<BookingResponseDTO>> getBookingsByResource(
            @PathVariable("resourceId") String resourceId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        PageResponseDTO<BookingResponseDTO> bookings = bookingService.getBookingsByResource(resourceId, page, size);
        return ResponseEntity.ok(bookings);
    }

    @Operation(summary = "Approve a pending booking", description = "FACILITY_MANAGER approves a pending booking; sets status to CONFIRMED and resource to BOOKED")
    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveBooking(@PathVariable("id") String id) {
        Optional<BookingResponseDTO> approved = bookingService.approveBooking(id);
        if (approved.isPresent()) {
            return ResponseEntity.ok(approved.get());
        }
        return ResponseEntity.badRequest()
                .body(new ApiResponseDTO("Booking not found or not pending approval"));
    }

    @Operation(summary = "Update booking status", description = "Update the status of a booking")
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateBookingStatus(@PathVariable("id") String id,
                                                @RequestBody UpdateBookingStatusRequestDTO request) {
        Optional<BookingResponseDTO> updated = bookingService.updateBookingStatus(id, request);
        if (updated.isPresent()) {
            return ResponseEntity.ok(updated.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Cancel booking", description = "Cancel a booking by ID")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable("id") String id,
                                          @RequestHeader("X-User-Id") String userId) {
        boolean cancelled = bookingService.cancelBooking(id, userId);
        if (cancelled) {
            return ResponseEntity.ok(new ApiResponseDTO("Booking cancelled successfully"));
        }
        return ResponseEntity.notFound().build();
    }
}
