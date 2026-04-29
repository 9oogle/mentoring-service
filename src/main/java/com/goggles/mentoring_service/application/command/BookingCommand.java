package com.goggles.mentoring_service.application.command;

import com.goggles.mentoring_service.domain._common.UserType;
import com.goggles.mentoring_service.domain.booking.MentoringBookingId;
import com.goggles.mentoring_service.domain.booking.SessionSlot;
import java.util.List;
import java.util.UUID;

public class BookingCommand {
  public record Create(
      MenteeInfo menteeInfo,
      UUID mentoringId,
      List<SessionSlot> sessionSlots,
      String requestMessage,
      UUID orderId) {}

  public record PaymentFailed(
          MentoringBookingId mentoringBookingId, UUID orderId, String failureReason
  ) {}

  public record Accept(UUID bookingId, UUID userId, UserType userType) {}

  public record Reject(UUID bookingId, UUID userId, UserType userType, String reason) {}

  public record Cancel(UUID bookingId, UUID userId, UserType userType, String reason) {}
}
