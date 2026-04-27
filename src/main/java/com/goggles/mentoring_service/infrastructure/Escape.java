package com.goggles.mentoring_service.infrastructure;

public class Escape {

	private Escape() {}

	public static String contains(String keyword) {
		String escaped = keyword.toLowerCase()
				.replace("\\", "\\\\")
				.replace("%", "\\%")
				.replace("_", "\\_");
		return "%" + escaped + "%";
	}
}
