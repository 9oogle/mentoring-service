package com.goggles.mentoring_service.domain.category;

import com.goggles.common.exception.ForbiddenException;
import com.goggles.mentoring_service.domain._common.UserType;
import com.goggles.mentoring_service.domain.category.exception.CategoryValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MentoringCategoryTest {

	private UUID adminId;

	@BeforeEach
	void setUp() {
		adminId = UUID.randomUUID();
	}

	// ── create ───────────────────────────────────────────────────────────────

	@Test
	void create_success() {
		MentoringCategory category =
				MentoringCategory.create(adminId, UserType.MASTER, "Java", "JAVA");

		assertThat(category.getMentoringCategoryId()).isNotNull();
		assertThat(category.getName()).isEqualTo("Java");
		assertThat(category.getCode()).isEqualTo("JAVA");
		assertThat(category.isActive()).isFalse();
		assertThat(category.getSortOrder()).isNull();
	}

	@Test
	void create_forbidden_if_not_master() {
		assertThatThrownBy(
				() -> MentoringCategory.create(UUID.randomUUID(), UserType.INSTRUCTOR, "Java",
						"JAVA")).isInstanceOf(ForbiddenException.class);
	}

	// ── activate / deactivate ────────────────────────────────────────────────

	@Test
	void activate_success() {
		MentoringCategory category =
				MentoringCategory.create(adminId, UserType.MASTER, "Java", "JAVA");

		category.activate(adminId, UserType.MASTER, 2);

		assertThat(category.isActive()).isTrue();
		assertThat(category.getSortOrder()).isEqualTo(2);
	}

	@Test
	void deactivate_success() {
		MentoringCategory category =
				MentoringCategory.create(adminId, UserType.MASTER, "Java", "JAVA");
		category.activate(adminId, UserType.MASTER, 2);

		category.deactivate(adminId, UserType.MASTER);

		assertThat(category.isActive()).isFalse();
		assertThat(category.getSortOrder()).isNull();
	}

	// ── move ─────────────────────────────────────────────────────────────────

	@Test
	void move_success() {
		MentoringCategory category =
				MentoringCategory.create(adminId, UserType.MASTER, "Java", "JAVA");
		category.activate(adminId, UserType.MASTER, 0);

		category.move(adminId, UserType.MASTER, 3);

		assertThat(category.getSortOrder()).isEqualTo(3);
	}

	@Test
	void move_throws_if_inactive() {
		MentoringCategory category =
				MentoringCategory.create(adminId, UserType.MASTER, "Java", "JAVA");

		assertThatThrownBy(() -> category.move(adminId, UserType.MASTER, 1)).isInstanceOf(
				CategoryValidationException.class);
	}

	// ── updateNameAndCode ────────────────────────────────────────────────────

	@Test
	void updateNameAndCode_success() {
		MentoringCategory category =
				MentoringCategory.create(adminId, UserType.MASTER, "Java", "JAVA");

		category.updateNameAndCode(adminId, UserType.MASTER, "Python", "PY");

		assertThat(category.getName()).isEqualTo("Python");
		assertThat(category.getCode()).isEqualTo("PY");
	}

	@Test
	void updateNameAndCode_forbidden_if_not_master() {
		MentoringCategory category =
				MentoringCategory.create(adminId, UserType.MASTER, "Java", "JAVA");

		assertThatThrownBy(
				() -> category.updateNameAndCode(UUID.randomUUID(), UserType.INSTRUCTOR, "Python",
						"PY")).isInstanceOf(ForbiddenException.class);
	}

	// ── softDelete ───────────────────────────────────────────────────────────

	@Test
	void softDelete_success() {
		MentoringCategory category =
				MentoringCategory.create(adminId, UserType.MASTER, "Java", "JAVA");
		category.activate(adminId, UserType.MASTER, 1);

		category.softDelete(adminId, UserType.MASTER);

		assertThat(category.isActive()).isFalse();
		assertThat(category.getSortOrder()).isNull();
	}

	@Test
	void softDelete_forbidden_if_not_master() {
		MentoringCategory category =
				MentoringCategory.create(adminId, UserType.MASTER, "Java", "JAVA");

		assertThatThrownBy(
				() -> category.softDelete(UUID.randomUUID(), UserType.INSTRUCTOR)).isInstanceOf(
				ForbiddenException.class);
	}
}