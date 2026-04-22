package com.goggles.mentoring_service.domain;

import java.util.UUID;

public interface MentoringRepository {
	void save(Mentoring mentoring);
	Mentoring findById(UUID id);
}
