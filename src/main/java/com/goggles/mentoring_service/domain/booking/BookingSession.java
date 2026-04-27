package com.goggles.mentoring_service.domain.booking;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "P_BOOKING_SESSION")
@Access(AccessType.FIELD)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookingSession {

	@Id
	@Column(columnDefinition = "uuid")
	private UUID id;

	private LocalDate sessionDate;
	private LocalTime sessionStartTime;
	private LocalTime sessionEndTime;

	@Enumerated(EnumType.STRING)
	@Column(length = 20, nullable = false)
	private SessionProgressStatus progressStatus = SessionProgressStatus.SCHEDULED;

	public static BookingSession of(SessionSlot slot) {
		BookingSession bookingSession = new BookingSession();
		bookingSession.id = UUID.randomUUID();
		bookingSession.sessionDate = slot.date();
		bookingSession.sessionStartTime = slot.startTime();
		bookingSession.sessionEndTime = slot.endTime();
		return bookingSession;
	}

	void complete() {
		this.progressStatus = SessionProgressStatus.COMPLETED;
	}

}
