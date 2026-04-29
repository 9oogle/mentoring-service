package com.goggles.mentoring_service.domain.category.exception;

import com.goggles.mentoring_service.domain._common.MentoringValidationException;

public class CategoryValidationException extends MentoringValidationException {

	public CategoryValidationException(String message) {
		super(message);
	}


	public static CategoryValidationException alreadyExistsName() {
		return new CategoryValidationException("이미 존재하는 카테고리 이름입니다.");
	}

	public static CategoryValidationException alreadyExistsCode() {
		return new CategoryValidationException("이미 존재하는 카테고리 코드입니다.");
	}

	public static CategoryValidationException cannotMoveInactiveCategory() {
		return new CategoryValidationException("비활성화된 카테고리는 이동할 수 없습니다.");
	}

	public static CategoryValidationException duplicateCategoryIds() {
		return new CategoryValidationException("중복된 카테고리 ID가 포함되어 있습니다.");
	}
}