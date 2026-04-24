package com.goggles.mentoring_service.infrastructure.persistence.jpa;

import com.goggles.mentoring_service.domain.booking.MentoringBooking;
import com.goggles.mentoring_service.domain.booking.MentoringBookingId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingJpaRepository extends JpaRepository<MentoringBooking, MentoringBookingId> {}
