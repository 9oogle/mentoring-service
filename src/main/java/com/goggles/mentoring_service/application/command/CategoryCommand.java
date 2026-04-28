package com.goggles.mentoring_service.application.command;

import com.goggles.mentoring_service.domain._common.UserType;
import com.goggles.mentoring_service.domain.category.MentoringCategoryId;
import com.goggles.mentoring_service.presentation.support.UserContext;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public class CategoryCommand {

	public record GetList(UUID userId, UserType userType) {}

	public record Create(String title, String code, int sortOrder, UUID creatorId,
						 UserType creatorType) {}


	public record Update(MentoringCategoryId mentoringCategoryId, String name, String code,
						 UUID userId, UserType userType) {
		public Update(MentoringCategoryId mentoringCategoryId, @Size(max = 100) String name, @Size(max = 10) String code, UserContext userContext) {
			this(mentoringCategoryId, name, code, userContext.userId(), userContext.userType());
		}
	}
}
