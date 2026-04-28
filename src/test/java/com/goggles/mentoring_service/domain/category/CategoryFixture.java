package com.goggles.mentoring_service.domain.category;

import com.goggles.mentoring_service.domain._common.UserType;

import java.util.UUID;

public class CategoryFixture {

	public static MentoringCategory createActive(int sortOrder) {
		UUID adminId = UUID.randomUUID();
		MentoringCategory category = MentoringCategory.create(adminId, UserType.MASTER,
				UUID.randomUUID()
						.toString(), UUID.randomUUID()
						.toString()
						.substring(0, 8)
						.toUpperCase());
		category.activate(adminId, UserType.MASTER, sortOrder);
		return category;
	}

	public static MentoringCategory createInactive() {
		return MentoringCategory.create(UUID.randomUUID(), UserType.MASTER, UUID.randomUUID()
				.toString(), UUID.randomUUID()
				.toString()
				.substring(0, 8)
				.toUpperCase());
	}
}