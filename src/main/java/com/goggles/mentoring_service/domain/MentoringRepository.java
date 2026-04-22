package com.goggles.mentoring_service.domain;

import java.util.Optional;
import java.util.UUID;

public interface MentoringRepository {
	void save(Mentoring mentoring);
	Optional<Mentoring> findById(MentoringId id);
}
