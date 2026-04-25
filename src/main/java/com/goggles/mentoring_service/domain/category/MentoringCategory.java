package com.goggles.mentoring_service.domain.category;

import com.goggles.common.domain.BaseAudit;
import com.goggles.mentoring_service.domain.category.exception.CategoryAdminForbiddenException;
import com.goggles.mentoring_service.domain.category.exception.InactiveCategoryCannotMoveException;
import com.goggles.mentoring_service.domain.common.UserType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

/**
 * Partial Unique Indexes for Active Categories:
 * <p>
 * CREATE UNIQUE INDEX uq_mentoring_category_code_active
 * ON p_mentoring_category (code)
 * WHERE deleted_at IS NULL;
 * <p>
 * CREATE UNIQUE INDEX uq_mentoring_category_name_active
 * ON p_mentoring_category (name)
 * WHERE deleted_at IS NULL;
 * <p>
 * CREATE UNIQUE INDEX uq_mentoring_category_sort_active
 * ON p_mentoring_category (sort_order)
 * WHERE is_active = true;
 */

@Getter
@Entity
@ToString
@Table(name = "P_MENTORING_CATEGORY")
@Access(AccessType.FIELD)
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MentoringCategory extends BaseAudit {

	@EmbeddedId
	private MentoringCategoryId mentoringCategoryId;

	@Column(length = 50, nullable = false)
	private String name;

	@Column(length = 10, nullable = false)
	private String code;

	// 0부터 시작하는 정수로, 활성화된 카테고리들 사이에서의 순서를 나타냄. 비활성 시 null
	private Integer sortOrder;

	private boolean active;

	private MentoringCategory(String name, String code) {
		this.mentoringCategoryId = MentoringCategoryId.of();
		this.name = name;
		this.code = code;
		this.sortOrder = null;
		this.active = false;
	}

	public static MentoringCategory create(UUID userId, UserType type, String name, String code) {
		checkIfUserTypeIsAdmin(userId, type);
		return new MentoringCategory(name, code);
	}

	private static void checkIfUserTypeIsAdmin(UUID userId, UserType type) {
		if (type != UserType.MASTER) {
			throw new CategoryAdminForbiddenException(userId, type);
		}
	}

	public void softDelete(UUID userId, UserType type) {
		checkIfUserTypeIsAdmin(userId, type);
		this.softDelete(userId);
		this.active = false;
		this.sortOrder = null;
	}

	public void updateNameAndCode(UUID userId, UserType type, String name, String code) {
		checkIfUserTypeIsAdmin(userId, type);
		this.name = name;
		this.code = code;
	}

	public void activate(UUID userId, UserType type, int sortOrder) {
		checkIfUserTypeIsAdmin(userId, type);
		this.sortOrder = sortOrder;
		this.active = true;
	}

	public void deactivate(UUID userId, UserType type) {
		checkIfUserTypeIsAdmin(userId, type);
		this.active = false;
		this.sortOrder = null;
	}

	public void move(UUID userId, UserType type, int newSortOrder) {
		checkIfUserTypeIsAdmin(userId, type);
		checkActivation();
		this.sortOrder = newSortOrder;
	}

	private void checkActivation() {
		if (!this.active) {
			throw new InactiveCategoryCannotMoveException();
		}
	}
}
