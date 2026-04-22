package com.goggles.mentoring_service.domain.exception;

import com.goggles.common.exception.ForbiddenException;
import com.goggles.mentoring_service.domain.UserType;

import java.util.UUID;

public class InvalidMentoringCategoryAdminUserTypeException extends ForbiddenException {
	public InvalidMentoringCategoryAdminUserTypeException(UUID userId, UserType userType) {
		super(String.format("권한이 없는 사용자입니다. USER_TYPE: %s", String.valueOf(userType)));
	}
}
