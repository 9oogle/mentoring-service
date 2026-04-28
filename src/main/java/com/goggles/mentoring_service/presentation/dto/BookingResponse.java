package com.goggles.mentoring_service.presentation.dto;

import com.goggles.mentoring_service.application.result.BookingResult;
import java.util.UUID;

public class BookingResponse {
  public record Create(UUID bookingId) {
    public static Create of(BookingResult.Create createResult) {
      return new Create(createResult.bookingId());
    }
  }
}
