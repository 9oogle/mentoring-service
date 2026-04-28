package com.goggles.mentoring_service.domain.booking;

import com.goggles.mentoring_service.application.result.BookingResult;
import com.goggles.mentoring_service.domain._common.UserType;
import com.goggles.mentoring_service.domain.booking.event.BookingEvent;
import com.goggles.mentoring_service.domain.mentoring.Mentoring;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static com.goggles.mentoring_service.domain.mentoring.MentoringFixture.MENTOR_ID;
import static com.goggles.mentoring_service.domain.mentoring.MentoringFixture.defaultMentoringBuilder;
import static org.mockito.Mockito.mock;

public class BookingFixture {
	private static final BookingEvent events = mock(BookingEvent.class);

	public static final UUID MENTEE_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");
	public static final String MENTEE_NAME = "김학생";
	public static final String REQUEST_MESSAGE = "잘 부탁드립니다";

	public static final LocalDate SESSION_DATE = LocalDate.of(2026, 6, 1);
	public static final LocalDate SESSION_DATE_2 = LocalDate.of(2026, 6, 2);
	public static final LocalTime SESSION_START_TIME = LocalTime.of(10, 0);
	public static final LocalTime SESSION_END_TIME = LocalTime.of(11, 0);

	public static final LocalDate RESCHEDULE_DATE = LocalDate.of(2026, 7, 1);

	// SESSION_DATE 기준 24시간 전/후 (세션: 2026-06-01 10:00, 취소 마감: 2026-05-31 10:00)
	public static final LocalDateTime BEFORE_DEADLINE = LocalDateTime.of(2026, 5, 31, 0, 0);
	public static final LocalDateTime AFTER_DEADLINE = LocalDateTime.of(2026, 6, 1, 9, 1);

	// 회차 일정 변경 기준 now (세션 시작 전 / 세션 지난 후)
	public static final LocalDateTime NOW_BEFORE_SESSION = LocalDateTime.of(2026, 5, 1, 0, 0);
	public static final LocalDateTime NOW_AFTER_SESSION = LocalDateTime.of(2026, 6, 2, 0, 0);

	public static final String REJECT_REASON = "일정 불가";
	public static final String CANCEL_REASON = "개인 사정";

	public static Mentoring mentoring() {
		return defaultMentoringBuilder().sessions(List.of())
				.build();
	}

	public static SessionSlot sessionSlot() {
		return new SessionSlot(SESSION_DATE, SESSION_START_TIME, SESSION_END_TIME);
	}

	public static SessionSlot sessionSlot2() {
		return new SessionSlot(SESSION_DATE_2, SESSION_START_TIME, SESSION_END_TIME);
	}

	public static MentoringBooking pendingBooking() {
		return MentoringBooking.create(MENTEE_ID, UserType.STUDENT, MENTEE_NAME, mentoring(),
				List.of(sessionSlot()), REQUEST_MESSAGE, null);
	}

	public static MentoringBooking paymentCompletedBooking() {
		MentoringBooking booking = pendingBooking();
		booking.completePayment(events);
		return booking;
	}

	public static MentoringBooking acceptedBooking() {
		MentoringBooking booking = paymentCompletedBooking();
		booking.accept(MENTOR_ID, UserType.INSTRUCTOR, events);
		return booking;
	}

	public static BookingResult.Detail bookingDetail() {
		return BookingResult.Detail.from(pendingBooking());
	}

	public static BookingResult.Summary bookingSummary() {
		return BookingResult.Summary.from(pendingBooking());
	}

	public static BookingResult.SessionList sessionList() {
		return BookingResult.SessionList.from(acceptedBooking());
	}
}
