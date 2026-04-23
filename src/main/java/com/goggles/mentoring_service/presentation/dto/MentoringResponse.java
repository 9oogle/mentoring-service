package com.goggles.mentoring_service.presentation.dto;

import java.util.UUID;

public class MentoringResponse {

	public record Create(UUID mentoringId) {}

	public record Summary(UUID mentoringId, String title, String subtitle, String mentorName,
						  String categoryName, MentoringType mentoringType, Format format,
						  int price, MentoringStatus status) {

		public static Summary of(MentoringResult.Summary summary) {
			return new Summary(summary.mentoringId(), summary.title(), summary.subtitle(),
					summary.mentorName(), summary.categoryName(), summary.mentoringType(),
					summary.format(), summary.price(), summary.status());
		}
	}

}