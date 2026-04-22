package com.goggles.mentoring_service.domain.exception;

import com.goggles.common.exception.BadRequestException;

public class InvalidMentoringPolicyException extends BadRequestException {

	public InvalidMentoringPolicyException(String message) {
		super(message);
	}
}

