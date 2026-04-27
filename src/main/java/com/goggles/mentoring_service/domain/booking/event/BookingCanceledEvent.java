package com.goggles.mentoring_service.domain.booking.event;

import java.util.List;
import java.util.UUID;

public record BookingCanceledEvent(UUID bookingId, UUID canceledBy, UUID menteeId, UUID mentorId,
								   String title, String reason,
								   List<com.goggles.mentoring_service.domain.booking.BookingSession> sessionSlots,
								   UUID orderId) {}