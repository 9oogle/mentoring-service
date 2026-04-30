package com.goggles.mentoring_service.infrastructure.event;

import com.goggles.common.event.Events;
import com.goggles.mentoring_service.domain.booking.MentoringBooking;
import com.goggles.mentoring_service.domain.booking.event.BookingAcceptedEvent;
import com.goggles.mentoring_service.domain.booking.event.BookingEvent;
import com.goggles.mentoring_service.domain.booking.event.PaymentCompletedEvent;
import com.goggles.mentoring_service.domain.booking.event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BookingEventImpl implements BookingEvent {
	private final Events events;


	@Override
	public void bookingPaymentCompleted(MentoringBooking mentoringBooking) {
		UUID bookingId = mentoringBooking.getMentoringBookingId()
				.bookingId();
		events.trigger(bookingId.toString(), "MENTORING_BOOKING",
				"mentoring.booking.payment_completed", new PaymentCompletedEvent(bookingId));
	}

	@Override
	public void bookingPaymentFailed(MentoringBooking mentoringBooking) {
		UUID bookingId = mentoringBooking.getMentoringBookingId()
				.bookingId();
		events.trigger(bookingId.toString(), "MENTORING_BOOKING",
				"mentoring.booking.payment_failed", new PaymentFailedEvent(bookingId));

	}

	@Override
	public void mentoringBookingAccepted(MentoringBooking mentoringBooking) {
		UUID bookingId = mentoringBooking.getMentoringBookingId()
				.bookingId();
		events.trigger(bookingId.toString(), "MENTORING_BOOKING", "mentoring.booking.accepted",
				BookingAcceptedEvent.from(mentoringBooking));
	}

	@Override
	public void mentoringBookingRejected(MentoringBooking mentoringBooking) {

	}

	@Override
	public void mentoringBookingCanceled(MentoringBooking mentoringBooking) {

	}
}
