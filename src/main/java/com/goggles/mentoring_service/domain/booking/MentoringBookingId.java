package com.goggles.mentoring_service.domain.booking;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record

MentoringBookingId(
		@JdbcTypeCode(SqlTypes.UUID) @Column(length = 36, name = "id") UUID bookingId) implements Serializable {

	public static MentoringBookingId of() {
		return new MentoringBookingId(UUID.randomUUID());
	}

	public static MentoringBookingId of(String id) {
		return new MentoringBookingId(UUID.fromString(id));
	}
}