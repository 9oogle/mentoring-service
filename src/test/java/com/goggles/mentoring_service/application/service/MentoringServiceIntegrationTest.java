package com.goggles.mentoring_service.application.service;

import com.goggles.mentoring_service.application.command.MentoringCommand;
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
}
