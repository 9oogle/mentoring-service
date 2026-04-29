package com.goggles.mentoring_service.presentation;

import com.goggles.mentoring_service.application.command.CategoryCommand;
import com.goggles.mentoring_service.application.result.CategoryResult;
import com.goggles.mentoring_service.application.service.CategoryService;
import com.goggles.mentoring_service.domain.category.MentoringCategoryId;
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
@RequestMapping("/api/v1/admin/")
@RequiredArgsConstructor
public class AdminController {

	private final CategoryService categoryService;


	@GetMapping("mentoring-categories")
	public CategoryResponse.CategoryListForAdmin getAllCategories(UserContext userContext) {
		CategoryCommand.GetList command =
				new CategoryCommand.GetList(userContext.userId(), userContext.userType());
		List<CategoryResult.Info> categories = categoryService.getAllCategories(command);
		return CategoryResponse.CategoryListForAdmin.of(categories);
	}

	@PostMapping("mentoring-categories")
	@ResponseStatus(HttpStatus.CREATED)
	public CategoryResponse.Create createCategory(UserContext userContext,
			@Valid @RequestBody CategoryRequest.Create request) {
		CategoryCommand.Create command = request.toCommand(userContext);
		UUID categoryId = categoryService.createCategory(command);
		return new CategoryResponse.Create(categoryId);
	}

	@PutMapping("mentoring-categories/active")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateActiveCategories(UserContext userContext,
			@Valid @RequestBody CategoryRequest.UpdateActive request) {
		categoryService.updateActiveCategories(request.toCommand(userContext));
	}

	@PatchMapping("mentoring-categories/{categoryId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateCategory(UserContext userContext, @PathVariable UUID categoryId,
			@Valid @RequestBody CategoryRequest.Update request) {
		CategoryCommand.Update command = request.toCommand(userContext, categoryId);
		categoryService.updateCategory(command);
	}

	@DeleteMapping("mentoring-categories/{categoryId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteCategory(UserContext userContext, @PathVariable UUID categoryId) {
		CategoryCommand.Delete command =
				new CategoryCommand.Delete(userContext.userId(), userContext.userType(),
						new MentoringCategoryId(categoryId));
		categoryService.deleteCategory(command);
	}

}