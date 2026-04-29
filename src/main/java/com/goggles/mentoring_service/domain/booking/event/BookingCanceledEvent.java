package com.goggles.mentoring_service.domain.booking.event;

import com.goggles.mentoring_service.domain.booking.BookedTime;

import java.util.List;
import java.util.UUID;

public record BookingCanceledEvent(UUID bookingId, UUID canceledBy, UUID menteeId, UUID mentorId,
								   String title, String reason, List<BookedTime> sessionSlots,
								   UUID orderId) {}