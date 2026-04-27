package com.goggles.mentoring_service.application.service;

import com.goggles.common.exception.ForbiddenException;
import com.goggles.mentoring_service.application.command.CategoryCommand;
import com.goggles.mentoring_service.application.result.CategoryResult;
import com.goggles.mentoring_service.domain._common.UserType;
import com.goggles.mentoring_service.domain.category.MentoringCategory;
import com.goggles.mentoring_service.domain.category.repository.MentoringCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
}
