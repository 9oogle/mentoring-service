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

	 static MentoringSession of(LocalDate date, LocalTime startTime, LocalTime endTime) {
		MentoringSession session = new MentoringSession();
		session.sessionDate = date;
		if(startTime.isAfter(endTime) || startTime.equals(endTime)) {
			throw new InvalidTimeRangeException("세션의 시작 시간은 종료 시간보다 이전이어야 합니다.");
		}
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
}
