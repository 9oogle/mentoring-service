package com.goggles.mentoring_service.presentation;

import com.goggles.mentoring_service.application.command.CategoryCommand;
import com.goggles.mentoring_service.application.result.CategoryResult;
import com.goggles.mentoring_service.application.service.CategoryService;
import com.goggles.mentoring_service.presentation.dto.CategoryRequest;
import com.goggles.mentoring_service.presentation.dto.CategoryResponse;
import com.goggles.mentoring_service.presentation.support.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/mentoring-categories")
@RequiredArgsConstructor
public class CategoryController {

	private final CategoryService categoryService;


	@GetMapping
	public CategoryResponse.CategoryList getActiveCategories() {
		List<CategoryResult.Info> categories = categoryService.getActiveCategories();
		return CategoryResponse.CategoryList.of(categories);
	}

}