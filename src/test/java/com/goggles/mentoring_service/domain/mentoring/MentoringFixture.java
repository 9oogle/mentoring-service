package com.goggles.mentoring_service.domain.mentoring;

import com.goggles.mentoring_service.application.command.TimeSchedules;
import com.goggles.mentoring_service.domain._common.UserType;
import com.goggles.mentoring_service.domain.booking.SessionSlot;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class MentoringFixture {

	public static final UUID MENTORING_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
	public static final UUID MENTOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
	public static final String MENTOR_NAME = "홍길동";
	public static final String MENTOR_FIELD = "백엔드";
	public static final String MENTOR_EMAIL = "mentor@test.com";
	public static final UserType MENTOR_TYPE = UserType.INSTRUCTOR;

	public static final UUID CATEGORY_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
	public static final String CATEGORY_NAME = "자바";
	public static final String CATEGORY_CODE = "JAVA";

	public static final String TITLE = "스프링 부트 멘토링";
	public static final String SUBTITLE = "실무 위주";
	public static final String DESCRIPTION = "멘토링 설명";
	public static final MentoringDuration DURATION = MentoringDuration.MINUTES_60;
	public static final int SESSION_COUNT = 1;
	public static final int MAX_PARTICIPANTS = 1;
	public static final int PRICE = 50_000;

	public static final LocalDate SESSION_DATE_1 = LocalDate.of(2026, 5, 1);
	public static final LocalDate SESSION_DATE_2 = LocalDate.of(2026, 5, 2);
	public static final LocalTime START_TIME = LocalTime.of(10, 0);
	public static final LocalTime END_TIME = LocalTime.of(11, 0);

	public static Mentoring.MentoringBuilder defaultMentoringBuilder() {
		return Mentoring.builder()
				.mentorId(MENTOR_ID)
				.mentorName(MENTOR_NAME)
				.mentorField(MENTOR_FIELD)
				.mentorEmail(MENTOR_EMAIL)
				.mentorType(MENTOR_TYPE)
				.categoryId(CATEGORY_ID)
				.categoryName(CATEGORY_NAME)
				.categoryCode(CATEGORY_CODE)
				.title(TITLE)
				.subtitle(SUBTITLE)
				.description(DESCRIPTION)
				.duration(DURATION)
				.status(MentoringStatus.INACTIVE)
				.mentoringType(MentoringType.ONE_ON_ONE)
				.format(Format.SINGLE)
				.sessionCount(SESSION_COUNT)
				.maxParticipants(MAX_PARTICIPANTS)
				.excludeHolidays(false)
				.price(PRICE)
				.endDate(null)
				.sessions(List.of())
				.repeatPatterns(List.of());
	}

	public static MentoringSession session(LocalDate date) {
		return MentoringSession.of(date, START_TIME, END_TIME);
	}

	public static SessionSlot sessionSlot(LocalDate date) {
		return new SessionSlot(date, START_TIME, END_TIME);
	}

	public static RepeatPattern repeatPattern(DayOfWeek dayOfWeek) {
		return RepeatPattern.of(dayOfWeek, START_TIME, END_TIME);
	}

	public static TimeSchedules timeSchedules(DayOfWeek dayOfWeek) {
		return new TimeSchedules(dayOfWeek, START_TIME, END_TIME);
	}
}
