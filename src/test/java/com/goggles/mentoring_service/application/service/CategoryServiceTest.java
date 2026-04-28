package com.goggles.mentoring_service.application.service;

import com.goggles.common.exception.ForbiddenException;
import com.goggles.mentoring_service.application.command.CategoryCommand;
import com.goggles.mentoring_service.application.result.CategoryResult;
import com.goggles.mentoring_service.domain._common.UserType;
import com.goggles.mentoring_service.domain.category.MentoringCategory;
import com.goggles.mentoring_service.domain.category.repository.MentoringCategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static com.goggles.mentoring_service.domain.category.CategoryFixture.createActive;
import static com.goggles.mentoring_service.domain.category.CategoryFixture.createInactive;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

	@InjectMocks
	private CategoryService categoryService;

	@Mock
	private MentoringCategoryRepository categoryRepository;

	// ── getActiveCategories ───────────────────────────────────────────────────

  @Test
  void getActiveCategories_success() {
    MentoringCategory c1 = createActive(0);
    MentoringCategory c2 = createActive(1);
    given(categoryRepository.findByActiveIsTrue()).willReturn(List.of(c1, c2));

		List<CategoryResult.Info> result = categoryService.getActiveCategories();

		assertThat(result).hasSize(2);
		assertThat(result.get(0)
				.getName()).isEqualTo(c1.getName());
		assertThat(result.get(0)
				.getCode()).isEqualTo(c1.getCode());
		assertThat(result.get(0)
				.getSortOrder()).isEqualTo(0);
		assertThat(result.get(0)
				.isActive()).isTrue();
		assertThat(result.get(1)
				.getName()).isEqualTo(c2.getName());
	}

  @Test
  void getActiveCategories_empty() {
    given(categoryRepository.findByActiveIsTrue()).willReturn(List.of());

		List<CategoryResult.Info> result = categoryService.getActiveCategories();

		assertThat(result).isEmpty();
	}

	// ── getAllCategories ──────────────────────────────────────────────────────

  @Test
  void getAllCategories_success() {
    MentoringCategory active = createActive(0);
    MentoringCategory inactive = createInactive();
    given(categoryRepository.findAllByOrderBySortOrderAsc()).willReturn(List.of(active, inactive));

		List<CategoryResult.Info> result = categoryService.getAllCategories(
				new CategoryCommand.GetList(UUID.randomUUID(), UserType.MASTER));

		assertThat(result).hasSize(2);
		assertThat(result.get(0)
				.isActive()).isTrue();
		assertThat(result.get(0)
				.getSortOrder()).isEqualTo(0);
		assertThat(result.get(1)
				.isActive()).isFalse();
		assertThat(result.get(1)
				.getSortOrder()).isNull();
	}

	@Test
	void getAllCategories_throws_forbidden_when_not_master() {
		assertThatThrownBy(() -> categoryService.getAllCategories(
				new CategoryCommand.GetList(UUID.randomUUID(), UserType.INSTRUCTOR))).isInstanceOf(
				ForbiddenException.class);
	}

	// ── createCategory ────────────────────────────────────────────────────────

	@Test
	void createCategory_success_when_master() {
		CategoryCommand.Create command = new CategoryCommand.Create("Java", "JAVA", 0,
				UUID.randomUUID(), UserType.MASTER);

		UUID result = categoryService.createCategory(command);

		verify(categoryRepository).save(any(MentoringCategory.class));
		assertThat(result).isNotNull();
	}

	@Test
	void createCategory_throws_forbidden_when_not_master() {
		CategoryCommand.Create command = new CategoryCommand.Create("Java", "JAVA", 0,
				UUID.randomUUID(), UserType.INSTRUCTOR);

		assertThatThrownBy(() -> categoryService.createCategory(command))
				.isInstanceOf(ForbiddenException.class);
		verifyNoInteractions(categoryRepository);
	}
}
