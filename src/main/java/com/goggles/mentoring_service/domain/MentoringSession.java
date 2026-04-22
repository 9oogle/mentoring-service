package com.goggles.mentoring_service.domain;

import com.goggles.mentoring_service.domain.exception.BookedSessionStatusCannotBeChangedException;
import com.goggles.mentoring_service.domain.exception.SessionNotAvailableException;
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
		MentoringSession session = new MentoringSession();
		session.sessionDate = date;
		session.sessionStartTime = startTime;
		session.sessionEndTime = endTime;
		return session;
	}

	public void deactivate() {
		if (this.status == SessionStatus.BOOKED) {
			throw new BookedSessionStatusCannotBeChangedException();
		}
		this.status = SessionStatus.NOT_AVAILABLE;
	}

	public void activate() {
		if (this.status == SessionStatus.BOOKED) {
			throw new BookedSessionStatusCannotBeChangedException();
		}
		this.status = SessionStatus.AVAILABLE;
	}

	public boolean isBooked() {
		return this.status == SessionStatus.BOOKED;
	}

	public void book() {
		if (this.status != SessionStatus.AVAILABLE) {
			throw new SessionNotAvailableException();
		}
		this.status = SessionStatus.BOOKED;
	}
}
