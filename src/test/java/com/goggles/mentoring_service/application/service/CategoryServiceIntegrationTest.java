package com.goggles.mentoring_service.application.service;

import com.goggles.common.exception.ForbiddenException;
import com.goggles.mentoring_service.domain.category.exception.CategoryNotFoundException;
import com.goggles.mentoring_service.domain.category.exception.CategoryValidationException;
import com.goggles.mentoring_service.application.command.CategoryCommand;
import com.goggles.mentoring_service.application.result.CategoryResult;
import com.goggles.mentoring_service.config.TestAuditConfig;
import com.goggles.mentoring_service.domain._common.UserType;
import com.goggles.mentoring_service.domain.category.MentoringCategory;
import com.goggles.mentoring_service.domain.category.MentoringCategoryId;
import com.goggles.mentoring_service.domain.category.repository.MentoringCategoryRepository;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.goggles.mentoring_service.domain.category.CategoryFixture.createActive;
import static com.goggles.mentoring_service.domain.category.CategoryFixture.createInactive;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(TestAuditConfig.class)
@ActiveProfiles("test")
@Transactional
class CategoryServiceIntegrationTest {

	private static final Logger log = LoggerFactory.getLogger(CategoryServiceIntegrationTest.class);

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private MentoringCategoryRepository categoryRepository;

	// ── getActiveCategories ───────────────────────────────────────────────────

	@Test
	void getActive_orderedBySortOrder() {
		MentoringCategory third = categoryRepository.save(createActive(2));
		MentoringCategory first = categoryRepository.save(createActive(0));
		MentoringCategory second = categoryRepository.save(createActive(1));
		categoryRepository.save(createInactive());

		List<CategoryResult.Info> result = categoryService.getActiveCategories();

		log.info("==== 활성 카테고리 조회 ====");
		result.forEach(
				c -> log.info("  [{}] {} sortOrder={}", c.isActive() ? "활성" : "비활성", c.getName(),
						c.getSortOrder()));

		assertThat(result).hasSize(3);
		assertThat(result).allMatch(CategoryResult.Info::isActive);
		assertThat(result.get(0)
				.getCategoryId()).isEqualTo(first.getMentoringCategoryId()
				.categoryId());
		assertThat(result.get(1)
				.getCategoryId()).isEqualTo(second.getMentoringCategoryId()
				.categoryId());
		assertThat(result.get(2)
				.getCategoryId()).isEqualTo(third.getMentoringCategoryId()
				.categoryId());
	}

	@Test
	void getActive_returnsEmpty() {
		categoryRepository.save(createInactive());

		List<CategoryResult.Info> result = categoryService.getActiveCategories();

		log.info("활성 카테고리 없음, 결과: {}건", result.size());
		assertThat(result).isEmpty();
	}

	// ── getAllCategories ──────────────────────────────────────────────────────

	@Test
	void getAll_includesInactive() {
		categoryRepository.save(createActive(0));
		categoryRepository.save(createActive(1));
		categoryRepository.save(createInactive());

		List<CategoryResult.Info> result = categoryService.getAllCategories(
				new CategoryCommand.GetList(UUID.randomUUID(), UserType.MASTER));

		log.info("==== 전체 카테고리 조회 (관리자) ====");
		result.forEach(
				c -> log.info("  [{}] {} sortOrder={}", c.isActive() ? "활성" : "비활성", c.getName(),
						c.getSortOrder()));

		assertThat(result).hasSize(3);
		assertThat(result.stream()
				.filter(CategoryResult.Info::isActive)).hasSize(2);
		assertThat(result.stream()
				.filter(c -> !c.isActive())).hasSize(1);
	}

	@Test
	void getAll_forbidden_if_not_master() {
		assertThatThrownBy(() -> categoryService.getAllCategories(
				new CategoryCommand.GetList(UUID.randomUUID(), UserType.INSTRUCTOR))).isInstanceOf(
				ForbiddenException.class);
	}

	// ── createCategory ────────────────────────────────────────────────────────

	@Test
	void create_savesInactive() {
		CategoryCommand.Create command = new CategoryCommand.Create("Java", "JAVA", 0,
				UUID.randomUUID(), UserType.MASTER);

		UUID categoryId = categoryService.createCategory(command);

		MentoringCategory saved = categoryRepository.findById(new MentoringCategoryId(categoryId))
				.orElseThrow();
		assertThat(saved.getName()).isEqualTo("Java");
		assertThat(saved.getCode()).isEqualTo("JAVA");
		assertThat(saved.isActive()).isFalse();
		assertThat(saved.getSortOrder()).isNull();
	}

	@Test
	void create_forbidden_if_not_master() {
		CategoryCommand.Create command = new CategoryCommand.Create("Java", "JAVA", 0,
				UUID.randomUUID(), UserType.INSTRUCTOR);

		assertThatThrownBy(() -> categoryService.createCategory(command))
				.isInstanceOf(ForbiddenException.class);
	}

	// ── updateActiveCategories ────────────────────────────────────────────────

	@Test
	void updateActive_success() {
		UUID adminId = UUID.randomUUID();
		MentoringCategory c0 = categoryRepository.save(createActive(0));
		MentoringCategory c1 = categoryRepository.save(createActive(1));
		MentoringCategory c2 = categoryRepository.save(createInactive());

		// c2를 0번, c0을 1번으로 재배치, c1은 비활성화
		categoryService.updateActiveCategories(new CategoryCommand.UpdateActive(adminId,
				UserType.MASTER, List.of(c2.getMentoringCategoryId().categoryId(),
						c0.getMentoringCategoryId().categoryId())));

		List<CategoryResult.Info> active = categoryService.getActiveCategories();
		log.info("==== 활성 카테고리 재배치 결과 ====");
		active.forEach(c -> log.info("  sortOrder={} id={}", c.getSortOrder(), c.getCategoryId()));

		assertThat(active).hasSize(2);
		assertThat(active.get(0).getCategoryId()).isEqualTo(c2.getMentoringCategoryId().categoryId());
		assertThat(active.get(0).getSortOrder()).isEqualTo(0);
		assertThat(active.get(1).getCategoryId()).isEqualTo(c0.getMentoringCategoryId().categoryId());
		assertThat(active.get(1).getSortOrder()).isEqualTo(1);
	}

	@Test
	void updateActive_empty_list() {
		UUID adminId = UUID.randomUUID();
		categoryRepository.save(createActive(0));
		categoryRepository.save(createActive(1));

		categoryService.updateActiveCategories(
				new CategoryCommand.UpdateActive(adminId, UserType.MASTER, List.of()));

		assertThat(categoryService.getActiveCategories()).isEmpty();
	}

	@Test
	void updateActive_not_found() {
		UUID adminId = UUID.randomUUID();

		assertThatThrownBy(() -> categoryService.updateActiveCategories(
				new CategoryCommand.UpdateActive(adminId, UserType.MASTER,
						List.of(UUID.randomUUID())))).isInstanceOf(CategoryNotFoundException.class);
	}

	@Test
	void updateActive_forbidden_if_not_master() {
		assertThatThrownBy(() -> categoryService.updateActiveCategories(
				new CategoryCommand.UpdateActive(UUID.randomUUID(), UserType.INSTRUCTOR,
						List.of()))).isInstanceOf(ForbiddenException.class);
	}

	// ── updateCategory ────────────────────────────────────────────────────────

	@Test
	void update_success() {
		MentoringCategory category = categoryRepository.save(createActive(0));
		MentoringCategoryId id = category.getMentoringCategoryId();

		categoryService.updateCategory(
				new CategoryCommand.Update(id, "NewName", "NEWCODE", UUID.randomUUID(), UserType.MASTER));

		assertThat(category.getName()).isEqualTo("NewName");
		assertThat(category.getCode()).isEqualTo("NEWCODE");
	}

	@Test
	void update_nameConflict() {
		MentoringCategory first = categoryRepository.save(createActive(0));
		MentoringCategory second = categoryRepository.save(createActive(1));

		assertThatThrownBy(() -> categoryService.updateCategory(
				new CategoryCommand.Update(first.getMentoringCategoryId(), second.getName(),
						first.getCode(), UUID.randomUUID(), UserType.MASTER)))
				.isInstanceOf(CategoryValidationException.class)
				.hasMessageContaining("이름");
	}

	@Test
	void update_codeConflict() {
		MentoringCategory first = categoryRepository.save(createActive(0));
		MentoringCategory second = categoryRepository.save(createActive(1));

		assertThatThrownBy(() -> categoryService.updateCategory(
				new CategoryCommand.Update(first.getMentoringCategoryId(), first.getName(),
						second.getCode(), UUID.randomUUID(), UserType.MASTER)))
				.isInstanceOf(CategoryValidationException.class)
				.hasMessageContaining("코드");
	}

	@Test
	void update_notFound() {
		MentoringCategoryId id = new MentoringCategoryId(UUID.randomUUID());

		assertThatThrownBy(() -> categoryService.updateCategory(
				new CategoryCommand.Update(id, "Name", "CODE", UUID.randomUUID(), UserType.MASTER)))
				.isInstanceOf(CategoryNotFoundException.class);
	}

	// ── deleteCategory ────────────────────────────────────────────────────────

	@Test
	void delete_success() {
		MentoringCategory category = categoryRepository.save(createActive(0));

		categoryService.deleteCategory(
				new CategoryCommand.Delete(UUID.randomUUID(), UserType.MASTER,
						category.getMentoringCategoryId()));

		assertThat(category.isActive()).isFalse();
		assertThat(category.getSortOrder()).isNull();
	}

	@Test
	void delete_notFound() {
		MentoringCategoryId id = new MentoringCategoryId(UUID.randomUUID());

		assertThatThrownBy(() -> categoryService.deleteCategory(
				new CategoryCommand.Delete(UUID.randomUUID(), UserType.MASTER, id)))
				.isInstanceOf(CategoryNotFoundException.class);
	}

	@Test
	void delete_forbidden_if_not_master() {
		MentoringCategory category = categoryRepository.save(createActive(0));

		assertThatThrownBy(() -> categoryService.deleteCategory(
				new CategoryCommand.Delete(UUID.randomUUID(), UserType.INSTRUCTOR,
						category.getMentoringCategoryId())))
				.isInstanceOf(ForbiddenException.class);
	}
}