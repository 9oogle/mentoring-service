package com.goggles.mentoring_service.domain.mentoring.exception;

import com.goggles.mentoring_service.domain.common.MentoringValidationException;

public class RepeatPatternRequiredException extends MentoringValidationException {

	public RepeatPatternRequiredException() {
		super("자동 반복 멘토링은 요일 반복 패턴이 설정되어야 활성화할 수 있습니다.");
	}
}