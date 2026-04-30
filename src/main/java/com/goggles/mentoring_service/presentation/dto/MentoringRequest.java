package com.goggles.mentoring_service.presentation.dto;

import com.goggles.mentoring_service.application.command.MentoringCommand;
import com.goggles.mentoring_service.application.command.TimeSchedules;
import com.goggles.mentoring_service.domain.booking.SessionSlot;
import com.goggles.mentoring_service.domain.mentoring.*;
import com.goggles.mentoring_service.domain.mentoring.exception.InvalidConditionException;
import com.goggles.mentoring_service.presentation.support.UserContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class MentoringRequest {

	public record Update(@Size(max = 100) String title, String subtitle, String description,
						 @Min(0) Integer price, LocalDate endDate,
						 @Valid List<RepeatPatternDto> repeatPatterns) {
		public MentoringCommand.Update toCommand(UserContext userContext) {
			List<TimeSchedules> patterns = repeatPatterns() == null ? List.of() :
					repeatPatterns().stream()
							.map(p -> new TimeSchedules(p.dayOfWeek(), p.startTime(), p.endTime()))
							.toList();
			return new MentoringCommand.Update(userContext.userId(), userContext.userType(),
					title(), subtitle(), description(), price(), endDate(), patterns);
		}

		public record RepeatPatternDto(@NotNull DayOfWeek dayOfWeek, @NotNull LocalTime startTime,
									   @NotNull LocalTime endTime) {}
	}


	public record Create(@NotNull UUID categoryId, @NotBlank @Size(max = 100) String title,
						 String subtitle, String description, @NotNull MentoringDuration duration,
						 @NotNull MentoringType mentoringType, @NotNull Format format,
						 @Min(1) int sessionCount, @Min(1) int maxParticipants,
						 boolean excludeHolidays, @Min(0) int price, LocalDate endDate,
						 @Valid @NotEmpty List<SessionDto> sessions,
						 @Valid @NotEmpty List<RepeatPatternDto> repeatPatterns) {
		public MentoringCommand.Create toCommand(UserContext userContext) {
			List<SessionSlot> sessionSlots = sessions().stream()
					.map(session -> new SessionSlot(session.date(), session.startTime(),
							session.endTime()))
					.toList();

			List<TimeSchedules> patterns = repeatPatterns().stream()
					.map(pattern -> new TimeSchedules(pattern.dayOfWeek(), pattern.startTime(),
							pattern.endTime()))
					.toList();

			return new MentoringCommand.Create(userContext.userId(), userContext.userName(),
					userContext.userField(), userContext.userEmail(), userContext.userType(),
					categoryId(), title(), subtitle(), description(), duration(), mentoringType(),
					format(), sessionCount(), maxParticipants(), excludeHolidays(), price(),
					endDate(), sessionSlots, patterns);
		}

		public record SessionDto(@NotNull LocalDate date, @NotNull LocalTime startTime,
								 @NotNull LocalTime endTime) {}

		public record RepeatPatternDto(@NotNull DayOfWeek dayOfWeek, @NotNull LocalTime startTime,
									   @NotNull LocalTime endTime) {}
	}

	public record Search(String keyword, UUID categoryId, UUID mentorId, String status,
						 String mentoringType, String sortBy) {
		public MentoringSearchCondition toCondition() {
			return new MentoringSearchCondition(keyword, categoryId, mentorId, toStatus(), toType(),
					toSort());
		}

		private MentoringStatus toStatus() {
			if (status == null) return null;
			try {
				return MentoringStatus.valueOf(status.toUpperCase());
			} catch (IllegalArgumentException e) {
				throw InvalidConditionException.forStatus();
			}
		}

		private MentoringType toType() {
			if (mentoringType == null) return null;
			try {
				return MentoringType.valueOf(mentoringType.toUpperCase());
			} catch (IllegalArgumentException e) {
				throw InvalidConditionException.forType();
			}
		}

		private MentoringSort toSort() {
			if (sortBy == null) return null;
			try {
				return MentoringSort.valueOf(sortBy.toUpperCase());
			} catch (IllegalArgumentException e) {
				throw InvalidConditionException.forSort();
			}
		}
	}

}
