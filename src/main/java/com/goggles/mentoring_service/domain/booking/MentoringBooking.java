package com.goggles.mentoring_service.domain.booking;

import com.goggles.common.domain.BaseAudit;
import com.goggles.mentoring_service.domain._common.UserType;
import com.goggles.mentoring_service.domain.booking.exception.CancellationDeadlineExceededException;
import com.goggles.mentoring_service.domain.booking.exception.CancellationReasonRequiredException;
import com.goggles.mentoring_service.domain.booking.exception.UnauthorizedBookingAccessException;
import com.goggles.mentoring_service.domain.mentoring.Mentoring;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

	@ElementCollection
	@CollectionTable(name = "P_BOOKED_TIME", joinColumns = @JoinColumn(name = "booking_id"))
	private final List<BookedTime> bookedTimes = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	@Column(length = 30, nullable = false)
	private BookingStatus status = BookingStatus.PENDING;

	@Column(columnDefinition = "TEXT")
	private String requestMessage;

	@Embedded
	private BookingClosure closure;

	private UUID orderId;

	private MentoringBooking(BookedMentoring bookedMentoring, Mentee mentee,
			List<BookedTime> bookedTimes, String requestMessage, UUID orderId) {
		this.mentoringBookingId = MentoringBookingId.of();
		this.bookedMentoring = bookedMentoring;
		this.mentee = mentee;
		this.bookedTimes.addAll(bookedTimes);
		this.requestMessage = requestMessage;
		this.orderId = orderId;
	}

	public static MentoringBooking create(UUID menteeId, UserType menteeUserType, String menteeName,
			Mentoring mentoring, List<SessionSlot> sessionSlots, String requestMessage,
			UUID orderId) {
		Mentee mentee = Mentee.of(menteeId, menteeUserType, menteeName);
		BookedMentoring bookedMentoring = BookedMentoring.of(mentoring);
		List<BookedTime> bookedTimes = sessionSlots.stream()
				.map(BookedTime::of)
				.toList();
		return new MentoringBooking(bookedMentoring, mentee, bookedTimes, requestMessage, orderId);
	}

	public void completePayment() {
		validateStatus(BookingStatus.PAYMENT_COMPLETED);
		this.status = BookingStatus.PAYMENT_COMPLETED;
	}

	public void failPayment(String failureReason, LocalDateTime now) {
		validateStatus(BookingStatus.PAYMENT_FAILED);
		this.status = BookingStatus.PAYMENT_FAILED;
		this.closure = BookingClosure.close(mentee.getId(), failureReason, now);
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
		for (BookedTime session : this.bookedTimes) {
			LocalDateTime sessionStart =
					LocalDateTime.of(session.getSessionDate(), session.getSessionStartTime());
			if (now.isAfter(sessionStart.minusHours(CANCELLATION_DEADLINE_HOURS))) {
				throw CancellationDeadlineExceededException.of();
			}
		}
	}

	private void validateStatus(BookingStatus transitionTo) {
		this.status.checkTransitionValidation(transitionTo);
	}
}
