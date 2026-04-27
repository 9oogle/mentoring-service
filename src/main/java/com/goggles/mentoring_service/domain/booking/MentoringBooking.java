package com.goggles.mentoring_service.domain.booking;

import com.goggles.common.domain.BaseAudit;
import com.goggles.mentoring_service.domain._common.UserType;
import com.goggles.mentoring_service.domain.booking.exception.CancellationDeadlineExceededException;
import com.goggles.mentoring_service.domain.booking.exception.CancellationReasonRequiredException;
import com.goggles.mentoring_service.domain.booking.exception.UnauthorizedBookingAccessException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "P_MENTORING_BOOKING")
@Access(AccessType.FIELD)
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MentoringBooking extends BaseAudit {

	private static final int CANCELLATION_DEADLINE_HOURS = 24;

	@EmbeddedId
	private MentoringBookingId mentoringBookingId;

	@Embedded
	private BookedMentoring bookedMentoring;

	@Embedded
	private Mentee mentee;

	@Enumerated(EnumType.STRING)
	@Column(length = 30, nullable = false)
	private BookingStatus status = BookingStatus.PENDING;

	@Embedded
	private BookingClosure closure;

	private MentoringBooking(BookedMentoring bookedMentoring, Mentee mentee) {
		this.mentoringBookingId = MentoringBookingId.of();
		this.bookedMentoring = bookedMentoring;
		this.mentee = mentee;
	}

	@Builder
	public static MentoringBooking create(UUID mentoringId, String categoryCode,
			String categoryName, String title, String subtitle, UUID mentorId, String mentorName,
			LocalDate sessionDate, LocalTime sessionStartTime, LocalTime sessionEndTime,
			UUID menteeId, UserType menteeUserType, String menteeName) {
		BookedMentoring bookedMentoring =
				BookedMentoring.of(mentoringId, categoryCode, categoryName, title, subtitle,
						mentorId, mentorName, sessionDate, sessionStartTime, sessionEndTime);
		Mentee mentee = Mentee.of(menteeId, menteeUserType, menteeName);
		return new MentoringBooking(bookedMentoring, mentee);
	}

	public void completePayment() {
		validateStatus(BookingStatus.PAYMENT_COMPLETED);
		this.status = BookingStatus.PAYMENT_COMPLETED;
	}

	public void failPayment() {
		validateStatus(BookingStatus.PAYMENT_FAILED);
		this.status = BookingStatus.PAYMENT_FAILED;
	}

	public void accept(UUID userId, UserType userType) {
		checkIfUserIsMentor(userId, userType);
		validateStatus(BookingStatus.ACCEPTED);
		this.status = BookingStatus.ACCEPTED;
	}

	public void reject(UUID userId, UserType userType, String reason, LocalDateTime now) {
		checkIfUserIsMentor(userId, userType);
		validateReason(reason);
		validateStatus(BookingStatus.REJECTED);
		this.status = BookingStatus.REJECTED;
		this.closure = BookingClosure.close(userId, reason, now);
	}

	public void cancel(UUID canceledBy, UserType userType, String reason, LocalDateTime now) {
		validateReason(reason);
		checkIfUserCanCancel(canceledBy, userType);
		checkCancellationDeadline(now);
		validateStatus(BookingStatus.CANCELED);
		this.status = BookingStatus.CANCELED;
		this.closure = BookingClosure.close(canceledBy, reason, now);
	}

	public UUID getClosedBy() {
		return this.closure != null ? this.closure.getClosedBy() : null;
	}

	public LocalDateTime getClosedAt() {
		return this.closure != null ? this.closure.getClosedAt() : null;
	}

	public String getCloseReason() {
		return this.closure != null ? this.closure.getCloseReason() : null;
	}

	private void checkIfUserIsMentor(UUID userId, UserType userType) {
		if (userType != UserType.INSTRUCTOR || !bookedMentoring.isMentor(userId)) {
			throw UnauthorizedBookingAccessException.noPermissionToProcess();
		}
	}

	private void checkIfUserCanCancel(UUID canceledBy, UserType userType) {
		if (userType == UserType.INSTRUCTOR) {
			if (!bookedMentoring.isMentor(canceledBy)) {
				throw UnauthorizedBookingAccessException.noPermissionToCancel();
			}
		}
		else if (userType == UserType.STUDENT) {
			if (!mentee.isMentee(canceledBy)) {
				throw UnauthorizedBookingAccessException.noPermissionToCancel();
			}
		}
		else {
			throw UnauthorizedBookingAccessException.noPermissionToCancel();
		}
	}

	private void validateReason(String reason) {
		if (reason == null || reason.isBlank()) {
			throw CancellationReasonRequiredException.of();
		}
	}

	private void checkCancellationDeadline(LocalDateTime now) {
		LocalDateTime sessionStart = LocalDateTime.of(bookedMentoring.getSessionDate(),
				bookedMentoring.getSessionStartTime());
		if (now.isAfter(sessionStart.minusHours(CANCELLATION_DEADLINE_HOURS))) {
			throw CancellationDeadlineExceededException.of();
		}
	}

	private void validateStatus(BookingStatus transitionTo) {
		this.status.checkTransitionValidation(transitionTo);
	}
}