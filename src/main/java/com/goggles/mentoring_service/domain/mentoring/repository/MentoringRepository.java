package com.goggles.mentoring_service.domain.mentoring.repository;

import com.goggles.mentoring_service.domain.mentoring.Mentoring;
import com.goggles.mentoring_service.domain.mentoring.MentoringId;
import com.goggles.mentoring_service.domain.mentoring.MentoringSearchCondition;
import com.goggles.mentoring_service.domain.mentoring.MentoringStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MentoringRepository {

	void save(Mentoring mentoring);

	Optional<Mentoring> findById(MentoringId id);

	Page<Mentoring> findAll(MentoringSearchCondition condition, Pageable pageable);

	List<Mentoring> findActiveWithRepeatPatterns();
}
