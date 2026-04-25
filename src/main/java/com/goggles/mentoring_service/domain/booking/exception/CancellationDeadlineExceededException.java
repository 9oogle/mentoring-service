package com.goggles.mentoring_service.domain.booking.exception;

import com.goggles.mentoring_service.domain.common.MentoringValidationException;

public class CancellationDeadlineExceededException extends MentoringValidationException {

	private CancellationDeadlineExceededException() {
		super("세션 시작 24시간 이전까지만 취소할 수 있습니다.");
	}

	public static CancellationDeadlineExceededException of() {
		return new CancellationDeadlineExceededException();
	}
}
