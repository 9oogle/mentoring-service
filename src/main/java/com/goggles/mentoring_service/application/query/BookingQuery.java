package com.goggles.mentoring_service.application.query;

import com.goggles.mentoring_service.domain._common.UserType;
import com.goggles.mentoring_service.domain.booking.BookingSort;
import com.goggles.mentoring_service.domain.booking.BookingStatus;

import java.util.UUID;

public class BookingQuery {
	public record GetMyBookings(UUID userId, UserType userType, BookingStatus status,
								BookingSort sort) {}
}
