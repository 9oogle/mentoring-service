package com.goggles.mentoring_service.domain.mentoring;

import com.goggles.mentoring_service.domain.category.MentoringCategoryId;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public class Category {
	private UUID categoryId;
	@Column(length = 50, name = "category_name")
	private String name;
	@Column(length = 10, name = "category_code")
	private String code;
}
