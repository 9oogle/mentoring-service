package com.goggles.mentoring_service.domain.booking.exception;

import com.goggles.common.exception.NotFoundException;

import java.util.UUID;

public class BookedSessionNotFoundException extends NotFoundException {

	public BookedSessionNotFoundException(UUID sessionId) {
		super("회차를 찾을 수 없습니다. sessionId=" + sessionId);
	}
}