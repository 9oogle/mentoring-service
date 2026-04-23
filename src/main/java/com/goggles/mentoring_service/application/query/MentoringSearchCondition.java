package com.goggles.mentoring_service.application.query;

import com.goggles.mentoring_service.domain.mentoring.MentoringStatus;
import com.goggles.mentoring_service.domain.mentoring.MentoringType;

import java.util.UUID;

public record MentoringSearchCondition(
		String keyword,
		UUID categoryId,
		UUID mentorId,
		MentoringStatus status,
		MentoringType mentoringType,
		MentoringSort sortBy
) {
	public MentoringSearchCondition {
		if (sortBy == null) sortBy = MentoringSort.CREATED_AT;
	}
}