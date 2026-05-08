package com.goggles.mentoring_service.domain.booking.event;

import com.goggles.mentoring_service.domain.booking.MentoringBooking;

import java.util.List;
import java.util.UUID;

public record BookingRejectedEvent(UUID bookingId, UUID menteeId, UUID mentorId, String mentorName,
								   String title, String reason,
								   List<BookingSessionSnapshot> sessionSlots, UUID orderId) {

	public static BookingRejectedEvent from(MentoringBooking booking) {
		return new BookingRejectedEvent(
				booking.getMentoringBookingId().bookingId(),
				booking.getMentee().getId(),
				booking.getBookedMentoring().getMentorId(),
				booking.getBookedMentoring().getMentorName(),
				booking.getBookedMentoring().getTitle(),
				booking.getCloseReason(),
				booking.getBookingSessions().stream().map(BookingSessionSnapshot::from).toList(),
				booking.getOrderId()
		);
	}
}