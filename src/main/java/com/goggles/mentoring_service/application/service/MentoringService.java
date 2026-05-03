package com.goggles.mentoring_service.application.service;

import com.goggles.common.pagination.CommonPageRequest;
import com.goggles.mentoring_service.application.command.MentoringCommand;
import com.goggles.mentoring_service.application.command.TimeSchedules;
import com.goggles.mentoring_service.application.result.MentoringResult;
import com.goggles.mentoring_service.domain._common.UserType;
import com.goggles.mentoring_service.domain.category.MentoringCategory;
import com.goggles.mentoring_service.domain.category.MentoringCategoryId;
import com.goggles.mentoring_service.domain.category.exception.CategoryNotFoundException;
import com.goggles.mentoring_service.domain.category.repository.MentoringCategoryRepository;
import com.goggles.mentoring_service.domain.mentoring.Mentoring;
import com.goggles.mentoring_service.domain.mentoring.MentoringId;
import com.goggles.mentoring_service.domain.mentoring.MentoringSearchCondition;
import com.goggles.mentoring_service.domain.mentoring.RepeatPattern;
import com.goggles.mentoring_service.domain.mentoring.exception.MentoringNotFoundException;
import com.goggles.mentoring_service.domain.mentoring.repository.MentoringRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
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

	@Transactional(readOnly = true)
	public MentoringResult.Detail getMentoring(UUID mentoringId) {
		return MentoringResult.Detail.from(getOrThrow(mentoringId));
	}

	@Transactional(readOnly = true)
	public MentoringResult.Schedules getMentoringSchedules(UUID mentoringId) {
		return MentoringResult.Schedules.from(getOrThrow(mentoringId));
	}

	@Transactional(readOnly = true)
	public Page<MentoringResult.Summary> searchMentorings(MentoringSearchCondition condition,
			CommonPageRequest pageRequest) {
		Page<Mentoring> page =
				mentoringRepository.findAll(condition, pageRequest.toPageable(Sort.unsorted()));
		return page.map(MentoringResult.Summary::from);
	}

	public void updateMentoring(UUID mentoringId, MentoringCommand.Update command) {
		Mentoring mentoring = getOrThrow(mentoringId);
		List<TimeSchedules> timeSchedules = command.timeSchedules();
		List<RepeatPattern> patterns = timeSchedules != null ? command.timeSchedules()
				.stream()
				.map(ts -> RepeatPattern.of(ts.dayOfWeek(), ts.startTime(), ts.endTime()))
				.toList() : null;
		mentoring.updateInfo(command.userId(), command.userType(), command.title(),
				command.subtitle(), command.description(), command.price(), command.endDate(),
				patterns, LocalDate.now());
	}


	public void deactivateMentoring(UUID mentoringId, UUID userId, UserType userType) {
		Mentoring mentoring = getOrThrow(mentoringId);
		mentoring.deactivate(userId, userType);
	}

	public void deleteMentoring(UUID mentoringId, UUID userId, UserType userType) {
		Mentoring mentoring = getOrThrow(mentoringId);
		mentoring.delete(userId, userType);
	}

	private Mentoring getOrThrow(UUID mentoringId) {
		MentoringId id = new MentoringId(mentoringId);
		return mentoringRepository.findById(id)
				.orElseThrow(() -> new MentoringNotFoundException(id));
	}


}