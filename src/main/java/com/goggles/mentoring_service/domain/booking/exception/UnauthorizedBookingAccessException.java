package com.goggles.mentoring_service.domain.booking.exception;

import com.goggles.common.exception.ForbiddenException;

public class UnauthorizedBookingAccessException extends ForbiddenException {

	private UnauthorizedBookingAccessException(String message) {
		super(message);
	}

	public static UnauthorizedBookingAccessException noPermissionToProcess() {
		return new UnauthorizedBookingAccessException("예약을 처리할 수 있는 권한이 없습니다.");
	}

	public static UnauthorizedBookingAccessException noPermissionToCancel() {
		return new UnauthorizedBookingAccessException("예약을 취소할 수 있는 권한이 없습니다.");
	}
}