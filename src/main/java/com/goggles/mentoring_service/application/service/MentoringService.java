package com.goggles.mentoring_service.application.service;

import com.goggles.common.pagination.CommonPageRequest;
import com.goggles.mentoring_service.application.command.MentoringCommand;
import com.goggles.mentoring_service.domain.mentoring.MentoringSearchCondition;
import com.goggles.mentoring_service.application.result.MentoringResult;
import com.goggles.mentoring_service.domain.category.MentoringCategory;
import com.goggles.mentoring_service.domain.category.MentoringCategoryId;
import com.goggles.mentoring_service.domain.category.exception.CategoryNotFoundException;
import com.goggles.mentoring_service.domain.category.repository.MentoringCategoryRepository;
import com.goggles.mentoring_service.domain.mentoring.Mentoring;
import com.goggles.mentoring_service.domain.mentoring.MentoringId;
import com.goggles.mentoring_service.domain.mentoring.exception.MentoringNotFoundException;
import com.goggles.mentoring_service.domain.mentoring.repository.MentoringRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MentoringService {

	private final MentoringRepository mentoringRepository;
	private final MentoringCategoryRepository categoryRepository;

	public UUID createMentoring(MentoringCommand.Create command) {
		UUID categoryId = command.categoryId();
		MentoringCategory category = categoryRepository.findById(new MentoringCategoryId(categoryId))
				.orElseThrow(() -> new CategoryNotFoundException(categoryId));

		Mentoring mentoring = command.toMentoring(category);

		mentoringRepository.save(mentoring);
		return mentoring.getMentoringId().mentoringId();
	}

	@Transactional(readOnly = true)
	public MentoringResult.Detail getMentoring(UUID mentoringId) {
		Mentoring mentoring = mentoringRepository.findById(new MentoringId(mentoringId))
				.orElseThrow(() -> new MentoringNotFoundException(new MentoringId(mentoringId)));
		return MentoringResult.Detail.from(mentoring);
	}

	@Transactional(readOnly = true)
	public MentoringResult.Schedules getMentoringSchedules(UUID mentoringId) {
		Mentoring mentoring = mentoringRepository.findById(new MentoringId(mentoringId))
				.orElseThrow(() -> new MentoringNotFoundException(new MentoringId(mentoringId)));
		return MentoringResult.Schedules.from(mentoring);
	}

	@Transactional(readOnly = true)
	public Page<MentoringResult.Summary> searchMentorings(
			MentoringSearchCondition condition, CommonPageRequest pageRequest) {
		Page<Mentoring> page = mentoringRepository.findAll(condition, pageRequest.toPageable(Sort.unsorted()));
		return page.map(MentoringResult.Summary::from);
	}
}