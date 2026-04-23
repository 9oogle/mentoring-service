package com.goggles.mentoring_service.presentation.support;

import com.goggles.mentoring_service.domain.common.UserType;

import java.util.UUID;

public record UserContext(
		UUID userId,
		UserType userType,
		String userName,
		String userEmail,
		String userField
) {
}
