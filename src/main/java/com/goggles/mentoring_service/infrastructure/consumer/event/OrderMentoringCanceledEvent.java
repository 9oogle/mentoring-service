package com.goggles.mentoring_service.infrastructure.consumer.event;

import java.util.UUID;

public record OrderMentoringCanceledEvent(UUID orderId, UUID userId, UUID enrollmentId) {}
