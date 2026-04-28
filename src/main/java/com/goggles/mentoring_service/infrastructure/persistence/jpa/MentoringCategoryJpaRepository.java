package com.goggles.mentoring_service.infrastructure.persistence.jpa;

import com.goggles.mentoring_service.domain.category.MentoringCategory;
import com.goggles.mentoring_service.domain.category.MentoringCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MentoringCategoryJpaRepository extends JpaRepository<MentoringCategory, MentoringCategoryId> {
	List<MentoringCategory> findByActiveIsTrueOrderBySortOrderAsc();

	@Query("SELECT c FROM MentoringCategory c ORDER BY c.sortOrder ASC NULLS LAST")
	List<MentoringCategory> findAllByOrderBySortOrderAsc();
}