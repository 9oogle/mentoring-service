package com.goggles.mentoring_service.infrastructure.persistence.jpa;

import com.goggles.mentoring_service.domain.booking.BookingSearchCondition;
import com.goggles.mentoring_service.domain.booking.MentoringBooking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookingQueryRepository {
  Page<MentoringBooking> findByUser(BookingSearchCondition condition, Pageable pageable);
}
