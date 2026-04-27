package com.goggles.mentoring_service.presentation.dto;

import com.goggles.mentoring_service.application.result.MentoringResult;
import com.goggles.mentoring_service.domain.mentoring.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class MentoringResponse {

	public record Create(UUID mentoringId) {}

	public record Detail(UUID mentoringId, String title, String subtitle, String description,
						 MentorInfo mentor, CategoryInfo category, MentoringStatus status,
						 Format format, MentoringType mentoringType, MentoringDuration duration,
						 int sessionCount, int maxParticipants, boolean excludeHolidays, int price,
						 LocalDate endDate) {
		public record MentorInfo(String name, String field) {
			public static MentorInfo of(MentoringResult.Detail.MentorInfo mentorInfo) {
				return new MentorInfo(mentorInfo.name(), mentorInfo.field());
			}
		}

		public record CategoryInfo(String name, String code) {

			public static CategoryInfo of(MentoringResult.Detail.CategoryInfo categoryInfo) {
				return new CategoryInfo(categoryInfo.name(), categoryInfo.code());
			}
		}

		public static Detail of(MentoringResult.Detail detail) {
			return new Detail(detail.mentoringId(), detail.title(), detail.subtitle(),
					detail.description(), MentorInfo.of(detail.mentor()),
					CategoryInfo.of(detail.category()), detail.status(), detail.format(),
					detail.mentoringType(), detail.duration(), detail.sessionCount(),
					detail.maxParticipants(), detail.excludeHolidays(), detail.price(),
					detail.endDate());
		}

	}

	public record Schedules(List<RepeatPatternDto> repeatPatterns, List<SessionDto> sessions) {
		public record RepeatPatternDto(DayOfWeek dayOfWeek, LocalTime startTime,
									   LocalTime endTime) {
			public static RepeatPatternDto of(
					MentoringResult.Schedules.RepeatPatternDto patternDto) {
				return new RepeatPatternDto(patternDto.dayOfWeek(), patternDto.startTime(),
						patternDto.endTime());
			}
		}

		public record SessionDto(LocalDate date, LocalTime startTime, LocalTime endTime,
								 SessionStatus status) {

			public static SessionDto of(MentoringResult.Schedules.SessionDto sessionDto) {
				return new SessionDto(sessionDto.date(), sessionDto.startTime(),
						sessionDto.endTime(), sessionDto.status());
			}
		}

		public static Schedules of(MentoringResult.Schedules schedules) {
			List<RepeatPatternDto> patterns = schedules.repeatPatterns()
					.stream()
					.map(RepeatPatternDto::of)
					.toList();
			List<SessionDto> sessions = schedules.sessions()
					.stream()
					.map(SessionDto::of)
					.toList();
			return new Schedules(patterns, sessions);
		}

	}

	public record Summary(UUID mentoringId, String title, String subtitle, String mentorName,
						  String categoryName, MentoringType mentoringType, Format format,
						  int price, MentoringStatus status) {

		public static Summary of(MentoringResult.Summary summary) {
			return new Summary(summary.mentoringId(), summary.title(), summary.subtitle(),
					summary.mentorName(), summary.categoryName(), summary.mentoringType(),
					summary.format(), summary.price(), summary.status());
		}
	}

}