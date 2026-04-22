package com.goggles.mentoring_service.domain.exception;

public abstract class MentoringDomainException extends RuntimeException {

	protected MentoringDomainException(String message) {
		super(message);
	}
}