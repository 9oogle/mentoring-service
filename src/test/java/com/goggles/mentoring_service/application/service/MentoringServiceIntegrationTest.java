package com.goggles.mentoring_service.application.service;

import com.goggles.common.pagination.CommonPageRequest;
import com.goggles.mentoring_service.application.command.MentoringCommand;
import com.goggles.mentoring_service.domain.mentoring.MentoringSearchCondition;
import com.goggles.mentoring_service.application.result.MentoringResult;
import com.goggles.mentoring_service.config.TestAuditConfig;
import com.goggles.mentoring_service.domain.category.MentoringCategory;
import com.goggles.mentoring_service.domain.category.repository.MentoringCategoryRepository;
import com.goggles.mentoring_service.domain.common.UserType;
import com.goggles.mentoring_service.domain.mentoring.Format;
import com.goggles.mentoring_service.domain.mentoring.Mentoring;
import com.goggles.mentoring_service.domain.mentoring.MentoringId;
import com.goggles.mentoring_service.domain.mentoring.MentoringStatus;
import com.goggles.mentoring_service.domain.mentoring.MentoringType;
import com.goggles.mentoring_service.domain.mentoring.repository.MentoringRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

import static com.goggles.mentoring_service.domain.mentoring.MentoringFixture.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(TestAuditConfig.class)
@ActiveProfiles("test")
@Transactional
class MentoringServiceIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(MentoringServiceIntegrationTest.class);

    @Autowired
    private MentoringService mentoringService;

    @Autowired
    private MentoringRepository mentoringRepository;

    @Autowired
    private MentoringCategoryRepository categoryRepository;

    @Autowired
    private EntityManager em;

    // ── 생성 ─────────────────────────────────────────────────────────────────

    @Test
    void createMentoring_persists_to_db() {
        MentoringCategory category = MentoringCategory.create(
                MENTOR_ID, UserType.MASTER, CATEGORY_NAME, CATEGORY_CODE);
        categoryRepository.save(category);

        MentoringCommand.Create command = new MentoringCommand.Create(
                MENTOR_ID, MENTOR_NAME, MENTOR_FIELD, MENTOR_EMAIL, MENTOR_TYPE,
                category.getMentoringCategoryId().categoryId(),
                TITLE, SUBTITLE, DESCRIPTION,
                DURATION, MentoringType.ONE_ON_ONE, Format.SINGLE,
                SESSION_COUNT, MAX_PARTICIPANTS, false, PRICE, null,
                List.of(sessionSlot(SESSION_DATE_1), sessionSlot(SESSION_DATE_2)),
                List.of(timeSchedules(DayOfWeek.MONDAY), timeSchedules(DayOfWeek.WEDNESDAY))
        );

        UUID mentoringId = mentoringService.createMentoring(command);

        em.flush();
        em.clear();

        Mentoring found = mentoringRepository.findById(new MentoringId(mentoringId)).orElseThrow();

        log.info("==== 생성된 멘토링 ====");
        log.info("id       : {}", found.getMentoringId().mentoringId());
        log.info("title    : {}", found.getTitle());
        log.info("status   : {}", found.getStatus());
        log.info("price    : {}", found.getPrice());
        log.info("duration : {}", found.getDuration());
        log.info("mentor   : {} / {} / {}", found.getMentor().getName(), found.getMentor().getField(), found.getMentor().getEmail());
        log.info("category : {} ({})", found.getMentoringCategory().getName(), found.getMentoringCategory().getCode());

        log.info("==== 반복 패턴 ({}) ====", found.getRepeatPatterns().size());
        found.getRepeatPatterns().forEach(p ->
                log.info("  {} {} ~ {}", p.getDayOfWeek(), p.getStartTime(), p.getEndTime()));

        log.info("==== 세션 ({}) ====", found.getSessions().size());
        found.getSessions().forEach(s ->
                log.info("  {} {} ~ {} [{}]", s.getSessionDate(), s.getSessionStartTime(), s.getSessionEndTime(), s.getStatus()));

        assertThat(found.getTitle()).isEqualTo(TITLE);
        assertThat(found.getStatus()).isEqualTo(MentoringStatus.INACTIVE);
        assertThat(found.getRepeatPatterns()).hasSize(2);
        assertThat(found.getSessions()).hasSize(2);
    }

    // ── 상세 조회 ─────────────────────────────────────────────────────────────

    @Test
    void getMentoring_returns_full_detail() {
        UUID mentoringId = saveMentoring(TITLE, PRICE);
        em.flush();
        em.clear();

        MentoringResult.Detail detail = mentoringService.getMentoring(mentoringId);

        log.info("==== 멘토링 상세 조회 ====");
        log.info("id       : {}", detail.mentoringId());
        log.info("title    : {}", detail.title());
        log.info("subtitle : {}", detail.subtitle());
        log.info("status   : {} | format: {} | type: {}", detail.status(), detail.format(), detail.mentoringType());
        log.info("price    : {}원 | duration: {}", detail.price(), detail.duration());
        log.info("mentor   : {} ({})", detail.mentor().name(), detail.mentor().field());
        log.info("category : {} [{}]", detail.category().name(), detail.category().code());

        assertThat(detail.mentoringId()).isEqualTo(mentoringId);
        assertThat(detail.title()).isEqualTo(TITLE);
        assertThat(detail.subtitle()).isEqualTo(SUBTITLE);
        assertThat(detail.description()).isEqualTo(DESCRIPTION);
        assertThat(detail.status()).isEqualTo(MentoringStatus.INACTIVE);
        assertThat(detail.format()).isEqualTo(Format.SINGLE);
        assertThat(detail.mentoringType()).isEqualTo(MentoringType.ONE_ON_ONE);
        assertThat(detail.price()).isEqualTo(PRICE);
        assertThat(detail.duration()).isEqualTo(DURATION);
        assertThat(detail.mentor().name()).isEqualTo(MENTOR_NAME);
        assertThat(detail.mentor().field()).isEqualTo(MENTOR_FIELD);
        assertThat(detail.category().name()).isEqualTo(CATEGORY_NAME);
        assertThat(detail.category().code()).isEqualTo(CATEGORY_CODE);
    }

    // ── 스케쥴 조회 ───────────────────────────────────────────────────────────

    @Test
    void getMentoringSchedules_returns_patterns_and_sessions() {
        MentoringCategory category = createAndSaveCategory();
        MentoringCommand.Create command = new MentoringCommand.Create(
                MENTOR_ID, MENTOR_NAME, MENTOR_FIELD, MENTOR_EMAIL, MENTOR_TYPE,
                category.getMentoringCategoryId().categoryId(),
                TITLE, SUBTITLE, DESCRIPTION,
                DURATION, MentoringType.ONE_ON_ONE, Format.SINGLE,
                SESSION_COUNT, MAX_PARTICIPANTS, false, PRICE, null,
                List.of(sessionSlot(SESSION_DATE_1), sessionSlot(SESSION_DATE_2)),
                List.of(timeSchedules(DayOfWeek.MONDAY), timeSchedules(DayOfWeek.WEDNESDAY))
        );
        UUID mentoringId = mentoringService.createMentoring(command);
        em.flush();
        em.clear();

        MentoringResult.Schedules schedules = mentoringService.getMentoringSchedules(mentoringId);

        log.info("==== 멘토링 스케쥴 조회 ====");
        log.info("반복 패턴 ({}):", schedules.repeatPatterns().size());
        schedules.repeatPatterns().forEach(p ->
                log.info("  요일: {} | {} ~ {}", p.dayOfWeek(), p.startTime(), p.endTime()));
        log.info("세션 ({}):", schedules.sessions().size());
        schedules.sessions().forEach(s ->
                log.info("  날짜: {} | {} ~ {} | 상태: {}", s.date(), s.startTime(), s.endTime(), s.status()));

        assertThat(schedules.repeatPatterns()).hasSize(2);
        assertThat(schedules.repeatPatterns().get(0).dayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(schedules.repeatPatterns().get(0).startTime()).isEqualTo(START_TIME);
        assertThat(schedules.repeatPatterns().get(1).dayOfWeek()).isEqualTo(DayOfWeek.WEDNESDAY);
        assertThat(schedules.sessions()).hasSize(2);
        assertThat(schedules.sessions().get(0).date()).isEqualTo(SESSION_DATE_1);
        assertThat(schedules.sessions().get(1).date()).isEqualTo(SESSION_DATE_2);
    }

    // ── 검색/목록 조회 ────────────────────────────────────────────────────────

    @Test
    void searchMentorings_filters_by_keyword_title() {
        saveMentoring("스프링 부트 멘토링", PRICE);
        saveMentoring("리액트 입문 과정", 30_000);
        em.flush();
        em.clear();

        MentoringSearchCondition condition = new MentoringSearchCondition("스프링", null, null, null, null, null);
        Page<MentoringResult.Summary> result = mentoringService.searchMentorings(condition, CommonPageRequest.of(0, 10));

        log.info("==== 키워드 '스프링' 검색 결과 ====");
        log.info("총 {}건 검색됨", result.getTotalElements());
        result.getContent().forEach(s ->
                log.info("  [{}] {} | {}원 | 멘토: {}", s.status(), s.title(), s.price(), s.mentorName()));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).title()).contains("스프링");
    }

    @Test
    void searchMentorings_filters_by_mentor_id() {
        UUID otherMentorId = UUID.fromString("00000000-0000-0000-0000-000000000099");
        saveMentoring(TITLE, PRICE);
        saveMentoringWithMentor(otherMentorId, "다른 멘토 멘토링");
        em.flush();
        em.clear();

        MentoringSearchCondition condition = new MentoringSearchCondition(null, null, MENTOR_ID, null, null, null);
        Page<MentoringResult.Summary> result = mentoringService.searchMentorings(condition, CommonPageRequest.of(0, 10));

        log.info("==== 멘토 ID {} 필터 결과 ====", MENTOR_ID);
        log.info("총 {}건 검색됨", result.getTotalElements());
        result.getContent().forEach(s ->
                log.info("  [{}] {} | 멘토: {}", s.status(), s.title(), s.mentorName()));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).mentorName()).isEqualTo(MENTOR_NAME);
    }

    @Test
    void searchMentorings_returns_all_without_condition() {
        saveMentoring("멘토링 A", 10_000);
        saveMentoring("멘토링 B", 20_000);
        saveMentoring("멘토링 C", 30_000);
        em.flush();
        em.clear();

        MentoringSearchCondition condition = new MentoringSearchCondition(null, null, null, null, null, null);
        Page<MentoringResult.Summary> result = mentoringService.searchMentorings(condition, CommonPageRequest.of(0, 10));

        log.info("==== 전체 멘토링 목록 조회 ====");
        log.info("총 {}건 (페이지당 {}, 현재 {}페이지)",
                result.getTotalElements(), result.getSize(), result.getNumber());
        result.getContent().forEach(s ->
                log.info("  [{}] {} | {}원", s.status(), s.title(), s.price()));

        assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(3);
    }

    @Test
    void searchMentorings_pagination_returns_correct_metadata() {
        for (int i = 1; i <= 5; i++) {
            saveMentoring("멘토링 " + i, i * 10_000);
        }
        em.flush();
        em.clear();

        log.info("size=10 (허용된 최솟값): 5건이므로 첫 페이지에 전체가 담김");
        Page<MentoringResult.Summary> page0 = mentoringService.searchMentorings(
                new MentoringSearchCondition(null, null, null, null, null, null),
                CommonPageRequest.of(0, 10));

        log.info("==== 페이지네이션 메타데이터 확인 (size=10) ====");
        log.info("0페이지: {}건 / 전체 {}건 / 총 {}페이지 / first={} / last={}",
                page0.getContent().size(), page0.getTotalElements(),
                page0.getTotalPages(), page0.isFirst(), page0.isLast());
        page0.getContent().forEach(s ->
                log.info("  {} | {}원", s.title(), s.price()));

        assertThat(page0.getContent()).hasSizeGreaterThanOrEqualTo(5);
        assertThat(page0.getTotalElements()).isGreaterThanOrEqualTo(5);
        assertThat(page0.getSize()).isEqualTo(10);
        assertThat(page0.getNumber()).isEqualTo(0);
        assertThat(page0.isFirst()).isTrue();

        log.info("2페이지 요청 → 범위 초과, 데이터 없음");
        Page<MentoringResult.Summary> page2 = mentoringService.searchMentorings(
                new MentoringSearchCondition(null, null, null, null, null, null),
                CommonPageRequest.of(2, 10));

        log.info("2페이지: {}건", page2.getContent().size());
        assertThat(page2.getContent()).isEmpty();
        assertThat(page2.getNumber()).isEqualTo(2);
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────────────────

    private MentoringCategory createAndSaveCategory() {
        MentoringCategory category = MentoringCategory.create(
                MENTOR_ID, UserType.MASTER, CATEGORY_NAME, CATEGORY_CODE);
        categoryRepository.save(category);
        return category;
    }

    private UUID saveMentoring(String title, int price) {
        MentoringCategory category = createAndSaveCategory();
        MentoringCommand.Create command = new MentoringCommand.Create(
                MENTOR_ID, MENTOR_NAME, MENTOR_FIELD, MENTOR_EMAIL, MENTOR_TYPE,
                category.getMentoringCategoryId().categoryId(),
                title, SUBTITLE, DESCRIPTION,
                DURATION, MentoringType.ONE_ON_ONE, Format.SINGLE,
                SESSION_COUNT, MAX_PARTICIPANTS, false, price, null,
                List.of(sessionSlot(SESSION_DATE_1)),
                List.of(timeSchedules(DayOfWeek.MONDAY))
        );
        return mentoringService.createMentoring(command);
    }

    private void saveMentoringWithMentor(UUID mentorId, String title) {
        MentoringCategory category = createAndSaveCategory();
        MentoringCommand.Create command = new MentoringCommand.Create(
                mentorId, "다른멘토", MENTOR_FIELD, MENTOR_EMAIL, MENTOR_TYPE,
                category.getMentoringCategoryId().categoryId(),
                title, SUBTITLE, DESCRIPTION,
                DURATION, MentoringType.ONE_ON_ONE, Format.SINGLE,
                SESSION_COUNT, MAX_PARTICIPANTS, false, PRICE, null,
                List.of(sessionSlot(SESSION_DATE_1)),
                List.of(timeSchedules(DayOfWeek.MONDAY))
        );
        mentoringService.createMentoring(command);
    }
}
