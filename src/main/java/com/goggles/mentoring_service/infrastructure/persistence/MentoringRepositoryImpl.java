package com.goggles.mentoring_service.infrastructure.persistence;

import com.goggles.mentoring_service.application.query.MentoringSearchCondition;
import com.goggles.mentoring_service.domain.mentoring.Mentoring;
import com.goggles.mentoring_service.domain.mentoring.MentoringId;
import com.goggles.mentoring_service.domain.mentoring.repository.MentoringRepository;
import com.goggles.mentoring_service.infrastructure.persistence.jpa.MentoringJpaRepository;
import com.goggles.mentoring_service.infrastructure.persistence.jpa.MentoringQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
@RequiredArgsConstructor
public class MentoringRepositoryImpl implements MentoringRepository {

	private final MentoringJpaRepository jpaRepository;
	private final MentoringQueryRepository queryRepository;

	@Override
	public void save(Mentoring mentoring) {
		jpaRepository.save(mentoring);
	}

	@Override
	public Optional<Mentoring> findById(MentoringId id) {
		return jpaRepository.findById(id);
	}

	@Override
	public Page<Mentoring> findAll(MentoringSearchCondition condition, Pageable pageable) {
		return queryRepository.search(condition, pageable);
	}
}