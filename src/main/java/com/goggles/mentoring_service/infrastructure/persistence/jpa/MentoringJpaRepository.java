package com.goggles.mentoring_service.infrastructure.persistence.jpa;

import com.goggles.mentoring_service.domain.mentoring.Mentoring;
import com.goggles.mentoring_service.domain.mentoring.MentoringId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentoringJpaRepository extends JpaRepository<Mentoring, MentoringId> {}