package com.goggles.mentoring_service.domain._common;

public abstract class MentoringDomainException extends RuntimeException {

	protected MentoringDomainException(String message) {
		super(message);
	}
}