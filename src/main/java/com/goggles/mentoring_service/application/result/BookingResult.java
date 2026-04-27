package com.goggles.mentoring_service.application.result;

import com.goggles.mentoring_service.domain.booking.BookingSession;
import com.goggles.mentoring_service.domain.booking.BookingStatus;
import com.goggles.mentoring_service.domain.booking.MentoringBooking;
import com.goggles.mentoring_service.domain.booking.SessionProgressStatus;
import com.goggles.mentoring_service.domain.mentoring.Mentoring;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class BookingResult {

	public record Create(UUID enrollmentId, UUID productId, String productName, long productPrice,
						 UUID instructorId, String instructorName) {

		public static Create of(MentoringBooking booking, Mentoring mentoring) {
			return new Create(booking.getMentoringBookingId()
					.bookingId(), mentoring.getMentoringId()
					.mentoringId(), mentoring.getTitle(), mentoring.getPrice(),
					mentoring.getMentor()
							.getId(), mentoring.getMentor()
					.getName());
		}
	}

	public record Summary(UUID bookingId, String mentoringTitle, String mentorName,
						  String menteeName, BookingStatus status,
						  List<SessionInfo> requestedSessions, LocalDateTime createdAt) {

		public static Summary from(MentoringBooking booking) {
			List<SessionInfo> sessionInfos = booking.getBookingSessions()
					.stream()
					.map(SessionInfo::from)
					.toList();
			return new Summary(booking.getMentoringBookingId()
					.bookingId(), booking.getBookedMentoring()
					.getTitle(), booking.getBookedMentoring()
					.getMentorName(), booking.getMentee()
					.getName(), booking.getStatus(), sessionInfos, booking.getCreatedAt());
		}
	}

	public record Detail(UUID bookingId, MentoringInfo mentoring, MenteeInfo mentee,
						 List<SessionInfo> sessions, BookingStatus status, String requestMessage,
						 ClosureInfo closure, LocalDateTime createdAt) {

		public static Detail from(MentoringBooking booking) {
			MentoringInfo mentoringInfo = new MentoringInfo(booking.getBookedMentoring()
					.getMentoringId(), booking.getBookedMentoring()
					.getTitle(), booking.getBookedMentoring()
					.getSubtitle(), booking.getBookedMentoring()
					.getMentorName(), booking.getBookedMentoring()
					.getCategoryName());

			MenteeInfo menteeInfo = new MenteeInfo(booking.getMentee()
					.getId(), booking.getMentee()
					.getName());

			List<SessionInfo> sessions = booking.getBookingSessions()
					.stream()
					.map(SessionInfo::from)
					.toList();

			ClosureInfo closureInfo = booking.getClosedBy() != null ?
					new ClosureInfo(booking.getClosedBy(), booking.getCloseReason(),
							booking.getClosedAt()) : null;

			return new Detail(booking.getMentoringBookingId()
					.bookingId(), mentoringInfo, menteeInfo, sessions, booking.getStatus(),
					booking.getRequestMessage(), closureInfo, booking.getCreatedAt());
		}

		public record MentoringInfo(UUID mentoringId, String title, String subtitle,
									String mentorName, String categoryName) {}

		public record MenteeInfo(UUID menteeId, String menteeName) {}

		public record ClosureInfo(UUID closedBy, String reason, LocalDateTime closedAt) {}
	}

	public record SessionInfo(UUID sessionId, LocalDate sessionDate, LocalTime startTime,
							  LocalTime endTime, SessionProgressStatus progressStatus) {

		public static SessionInfo from(BookingSession bookingSession) {
			return new SessionInfo(bookingSession.getId(), bookingSession.getSessionDate(),
					bookingSession.getSessionStartTime(), bookingSession.getSessionEndTime(),
					bookingSession.getProgressStatus());
		}
	}

	public record SessionList(UUID bookingId, List<SessionInfo> sessions) {

		public static SessionList from(MentoringBooking booking) {
			List<SessionInfo> sessions = booking.getBookingSessions()
					.stream()
					.map(SessionInfo::from)
					.toList();
			return new SessionList(booking.getMentoringBookingId()
					.bookingId(), sessions);
		}
	}
}
