package com.goggles.mentoring_service.domain.exception;


public class InactiveCategoryCannotMoveException extends MentoringValidationException {
	public InactiveCategoryCannotMoveException() {
		super("비활성화된 카테고리는 이동할 수 없습니다.");
	}
}
