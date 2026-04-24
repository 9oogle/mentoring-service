package com.goggles.mentoring_service.application.service;

import com.goggles.mentoring_service.domain.booking.repository.MentoringBookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingService {
	private final MentoringBookingRepository bookingRepository;
}
