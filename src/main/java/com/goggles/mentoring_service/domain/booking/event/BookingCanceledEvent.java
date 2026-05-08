package com.goggles.mentoring_service.domain.booking.event;

import com.goggles.mentoring_service.domain.booking.MentoringBooking;

import java.util.List;
import java.util.UUID;

public record BookingCanceledEvent(UUID bookingId, UUID canceledBy, UUID menteeId, UUID mentorId,
								   String title, String reason,
								   List<BookingSessionSnapshot> sessionSlots, UUID orderId) {

	public static BookingCanceledEvent from(MentoringBooking booking) {
		return new BookingCanceledEvent(
				booking.getMentoringBookingId().bookingId(),
				booking.getClosedBy(),
				booking.getMentee().getId(),
				booking.getBookedMentoring().getMentorId(),
				booking.getBookedMentoring().getTitle(),
				booking.getCloseReason(),
				booking.getBookingSessions().stream().map(BookingSessionSnapshot::from).toList(),
				booking.getOrderId()
		);
	}
}