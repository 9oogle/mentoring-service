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

	public record Detail(
			UUID mentoringId,
			String title,
			String subtitle,
			String description,
			MentorInfo mentor,
			CategoryInfo category,
			MentoringStatus status,
			Format format,
			MentoringType mentoringType,
			MentoringDuration duration,
			int sessionCount,
			int maxParticipants,
			boolean excludeHolidays,
			int price,
			LocalDate endDate
	) {
		public record MentorInfo(String name, String field) {}
		public record CategoryInfo(String name, String code) {}

		public static Detail from(Mentoring m) {
			return new Detail(
					m.getMentoringId().mentoringId(),
					m.getTitle(), m.getSubtitle(), m.getDescription(),
					new MentorInfo(m.getMentor().getName(), m.getMentor().getField()),
					new CategoryInfo(m.getMentoringCategory().getName(), m.getMentoringCategory().getCode()),
					m.getStatus(), m.getFormat(), m.getMentoringType(), m.getDuration(),
					m.getSessionCount(), m.getMaxParticipants(), m.isExcludeHolidays(),
					m.getPrice(), m.getEndDate()
			);
		}
	}

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
