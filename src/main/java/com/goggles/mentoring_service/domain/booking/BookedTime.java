package com.goggles.mentoring_service.domain.booking;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookedTime {
	private LocalDate sessionDate;
	private LocalTime sessionStartTime;
	private LocalTime sessionEndTime;

	public static BookedTime of(SessionSlot slot) {
		BookedTime bookedTime = new BookedTime();
		bookedTime.sessionDate = slot.date();
		bookedTime.sessionStartTime = slot.startTime();
		bookedTime.sessionEndTime = slot.endTime();
		return bookedTime;
	}
}
