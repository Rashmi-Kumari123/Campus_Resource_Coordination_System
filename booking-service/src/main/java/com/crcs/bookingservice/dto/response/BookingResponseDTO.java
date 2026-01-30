package com.crcs.bookingservice.dto.response;

import com.crcs.bookingservice.model.Booking;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDTO {
    private String id;
    private String userId;
    private String resourceId;
    private String resourceName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Booking.BookingStatus status;
    private String purpose;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
