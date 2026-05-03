package com.goggles.mentoring_service.domain.booking.exception;

import com.goggles.mentoring_service.domain._common.MentoringValidationException;
import com.goggles.mentoring_service.domain.booking.BookingStatus;

public class SessionOperationNotAllowedException extends MentoringValidationException {

  private SessionOperationNotAllowedException(String message) {
    super(message);
  }

  public static SessionOperationNotAllowedException completionNotAllowed(BookingStatus status) {
    return new SessionOperationNotAllowedException(
        "수락된 예약만 회차를 완료 처리할 수 있습니다. 현재 상태: " + status);
  }
}
