package com.goggles.mentoring_service.domain.category.exception;

import com.goggles.common.exception.NotFoundException;

import java.util.UUID;

public class CategoryNotFoundException extends NotFoundException {

	public CategoryNotFoundException(UUID categoryId) {
		super(String.format("카테고리를 찾을 수 없습니다. ID: %s", categoryId));
	}
}