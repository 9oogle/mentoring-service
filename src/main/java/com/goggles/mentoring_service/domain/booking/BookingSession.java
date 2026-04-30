package com.goggles.mentoring_service.domain.booking;

import com.goggles.mentoring_service.domain.booking.exception.InvalidRescheduleException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

	void reschedule(LocalDate newDate, LocalTime newStartTime, LocalTime newEndTime,
			LocalDateTime now) {
		if (progressStatus == SessionProgressStatus.COMPLETED) {
			throw InvalidRescheduleException.sessionAlreadyCompleted();
		}
		if (!LocalDateTime.of(sessionDate, sessionStartTime)
				.isAfter(now)) {
			throw InvalidRescheduleException.sessionAlreadyPassed();
		}
		if (!LocalDateTime.of(newDate, newStartTime)
				.isAfter(now)) {
			throw InvalidRescheduleException.newTimeIsInPast();
		}
		this.sessionDate = newDate;
		this.sessionStartTime = newStartTime;
		this.sessionEndTime = newEndTime;
	}
}
