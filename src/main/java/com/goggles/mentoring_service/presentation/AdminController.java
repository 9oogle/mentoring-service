package com.goggles.mentoring_service.presentation;

import com.goggles.mentoring_service.application.command.CategoryCommand;
import com.goggles.mentoring_service.application.result.CategoryResult;
import com.goggles.mentoring_service.application.service.CategoryService;
import com.goggles.mentoring_service.presentation.dto.CategoryResponse;
import com.goggles.mentoring_service.presentation.support.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/")
@RequiredArgsConstructor
public class AdminController {

	private final CategoryService categoryService;


	@GetMapping("mentorings-categories")
	public CategoryResponse.CategoryListForAdmin getAllCategories(UserContext userContext) {
		CategoryCommand.GetList command = new CategoryCommand.GetList(userContext.userId(), userContext.userType());
		List<CategoryResult.Info> categories = categoryService.getAllCategories(command);
		return CategoryResponse.CategoryListForAdmin.of(categories);
	}

}