package com.goggles.mentoring_service.infrastructure.persistence;

import com.goggles.mentoring_service.domain.mentoring.Mentoring;
import com.goggles.mentoring_service.domain.mentoring.MentoringId;
import com.goggles.mentoring_service.domain.mentoring.repository.MentoringRepository;
import com.goggles.mentoring_service.infrastructure.persistence.jpa.MentoringJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MentoringRepositoryImpl implements MentoringRepository {

	private final MentoringJpaRepository jpaRepository;

	@Override
	public void save(Mentoring mentoring) {
		jpaRepository.save(mentoring);
	}

	@Override
	public Optional<Mentoring> findById(MentoringId id) {
		return jpaRepository.findById(id);
	}
}