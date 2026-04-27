package com.goggles.mentoring_service.application.command;

import com.goggles.mentoring_service.domain._common.UserType;
import com.goggles.mentoring_service.presentation.support.UserContext;

import java.util.UUID;

public class CategoryCommand {

	public record GetList(UUID userId, UserType userType) {
		public static GetList toRequest(UserContext userContext) {
			return new GetList(userContext.userId(), userContext.userType());
		}
	}
}
