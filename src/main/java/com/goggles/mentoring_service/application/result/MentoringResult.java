package com.goggles.mentoring_service.application.result;

import com.goggles.mentoring_service.domain.mentoring.Format;
import com.goggles.mentoring_service.domain.mentoring.Mentoring;
import com.goggles.mentoring_service.domain.mentoring.MentoringDuration;
import com.goggles.mentoring_service.domain.mentoring.MentoringStatus;
import com.goggles.mentoring_service.domain.mentoring.MentoringType;
import com.goggles.mentoring_service.domain.mentoring.SessionStatus;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class MentoringResult {
	public record Summary(
			UUID mentoringId,
			String title,
			String subtitle,
			String mentorName,
			String categoryName,
			MentoringType mentoringType,
			Format format,
			int price,
			MentoringStatus status
	) {
		public static Summary from(Mentoring m) {
			return new Summary(
					m.getMentoringId().mentoringId(),
					m.getTitle(), m.getSubtitle(),
					m.getMentor().getName(),
					m.getMentoringCategory().getName(),
					m.getMentoringType(), m.getFormat(),
					m.getPrice(), m.getStatus()
			);
		}
	}
}
