package com.goggles.mentoring_service.domain.mentoring.exception;

import com.goggles.mentoring_service.domain.common.MentoringValidationException;

public class InvalidTimeRangeException extends MentoringValidationException {

	private InvalidTimeRangeException(String message) {
		super(message);
	}

	public static InvalidTimeRangeException forSession() {
		return new InvalidTimeRangeException("세션의 시작 시간은 종료 시간보다 이전이어야 합니다.");
	}

	public static InvalidTimeRangeException forRepeatPattern() {
		return new InvalidTimeRangeException("반복 패턴의 시작 시간은 종료 시간보다 이전이어야 합니다.");
	}
}