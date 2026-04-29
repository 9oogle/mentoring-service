package com.goggles.mentoring_service.application.result;

import com.goggles.mentoring_service.domain.booking.MentoringBooking;
import com.goggles.mentoring_service.domain.mentoring.Mentoring;

import com.goggles.mentoring_service.domain.booking.BookingStatus;
import com.goggles.mentoring_service.domain.booking.MentoringBooking;
import com.goggles.mentoring_service.domain.booking.MentoringBookingId;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
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

  public record Summary(
      UUID bookingId,
      String mentoringTitle,
      String mentorName,
      String menteeName,
      BookingStatus status,
      List<BookedTimeInfo> requestedSessions,
      LocalDateTime createdAt) {

    public static Summary from(MentoringBooking booking) {

      List<BookedTimeInfo> sessionInfos =
          booking.getBookedTimes().stream()
              .map(
                  t ->
                      new BookedTimeInfo(
                          t.getSessionDate(), t.getSessionStartTime(), t.getSessionEndTime()))
              .toList();
      return new Summary(
          booking.getMentoringBookingId().bookingId(),
          booking.getBookedMentoring().getTitle(),
          booking.getBookedMentoring().getMentorName(),
          booking.getMentee().getName(),
          booking.getStatus(),
          sessionInfos,
          booking.getCreatedAt());
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

    public static Detail from(MentoringBooking booking) {
      MentoringInfo mentoringInfo =
          new MentoringInfo(
              booking.getBookedMentoring().getMentoringId(),
              booking.getBookedMentoring().getTitle(),
              booking.getBookedMentoring().getSubtitle(),
              booking.getBookedMentoring().getMentorName(),
              booking.getBookedMentoring().getCategoryName());

      MenteeInfo menteeInfo =
          new MenteeInfo(booking.getMentee().getId(), booking.getMentee().getName());

      List<BookedTimeInfo> times =
          booking.getBookedTimes().stream()
              .map(
                  time ->
                      new BookedTimeInfo(
                          time.getSessionDate(),
                          time.getSessionStartTime(),
                          time.getSessionEndTime()))
              .toList();

      ClosureInfo closureInfo =
          booking.getClosedBy() != null
              ? new ClosureInfo(
                  booking.getClosedBy(), booking.getCloseReason(), booking.getClosedAt())
              : null;

      return new Detail(
          booking.getMentoringBookingId().bookingId(),
          mentoringInfo,
          menteeInfo,
          times,
          booking.getStatus(),
          booking.getRequestMessage(),
          closureInfo,
          booking.getCreatedAt());
    }
  }

  public record BookedTimeInfo(LocalDate sessionDate, LocalTime startTime, LocalTime endTime) {}
}
