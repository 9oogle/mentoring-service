package com.goggles.mentoring_service.domain;

import java.util.List;
import java.util.Optional;

public interface MentoringCategoryRepository {
	MentoringCategory save(MentoringCategory category);

	void saveAll(List<MentoringCategory> categories);

	Optional<MentoringCategory> findById(MentoringCategoryId id);

	List<MentoringCategory> findByActiveIsTrue();
}
