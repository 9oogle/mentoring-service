package com.goggles.mentoring_service.domain.booking.event;

import com.goggles.mentoring_service.domain.booking.MentoringBooking;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record BookingAcceptedEvent(UUID bookingId, UUID menteeId, String menteeName, UUID mentorId,
								   String mentorName, String title) {
	public static BookingAcceptedEvent from(MentoringBooking mentoringBooking) {
		UUID bookingId = mentoringBooking.getMentoringBookingId()
				.bookingId();
		UUID menteeId = mentoringBooking.getMentee()
				.getId();
		String menteeName = mentoringBooking.getMentee()
				.getName();
		UUID mentorId = mentoringBooking.getBookedMentoring()
				.getMentorId();
		String mentorName = mentoringBooking.getBookedMentoring()
				.getMentorName();
		String title = mentoringBooking.getBookedMentoring()
				.getTitle();
		return new BookingAcceptedEvent(bookingId, menteeId, menteeName, mentorId, mentorName,
				title);
	}

	public record BookedSession(LocalDate sessionDate, LocalTime sessionStartTime,
								LocalTime sessionEndTime) {}
}
