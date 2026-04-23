package com.goggles.mentoring_service.domain.mentoring;

import com.goggles.mentoring_service.domain.mentoring.exception.MentoringPolicyViolationException;
import com.goggles.mentoring_service.domain.mentoring.exception.RepeatPatternRequiredException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static com.goggles.mentoring_service.domain.mentoring.MentoringFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MentoringTest {

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
        assertThatThrownBy(() -> defaultMentoringBuilder().price(-1).build())
                .isInstanceOf(MentoringPolicyViolationException.class);
    }

    @Test
    void create_group_min_participants() {
        assertThatThrownBy(() -> defaultMentoringBuilder()
                .mentoringType(MentoringType.GROUP)
                .maxParticipants(1)
                .build())
                .isInstanceOf(MentoringPolicyViolationException.class);
    }

    @Test
    void create_one_on_one_max_participants() {
        assertThatThrownBy(() -> defaultMentoringBuilder()
                .mentoringType(MentoringType.ONE_ON_ONE)
                .maxParticipants(2)
                .build())
                .isInstanceOf(MentoringPolicyViolationException.class);
    }

    @Test
    void create_multi_requires_min_2_sessions() {
        assertThatThrownBy(() -> defaultMentoringBuilder()
                .format(Format.MULTI)
                .sessionCount(1)
                .build())
                .isInstanceOf(MentoringPolicyViolationException.class);
    }

    @Test
    void addSessions_success() {
        mentoring.addSessions(List.of(session(SESSION_DATE_1), session(SESSION_DATE_2)));

        assertThat(mentoring.getSessions()).hasSize(2);
        assertThat(mentoring.getSessions().getFirst().getStatus()).isEqualTo(SessionStatus.AVAILABLE);
    }

    @Test
    void updateRepeatPatterns_success() {
        mentoring.updateRepeatPatterns(List.of(
                repeatPattern(DayOfWeek.MONDAY),
                repeatPattern(DayOfWeek.WEDNESDAY)
        ));

        assertThat(mentoring.getRepeatPatterns()).hasSize(2);
        assertThat(mentoring.getRepeatPatterns().getFirst().getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
    }

    @Test
    void updateRepeatPatterns_replaces_all() {
        mentoring.updateRepeatPatterns(List.of(repeatPattern(DayOfWeek.MONDAY)));
        mentoring.updateRepeatPatterns(List.of(repeatPattern(DayOfWeek.FRIDAY)));

        assertThat(mentoring.getRepeatPatterns()).hasSize(1);
        assertThat(mentoring.getRepeatPatterns().getFirst().getDayOfWeek()).isEqualTo(DayOfWeek.FRIDAY);
    }

    @Test
    void addSessions_outside_business_hours() {
        MentoringSession earlySession = MentoringSession.of(
                SESSION_DATE_1, LocalTime.of(5, 0), LocalTime.of(6, 0));

        assertThatThrownBy(() -> mentoring.addSessions(List.of(earlySession)))
                .isInstanceOf(MentoringPolicyViolationException.class);
    }

    @Test
    void addSessions_duration_mismatch() {
        MentoringSession wrongDuration = MentoringSession.of(
                SESSION_DATE_1, LocalTime.of(10, 0), LocalTime.of(10, 30));

        assertThatThrownBy(() -> mentoring.addSessions(List.of(wrongDuration)))
                .isInstanceOf(MentoringPolicyViolationException.class);
    }

    @Test
    void addSessions_beyond_end_date() {
        Mentoring withEndDate = defaultMentoringBuilder()
                .endDate(LocalDate.of(2026, 4, 30))
                .build();
        MentoringSession lateSession = MentoringSession.of(
                LocalDate.of(2026, 5, 1), START_TIME, END_TIME);

        assertThatThrownBy(() -> withEndDate.addSessions(List.of(lateSession)))
                .isInstanceOf(MentoringPolicyViolationException.class);
    }

    @Test
    void generateSessions_success() {
        Mentoring multiMentoring = defaultMentoringBuilder()
                .format(Format.MULTI)
                .sessionCount(2)
                .build();
        multiMentoring.updateRepeatPatterns(List.of(repeatPattern(DayOfWeek.MONDAY)));

        // 2026-05-01(금) ~ 2026-05-31(일), 월요일: 5/4, 5/11, 5/18, 5/25 → 4개
        List<MentoringSession> generated = multiMentoring.generateSessions(
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                date -> false
        );

        assertThat(generated).hasSize(4);
        assertThat(generated.get(0).getSessionDate()).isEqualTo(LocalDate.of(2026, 5, 4));
    }

    @Test
    void generateSessions_excludes_holidays() {
        Mentoring multiMentoring = defaultMentoringBuilder()
                .format(Format.MULTI)
                .sessionCount(2)
                .excludeHolidays(true)
                .build();
        multiMentoring.updateRepeatPatterns(List.of(repeatPattern(DayOfWeek.TUESDAY)));

        // 5/5(어린이날, 화), 5/12, 5/19, 5/26 중 5/5 제외 → 3개
        List<MentoringSession> generated = multiMentoring.generateSessions(
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                date -> date.equals(LocalDate.of(2026, 5, 5))
        );

        assertThat(generated).hasSize(3);
        assertThat(generated).noneMatch(s -> s.getSessionDate().equals(LocalDate.of(2026, 5, 5)));
    }

    @Test
    void generateSessions_only_for_multi_format() {
        assertThatThrownBy(() -> mentoring.generateSessions(
                SESSION_DATE_1, SESSION_DATE_2, date -> false))
                .isInstanceOf(MentoringPolicyViolationException.class);
    }

    @Test
    void activate_multi_requires_repeat_patterns() {
        Mentoring multiMentoring = defaultMentoringBuilder()
                .format(Format.MULTI)
                .sessionCount(2)
                .build();

        assertThatThrownBy(multiMentoring::activate)
                .isInstanceOf(RepeatPatternRequiredException.class);
    }

    @Test
    void activate_success() {
        mentoring.activate();

        assertThat(mentoring.getStatus()).isEqualTo(MentoringStatus.ACTIVE);
    }

    @Test
    void deactivate_success() {
        mentoring.activate();
        mentoring.deactivate();

        assertThat(mentoring.getStatus()).isEqualTo(MentoringStatus.INACTIVE);
    }
}