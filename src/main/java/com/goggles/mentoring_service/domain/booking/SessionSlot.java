package com.goggles.mentoring_service.domain.booking;

import java.time.LocalDate;
import java.time.LocalTime;

public record SessionSlot(LocalDate date, LocalTime startTime, LocalTime endTime) {}
