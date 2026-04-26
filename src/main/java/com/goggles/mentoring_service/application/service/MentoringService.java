package com.goggles.mentoring_service.application.service;

import com.goggles.mentoring_service.application.command.MentoringCommand;
import com.goggles.mentoring_service.domain.category.MentoringCategory;
import com.goggles.mentoring_service.domain.category.MentoringCategoryId;
import com.goggles.mentoring_service.domain.category.exception.CategoryNotFoundException;
import com.goggles.mentoring_service.domain.category.repository.MentoringCategoryRepository;
import com.goggles.mentoring_service.domain.mentoring.Mentoring;
import com.goggles.mentoring_service.domain.mentoring.repository.MentoringRepository;
import lombok.RequiredArgsConstructor;
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
		MentoringCategory category =
				categoryRepository.findById(new MentoringCategoryId(categoryId))
						.orElseThrow(() -> new CategoryNotFoundException(categoryId));

		Mentoring mentoring = command.toMentoring(category);

		mentoringRepository.save(mentoring);
		return mentoring.getMentoringId()
				.mentoringId();
	}
}
