package com.goggles.mentoring_service.domain.mentoring.exception;

import com.goggles.mentoring_service.domain.common.MentoringValidationException;

public class MentoringPolicyViolationException extends MentoringValidationException {

	public MentoringPolicyViolationException(String message) {
		super(message);
	}
}