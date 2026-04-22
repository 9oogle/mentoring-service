package com.goggles.mentoring_service.domain;

import com.goggles.common.domain.BaseAudit;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import com.goggles.mentoring_service.domain.exception.BookedSessionCannotBeDeletedException;
import com.goggles.mentoring_service.domain.exception.InvalidMentoringPolicyException;
import com.goggles.mentoring_service.domain.exception.RepeatPatternRequiredException;
import com.goggles.mentoring_service.domain.exception.SessionNotFoundException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


@Getter
@Entity
@Table(name = "P_MENTORING")
@Access(AccessType.FIELD)
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Mentoring extends BaseAudit {

	@EmbeddedId
	private MentoringId mentoringId;

	@Embedded
	private Mentor mentor;

	@Embedded
	private MentoringCategorySnapshot mentoringCategory;

	@Column(length = 100, nullable = false)
	private String title;

	private String subtitle;

	@Column(columnDefinition = "TEXT")
	private String description;

	private MentoringDuration duration;

	private MentoringStatus status = MentoringStatus.INACTIVE;

	private BookingType bookingType = BookingType.ONE_TIME;

	private MentoringType mentoringType = MentoringType.ONE_ON_ONE;

	private int sessionCount = 1;

	private int maxParticipants = 1;

	private LocalDate endDate;

	private boolean excludeHolidays;

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "P_REPEAT_PATTERN", joinColumns = @JoinColumn(name = "mentoring_id"))
	@OrderColumn(name = "pattern_order")
	private List<RepeatPattern> repeatPatterns = new ArrayList<>();

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "P_MENTORING_SESSION", joinColumns = @JoinColumn(name = "mentoring_id"))
	@OrderColumn(name = "session_order")
	private List<MentoringSession> sessions = new ArrayList<>();


	private Mentoring(Mentor mentor, MentoringCategorySnapshot mentoringCategory, String title,
			String subtitle, String description, MentoringDuration duration,
			MentoringStatus status, MentoringType mentoringType,
			BookingType bookingType, int sessionCount, int maxParticipants, boolean excludeHolidays) {
		validateTypeConstraints(mentoringType, bookingType, sessionCount, maxParticipants);
		this.mentoringId = MentoringId.of();
		this.mentor = mentor;
		this.mentoringCategory = mentoringCategory;
		this.title = title;
		this.subtitle = subtitle;
		this.description = description;
		this.duration = duration;
		this.status = status;
		this.bookingType = bookingType;
		this.mentoringType = mentoringType;
		this.sessionCount = sessionCount;
		this.maxParticipants = maxParticipants;
		this.excludeHolidays = excludeHolidays;
	}

	private static void validateTypeConstraints(MentoringType mentoringType, BookingType bookingType,
			int sessionCount, int maxParticipants) {
		if (mentoringType == MentoringType.GROUP && maxParticipants <= 1) {
			throw new InvalidMentoringPolicyException("그룹 멘토링은 최대 참여자 수가 2명 이상이어야 합니다.");
		}
		if (mentoringType == MentoringType.ONE_ON_ONE && maxParticipants != 1) {
			throw new InvalidMentoringPolicyException("1:1 멘토링의 최대 참여자 수는 1명이어야 합니다.");
		}
		if (bookingType == BookingType.SELF_SELECT && sessionCount <= 1) {
			throw new InvalidMentoringPolicyException("자유 선택 예약은 세션 수가 2회 이상이어야 합니다.");
		}
	}

	@Builder
	public static Mentoring create(Mentor mentor, MentoringCategorySnapshot mentoringCategory, String title,
			String subtitle, String description, MentoringDuration duration,
			MentoringStatus status,MentoringType mentoringType,
			BookingType bookingType, int sessionCount, int maxParticipants, boolean excludeHolidays) {
		return new Mentoring(mentor, mentoringCategory, title, subtitle, description, duration,
				status, mentoringType,bookingType, sessionCount, maxParticipants, excludeHolidays);
	}

	public void activate() {
		if (bookingType == BookingType.AUTO_REPEAT && repeatPatterns.isEmpty()) {
			throw new RepeatPatternRequiredException();
		}
		this.status = MentoringStatus.ACTIVE;
	}

	public void deactivate() {
		this.status = MentoringStatus.INACTIVE;
	}

	public void updateEndDate(LocalDate newEndDate) {
		this.endDate = newEndDate;
	}

	public void updateRepeatPatterns(List<RepeatPattern> newPatterns) {
		this.repeatPatterns.clear();
		this.repeatPatterns.addAll(newPatterns);
	}

	public void addSessions(List<MentoringSession> sessions) {
		this.sessions.addAll(sessions);
	}

	public void updateSessions(List<MentoringSession> newSessions) {
		this.sessions.removeIf(session -> session.getSessionDate()
				.isAfter(LocalDate.now()) && !session.isBooked());
		this.sessions.addAll(newSessions);
	}

	public void removeSessions(List<MentoringSession> sessionsToRemove) {
		if (sessionsToRemove.stream().anyMatch(MentoringSession::isBooked)) {
			throw new BookedSessionCannotBeDeletedException();
		}
		this.sessions.removeAll(sessionsToRemove);
	}

	public void deactivateSession(LocalDate date, LocalTime startTime) {
		findSession(date, startTime).deactivate();
	}

	public void activateSession(LocalDate date, LocalTime startTime) {
		findSession(date, startTime).activate();
	}

	public void bookSession(LocalDate date, LocalTime startTime) {
		findSession(date, startTime).book();
	}

	private MentoringSession findSession(LocalDate date, LocalTime startTime) {
		return this.sessions.stream()
				.filter(s -> s.getSessionDate().equals(date) && s.getSessionStartTime().equals(startTime))
				.findFirst()
				.orElseThrow(() -> new SessionNotFoundException(date, startTime));
	}

}
