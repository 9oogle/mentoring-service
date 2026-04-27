package com.goggles.mentoring_service.domain.mentoring;

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