package com.goggles.mentoring_service.application.result;

import com.goggles.mentoring_service.domain.booking.MentoringBookingId;
import java.util.UUID;

public class BookingResult {

  public record Create(UUID bookingId) {

    public static Create of(MentoringBookingId mentoringBookingId) {
      return new Create(mentoringBookingId.bookingId());
    }
  }
}
