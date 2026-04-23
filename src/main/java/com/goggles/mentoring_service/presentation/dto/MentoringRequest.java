package com.goggles.mentoring_service.presentation.dto;

import com.goggles.mentoring_service.application.command.MentoringCommand;
import com.goggles.mentoring_service.domain.mentoring.BookingType;
import com.goggles.mentoring_service.domain.mentoring.MentoringDuration;
import com.goggles.mentoring_service.domain.mentoring.MentoringType;
import com.goggles.mentoring_service.presentation.support.UserContext;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public class MentoringRequest {

	public record Create(
			@NotNull UUID categoryId,
			@NotBlank @Size(max = 100) String title,
			String subtitle,
			String description,
			@NotNull MentoringDuration duration,
			@NotNull MentoringType mentoringType,
			@NotNull BookingType bookingType,
			@Min(1) int sessionCount,
			@Min(1) int maxParticipants,
			boolean excludeHolidays,
			@Min(0) int price,
			LocalDate endDate
	) {

		public MentoringCommand.Create toCommand(UserContext userContext) {
			return new MentoringCommand.Create(
					userContext.userId(), userContext.userName(), userContext.userField(),
					userContext.userEmail(), userContext.userType(),
					categoryId(), title(), subtitle(), description(),
					duration(), mentoringType(), bookingType(),
					sessionCount(), maxParticipants(), excludeHolidays(), price(), endDate()
			);
		}
	}
}
