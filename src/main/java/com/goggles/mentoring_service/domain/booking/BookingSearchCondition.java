package com.goggles.mentoring_service.domain.booking;

import com.goggles.mentoring_service.domain._common.UserType;

import java.util.UUID;

public record BookingSearchCondition(UUID userId, UserType userType, BookingStatus status,
									 BookingSort sort) {}