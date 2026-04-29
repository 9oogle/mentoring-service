package com.goggles.mentoring_service.application.result;

import com.goggles.mentoring_service.domain.booking.MentoringBooking;
import com.goggles.mentoring_service.domain.mentoring.Mentoring;

import java.util.UUID;

public class BookingResult {

  public record Create(UUID enrollmentId, UUID productId, String productName, long productPrice,
                       UUID instructorId, String instructorName) {

    public static Create of(MentoringBooking booking, Mentoring mentoring){
        return new Create(
            booking.getMentoringBookingId().bookingId(),
            mentoring.getMentoringId().mentoringId(),
            mentoring.getTitle(),
            mentoring.getPrice(),
            mentoring.getMentor().getId(),
            mentoring.getMentor().getName()
        );
    }
  }
}
