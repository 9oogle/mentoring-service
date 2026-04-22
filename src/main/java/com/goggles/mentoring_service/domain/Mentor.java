package com.goggles.mentoring_service.domain;


import com.goggles.mentoring_service.domain.exception.InvalidMentorUserTypeException;
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

	@Column(name = "mentor_name",length = 100, nullable = false)
	private String name;

	@Column(name= "mentor_field", length = 100, nullable = false)
	private String field;

	@Builder
	protected Mentor(UUID id, String name, String field, UserType userType) {
		checkIfMentorAuthValidate(id, userType);
		this.id = id;
		this.name = name;
		this.field = field;
	}

	private static void checkIfMentorAuthValidate(UUID id, UserType userType) {
		if (userType != ALLOWED_USER_TYPE) {
			throw new InvalidMentorUserTypeException(id, userType);
		}
	}

}
