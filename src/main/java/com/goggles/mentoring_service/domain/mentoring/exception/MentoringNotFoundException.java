package com.goggles.mentoring_service.domain.mentoring.exception;

import com.goggles.common.exception.NotFoundException;
import com.goggles.mentoring_service.domain.mentoring.MentoringId;

public class MentoringNotFoundException extends NotFoundException {

	public MentoringNotFoundException(MentoringId mentoringId) {
		super("해당 멘토링을 찾을 수 없습니다. ID: " + mentoringId.mentoringId());
	}
}