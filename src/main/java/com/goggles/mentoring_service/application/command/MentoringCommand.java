package com.goggles.mentoring_service.application.command;

import com.goggles.mentoring_service.domain.category.MentoringCategory;
import com.goggles.mentoring_service.domain.common.UserType;
import com.goggles.mentoring_service.domain.mentoring.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class MentoringCommand {

	public record Create(UUID mentorId, String mentorName, String mentorField, String mentorEmail,
						 UserType mentorType, UUID categoryId, String title, String subtitle,
						 String description, MentoringDuration duration,
						 MentoringType mentoringType, Format format, int sessionCount,
						 int maxParticipants, boolean excludeHolidays, int price, LocalDate endDate,
						 List<SessionSlot> sessionSlots, List<TimeSchedules> timeSchedules) {
		public Mentoring toMentoring(MentoringCategory category) {
			List<RepeatPattern> repeatPatterns = timeSchedules().stream()
					.map(timeSchedules -> RepeatPattern.of(timeSchedules.dayOfWeek(),
							timeSchedules.startTime(), timeSchedules.endTime()))
					.toList();
			List<MentoringSession> sessions = sessionSlots().stream()
					.map(sessionSlot -> MentoringSession.of(sessionSlot.date(),
							sessionSlot.startTime(), sessionSlot.endTime()))
					.toList();
			return Mentoring.builder()
					.mentorId(mentorId())
					.mentorName(mentorName())
					.mentorField(mentorField())
					.mentorEmail(mentorEmail())
					.mentorType(mentorType())
					.categoryId(category.getMentoringCategoryId()
							.categoryId())
					.categoryName(category.getName())
					.categoryCode(category.getCode())
					.title(title())
					.subtitle(subtitle())
					.description(description())
					.duration(duration())
					.status(MentoringStatus.INACTIVE)
					.mentoringType(mentoringType())
					.format(format())
					.sessionCount(sessionCount())
					.maxParticipants(maxParticipants())
					.excludeHolidays(excludeHolidays())
					.price(price())
					.endDate(endDate())
					.repeatPatterns(repeatPatterns)
					.sessions(sessions)
					.build();
		}
	}


}
