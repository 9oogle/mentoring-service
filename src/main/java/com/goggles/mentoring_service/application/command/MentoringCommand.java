package com.goggles.mentoring_service.application.command;

import com.goggles.mentoring_service.domain.category.MentoringCategory;
import com.goggles.mentoring_service.domain.common.UserType;
import com.goggles.mentoring_service.domain.mentoring.BookingType;
import com.goggles.mentoring_service.domain.mentoring.Mentoring;
import com.goggles.mentoring_service.domain.mentoring.MentoringDuration;
import com.goggles.mentoring_service.domain.mentoring.MentoringStatus;
import com.goggles.mentoring_service.domain.mentoring.MentoringType;

import java.time.LocalDate;
import java.util.UUID;

public class MentoringCommand {

	public record Create(
			UUID mentorId,
			String mentorName,
			String mentorField,
			String mentorEmail,
			UserType mentorType,
			UUID categoryId,
			String title,
			String subtitle,
			String description,
			MentoringDuration duration,
			MentoringType mentoringType,
			BookingType bookingType,
			int sessionCount,
			int maxParticipants,
			boolean excludeHolidays,
			int price,
			LocalDate endDate
	) {
		public Mentoring toMentoring(MentoringCategory category) {
			return Mentoring.builder()
					.mentorId(mentorId())
					.mentorName(mentorName())
					.mentorField(mentorField())
					.mentorEmail(mentorEmail())
					.mentorType(mentorType())
					.categoryId(category.getMentoringCategoryId().categoryId())
					.categoryName(category.getName())
					.categoryCode(category.getCode())
					.title(title())
					.subtitle(subtitle())
					.description(description())
					.duration(duration())
					.status(MentoringStatus.INACTIVE)
					.mentoringType(mentoringType())
					.bookingType(bookingType())
					.sessionCount(sessionCount())
					.maxParticipants(maxParticipants())
					.excludeHolidays(excludeHolidays())
					.price(price())
					.endDate(endDate())
					.build();
		}
	}
}
