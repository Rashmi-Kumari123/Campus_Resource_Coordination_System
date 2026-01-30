package com.crcs.bookingservice.dto.request;

import com.crcs.bookingservice.model.Booking;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateBookingStatusRequestDTO {
    @NotNull(message = "Status is required")
    private Booking.BookingStatus status;
}
