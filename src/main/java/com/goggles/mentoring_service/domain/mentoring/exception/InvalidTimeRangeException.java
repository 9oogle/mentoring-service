package com.goggles.mentoring_service.domain.mentoring.exception;

import com.goggles.mentoring_service.domain.common.MentoringValidationException;

public class InvalidTimeRangeException extends MentoringValidationException {

	public InvalidTimeRangeException(String message) {
		super(message);
	}
}
