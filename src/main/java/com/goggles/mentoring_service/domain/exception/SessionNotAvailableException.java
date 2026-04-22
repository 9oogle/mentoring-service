package com.goggles.mentoring_service.domain.exception;

import com.goggles.common.exception.ConflictException;

public class SessionNotAvailableException extends ConflictException {

	public SessionNotAvailableException() {
		super("예약 가능한 세션이 아닙니다.");
	}
}