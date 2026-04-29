package com.goggles.mentoring_service.application.command;

import com.goggles.mentoring_service.domain._common.UserType;
import com.goggles.mentoring_service.domain.category.MentoringCategoryId;

import java.util.List;
import java.util.UUID;

public class CategoryCommand {

	public record GetList(UUID userId, UserType userType) {}

	public record Create(String title, String code, Integer sortOrder, UUID creatorId,
						 UserType creatorType) {}

	public record UpdateActive(UUID userId, UserType userType,
							   List<MentoringCategoryId> categoryIds) {}

	public record Update(MentoringCategoryId mentoringCategoryId, String name, String code,
						 UUID userId, UserType userType) {}

	public record Delete(UUID userId, UserType userType, MentoringCategoryId categoryId) {}
}
