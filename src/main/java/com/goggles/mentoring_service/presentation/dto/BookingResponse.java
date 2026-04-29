package com.goggles.mentoring_service.presentation.dto;

import com.goggles.mentoring_service.application.result.BookingResult;
import java.util.UUID;

public class BookingResponse {
  public record Create(UUID enrollmentId, UUID productId, String productName, long productPrice,
                       UUID instructorId, String instructorName) {
    public static Create of(BookingResult.Create createResult) {
      return new Create(
          createResult.enrollmentId(),
          createResult.productId(),
          createResult.productName(),
          createResult.productPrice(),
          createResult.instructorId(),
          createResult.instructorName()
      );
    }
  }
}
