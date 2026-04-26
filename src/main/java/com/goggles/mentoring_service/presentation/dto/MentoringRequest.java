package com.goggles.mentoring_service.presentation.dto;

import com.goggles.mentoring_service.application.command.MentoringCommand;
import com.goggles.mentoring_service.application.command.SessionSlot;
import com.goggles.mentoring_service.application.command.TimeSchedules;
import com.goggles.mentoring_service.domain.mentoring.Format;
import com.goggles.mentoring_service.domain.mentoring.MentoringDuration;
import com.goggles.mentoring_service.domain.mentoring.MentoringType;
import com.goggles.mentoring_service.presentation.support.UserContext;
import jakarta.validation.constraints.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class MentoringRequest {

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
}
