package com.goggles.mentoring_service.domain.booking.event;

import com.goggles.mentoring_service.domain.booking.MentoringBooking;

import java.util.UUID;

public record BookingRequestedEvent(UUID bookingId, UUID mentorId, String menteeName, String title) {

	public static BookingRequestedEvent from(MentoringBooking booking) {
		return new BookingRequestedEvent(
				booking.getMentoringBookingId().bookingId(),
				booking.getBookedMentoring().getMentorId(),
				booking.getMentee().getName(),
				booking.getBookedMentoring().getTitle()
		);
	}
}