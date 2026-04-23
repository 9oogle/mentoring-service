package com.goggles.mentoring_service.infrastructure.persistence.jpa;

import com.goggles.mentoring_service.domain.category.MentoringCategory;
import com.goggles.mentoring_service.domain.category.MentoringCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MentoringCategoryJpaRepository extends JpaRepository<MentoringCategory, MentoringCategoryId> {
}