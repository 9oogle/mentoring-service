package com.goggles.mentoring_service.presentation.dto;

import com.goggles.mentoring_service.application.result.CategoryResult;

import java.util.List;
import java.util.UUID;

public class CategoryResponse {

	public record CategoryList(List<ListItem> categories) {
		public static CategoryList of(List<CategoryResult.Info> categoryList) {
			List<ListItem> items = categoryList.stream()
					.map(ListItem::of)
					.toList();
			return new CategoryList(items);
		}

	}

	public record ListItem(UUID categoryId, String name, String code, Integer sortOrder) {
		public static ListItem of(CategoryResult.Info info) {
			return new ListItem(info.getCategoryId(), info.getName(), info.getCode(),
					info.getSortOrder());
		}

	}

	public record CategoryListForAdmin(List<ListItemForAdmin> categories) {
		public static CategoryListForAdmin of(List<CategoryResult.Info> categoryList) {
			List<ListItemForAdmin> items = categoryList.stream()
					.map(ListItemForAdmin::of)
					.toList();
			return new CategoryListForAdmin(items);
		}

	}

	public record ListItemForAdmin(UUID categoryId, String name, String code, Integer sortOrder,
								   boolean active) {
		public static ListItemForAdmin of(CategoryResult.Info info) {
			return new ListItemForAdmin(info.getCategoryId(), info.getName(), info.getCode(),
					info.getSortOrder(), info.isActive());
		}

	}
}
