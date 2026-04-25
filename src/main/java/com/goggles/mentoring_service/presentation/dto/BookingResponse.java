package com.goggles.mentoring_service.presentation.dto;

import com.goggles.mentoring_service.application.result.BookingResult;
import com.goggles.mentoring_service.domain.booking.BookingStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    }
  }

  public record Detail(
      UUID bookingId,
      MentoringInfo mentoring,
      MenteeInfo mentee,
      List<BookedTimeInfo> bookedTimes,
      BookingStatus status,
      String requestMessage,
      ClosureInfo closure,
      LocalDateTime createdAt) {

    public record MentoringInfo(
        UUID mentoringId, String title, String subtitle, String mentorName, String categoryName) {}

    public record MenteeInfo(UUID menteeId, String menteeName) {}

    public record ClosureInfo(UUID closedBy, String reason, LocalDateTime closedAt) {}

    public static Detail of(BookingResult.Detail result) {
      List<BookedTimeInfo> times =
          result.bookedTimes().stream()
              .map(timeInfo -> new BookedTimeInfo(timeInfo.sessionDate(), timeInfo.startTime(), timeInfo.endTime()))
              .toList();

      ClosureInfo closureInfo =
          result.closure() != null
              ? new ClosureInfo(
                  result.closure().closedBy(),
                  result.closure().reason(),
                  result.closure().closedAt())
              : null;

      MentoringInfo mentoringInfo =
          new MentoringInfo(
              result.mentoring().mentoringId(),
              result.mentoring().title(),
              result.mentoring().subtitle(),
              result.mentoring().mentorName(),
              result.mentoring().categoryName());
      MenteeInfo menteeInfo =
          new MenteeInfo(result.mentee().menteeId(), result.mentee().menteeName());

      return new Detail(
          result.bookingId(),
          mentoringInfo,
          menteeInfo,
          times,
          result.status(),
          result.requestMessage(),
          closureInfo,
          result.createdAt());
    }
  }

  public record BookedTimeInfo(LocalDate sessionDate, LocalTime startTime, LocalTime endTime) {}
}
