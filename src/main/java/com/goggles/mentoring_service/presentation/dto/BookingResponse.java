package com.goggles.mentoring_service.presentation.dto;

import com.goggles.mentoring_service.application.result.BookingResult;
import com.goggles.mentoring_service.domain.booking.BookingStatus;
import java.time.LocalDateTime;
import java.util.List;
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

  public record Summary(
      UUID bookingId,
      String mentoringTitle,
      String mentorName,
      String menteeName,
      BookingStatus status,
      List<BookingResult.BookedTimeInfo> requestedSessions,
      LocalDateTime createdAt) {

    public static Summary of(BookingResult.Summary result) {
      return new Summary(
          result.bookingId(),
          result.mentoringTitle(),
          result.mentorName(),
          result.menteeName(),
          result.status(),
          result.requestedSessions(),
          result.createdAt());
    }
  }

  public record Detail(
      UUID bookingId,
      BookingResult.Detail.MentoringInfo mentoring,
      BookingResult.Detail.MenteeInfo mentee,
      List<BookingResult.BookedTimeInfo> bookedTimes,
      BookingStatus status,
      String requestMessage,
      BookingResult.Detail.ClosureInfo closure,
      LocalDateTime createdAt) {

    public static Detail of(BookingResult.Detail result) {
      return new Detail(
          result.bookingId(),
          result.mentoring(),
          result.mentee(),
          result.bookedTimes(),
          result.status(),
          result.requestMessage(),
          result.closure(),
          result.createdAt());
    }
  }
}
