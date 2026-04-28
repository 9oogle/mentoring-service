package com.goggles.mentoring_service.application.service;

import com.goggles.common.exception.ForbiddenException;
import com.goggles.mentoring_service.application.command.CategoryCommand;
import com.goggles.mentoring_service.application.result.CategoryResult;
import com.goggles.mentoring_service.domain._common.UserType;
import com.goggles.mentoring_service.domain.category.MentoringCategory;
import com.goggles.mentoring_service.domain.category.exception.CategoryNotFoundException;
import com.goggles.mentoring_service.domain.category.exception.CategoryValidationException;
import com.goggles.mentoring_service.domain.category.MentoringCategoryId;
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
	void getActive_success() {
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
	void getActive_returnsEmpty() {
		given(categoryRepository.findByActiveIsTrue()).willReturn(List.of());

		List<CategoryResult.Info> result = categoryService.getActiveCategories();

		assertThat(result).isEmpty();
	}

	// ── getAllCategories ──────────────────────────────────────────────────────

	@Test
	void getAll_success() {
		MentoringCategory active = createActive(0);
		MentoringCategory inactive = createInactive();
		given(categoryRepository.findAllByOrderBySortOrderAsc()).willReturn(
				List.of(active, inactive));

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
	void getAll_forbidden_if_not_master() {
		assertThatThrownBy(() -> categoryService.getAllCategories(
				new CategoryCommand.GetList(UUID.randomUUID(), UserType.INSTRUCTOR))).isInstanceOf(
				ForbiddenException.class);
	}

	// ── createCategory ────────────────────────────────────────────────────────

	@Test
	void create_success() {
		CategoryCommand.Create command = new CategoryCommand.Create("Java", "JAVA", 0,
				UUID.randomUUID(), UserType.MASTER);

		UUID result = categoryService.createCategory(command);

		verify(categoryRepository).save(any(MentoringCategory.class));
		assertThat(result).isNotNull();
	}

	@Test
	void create_forbidden_if_not_master() {
		CategoryCommand.Create command = new CategoryCommand.Create("Java", "JAVA", 0,
				UUID.randomUUID(), UserType.INSTRUCTOR);

		assertThatThrownBy(() -> categoryService.createCategory(command))
				.isInstanceOf(ForbiddenException.class);
		verifyNoInteractions(categoryRepository);
	}

	// ── updateActiveCategories ────────────────────────────────────────────────

	@Test
	void updateActive_success() {
		UUID adminId = UUID.randomUUID();
		MentoringCategory c0 = createActive(0);
		MentoringCategory c1 = createActive(1);
		MentoringCategory c2 = createInactive();
		given(categoryRepository.findAllByOrderBySortOrderAsc()).willReturn(List.of(c0, c1, c2));

		// 순서를 [c2, c0]으로 변경, c1은 제외(비활성화)
		categoryService.updateActiveCategories(new CategoryCommand.UpdateActive(adminId,
				UserType.MASTER, List.of(c2.getMentoringCategoryId().categoryId(),
						c0.getMentoringCategoryId().categoryId())));

		assertThat(c2.isActive()).isTrue();
		assertThat(c2.getSortOrder()).isEqualTo(0);
		assertThat(c0.isActive()).isTrue();
		assertThat(c0.getSortOrder()).isEqualTo(1);
		assertThat(c1.isActive()).isFalse();
		assertThat(c1.getSortOrder()).isNull();
	}

	@Test
	void updateActive_empty_list() {
		UUID adminId = UUID.randomUUID();
		MentoringCategory c0 = createActive(0);
		MentoringCategory c1 = createActive(1);
		given(categoryRepository.findAllByOrderBySortOrderAsc()).willReturn(List.of(c0, c1));

		categoryService.updateActiveCategories(
				new CategoryCommand.UpdateActive(adminId, UserType.MASTER, List.of()));

		assertThat(c0.isActive()).isFalse();
		assertThat(c1.isActive()).isFalse();
	}

	@Test
	void updateActive_not_found() {
		given(categoryRepository.findAllByOrderBySortOrderAsc()).willReturn(List.of());

		assertThatThrownBy(() -> categoryService.updateActiveCategories(
				new CategoryCommand.UpdateActive(UUID.randomUUID(), UserType.MASTER,
						List.of(UUID.randomUUID())))).isInstanceOf(CategoryNotFoundException.class);
	}

	@Test
	void updateActive_forbidden_if_not_master() {
		assertThatThrownBy(() -> categoryService.updateActiveCategories(
				new CategoryCommand.UpdateActive(UUID.randomUUID(), UserType.INSTRUCTOR,
						List.of()))).isInstanceOf(ForbiddenException.class);
		verifyNoInteractions(categoryRepository);
	}

	// ── updateCategory ────────────────────────────────────────────────────────

	@Test
	void update_success() {
		MentoringCategory category = createActive(0);
		MentoringCategoryId id = category.getMentoringCategoryId();
		given(categoryRepository.findById(id)).willReturn(java.util.Optional.of(category));
		given(categoryRepository.existsByName("NewName")).willReturn(false);
		given(categoryRepository.existsByCode("NEW")).willReturn(false);

		categoryService.updateCategory(
				new CategoryCommand.Update(id, "NewName", "NEW", UUID.randomUUID(), UserType.MASTER));

		assertThat(category.getName()).isEqualTo("NewName");
		assertThat(category.getCode()).isEqualTo("NEW");
	}

	@Test
	void update_nameConflict() {
		MentoringCategory category = createActive(0);
		MentoringCategoryId id = category.getMentoringCategoryId();
		given(categoryRepository.findById(id)).willReturn(java.util.Optional.of(category));
		given(categoryRepository.existsByName("DupName")).willReturn(true);

		assertThatThrownBy(() -> categoryService.updateCategory(
				new CategoryCommand.Update(id, "DupName", category.getCode(), UUID.randomUUID(),
						UserType.MASTER))).isInstanceOf(CategoryValidationException.class)
				.hasMessageContaining("이름");
	}

	@Test
	void update_codeConflict() {
		MentoringCategory category = createActive(0);
		MentoringCategoryId id = category.getMentoringCategoryId();
		given(categoryRepository.findById(id)).willReturn(java.util.Optional.of(category));
		given(categoryRepository.existsByCode("DUPCODE")).willReturn(true);

		assertThatThrownBy(() -> categoryService.updateCategory(
				new CategoryCommand.Update(id, category.getName(), "DUPCODE", UUID.randomUUID(),
						UserType.MASTER))).isInstanceOf(CategoryValidationException.class)
				.hasMessageContaining("코드");
	}

	@Test
	void update_null_name() {
		MentoringCategory category = createActive(0);
		MentoringCategoryId id = category.getMentoringCategoryId();
		given(categoryRepository.findById(id)).willReturn(java.util.Optional.of(category));
		given(categoryRepository.existsByCode("NEW")).willReturn(false);

		categoryService.updateCategory(
				new CategoryCommand.Update(id, null, "NEW", UUID.randomUUID(), UserType.MASTER));

		assertThat(category.getName()).isNotNull();
		assertThat(category.getCode()).isEqualTo("NEW");
	}

	@Test
	void update_null_code() {
		MentoringCategory category = createActive(0);
		MentoringCategoryId id = category.getMentoringCategoryId();
		given(categoryRepository.findById(id)).willReturn(java.util.Optional.of(category));
		given(categoryRepository.existsByName("NewName")).willReturn(false);

		categoryService.updateCategory(
				new CategoryCommand.Update(id, "NewName", null, UUID.randomUUID(), UserType.MASTER));

		assertThat(category.getName()).isEqualTo("NewName");
		assertThat(category.getCode()).isNotNull();
	}

	@Test
	void update_notFound() {
		MentoringCategoryId id = new MentoringCategoryId(UUID.randomUUID());
		given(categoryRepository.findById(id)).willReturn(java.util.Optional.empty());

		assertThatThrownBy(() -> categoryService.updateCategory(
				new CategoryCommand.Update(id, "Name", "CODE", UUID.randomUUID(),
						UserType.MASTER))).isInstanceOf(CategoryNotFoundException.class);
	}

	// ── deleteCategory ────────────────────────────────────────────────────────

	@Test
	void delete_success() {
		MentoringCategory category = createActive(0);
		MentoringCategoryId id = category.getMentoringCategoryId();
		given(categoryRepository.findById(id)).willReturn(java.util.Optional.of(category));

		categoryService.deleteCategory(
				new CategoryCommand.Delete(UUID.randomUUID(), UserType.MASTER, id));

		assertThat(category.isActive()).isFalse();
		assertThat(category.getSortOrder()).isNull();
	}

	@Test
	void delete_notFound() {
		MentoringCategoryId id = new MentoringCategoryId(UUID.randomUUID());
		given(categoryRepository.findById(id)).willReturn(java.util.Optional.empty());

		assertThatThrownBy(() -> categoryService.deleteCategory(
				new CategoryCommand.Delete(UUID.randomUUID(), UserType.MASTER, id)))
				.isInstanceOf(CategoryNotFoundException.class);
	}

	@Test
	void delete_forbidden_if_not_master() {
		MentoringCategory category = createActive(0);
		MentoringCategoryId id = category.getMentoringCategoryId();
		given(categoryRepository.findById(id)).willReturn(java.util.Optional.of(category));

		assertThatThrownBy(() -> categoryService.deleteCategory(
				new CategoryCommand.Delete(UUID.randomUUID(), UserType.INSTRUCTOR, id)))
				.isInstanceOf(ForbiddenException.class);
	}
}
