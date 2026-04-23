package com.goggles.mentoring_service.domain.booking;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookingClosure {

	@JdbcTypeCode(SqlTypes.UUID)
	private UUID closedBy;
	private LocalDateTime closedAt;

	@Column(columnDefinition = "TEXT")
	private String closeReason;

	private BookingClosure(UUID closedBy, LocalDateTime closedAt, String closeReason) {
		this.closedBy = closedBy;
		this.closedAt = closedAt;
		this.closeReason = closeReason;
	}

	public static BookingClosure close(UUID userId, String reason) {
		return new BookingClosure(userId, LocalDateTime.now(), reason);
	}

}