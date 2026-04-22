package com.goggles.mentoring_service.domain.mentoring.exception;

import com.goggles.common.exception.NotFoundException;

import java.time.LocalDate;
import java.time.LocalTime;

public class SessionNotFoundException extends NotFoundException {

	public SessionNotFoundException(LocalDate date, LocalTime startTime) {
		super(date + " " + startTime + "에 해당하는 세션이 존재하지 않습니다.");
	}
}