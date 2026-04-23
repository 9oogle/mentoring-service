package com.goggles.mentoring_service.domain.booking.event;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

// TODO: 주문 서비스와 페이로드 협의 후 확정
public record BookingRequestedEvent(UUID bookingId, UUID mentoringId, UUID menteeId, UUID mentorId,
									String title, LocalDate sessionDate, LocalTime sessionStartTime,
									LocalTime sessionEndTime) {}