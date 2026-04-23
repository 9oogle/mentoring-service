package com.goggles.mentoring_service.domain.mentoring;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record MentoringId(
		@JdbcTypeCode(SqlTypes.UUID) @Column(length = 36, name = "mentoring_id") UUID mentoringId) implements Serializable {

	public static MentoringId of() {
		return new MentoringId(UUID.randomUUID());
	}

	public static MentoringId of(String mentoringIdString) {
		return new MentoringId(UUID.fromString(mentoringIdString));
	}
}
