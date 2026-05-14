package com.goggles.mentoring_service.infrastructure.persistence.jpa;

import com.goggles.mentoring_service.domain.booking.BookingStatus;
import com.goggles.mentoring_service.domain.booking.MentoringBooking;
import com.goggles.mentoring_service.domain.booking.MentoringBookingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface BookingJpaRepository extends JpaRepository<MentoringBooking, MentoringBookingId> {

	@Query("SELECT DISTINCT b FROM MentoringBooking b JOIN b.bookingSessions s " +
			"WHERE b.status = :status " +
			"AND (s.sessionDate < :thresholdDate " +
			"     OR (s.sessionDate = :thresholdDate AND s.sessionStartTime <= :thresholdTime))")
	List<MentoringBooking> findByStatusWithApproachingSessions(
			@Param("status") BookingStatus status,
			@Param("thresholdDate") LocalDate thresholdDate,
			@Param("thresholdTime") LocalTime thresholdTime);
}
