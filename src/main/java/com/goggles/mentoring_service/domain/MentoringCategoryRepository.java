package com.goggles.mentoring_service.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MentoringCategoryRepository {
	MentoringCategory save(MentoringCategory category);
	void saveAll(List<MentoringCategory> categories);
	Optional<MentoringCategory> findById(UUID mentoringCategoryId);
	List<MentoringCategory> findByActiveIsTrue();
}
