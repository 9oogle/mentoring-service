package com.goggles.mentoring_service.presentation.dto;

import com.goggles.mentoring_service.application.command.BookingCommand;
import com.goggles.mentoring_service.application.command.MenteeInfo;
import com.goggles.mentoring_service.domain.booking.MentoringBookingId;
import com.goggles.mentoring_service.domain.booking.SessionSlot;
import com.goggles.mentoring_service.presentation.support.UserContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class BookingRequest {
  public record Create(
      @NotNull UUID mentoringId,
      @NotEmpty List<@Valid BookingTimeSlot> bookingTimeSlots,
      String requestMessage, UUID orderId) {
    public BookingCommand.Create toCommand(UserContext userContext) {
      MenteeInfo menteeInfo =
          new MenteeInfo(userContext.userId(), userContext.userType(), userContext.userName());
      List<SessionSlot> sessionSlots =
          bookingTimeSlots().stream()
              .map(slot -> new SessionSlot(slot.date(), slot.startTime(), slot.endTime()))
              .toList();
      return new BookingCommand.Create(menteeInfo, mentoringId(), sessionSlots, requestMessage(),
              orderId());
    }
  }

  public record BookingTimeSlot(
      @NotNull LocalDate date, @NotNull LocalTime startTime, @NotNull LocalTime endTime) {}

  public record Reject(@NotBlank String reason) {
    public BookingCommand.Reject toCommand(UUID bookingId, UserContext userContext) {
      return new BookingCommand.Reject(
          bookingId, userContext.userId(), userContext.userType(), reason());
	}
}
  public record PaymentFailed(
        @NotNull UUID mentoringBookingId, @NotNull UUID orderId, @NotNull String failureReason
  ) {
    public BookingCommand.PaymentFailed toCommand() {
      MentoringBookingId mentoringBookingId = new MentoringBookingId(mentoringBookingId());
        return new BookingCommand.PaymentFailed(mentoringBookingId, orderId(), failureReason());
    }
  }
}
