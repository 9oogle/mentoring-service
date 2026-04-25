package com.goggles.mentoring_service.domain.mentoring.exception;

import com.goggles.common.exception.ConflictException;

public class BookedSessionStatusCannotBeChangedException extends ConflictException {

	public BookedSessionStatusCannotBeChangedException() {
		super("예약된 세션은 상태를 변경할 수 없습니다.");
	}
}