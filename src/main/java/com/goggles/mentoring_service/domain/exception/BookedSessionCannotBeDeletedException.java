package com.goggles.mentoring_service.domain.exception;

import com.goggles.common.exception.ConflictException;

public class BookedSessionCannotBeDeletedException extends ConflictException {

	public BookedSessionCannotBeDeletedException() {
		super("예약된 세션은 삭제할 수 없습니다.");
	}
}