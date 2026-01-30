package com.crcs.bookingservice.repository;

import com.crcs.bookingservice.model.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {
    Optional<Booking> findById(String id);
    Page<Booking> findByUserId(String userId, Pageable pageable);
    Page<Booking> findByResourceId(String resourceId, Pageable pageable);
    Page<Booking> findByStatus(Booking.BookingStatus status, Pageable pageable);
    
    @Query("SELECT b FROM Booking b WHERE b.resourceId = :resourceId " +
           "AND b.status != 'CANCELLED' " +
           "AND ((b.startTime <= :endTime AND b.endTime >= :startTime))")
    List<Booking> findConflictingBookings(@Param("resourceId") String resourceId,
                                          @Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);
}
