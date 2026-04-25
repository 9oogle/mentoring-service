package com.goggles.mentoring_service.application.query;

import com.goggles.mentoring_service.domain._common.UserType;
import com.goggles.mentoring_service.domain.booking.BookingStatus;
import java.util.UUID;

public record BookingSearchCondition(
    UUID userId, UserType userType, BookingStatus status, BookingSort sort) {}
