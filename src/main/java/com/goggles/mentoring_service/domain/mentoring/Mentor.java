package com.goggles.mentoring_service.domain.mentoring;


import com.goggles.mentoring_service.domain.common.UserType;
import com.goggles.mentoring_service.domain.mentoring.exception.InvalidMentorUserTypeException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Embeddable
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Mentor {

	private static final UserType ALLOWED_USER_TYPE = UserType.INSTRUCTOR;

	@JdbcTypeCode(SqlTypes.UUID)
	@Column(name = "mentor_id", nullable = false)
	private UUID id;

	@Column(name = "mentor_name", length = 100, nullable = false)
	private String name;

	@Column(name = "mentor_field", length = 100, nullable = false)
	private String field;

	@Column(name = "mentor_email", length = 100, nullable = false)
	private String email;

	@Builder
	protected Mentor(UUID id, String name, String field, String email, UserType userType) {
		checkIfMentorAuthValidate(id, userType);
		this.id = id;
		this.name = name;
		this.field = field;
		this.email = email;
	}

	private static void checkIfMentorAuthValidate(UUID id, UserType userType) {
		if (userType != ALLOWED_USER_TYPE) {
			throw new InvalidMentorUserTypeException(id, userType);
		}
	}

}
