package com.goggles.mentoring_service.presentation;

import com.goggles.mentoring_service.application.command.CategoryCommand;
import com.goggles.mentoring_service.application.service.CategoryService;
import com.goggles.mentoring_service.presentation.dto.CategoryRequest;
import com.goggles.mentoring_service.presentation.dto.CategoryResponse;
import com.goggles.mentoring_service.presentation.support.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/mentoring-categories")
@RequiredArgsConstructor
public class CategoryController {

	private final CategoryService categoryService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CategoryResponse.Create createCategory(UserContext userContext,
			@Valid @RequestBody CategoryRequest.Create request) {
		CategoryCommand.Create command = request.toCommand(userContext);
		UUID categoryId = categoryService.createCategory(command);
		return new CategoryResponse.Create(categoryId);
	}
}