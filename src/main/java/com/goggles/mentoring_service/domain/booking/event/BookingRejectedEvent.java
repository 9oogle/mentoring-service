package com.goggles.mentoring_service.domain.booking.event;

import java.util.List;
import java.util.UUID;

public record BookingRejectedEvent(UUID bookingId, UUID menteeId, UUID mentorId, String mentorName,
								   String title, String reason,
								   List<BookingSessionSnapshot> sessionSlots, UUID orderId) {}