package com.goggles.mentoring_service.domain.mentoring;

import com.goggles.mentoring_service.domain._common.UserType;
import com.goggles.mentoring_service.domain.mentoring.exception.MentoringPolicyViolationException;
import com.goggles.mentoring_service.domain.mentoring.exception.RepeatPatternRequiredException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static com.goggles.mentoring_service.domain.mentoring.MentoringFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MentoringTest {

	private static final Logger log = LoggerFactory.getLogger(MentoringTest.class);

	private Mentoring mentoring;

	@BeforeEach
	void setUp() {
		mentoring = defaultMentoringBuilder().build();
	}

	@Test
	void create_success() {
		assertThat(mentoring.getMentoringId()).isNotNull();
		assertThat(mentoring.getStatus()).isEqualTo(MentoringStatus.INACTIVE);
		assertThat(mentoring.getRepeatPatterns()).isEmpty();
		assertThat(mentoring.getSessions()).isEmpty();
	}

	@Test
	void create_negative_price() {
		assertThatThrownBy(() -> defaultMentoringBuilder().price(-1)
				.build()).isInstanceOf(MentoringPolicyViolationException.class);
	}

	@Test
	void create_group_min_participants() {
		assertThatThrownBy(() -> defaultMentoringBuilder().mentoringType(MentoringType.GROUP)
				.maxParticipants(1)
				.build()).isInstanceOf(MentoringPolicyViolationException.class);
	}

	@Test
	void create_one_on_one_max_participants() {
		assertThatThrownBy(() -> defaultMentoringBuilder().mentoringType(MentoringType.ONE_ON_ONE)
				.maxParticipants(2)
				.build()).isInstanceOf(MentoringPolicyViolationException.class);
	}

	@Test
	void create_multi_requires_min_2_sessions() {
		assertThatThrownBy(() -> defaultMentoringBuilder().format(Format.MULTI)
				.sessionCount(1)
				.build()).isInstanceOf(MentoringPolicyViolationException.class);
	}

	@Test
	void addSessions_success() {
		mentoring.addSessions(List.of(session(SESSION_DATE_1), session(SESSION_DATE_2)));

		assertThat(mentoring.getSessions()).hasSize(2);
		assertThat(mentoring.getSessions()
				.getFirst()
				.getStatus()).isEqualTo(SessionStatus.AVAILABLE);
	}

	@Test
	void updateRepeatPatterns_success() {
		mentoring.updateRepeatPatterns(
				List.of(repeatPattern(DayOfWeek.MONDAY), repeatPattern(DayOfWeek.WEDNESDAY)),
				LocalDate.now());

		assertThat(mentoring.getRepeatPatterns()).hasSize(2);
		assertThat(mentoring.getRepeatPatterns()
				.getFirst()
				.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
	}

	@Test
	void updateRepeatPatterns_replaces_all() {
		mentoring.updateRepeatPatterns(
				List.of(repeatPattern(DayOfWeek.MONDAY), repeatPattern(DayOfWeek.WEDNESDAY)),
				LocalDate.now());
		mentoring.updateRepeatPatterns(List.of(repeatPattern(DayOfWeek.FRIDAY)), LocalDate.now());

		assertThat(mentoring.getRepeatPatterns()).hasSize(1);
		assertThat(mentoring.getRepeatPatterns()
				.getFirst()
				.getDayOfWeek()).isEqualTo(DayOfWeek.FRIDAY);
	}

	@Test
	void addSessions_outside_business_hours() {
		MentoringSession earlySession =
				MentoringSession.of(SESSION_DATE_1, LocalTime.of(5, 0), LocalTime.of(6, 0));

		assertThatThrownBy(() -> mentoring.addSessions(List.of(earlySession))).isInstanceOf(
				MentoringPolicyViolationException.class);
	}

	@Test
	void addSessions_duration_mismatch() {
		MentoringSession wrongDuration =
				MentoringSession.of(SESSION_DATE_1, LocalTime.of(10, 0), LocalTime.of(10, 30));

		assertThatThrownBy(() -> mentoring.addSessions(List.of(wrongDuration))).isInstanceOf(
				MentoringPolicyViolationException.class);
	}

	@Test
	void addSessions_beyond_end_date() {
		Mentoring withEndDate = defaultMentoringBuilder().endDate(LocalDate.of(2026, 4, 30))
				.build();
		MentoringSession lateSession =
				MentoringSession.of(LocalDate.of(2026, 5, 1), START_TIME, END_TIME);

		assertThatThrownBy(() -> withEndDate.addSessions(List.of(lateSession))).isInstanceOf(
				MentoringPolicyViolationException.class);
	}

	@Test
	void generateSessions_success() {
		Mentoring multiMentoring = defaultMentoringBuilder().format(Format.MULTI)
				.sessionCount(2)
				.build();
		multiMentoring.updateRepeatPatterns(List.of(repeatPattern(DayOfWeek.MONDAY)),
				LocalDate.now());

		LocalDate from = LocalDate.of(2026, 5, 1);
		LocalDate to = LocalDate.of(2026, 5, 31);
		List<MentoringSession> generated = multiMentoring.generateSessions(from, to, date -> false);

		log.info("세션 자동 생성: {} ~ {}, 월요일 → {}개 (5/4, 5/11, 5/18, 5/25)", from, to,
				generated.size());
		generated.forEach(
				s -> log.info("  생성된 세션: {} {} ~ {}", s.getSessionDate(), s.getSessionStartTime(),
						s.getSessionEndTime()));

		assertThat(generated).hasSize(4);
		assertThat(generated.getFirst()
				.getSessionDate()).isEqualTo(LocalDate.of(2026, 5, 4));
	}

	@Test
	void generateSessions_excludes_holidays() {
		Mentoring multiMentoring = defaultMentoringBuilder().format(Format.MULTI)
				.sessionCount(2)
				.excludeHolidays(true)
				.build();
		multiMentoring.updateRepeatPatterns(List.of(repeatPattern(DayOfWeek.TUESDAY)),
				LocalDate.now());

		LocalDate from = LocalDate.of(2026, 5, 1);
		LocalDate to = LocalDate.of(2026, 5, 31);
		LocalDate holiday = LocalDate.of(2026, 5, 5);
		List<MentoringSession> generated =
				multiMentoring.generateSessions(from, to, date -> date.equals(holiday));

		log.info("세션 자동 생성 (공휴일 제외): {} ~ {}, 화요일 후보 5/5·5/12·5/19·5/26 중 {}(어린이날) 제외 → {}개", from,
				to, holiday, generated.size());
		generated.forEach(s -> log.info("  생성된 세션: {}", s.getSessionDate()));

		assertThat(generated).hasSize(3);
		assertThat(generated).noneMatch(s -> s.getSessionDate()
				.equals(holiday));
	}

	@Test
	void activate_multi_requires_repeat_patterns() {
		Mentoring multiMentoring = defaultMentoringBuilder().format(Format.MULTI)
				.sessionCount(2)
				.build();

		assertThatThrownBy(multiMentoring::activate).isInstanceOf(
				RepeatPatternRequiredException.class);
	}

	@Test
	void activate_success() {
		mentoring.activate();

		assertThat(mentoring.getStatus()).isEqualTo(MentoringStatus.ACTIVE);
	}

	@Test
	void deactivate_success() {
		UserType userType = UserType.INSTRUCTOR;
		mentoring.activate();
		mentoring.deactivate(MENTOR_ID, userType);

		assertThat(mentoring.getStatus()).isEqualTo(MentoringStatus.INACTIVE);
	}
}