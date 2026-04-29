package com.goggles.mentoring_service.domain.booking.event;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record BookingRequestedEvent(UUID bookingId, UUID mentoringId, UUID menteeId, UUID mentorId,
									String title, LocalDate sessionDate, LocalTime sessionStartTime,
									LocalTime sessionEndTime) {}