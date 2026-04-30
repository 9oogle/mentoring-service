package com.goggles.mentoring_service.domain.booking;

import com.goggles.mentoring_service.domain.mentoring.Mentoring;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

	@JdbcTypeCode(SqlTypes.UUID)
	@Column(name = "mentor_id", nullable = false)
	private UUID mentorId;
	private String mentorName;

	static BookedMentoring of(Mentoring mentoring) {
		BookedMentoring bookedMentoring = new BookedMentoring();
		bookedMentoring.mentoringId = mentoring.getMentoringId()
				.mentoringId();
		bookedMentoring.categoryCode = mentoring.getMentoringCategory()
				.getCode();
		bookedMentoring.categoryName = mentoring.getMentoringCategory()
				.getName();
		bookedMentoring.title = mentoring.getTitle();
		bookedMentoring.subtitle = mentoring.getSubtitle();
		bookedMentoring.mentorId = mentoring.getMentor()
				.getId();
		bookedMentoring.mentorName = mentoring.getMentor()
				.getName();
		return bookedMentoring;
	}

	static BookedMentoring of(UUID mentoringId, String categoryCode, String categoryName,
			String title, String subtitle, UUID mentorId, String mentorName) {
		BookedMentoring bookedMentoring = new BookedMentoring();
		bookedMentoring.mentoringId = mentoringId;
		bookedMentoring.categoryCode = categoryCode;
		bookedMentoring.categoryName = categoryName;
		bookedMentoring.title = title;
		bookedMentoring.subtitle = subtitle;
		bookedMentoring.mentorId = mentorId;
		bookedMentoring.mentorName = mentorName;
		return bookedMentoring;
	}

	public boolean isMentor(UUID userId) {
		return this.mentorId.equals(userId);
	}
}