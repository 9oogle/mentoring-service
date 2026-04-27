package com.goggles.mentoring_service.domain.booking.event;

import java.util.List;
import java.util.UUID;

public record BookingAcceptedEvent(UUID bookingId, UUID menteeId, UUID mentorId, String mentorName,
								   String title,
								   List<com.goggles.mentoring_service.domain.booking.BookingSession> sessionSlots) {}