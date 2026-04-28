package com.goggles.mentoring_service.domain.category.repository;

import com.goggles.mentoring_service.domain.category.MentoringCategory;
import com.goggles.mentoring_service.domain.category.MentoringCategoryId;

import java.util.List;
import java.util.Optional;

public interface MentoringCategoryRepository {
	MentoringCategory save(MentoringCategory category);

	void saveAll(List<MentoringCategory> categories);

	Optional<MentoringCategory> findById(MentoringCategoryId id);

	List<MentoringCategory> findByActiveIsTrue();

	List<MentoringCategory> findAllByOrderBySortOrderAsc();
}
