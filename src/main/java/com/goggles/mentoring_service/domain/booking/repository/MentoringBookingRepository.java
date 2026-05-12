package com.goggles.mentoring_service.domain.booking.repository;

import com.goggles.mentoring_service.domain.booking.BookingSearchCondition;
import com.goggles.mentoring_service.domain.booking.MentoringBooking;
import com.goggles.mentoring_service.domain.booking.MentoringBookingId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface MentoringBookingRepository {

	MentoringBooking save(MentoringBooking mentoringBooking);

	Optional<MentoringBooking> findById(MentoringBookingId id);

	Page<MentoringBooking> findByUser(BookingSearchCondition condition, Pageable pageable);

	List<MentoringBooking> findPaymentCompletedWithApproachingSessions(LocalDate thresholdDate, LocalTime thresholdTime);
}
