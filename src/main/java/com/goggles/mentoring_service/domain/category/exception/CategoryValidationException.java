package com.goggles.mentoring_service.domain.category.exception;

import com.goggles.mentoring_service.domain._common.MentoringValidationException;
import com.goggles.mentoring_service.domain.booking.exception.UnauthorizedBookingAccessException;

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

	public static CategoryValidationException noPermissionToActivate() {
		return new CategoryValidationException("카테고리를 활성화할 수 있는 권한이 없습니다.");
	}
}