package com.goggles.mentoring_service.domain.booking.exception;

import com.goggles.common.exception.NotFoundException;
import com.goggles.mentoring_service.domain.booking.MentoringBookingId;

public class BookingNotFoundException extends NotFoundException {

	public BookingNotFoundException(MentoringBookingId bookingId) {
		super("해당 예약을 찾을 수 없습니다. ID: " + bookingId.bookingId());
	}
}