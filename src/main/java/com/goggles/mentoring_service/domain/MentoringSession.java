package com.goggles.mentoring_service.domain;

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
			throw new IllegalStateException("예약된 세션은 상태를 변경할 수 없습니다.");
		}
		this.status = SessionStatus.NOT_AVAILABLE;
	}

	public void activate() {
		if (this.status == SessionStatus.BOOKED) {
			throw new IllegalStateException("예약된 세션은 상태를 변경할 수 없습니다.");
		}
		this.status = SessionStatus.AVAILABLE;
	}

	public boolean isBooked() {
		return this.status == SessionStatus.BOOKED;
	}

	public void book() {
		if (this.status != SessionStatus.AVAILABLE) {
			throw new IllegalStateException("예약 가능한 세션이 아닙니다.");
		}
		this.status = SessionStatus.BOOKED;
	}
}
