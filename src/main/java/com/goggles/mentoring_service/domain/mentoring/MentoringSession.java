package com.goggles.mentoring_service.domain.mentoring;

import com.goggles.mentoring_service.domain.mentoring.exception.BookedSessionStatusCannotBeChangedException;
import com.goggles.mentoring_service.domain.mentoring.exception.InvalidTimeRangeException;
import com.goggles.mentoring_service.domain.mentoring.exception.SessionNotAvailableException;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Embeddable
@Getter
@ToString
@EqualsAndHashCode(of = {"sessionDate", "sessionStartTime"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MentoringSession {
	private LocalDate sessionDate;
	private LocalTime sessionStartTime;
	private LocalTime sessionEndTime;
	private SessionStatus status = SessionStatus.AVAILABLE;

	public static MentoringSession of(LocalDate date, LocalTime startTime, LocalTime endTime) {
		if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
			throw InvalidTimeRangeException.forSession();
		}
		MentoringSession session = new MentoringSession();
		session.sessionDate = date;
		session.sessionStartTime = startTime;
		session.sessionEndTime = endTime;
		return session;
	}

	void deactivate() {
		if (this.status == SessionStatus.BOOKED) {
			throw new BookedSessionStatusCannotBeChangedException();
		}
		this.status = SessionStatus.NOT_AVAILABLE;
	}

	void activate() {
		if (this.status == SessionStatus.BOOKED) {
			throw new BookedSessionStatusCannotBeChangedException();
		}
		this.status = SessionStatus.AVAILABLE;
	}

	boolean isBooked() {
		return this.status == SessionStatus.BOOKED;
	}

	void book() {
		if (this.status != SessionStatus.AVAILABLE) {
			throw new SessionNotAvailableException();
		}
		this.status = SessionStatus.BOOKED;
	}


	void unbook() {
		if (this.status == SessionStatus.BOOKED)  this.status = SessionStatus.AVAILABLE;
	}
}
