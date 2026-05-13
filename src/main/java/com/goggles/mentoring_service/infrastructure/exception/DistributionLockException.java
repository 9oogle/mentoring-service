package com.goggles.mentoring_service.infrastructure.exception;

import com.goggles.common.exception.ConflictException;

public class DistributionLockException extends ConflictException {

	public DistributionLockException(String message) {
		super(message);
	}
}

