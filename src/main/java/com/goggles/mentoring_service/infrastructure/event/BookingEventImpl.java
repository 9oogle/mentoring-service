package com.goggles.mentoring_service.infrastructure.event;

import com.goggles.common.event.Events;
import com.goggles.mentoring_service.domain.booking.MentoringBooking;
import com.goggles.mentoring_service.domain.booking.event.BookingAcceptedEvent;
import com.goggles.mentoring_service.domain.booking.event.BookingCanceledEvent;
import com.goggles.mentoring_service.domain.booking.event.BookingEvent;
import com.goggles.mentoring_service.domain.booking.event.BookingRejectedEvent;
import com.goggles.mentoring_service.domain.booking.event.BookingRequestedEvent;
import com.goggles.mentoring_service.infrastructure.config.KafkaTopicProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BookingEventImpl implements BookingEvent {
	private final Events events;
	private final KafkaTopicProperties kafkaTopicProperties;

	@Override
	public void mentoringBookingAccepted(MentoringBooking mentoringBooking) {
		UUID bookingId = mentoringBooking.getMentoringBookingId()
				.bookingId();
		events.trigger(bookingId.toString(), "MENTORING_BOOKING",
				kafkaTopicProperties.booking().accepted(), BookingAcceptedEvent.from(mentoringBooking));
	}

	@Override
	public void mentoringBookingRejected(MentoringBooking mentoringBooking) {
		UUID bookingId = mentoringBooking.getMentoringBookingId().bookingId();
		events.trigger(bookingId.toString(), "MENTORING_BOOKING",
				kafkaTopicProperties.booking().rejected(), BookingRejectedEvent.from(mentoringBooking));
	}

	@Override
	public void mentoringBookingCanceled(MentoringBooking mentoringBooking) {
		UUID bookingId = mentoringBooking.getMentoringBookingId().bookingId();
		events.trigger(bookingId.toString(), "MENTORING_BOOKING",
				kafkaTopicProperties.booking().canceled(), BookingCanceledEvent.from(mentoringBooking));
	}

	@Override
	public void bookingRequested(MentoringBooking mentoringBooking) {
		UUID bookingId = mentoringBooking.getMentoringBookingId().bookingId();
		events.trigger(bookingId.toString(), "MENTORING_BOOKING",
				kafkaTopicProperties.booking().requested(), BookingRequestedEvent.from(mentoringBooking));
	}
}
