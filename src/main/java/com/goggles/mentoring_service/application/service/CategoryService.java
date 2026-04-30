package com.goggles.mentoring_service.application.service;

import com.goggles.common.exception.ForbiddenException;
import com.goggles.mentoring_service.application.command.CategoryCommand;
import com.goggles.mentoring_service.application.result.CategoryResult;
import com.goggles.mentoring_service.domain._common.UserType;
import com.goggles.mentoring_service.domain.category.MentoringCategory;
import com.goggles.mentoring_service.domain.category.MentoringCategoryId;
import com.goggles.mentoring_service.domain.category.exception.CategoryNotFoundException;
import com.goggles.mentoring_service.domain.category.exception.CategoryValidationException;
import com.goggles.mentoring_service.domain.category.repository.MentoringCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
	private final MentoringCategoryRepository categoryRepository;

	@Transactional(readOnly = true)
	public List<CategoryResult.Info> getActiveCategories() {
		List<MentoringCategory> categories = categoryRepository.findByActiveIsTrue();
		return CategoryResult.Info.of(categories);
	}

	@Transactional(readOnly = true)
	public List<CategoryResult.Info> getAllCategories(CategoryCommand.GetList command) {
		checkAdmin(command);
		List<MentoringCategory> categories = categoryRepository.findAllByOrderBySortOrderAsc();
		return CategoryResult.Info.of(categories);
	}

	private void checkAdmin(CategoryCommand.GetList command) {
		if (command.userType() != UserType.MASTER) throw new ForbiddenException("관리자 권한이 필요합니다.");
	}

	@Transactional
	public UUID createCategory(CategoryCommand.Create command) {
		if (command.creatorType() != UserType.MASTER) throw new ForbiddenException("관리자 권한이 필요합니다.");
		if (categoryRepository.existsByName(command.title()))
			throw CategoryValidationException.alreadyExistsName();
		if (categoryRepository.existsByCode(command.code()))
			throw CategoryValidationException.alreadyExistsCode();
		MentoringCategory category =
				MentoringCategory.create(command.creatorId(), command.creatorType(),
						command.title(), command.code());
		categoryRepository.save(category);
		return category.getMentoringCategoryId()
				.categoryId();
	}

	@Transactional
	public void updateActiveCategories(CategoryCommand.UpdateActive command) {
		if (command.userType() != UserType.MASTER) throw new ForbiddenException("관리자 권한이 필요합니다.");

		List<MentoringCategory> all = categoryRepository.findAllByOrderBySortOrderAsc();
		Map<MentoringCategoryId, MentoringCategory> categoryMap = all.stream()
				.collect(Collectors.toMap(MentoringCategory::getMentoringCategoryId, c -> c));

		List<MentoringCategoryId> newCategorySetIds = command.categoryIds();
		if (new HashSet<>(newCategorySetIds).size() != newCategorySetIds.size())
			throw CategoryValidationException.duplicateCategoryIds();
		for (MentoringCategoryId id : newCategorySetIds) {
			if (!categoryMap.containsKey(id)) throw new CategoryNotFoundException(id.categoryId());
		}

		Set<MentoringCategoryId> newIds = new HashSet<>(newCategorySetIds);

		all.stream()
				.filter(category -> category.isActive() &&
						!newIds.contains(category.getMentoringCategoryId()))
				.forEach(category -> category.deactivate(command.userId(), command.userType()));

		for (int i = 0; i < newCategorySetIds.size(); i++) {
			MentoringCategory category = categoryMap.get(newCategorySetIds.get(i));
			if (!category.isActive() || !Objects.equals(category.getSortOrder(), i)) {
				category.activate(command.userId(), command.userType(), i);
			}
		}
	}

	@Transactional
	public void updateCategory(CategoryCommand.Update command) {
		MentoringCategory category = categoryRepository.findById(command.mentoringCategoryId())
				.orElseThrow(() -> new CategoryNotFoundException(command.mentoringCategoryId()
						.categoryId()));
		if (command.name() != null && !category.getName()
				.equals(command.name()) && categoryRepository.existsByName(command.name()))
			throw CategoryValidationException.alreadyExistsName();

		if (command.code() != null && !category.getCode()
				.equals(command.code()) && categoryRepository.existsByCode(command.code()))
			throw CategoryValidationException.alreadyExistsCode();
		category.updateNameAndCode(command.userId(), command.userType(), command.name(),
				command.code());
	}

	@Transactional
	public void deleteCategory(CategoryCommand.Delete command) {
		MentoringCategoryId categoryId = command.categoryId();
		MentoringCategory category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new CategoryNotFoundException(categoryId.categoryId()));
		category.softDelete(command.userId(), command.userType());
	}

}
