package com.goggles.mentoring_service.domain.category.exception;

import com.goggles.common.exception.ForbiddenException;
import com.goggles.mentoring_service.domain._common.UserType;

import java.util.UUID;

public class CategoryAdminForbiddenException extends ForbiddenException {

	public CategoryAdminForbiddenException(UUID userId, UserType userType) {
		super(String.format("권한이 없는 사용자입니다. USER_TYPE: %s", userType));
	}
}