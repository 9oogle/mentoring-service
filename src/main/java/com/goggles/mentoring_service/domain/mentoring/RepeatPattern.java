package com.goggles.mentoring_service.domain.mentoring;

import com.goggles.mentoring_service.domain.mentoring.exception.InvalidTimeRangeException;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Embeddable
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RepeatPattern {
	private DayOfWeek dayOfWeek;
	private LocalTime startTime;
	private LocalTime endTime;


	public static RepeatPattern of(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
		RepeatPattern pattern = new RepeatPattern();
		pattern.dayOfWeek = dayOfWeek;
		if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
			throw InvalidTimeRangeException.forRepeatPattern();
		}
		pattern.startTime = startTime;
		pattern.endTime = endTime;
		return pattern;
	}
}
