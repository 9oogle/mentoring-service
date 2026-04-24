package com.goggles.mentoring_service.presentation;

import com.goggles.common.pagination.CommonPageRequest;
import com.goggles.common.pagination.CommonPageResponse;
import com.goggles.mentoring_service.application.command.CategoryCommand;
import com.goggles.mentoring_service.application.command.MentoringCommand;
import com.goggles.mentoring_service.application.query.MentoringSearchCondition;
import com.goggles.mentoring_service.application.query.MentoringSort;
import com.goggles.mentoring_service.application.result.CategoryResult;
import com.goggles.mentoring_service.application.result.MentoringResult;
import com.goggles.mentoring_service.application.service.CategoryService;
import com.goggles.mentoring_service.application.service.MentoringService;
import com.goggles.mentoring_service.domain.mentoring.MentoringStatus;
import com.goggles.mentoring_service.domain.mentoring.MentoringType;
import com.goggles.mentoring_service.presentation.dto.CategoryResponse;
import com.goggles.mentoring_service.presentation.dto.MentoringRequest;
import com.goggles.mentoring_service.presentation.dto.MentoringResponse;
import com.goggles.mentoring_service.presentation.support.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/")
@RequiredArgsConstructor
public class AdminController {

	private final CategoryService categoryService;


	@GetMapping("mentorings-categories")
	public CategoryResponse.CategoryListForAdmin getAllCategories(UserContext userContext) {
		CategoryCommand.GetList command =
				CategoryCommand.GetList.toRequest(userContext);
		List<CategoryResult.Info> categories= categoryService.getAllCategories(command);
		return  CategoryResponse.CategoryListForAdmin.of(categories);
	}

}