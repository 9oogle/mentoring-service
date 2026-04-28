package com.goggles.mentoring_service.application.command;

import com.goggles.mentoring_service.domain._common.UserType;

import java.util.UUID;

public class CategoryCommand {

	public record GetList(UUID userId, UserType userType) {}
	public record Create(String title, String code, int sortOrder, UUID creatorId,
						 UserType creatorType) {}


}
