package com.goggles.mentoring_service.domain.booking.exception;

import com.goggles.mentoring_service.domain._common.MentoringValidationException;

public class InvalidRescheduleException extends MentoringValidationException {

	private InvalidRescheduleException(String message) {
		super(message);
	}

	public static InvalidRescheduleException sessionAlreadyPassed() {
		return new InvalidRescheduleException("이미 지난 회차는 일정을 변경할 수 없습니다.");
	}

	public static InvalidRescheduleException sessionAlreadyCompleted() {
		return new InvalidRescheduleException("완료된 회차는 일정을 변경할 수 없습니다.");
	}

	public static InvalidRescheduleException bookingNotAccepted() {
		return new InvalidRescheduleException("수락된 예약만 회차 일정을 변경할 수 있습니다.");
	}

	public static InvalidRescheduleException newTimeIsInPast() {
		return new InvalidRescheduleException("변경할 일정은 현재 시간 이후여야 합니다.");
	}
}