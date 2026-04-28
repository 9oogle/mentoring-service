package com.goggles.mentoring_service.application.service;

import static com.goggles.mentoring_service.domain.booking.BookingFixture.*;
import static com.goggles.mentoring_service.domain.mentoring.MentoringFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.goggles.mentoring_service.application.command.BookingCommand;
import com.goggles.mentoring_service.application.command.MenteeInfo;
import com.goggles.mentoring_service.application.result.BookingResult;
import com.goggles.mentoring_service.config.TestAuditConfig;
import com.goggles.mentoring_service.domain._common.UserType;
import com.goggles.mentoring_service.domain.booking.BookingStatus;
import com.goggles.mentoring_service.domain.booking.MentoringBooking;
import com.goggles.mentoring_service.domain.booking.MentoringBookingId;
import com.goggles.mentoring_service.domain.booking.SessionSlot;
import com.goggles.mentoring_service.domain.booking.repository.MentoringBookingRepository;
import com.goggles.mentoring_service.domain.category.MentoringCategory;
import com.goggles.mentoring_service.domain.category.repository.MentoringCategoryRepository;
import com.goggles.mentoring_service.domain.mentoring.Format;
import com.goggles.mentoring_service.domain.mentoring.Mentoring;
import com.goggles.mentoring_service.domain.mentoring.MentoringId;
import com.goggles.mentoring_service.domain.mentoring.MentoringType;
import com.goggles.mentoring_service.domain.mentoring.SessionStatus;
import com.goggles.mentoring_service.domain.mentoring.exception.MentoringNotFoundException;
import com.goggles.mentoring_service.domain.mentoring.repository.MentoringRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(TestAuditConfig.class)
@ActiveProfiles("test")
@Transactional
class BookingServiceIntegrationTest {

  private static final Logger log = LoggerFactory.getLogger(BookingServiceIntegrationTest.class);

  @Autowired private BookingService bookingService;
  @Autowired private MentoringRepository mentoringRepository;
  @Autowired private MentoringBookingRepository bookingRepository;
  @Autowired private MentoringCategoryRepository categoryRepository;
  @Autowired private EntityManager em;

  @Test
  void createBooking_persists_to_db() {
    UUID mentoringId = saveMentoringWithSession(SESSION_DATE_1);
    BookingCommand.Create command = bookingCommand(mentoringId, sessionSlot(SESSION_DATE_1));

    BookingResult.Create result = bookingService.createBooking(command);

    em.flush();
    em.clear();

    MentoringBooking booking =
        bookingRepository.findById(new MentoringBookingId(result.bookingId())).orElseThrow();

    log.info("==== 생성된 예약 ====");
    log.info("bookingId  : {}", booking.getMentoringBookingId().bookingId());
    log.info("status     : {}", booking.getStatus());
    log.info("mentoringId: {}", booking.getBookedMentoring().getMentoringId());
    log.info("menteeId   : {}", booking.getMentee().getId());

    assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING);
    assertThat(booking.getBookedMentoring().getMentoringId()).isEqualTo(mentoringId);
    assertThat(booking.getMentee().getId()).isEqualTo(MENTEE_ID);
  }

  @Test
  void createBooking_marks_session_as_booked() {
    UUID mentoringId = saveMentoringWithSession(SESSION_DATE_1);
    BookingCommand.Create command = bookingCommand(mentoringId, sessionSlot(SESSION_DATE_1));

    bookingService.createBooking(command);

    em.flush();
    em.clear();

    Mentoring mentoring = mentoringRepository.findById(new MentoringId(mentoringId)).orElseThrow();
    SessionStatus sessionStatus =
        mentoring.getSessions().stream()
            .filter(s -> s.getSessionDate().equals(SESSION_DATE_1))
            .findFirst()
            .orElseThrow()
            .getStatus();

    log.info("==== 세션 상태 확인 ====");
    log.info("세션 날짜: {} | 상태: {}", SESSION_DATE_1, sessionStatus);

    assertThat(sessionStatus).isEqualTo(SessionStatus.BOOKED);
  }

  @Test
  void createBooking_mentoring_not_found() {
    UUID unknownId = UUID.randomUUID();
    BookingCommand.Create command = bookingCommand(unknownId, sessionSlot());

    assertThatThrownBy(() -> bookingService.createBooking(command))
        .isInstanceOf(MentoringNotFoundException.class);
  }

  // ── 헬퍼 ─────────────────────────────────────────────────────────────────

  private UUID saveMentoringWithSession(LocalDate sessionDate) {
    MentoringCategory category =
        MentoringCategory.create(MENTOR_ID, UserType.MASTER, CATEGORY_NAME, CATEGORY_CODE);
    categoryRepository.save(category);

    Mentoring mentoring =
        defaultMentoringBuilder()
            .categoryId(category.getMentoringCategoryId().categoryId())
            .categoryName(category.getName())
            .categoryCode(category.getCode())
            .format(Format.SINGLE)
            .mentoringType(MentoringType.ONE_ON_ONE)
            .sessions(List.of(session(sessionDate)))
            .build();
    mentoringRepository.save(mentoring);

    return mentoring.getMentoringId().mentoringId();
  }

  private BookingCommand.Create bookingCommand(UUID mentoringId, SessionSlot slot) {
    MenteeInfo menteeInfo = new MenteeInfo(MENTEE_ID, UserType.STUDENT, MENTEE_NAME);
    return new BookingCommand.Create(menteeInfo, mentoringId, List.of(slot), REQUEST_MESSAGE, UUID.randomUUID());
  }
}
