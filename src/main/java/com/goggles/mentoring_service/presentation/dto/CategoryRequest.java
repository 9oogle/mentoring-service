package com.goggles.mentoring_service.presentation.dto;

import com.goggles.mentoring_service.application.command.CategoryCommand;
import com.goggles.mentoring_service.application.command.MentoringCommand;
import com.goggles.mentoring_service.application.command.SessionSlot;
import com.goggles.mentoring_service.application.command.TimeSchedules;
import com.goggles.mentoring_service.domain.category.MentoringCategoryId;
import com.goggles.mentoring_service.domain.mentoring.Format;
import com.goggles.mentoring_service.domain.mentoring.MentoringDuration;
import com.goggles.mentoring_service.domain.mentoring.MentoringType;
import com.goggles.mentoring_service.presentation.support.UserContext;
import jakarta.validation.constraints.*;


public class CategoryRequest {

	public record Create( @NotBlank @Size(max = 100) String title,
						  @NotBlank @Size(max = 10) String code,@NotNull int sortOrder) {
		public CategoryCommand.Create toCommand(UserContext userContext) {
			return new CategoryCommand.Create(title(), code(), sortOrder(), userContext.userId(),
					userContext.userType());
		}}

	public record Update(
			@Size(max = 100) String title,
			@Size(max = 10) String code) {
		public CategoryCommand.Update toCommand(UserContext userContext, UUID categoryId) {
			MentoringCategoryId mentoringCategoryId = new MentoringCategoryId(categoryId);
			return new CategoryCommand.Update(mentoringCategoryId, title(), code(),
					userContext.userId(), userContext.userType());
		}
	}

	public record UpdateActive(@NotNull List<UUID> categoryIds) {
		public CategoryCommand.UpdateActive toCommand(UserContext userContext) {
			List<MentoringCategoryId> categoryIds = categoryIds().stream()
					.map(MentoringCategoryId::new)
					.toList();
			return new CategoryCommand.UpdateActive(userContext.userId(), userContext.userType(),
					categoryIds);
		}
	}
}
