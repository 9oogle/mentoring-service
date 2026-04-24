package com.goggles.mentoring_service.presentation;

import com.goggles.mentoring_service.application.result.CategoryResult;
import com.goggles.mentoring_service.application.service.CategoryService;
import com.goggles.mentoring_service.presentation.dto.CategoryResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mentorings-categories")
@RequiredArgsConstructor
public class MentoringCategoryController {

  private final CategoryService categoryService;

  @GetMapping
  public CategoryResponse.CategoryList getActiveCategories() {
    List<CategoryResult.Info> categories = categoryService.getActiveCategories();
    return CategoryResponse.CategoryList.of(categories);
  }
}
