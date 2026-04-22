package com.goggles.mentoring_service.domain.mentoring;

import com.goggles.mentoring_service.domain.category.MentoringCategoryId;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record CategorySnapshot(MentoringCategoryId mentoringCategoryId,
							   @Column(length = 50, name = "mentoring_category_name") String name,
							   @Column(length = 10, name = "mentoring_category_code") String code) {}
