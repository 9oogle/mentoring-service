package com.goggles.mentoring_service.application.service;

import com.goggles.common.exception.ForbiddenException;
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
	void getActiveCategories_returns_only_active_ordered_by_sortOrder() {
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
	void getActiveCategories_returns_empty_when_no_active_categories() {
		categoryRepository.save(createInactive());

		List<CategoryResult.Info> result = categoryService.getActiveCategories();

		log.info("활성 카테고리 없음, 결과: {}건", result.size());
		assertThat(result).isEmpty();
	}

	// ── getAllCategories ──────────────────────────────────────────────────────

	@Test
	void getAllCategories_returns_all_including_inactive() {
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
	void getAllCategories_throws_forbidden_when_not_master() {
		assertThatThrownBy(() -> categoryService.getAllCategories(
				new CategoryCommand.GetList(UUID.randomUUID(), UserType.INSTRUCTOR))).isInstanceOf(
				ForbiddenException.class);
	}

	// ── createCategory ────────────────────────────────────────────────────────

	@Test
	void createCategory_saves_inactive_category() {
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
	void createCategory_throws_forbidden_when_not_master() {
		CategoryCommand.Create command = new CategoryCommand.Create("Java", "JAVA", 0,
				UUID.randomUUID(), UserType.INSTRUCTOR);

		assertThatThrownBy(() -> categoryService.createCategory(command))
				.isInstanceOf(ForbiddenException.class);
	}
}