package com.goggles.mentoring_service.presentation.dto;

import com.goggles.mentoring_service.application.command.CategoryCommand;
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

public class CategoryRequest {

	public record Create( @NotBlank @Size(max = 100) String title,
						  @NotBlank @Size(max = 10) String code,@NotNull int sortOrder) {
		public CategoryCommand.Create toCommand(UserContext userContext) {
			return new CategoryCommand.Create(title(), code(), sortOrder(), userContext.userId(),
					userContext.userType());
		}}

}
