package com.goggles.mentoring_service.presentation.dto;

import com.goggles.mentoring_service.application.command.CategoryCommand;
import com.goggles.mentoring_service.domain.category.MentoringCategoryId;
import com.goggles.mentoring_service.presentation.support.UserContext;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;


public class CategoryRequest {

	public record Create(@NotBlank @Size(max = 100) String title,
						 @NotBlank @Size(max = 10) String code,
						 @NotNull @Min(0) Integer sortOrder) {
		public CategoryCommand.Create toCommand(UserContext userContext) {
			return new CategoryCommand.Create(title(), code(), sortOrder(), userContext.userId(),
					userContext.userType());
		}
	}

	public record Update(@Size(min = 1, max = 100) String title,
						 @Size(min = 1, max = 10) String code) {
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
