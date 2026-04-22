package com.goggles.mentoring_service.domain.exception;

import com.goggles.common.exception.BadRequestException;

public class RepeatPatternRequiredException extends BadRequestException {

	public RepeatPatternRequiredException() {
		super("자동 반복 멘토링은 요일 반복 패턴이 설정되어야 활성화할 수 있습니다.");
	}
}