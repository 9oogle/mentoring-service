package com.goggles.mentoring_service.domain.booking.repository;

import com.goggles.mentoring_service.domain.booking.BookingSearchCondition;
import com.goggles.mentoring_service.domain.booking.MentoringBooking;
import com.goggles.mentoring_service.domain.booking.MentoringBookingId;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MentoringBookingRepository {

	MentoringBooking save(MentoringBooking mentoringBooking);

  Optional<MentoringBooking> findById(MentoringBookingId id);

  Page<MentoringBooking> findByUser(BookingSearchCondition condition, Pageable pageable);
}
