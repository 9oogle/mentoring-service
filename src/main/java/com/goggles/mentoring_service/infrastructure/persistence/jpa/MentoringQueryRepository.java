package com.goggles.mentoring_service.infrastructure.persistence.jpa;

import com.goggles.mentoring_service.application.query.MentoringSearchCondition;
import com.goggles.mentoring_service.domain.mentoring.Mentoring;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MentoringQueryRepository {
	Page<Mentoring> search(MentoringSearchCondition condition, Pageable pageable);
}