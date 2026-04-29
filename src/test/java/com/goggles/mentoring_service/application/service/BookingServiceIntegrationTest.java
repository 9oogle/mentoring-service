package com.goggles.mentoring_service.application.service;

import static com.goggles.mentoring_service.domain.booking.BookingFixture.*;
import static com.goggles.mentoring_service.domain.mentoring.MentoringFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.goggles.common.exception.ForbiddenException;
import com.goggles.common.pagination.CommonPageRequest;
import com.goggles.mentoring_service.application.command.BookingCommand;
import com.goggles.mentoring_service.application.command.MenteeInfo;
import com.goggles.mentoring_service.domain.booking.BookingSearchCondition;
import com.goggles.mentoring_service.domain.booking.BookingSort;
import com.goggles.mentoring_service.application.result.BookingResult;
import com.goggles.mentoring_service.config.TestAuditConfig;
import com.goggles.mentoring_service.domain._common.UserType;
import com.goggles.mentoring_service.domain.booking.BookingStatus;
import com.goggles.mentoring_service.domain.booking.MentoringBooking;
import com.goggles.mentoring_service.domain.booking.MentoringBookingId;
import com.goggles.mentoring_service.domain.booking.SessionSlot;
import com.goggles.mentoring_service.domain.booking.exception.BookingNotFoundException;
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
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(TestAuditConfig.class)
@ActiveProfiles("test")
@Transactional
class BookingServiceIntegrationTest {

  private static final Logger log = LoggerFactory.getLogger(BookingServiceIntegrationTest.class);

  private static final UUID OTHER_MENTEE_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000099");
  private static final UUID UNAUTHORIZED_USER_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000199");
  private static final String OTHER_MENTEE_NAME = "다른학생";
  private static final int PAGE_NUMBER = 0;
  private static final int PAGE_SIZE = 10;
  private static final LocalDate MENTORING_SESSION_DATE_2 =
      com.goggles.mentoring_service.domain.mentoring.MentoringFixture.SESSION_DATE_2;

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
        bookingRepository.findById(new MentoringBookingId(result.enrollmentId())).orElseThrow();

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

  // ── 상세 조회 ─────────────────────────────────────────────────────────────

  @Test
  void getBooking_byMentee_success() {
    UUID mentoringId = saveMentoringWithSession(SESSION_DATE_1);
    BookingResult.Create created =
        bookingService.createBooking(bookingCommand(mentoringId, sessionSlot(SESSION_DATE_1)));

    em.flush();
    em.clear();

    BookingResult.Detail result =
        bookingService.getBooking(created.enrollmentId(), MENTEE_ID, UserType.STUDENT);

    log.info("==== 멘티 예약 상세 조회 결과 ====");
    log.info("bookingId: {}", result.bookingId());
    log.info("menteeId : {}", result.mentee().menteeId());
    log.info("status   : {}", result.status());

    assertThat(result.bookingId()).isEqualTo(created.enrollmentId());
    assertThat(result.mentee().menteeId()).isEqualTo(MENTEE_ID);
    assertThat(result.status()).isEqualTo(BookingStatus.PENDING);
  }

  @Test
  void getBooking_byMentor_success() {
    UUID mentoringId = saveMentoringWithSession(SESSION_DATE_1);
    BookingResult.Create created =
        bookingService.createBooking(bookingCommand(mentoringId, sessionSlot(SESSION_DATE_1)));

    em.flush();
    em.clear();

    BookingResult.Detail result =
        bookingService.getBooking(created.enrollmentId(), MENTOR_ID, UserType.INSTRUCTOR);

    log.info("==== 멘토 예약 상세 조회 결과 ====");
    log.info("bookingId: {}", result.bookingId());
    log.info("status   : {}", result.status());

    assertThat(result.bookingId()).isEqualTo(created.enrollmentId());
    assertThat(result.status()).isEqualTo(BookingStatus.PENDING);
  }

  @Test
  void getBooking_notFound() {
    UUID unknownBookingId = UUID.randomUUID();

    assertThatThrownBy(
            () -> bookingService.getBooking(unknownBookingId, MENTEE_ID, UserType.STUDENT))
        .isInstanceOf(BookingNotFoundException.class);
  }

  @Test
  void getBooking_byOtherUser_forbidden() {
    UUID mentoringId = saveMentoringWithSession(SESSION_DATE_1);
    BookingResult.Create created =
        bookingService.createBooking(bookingCommand(mentoringId, sessionSlot(SESSION_DATE_1)));

    em.flush();
    em.clear();

    assertThatThrownBy(
            () -> bookingService.getBooking(created.enrollmentId(), UNAUTHORIZED_USER_ID, UserType.STUDENT))
        .isInstanceOf(ForbiddenException.class);
  }

  // ── 목록 조회 ─────────────────────────────────────────────────────────────

  @Test
  void getMyBookings_success() {
    UUID mentoringId = saveMentoringWithSessions(SESSION_DATE_1, MENTORING_SESSION_DATE_2);

    bookingService.createBooking(
        bookingCommand(mentoringId, sessionSlot(SESSION_DATE_1), MENTEE_ID, MENTEE_NAME));
    bookingService.createBooking(
        bookingCommand(
            mentoringId,
            sessionSlot(MENTORING_SESSION_DATE_2),
            OTHER_MENTEE_ID,
            OTHER_MENTEE_NAME));

    em.flush();
    em.clear();

    BookingSearchCondition condition =
        new BookingSearchCondition(MENTEE_ID, UserType.STUDENT, null, BookingSort.CREATED_AT_DESC);
    Page<BookingResult.Summary> result =
        bookingService.getMyBookings(condition, CommonPageRequest.of(PAGE_NUMBER, PAGE_SIZE));

    log.info("==== 내 예약 목록 조회 결과 ====");
    log.info("총 {}건", result.getTotalElements());

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().getFirst().menteeName()).isEqualTo(MENTEE_NAME);
  }

  @Test
  void getMyBookings_empty() {
    UUID mentoringId = saveMentoringWithSession(SESSION_DATE_1);
    bookingService.createBooking(
        bookingCommand(mentoringId, sessionSlot(SESSION_DATE_1), OTHER_MENTEE_ID, OTHER_MENTEE_NAME));

    em.flush();
    em.clear();

    BookingSearchCondition condition =
        new BookingSearchCondition(MENTEE_ID, UserType.STUDENT, null, BookingSort.CREATED_AT_DESC);
    Page<BookingResult.Summary> result =
        bookingService.getMyBookings(condition, CommonPageRequest.of(PAGE_NUMBER, PAGE_SIZE));

    assertThat(result.getContent()).isEmpty();
    assertThat(result.getTotalElements()).isZero();
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

  private UUID saveMentoringWithSessions(LocalDate firstDate, LocalDate secondDate) {
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
            .sessions(List.of(session(firstDate), session(secondDate)))
            .build();
    mentoringRepository.save(mentoring);

    return mentoring.getMentoringId().mentoringId();
  }

  private BookingCommand.Create bookingCommand(UUID mentoringId, SessionSlot slot) {
    MenteeInfo menteeInfo = new MenteeInfo(MENTEE_ID, UserType.STUDENT, MENTEE_NAME);
    return new BookingCommand.Create(menteeInfo, mentoringId, List.of(slot), REQUEST_MESSAGE, UUID.randomUUID());
  }

  private BookingCommand.Create bookingCommand(UUID mentoringId, SessionSlot slot, UUID menteeId, String menteeName) {
    MenteeInfo menteeInfo = new MenteeInfo(menteeId, UserType.STUDENT, menteeName);
    return new BookingCommand.Create(menteeInfo, mentoringId, List.of(slot), REQUEST_MESSAGE, null);
  }
}
