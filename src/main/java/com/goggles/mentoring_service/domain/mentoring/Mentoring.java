package com.goggles.mentoring_service.domain.mentoring;

import com.goggles.common.domain.BaseAudit;
import com.goggles.mentoring_service.domain._common.UserType;
import com.goggles.mentoring_service.domain.booking.SessionSlot;
import com.goggles.mentoring_service.domain.mentoring.exception.BookedSessionCannotBeDeletedException;
import com.goggles.mentoring_service.domain.mentoring.exception.MentoringPolicyViolationException;
import com.goggles.mentoring_service.domain.mentoring.exception.RepeatPatternRequiredException;
import com.goggles.mentoring_service.domain.mentoring.exception.SessionNotFoundException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;


@Getter
@Entity
@Table(name = "P_MENTORING")
@Access(AccessType.FIELD)
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Mentoring extends BaseAudit {

	@Getter(AccessLevel.NONE)
	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "P_REPEAT_PATTERN", joinColumns = @JoinColumn(name = "mentoring_id"))
	@OrderColumn(name = "pattern_order")
	private final List<RepeatPattern> repeatPatterns = new ArrayList<>();

	@Getter(AccessLevel.NONE)
	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "P_MENTORING_SESSION", joinColumns = @JoinColumn(name = "mentoring_id"))
	@OrderColumn(name = "session_order")
	private final List<MentoringSession> sessions = new ArrayList<>();

	@EmbeddedId
	private MentoringId mentoringId;

	@Embedded
	private Mentor mentor;

	@Embedded
	private Category mentoringCategory;

	@Column(length = 100, nullable = false)
	private String title;

	private String subtitle;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Enumerated(EnumType.STRING)
	private MentoringDuration duration;

	@Enumerated(EnumType.STRING)
	private MentoringStatus status = MentoringStatus.ACTIVE;

	@Enumerated(EnumType.STRING)
	private Format format = Format.SINGLE;

	@Enumerated(EnumType.STRING)
	private MentoringType mentoringType = MentoringType.ONE_ON_ONE;

	private int sessionCount = 1;

	private int maxParticipants = 1;

	private LocalDate endDate;

	private boolean excludeHolidays = true;

	@Column(nullable = false)
	private long price;


	private Mentoring(Mentor mentor, Category mentoringCategory, String title, String subtitle,
			String description, MentoringDuration duration, MentoringStatus status,
			MentoringType mentoringType, Format format, int sessionCount, int maxParticipants,
			boolean excludeHolidays, long price, LocalDate endDate,
			List<RepeatPattern> repeatPatterns, List<MentoringSession> mentoringSessions) {
		validateTypeConstraints(mentoringType, format, sessionCount, maxParticipants);
		if (price < 0) {
			throw MentoringPolicyViolationException.invalidPrice();
		}
		this.mentoringId = MentoringId.of();
		this.mentor = mentor;
		this.mentoringCategory = mentoringCategory;
		this.title = title;
		this.subtitle = subtitle;
		this.description = description;
		this.duration = duration;
		this.status = status;
		this.format = format;
		this.mentoringType = mentoringType;
		this.sessionCount = sessionCount;
		this.maxParticipants = maxParticipants;
		this.excludeHolidays = excludeHolidays;
		this.price = price;
		this.endDate = endDate;
		this.repeatPatterns.addAll(repeatPatterns);
		this.sessions.addAll(mentoringSessions);
	}

	private static void validateTypeConstraints(MentoringType mentoringType, Format format,
			int sessionCount, int maxParticipants) {
		if (mentoringType == MentoringType.GROUP && maxParticipants <= 1) {
			throw MentoringPolicyViolationException.groupMentoringMinParticipants();
		}
		if (mentoringType == MentoringType.ONE_ON_ONE && maxParticipants != 1) {
			throw MentoringPolicyViolationException.oneOnOneMaxParticipants();
		}
		if (format == Format.MULTI && sessionCount <= 1) {
			throw MentoringPolicyViolationException.selfSelectMinSessions();
		}
	}

	@Builder
	public static Mentoring create(UUID mentorId, String mentorName, String mentorField,
			String mentorEmail, UserType mentorType, UUID categoryId, String categoryName,
			String categoryCode, String title, String subtitle, String description,
			MentoringDuration duration, MentoringStatus status, MentoringType mentoringType,
			Format format, int sessionCount, int maxParticipants, boolean excludeHolidays,
			int price, LocalDate endDate, List<RepeatPattern> repeatPatterns,
			List<MentoringSession> sessions) {
		Mentor mentor = Mentor.builder()
				.id(mentorId)
				.name(mentorName)
				.field(mentorField)
				.email(mentorEmail)
				.userType(mentorType)
				.build();
		Category category = Category.of(categoryId, categoryName, categoryCode);
		return new Mentoring(mentor, category, title, subtitle, description, duration, status,
				mentoringType, format, sessionCount, maxParticipants, excludeHolidays, price,
				endDate, repeatPatterns, sessions);
	}

	private static void validateSessionWithinWorkingHours(MentoringSession session) {
		if (session.getSessionStartTime()
				.isBefore(SessionPolicy.BUSINESS_START) || session.getSessionEndTime()
				.isAfter(SessionPolicy.BUSINESS_END)) {
			throw MentoringPolicyViolationException.sessionOutsideBusinessHours();
		}
	}

	public List<RepeatPattern> getRepeatPatterns() {
		return Collections.unmodifiableList(repeatPatterns);
	}

	public List<MentoringSession> getSessions() {
		return Collections.unmodifiableList(sessions);
	}

	public void activate() {
		if (format == Format.MULTI && repeatPatterns.isEmpty()) {
			throw new RepeatPatternRequiredException();
		}
		this.status = MentoringStatus.ACTIVE;
	}

	public void deactivate(UUID userId, UserType userType) {
		checkIfUserIsOwnerOrManager(userId, userType);
		this.status = MentoringStatus.INACTIVE;
	}

	private void checkIfUserIsOwnerOrManager(UUID userId, UserType userType) {
		if (!mentor.isSameUser(userId, userType) && userType != UserType.MASTER) {
			throw MentoringPolicyViolationException.noPermissionToDeactivate();
		}
	}

	public void updateInfo(UUID userId, UserType userType, String title, String subtitle,
			String description, Integer price, LocalDate endDate, List<RepeatPattern> newPatterns,
			LocalDate now) {
		checkIfUserIsOwnerOrManager(userId, userType);

		if (title != null && !title.isBlank()) {
			this.title = title;
		}
		if (subtitle != null) {
			this.subtitle = subtitle;
		}
		if (description != null) {
			this.description = description;
		}
		if (endDate != null) {
			this.endDate = endDate;
		}
		if (price != null) {
			if (price < 0) {
				throw MentoringPolicyViolationException.invalidPrice();
			}
			this.price = price;
		}
		if (newPatterns != null) {
			updateRepeatPatterns(newPatterns, now);
		}
	}

	public void delete(UUID userId, UserType userType) {
		checkIfUserIsOwnerOrManager(userId, userType);
		if (sessions.stream()
				.anyMatch(MentoringSession::isBooked)) {
			throw new BookedSessionCannotBeDeletedException();
		}
		softDelete(userId);
	}

	public void updateEndDate(LocalDate newEndDate) {
		this.endDate = newEndDate;
	}

	public void updateRepeatPatterns(List<RepeatPattern> newPatterns, LocalDate now) {
		this.repeatPatterns.clear();
		this.repeatPatterns.addAll(newPatterns);
		this.sessions.removeIf(session -> session.getSessionDate()
				.isAfter(now) && !session.isBooked());
	}

	public void addSessions(List<MentoringSession> newSessions) {
		newSessions.forEach(this::validateSession);
		this.sessions.addAll(newSessions);
	}

	public void addSessionsExcludingBooked(List<MentoringSession> candidates) {
		Set<LocalDateTime> bookedSlots = sessions.stream()
				.filter(MentoringSession::isBooked)
				.map(session -> LocalDateTime.of(session.getSessionDate(), session.getSessionStartTime()))
				.collect(Collectors.toSet());
		List<MentoringSession> filtered = candidates.stream()
				.filter(session -> !bookedSlots.contains(
						LocalDateTime.of(session.getSessionDate(), session.getSessionStartTime())))
				.toList();
		addSessions(filtered);
	}

	private void validateSession(MentoringSession session) {
		validateSessionWithinWorkingHours(session);
		validateSessionDuration(session);
		validateSessionBeforeEndDate(session);
	}

	private void validateSessionBeforeEndDate(MentoringSession session) {
		if (this.endDate != null && session.getSessionDate()
				.isAfter(this.endDate)) {
			throw MentoringPolicyViolationException.sessionBeyondEndDate();
		}
	}

	private void validateSessionDuration(MentoringSession session) {
		long minutes = Duration.between(session.getSessionStartTime(), session.getSessionEndTime())
				.toMinutes();
		if (minutes != this.duration.getMinutes()) {
			throw MentoringPolicyViolationException.sessionDurationMismatch();
		}
	}

	public List<MentoringSession> generateSessions(LocalDate from, LocalDate to,
			HolidayProvider holidayProvider) {
		LocalDate effectiveTo = (endDate != null && endDate.isBefore(to)) ? endDate : to;
		List<MentoringSession> generated = new ArrayList<>();

		for (LocalDate date = from; !date.isAfter(effectiveTo); date = date.plusDays(1)) {
			if (excludeHolidays && holidayProvider.isHoliday(date)) continue;
			final LocalDate current = date;
			repeatPatterns.stream()
					.filter(p -> p.getDayOfWeek() == current.getDayOfWeek())
					.map(p -> MentoringSession.of(current, p.getStartTime(), p.getEndTime()))
					.forEach(generated::add);
		}
		return generated;
	}

	public void updateSessions(List<MentoringSession> newSessions, LocalDate now) {
		this.sessions.removeIf(session -> session.getSessionDate()
				.isAfter(now) && !session.isBooked());
		this.sessions.addAll(newSessions);
	}

	public void removeSessions(List<MentoringSession> sessionsToRemove) {
		if (sessionsToRemove.stream()
				.anyMatch(MentoringSession::isBooked)) {
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

	public void bookSession(List<SessionSlot> sessionSlots) {
		List<MentoringSession> targets = sessionSlots.stream()
				.map(slot -> findSession(slot.date(), slot.startTime()))
				.toList();
		targets.forEach(MentoringSession::book);
	}

	private MentoringSession findSession(LocalDate date, LocalTime startTime) {
		return this.sessions.stream()
				.filter(session -> session.getSessionDate()
						.equals(date) && session.getSessionStartTime()
						.equals(startTime))
				.findFirst()
				.orElseThrow(() -> new SessionNotFoundException(date, startTime));
	}

	public LocalTime getSlotEndTime(LocalDate date, LocalTime startTime) {
		return findSession(date, startTime).getSessionEndTime();
	}

	public void unbookSession(LocalDate date, LocalTime startTime) {
		findSession(date, startTime).unbook();
	}

	public void cleanupPastSessions(LocalDate today) {
		sessions.removeIf(session -> session.getSessionDate().isBefore(today) && !session.isBooked());
	}

	public void addSessionsIfAbsent(List<MentoringSession> candidates) {
		Set<LocalDateTime> existing = sessions.stream()
				.map(session -> LocalDateTime.of(session.getSessionDate(), session.getSessionStartTime()))
				.collect(Collectors.toSet());
		List<MentoringSession> toAdd = candidates.stream()
				.filter(session -> existing.add(
						LocalDateTime.of(session.getSessionDate(), session.getSessionStartTime())))
				.toList();
		if (!toAdd.isEmpty()) {
			addSessions(toAdd);
		}
	}

}
