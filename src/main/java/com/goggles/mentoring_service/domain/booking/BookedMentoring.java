package com.goggles.mentoring_service.domain.booking;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookedMentoring {

	@JdbcTypeCode(SqlTypes.UUID)
	@Column(name = "mentoring_id", nullable = false)
	private UUID mentoringId;

	private String categoryCode;
	private String categoryName;
	private String title;
	private String subtitle;
	private UUID mentorId;
	private String mentorName;
	private String mentorEmail;

	private LocalDate sessionDate;
	private LocalTime sessionStartTime;
	private LocalTime sessionEndTime;


	public static BookedMentoring of(UUID mentoringId, String categoryCode, String categoryName,
			String title, String subtitle, UUID mentorId, String mentorName, String mentorEmail,
			LocalDate sessionDate, LocalTime sessionStartTime, LocalTime sessionEndTime) {
		BookedMentoring bookedMentoring = new BookedMentoring();
		bookedMentoring.mentoringId = mentoringId;
		bookedMentoring.categoryCode = categoryCode;
		bookedMentoring.categoryName = categoryName;
		bookedMentoring.title = title;
		bookedMentoring.subtitle = subtitle;
		bookedMentoring.mentorId = mentorId;
		bookedMentoring.mentorName = mentorName;
		bookedMentoring.mentorEmail = mentorEmail;
		bookedMentoring.sessionDate = sessionDate;
		bookedMentoring.sessionStartTime = sessionStartTime;
		bookedMentoring.sessionEndTime = sessionEndTime;
		return bookedMentoring;
	}

	public boolean isMentor(UUID userId) {
		return this.mentorId.equals(userId);
	}
}