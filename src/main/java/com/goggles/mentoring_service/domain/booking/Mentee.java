package com.goggles.mentoring_service.domain.booking;

import com.goggles.mentoring_service.domain.booking.exception.InvalidMenteeUserTypeException;
import com.goggles.mentoring_service.domain.common.UserType;
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
public class Mentee {
	private static final UserType ALLOWED_USER_TYPE = UserType.STUDENT;

	@JdbcTypeCode(SqlTypes.UUID)
	@Column(name = "student_id", nullable = false)
	private UUID id;

	@Column(name = "student_name", length = 100, nullable = false)
	private String name;

	static Mentee of(UUID id, UserType type, String name) {
		validateUserType(id, type);
		Mentee mentee = new Mentee();
		mentee.id = id;
		mentee.name = name;
		return mentee;
	}

	private static void validateUserType(UUID id, UserType userType) {
		if (userType != ALLOWED_USER_TYPE) {
			throw new InvalidMenteeUserTypeException(id, userType);
		}
	}

	public boolean isMentee(UUID userId) {
		return this.id.equals(userId);
	}
}