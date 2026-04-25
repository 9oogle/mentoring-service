package com.goggles.mentoring_service.domain.booking.event;

import java.util.UUID;

public record PaymentFailedEvent(
		UUID bookingId
) {
}