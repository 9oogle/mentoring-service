package com.goggles.mentoring_service.infrastructure.persistence;

import com.goggles.mentoring_service.domain.booking.BookingSearchCondition;
import com.goggles.mentoring_service.domain.booking.BookingStatus;
import com.goggles.mentoring_service.domain.booking.MentoringBooking;
import com.goggles.mentoring_service.domain.booking.MentoringBookingId;
import com.goggles.mentoring_service.domain.booking.repository.MentoringBookingRepository;
import com.goggles.mentoring_service.infrastructure.persistence.jpa.BookingJpaRepository;
import com.goggles.mentoring_service.infrastructure.persistence.jpa.BookingQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BookingRepositoryImpl implements MentoringBookingRepository {

	private final BookingJpaRepository jpaRepository;
	private final BookingQueryRepository queryRepository;

	@Override
	public MentoringBooking save(MentoringBooking mentoringBooking) {
		return jpaRepository.save(mentoringBooking);
	}

	@Override
	public Optional<MentoringBooking> findById(MentoringBookingId id) {
		return jpaRepository.findById(id);
	}

	@Override
	public Page<MentoringBooking> findByUser(BookingSearchCondition condition, Pageable pageable) {
		return queryRepository.findByUser(condition, pageable);
	}

	@Override
	public List<MentoringBooking> findPaymentCompletedWithApproachingSessions(
			LocalDate thresholdDate, LocalTime thresholdTime) {
		return jpaRepository.findByStatusWithApproachingSessions(
				BookingStatus.PAYMENT_COMPLETED, thresholdDate, thresholdTime);
	}
}
