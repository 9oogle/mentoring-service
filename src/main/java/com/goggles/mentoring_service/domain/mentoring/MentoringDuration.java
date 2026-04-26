package com.goggles.mentoring_service.domain.mentoring;

import lombok.Getter;

public enum MentoringDuration {
	MINUTES_30(30), MINUTES_60(60), MINUTES_90(90), MINUTES_120(120), MINUTES_150(150),
	MINUTES_180(180);

	@Getter
	private final int minutes;

	MentoringDuration(int minutes) {
		this.minutes = minutes;
	}
}
