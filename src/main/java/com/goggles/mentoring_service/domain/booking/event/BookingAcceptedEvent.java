package com.goggles.mentoring_service.domain.booking.event;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record BookingAcceptedEvent(
		UUID bookingId,
		UUID menteeId,
		String mentorName,
		String title,
		LocalDate sessionDate,
		LocalTime sessionStartTime,
		LocalTime sessionEndTime
) {
}