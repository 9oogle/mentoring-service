package com.goggles.mentoring_service.domain.booking.event;

import java.time.LocalDate;
import java.util.UUID;

public record BookingCanceledEvent(UUID bookingId, UUID canceledBy, UUID menteeId, UUID mentorId,
								   String title, String reason, LocalDate sessionDate,
								   UUID orderId) {}