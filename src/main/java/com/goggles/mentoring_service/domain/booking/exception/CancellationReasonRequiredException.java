package com.goggles.mentoring_service.domain.booking.exception;

import com.goggles.mentoring_service.domain._common.MentoringValidationException;

public class CancellationReasonRequiredException extends MentoringValidationException {

	private CancellationReasonRequiredException() {
		super("취소 사유는 필수입니다.");
	}

	public static CancellationReasonRequiredException of() {
		return new CancellationReasonRequiredException();
	}
}
