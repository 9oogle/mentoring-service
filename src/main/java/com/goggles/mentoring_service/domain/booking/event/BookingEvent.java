package com.goggles.mentoring_service.domain.booking.event;

import com.goggles.mentoring_service.domain.booking.MentoringBooking;

public interface BookingEvent {
	void bookingPaymentCompleted(MentoringBooking mentoringBooking);

	void bookingPaymentFailed(MentoringBooking mentoringBooking);

	void mentoringBookingAccepted(MentoringBooking mentoringBooking);

	void mentoringBookingRejected(MentoringBooking mentoringBooking);

	void mentoringBookingCanceled(MentoringBooking mentoringBooking);
}
