package com.goggles.mentoring_service.domain.booking.repository;

import com.goggles.mentoring_service.domain.booking.MentoringBooking;
import com.goggles.mentoring_service.domain.booking.MentoringBookingId;

import java.util.Optional;

public interface MentoringBookingRepository {

	void save(MentoringBooking mentoringBooking);

	Optional<MentoringBooking> findById(MentoringBookingId id);
}
