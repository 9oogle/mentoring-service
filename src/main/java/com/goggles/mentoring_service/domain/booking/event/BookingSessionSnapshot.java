package com.goggles.mentoring_service.domain.booking.event;

import com.goggles.mentoring_service.domain.booking.BookingSession;
import com.goggles.mentoring_service.domain.booking.SessionProgressStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record BookingSessionSnapshot(UUID sessionId, LocalDate date, LocalTime startTime,
									  LocalTime endTime, SessionProgressStatus progressStatus) {

	public static BookingSessionSnapshot from(BookingSession session) {
		return new BookingSessionSnapshot(session.getId(), session.getSessionDate(),
				session.getSessionStartTime(), session.getSessionEndTime(),
				session.getProgressStatus());
	}
}
