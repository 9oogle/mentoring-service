package com.goggles.mentoring_service.domain.mentoring.repository;

import com.goggles.mentoring_service.domain.mentoring.Mentoring;
import com.goggles.mentoring_service.domain.mentoring.MentoringId;

import java.util.Optional;

public interface MentoringRepository {
	void save(Mentoring mentoring);

	Optional<Mentoring> findById(MentoringId id);
}
