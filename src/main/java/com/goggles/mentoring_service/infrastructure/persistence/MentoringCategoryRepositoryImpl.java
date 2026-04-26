package com.goggles.mentoring_service.infrastructure.persistence;

import com.goggles.mentoring_service.domain.category.MentoringCategory;
import com.goggles.mentoring_service.domain.category.MentoringCategoryId;
import com.goggles.mentoring_service.domain.category.repository.MentoringCategoryRepository;
import com.goggles.mentoring_service.infrastructure.persistence.jpa.MentoringCategoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MentoringCategoryRepositoryImpl implements MentoringCategoryRepository {

	private final MentoringCategoryJpaRepository jpaRepository;

	@Override
	public MentoringCategory save(MentoringCategory category) {
		return jpaRepository.save(category);
	}

	@Override
	public void saveAll(List<MentoringCategory> categories) {
		jpaRepository.saveAll(categories);
	}

	@Override
	public Optional<MentoringCategory> findById(MentoringCategoryId id) {
		return jpaRepository.findById(id);
	}

	@Override
	public List<MentoringCategory> findByActiveIsTrue() {
		return jpaRepository.findByActiveIsTrue();
	}
}