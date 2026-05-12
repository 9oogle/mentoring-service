package com.goggles.mentoring_service.infrastructure.persistence.jpa;

import com.goggles.mentoring_service.domain.mentoring.Mentoring;
import com.goggles.mentoring_service.domain.mentoring.MentoringId;
import com.goggles.mentoring_service.domain.mentoring.MentoringStatus;
import com.goggles.mentoring_service.domain.mentoring.SessionStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MentoringJpaRepository extends JpaRepository<Mentoring, MentoringId> {

	@Query("SELECT m FROM Mentoring m WHERE m.status = :status AND SIZE(m.repeatPatterns) > 0")
	List<Mentoring> findByStatusWithRepeatPatterns(MentoringStatus status);
}