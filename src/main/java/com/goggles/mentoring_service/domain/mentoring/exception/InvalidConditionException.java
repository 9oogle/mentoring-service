package com.goggles.mentoring_service.domain.mentoring.exception;

import com.goggles.mentoring_service.domain._common.MentoringValidationException;

public class InvalidConditionException extends MentoringValidationException {
	public InvalidConditionException(String message) {
		super(message);
	}

	public static InvalidConditionException forStatus() {
		return new InvalidConditionException("유효하지 않은 멘토링 상태입니다. ACTIVE 또는 INACTIVE만 허용됩니다.");
	}

	public static InvalidConditionException forType() {
		return new InvalidConditionException("유효하지 않은 멘토링 유형입니다. ONE_ON_ONE 또는 GROUP만 허용됩니다.");
	}

	public static InvalidConditionException forSort() {
		return new InvalidConditionException(
				"유효하지 않은 정렬 옵션입니다. CREATED_AT, PRICE_ASC, PRICE_DESC, DURATION_ASC, DURATION_DESC만 허용됩니다.");
	}

}
