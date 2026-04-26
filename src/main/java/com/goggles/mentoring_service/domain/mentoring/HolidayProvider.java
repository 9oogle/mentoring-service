package com.goggles.mentoring_service.domain.mentoring;

import java.time.LocalDate;

public interface HolidayProvider {
	boolean isHoliday(LocalDate date);
}