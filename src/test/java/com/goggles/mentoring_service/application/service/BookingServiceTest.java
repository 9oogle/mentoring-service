package com.goggles.mentoring_service.application.service;

import static com.goggles.mentoring_service.domain.booking.BookingFixture.*;
import static com.goggles.mentoring_service.domain.mentoring.MentoringFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.goggles.common.exception.ForbiddenException;
import com.goggles.common.pagination.CommonPageRequest;
import com.goggles.mentoring_service.application.command.BookingCommand;
import com.goggles.mentoring_service.application.command.MenteeInfo;
import com.goggles.mentoring_service.application.query.BookingSearchCondition;
import com.goggles.mentoring_service.application.query.BookingSort;
import com.goggles.mentoring_service.application.result.BookingResult;
import com.goggles.mentoring_service.domain._common.UserType;
import com.goggles.mentoring_service.domain.booking.MentoringBooking;
import com.goggles.mentoring_service.domain.booking.exception.BookingNotFoundException;
import com.goggles.mentoring_service.domain.booking.repository.MentoringBookingRepository;
import com.goggles.mentoring_service.domain.mentoring.Mentoring;
import com.goggles.mentoring_service.domain.mentoring.exception.MentoringNotFoundException;
import com.goggles.mentoring_service.domain.mentoring.repository.MentoringRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

  private static final Logger log = LoggerFactory.getLogger(BookingServiceTest.class);

  @InjectMocks private BookingService bookingService;

  @Mock private MentoringBookingRepository bookingRepository;

  @Mock private MentoringRepository mentoringRepository;

  @Test
  void createBooking_success() {
    Mentoring mentoring =
        defaultMentoringBuilder().sessions(List.of(session(SESSION_DATE_1))).build();
    given(mentoringRepository.findById(any())).willReturn(Optional.of(mentoring));

    BookingResult.Create result = bookingService.createBooking(defaultCommand(mentoring));

    log.info("==== 예약 생성 결과 ====");
    log.info("bookingId: {}", result.enrollmentId());

    assertThat(result.enrollmentId()).isNotNull();
    verify(bookingRepository).save(any());
  }

  @Test
  void createBooking_mentoring_not_found() {
    given(mentoringRepository.findById(any())).willReturn(Optional.empty());
    UUID unknownId = UUID.randomUUID();
    BookingCommand.Create command =
        new BookingCommand.Create(
            new MenteeInfo(MENTEE_ID, UserType.STUDENT, MENTEE_NAME),
            unknownId,
            List.of(sessionSlot()),
            REQUEST_MESSAGE, null);

    log.info("존재하지 않는 멘토링 예약 시도: {}", unknownId);
    assertThatThrownBy(() -> bookingService.createBooking(command))
        .isInstanceOf(MentoringNotFoundException.class);
  }

  @Test
  void createBooking_fails_if_instructor_books() {
    Mentoring mentoring =
        defaultMentoringBuilder().sessions(List.of(session(SESSION_DATE_1))).build();
    given(mentoringRepository.findById(any())).willReturn(Optional.of(mentoring));

    BookingCommand.Create command =
        new BookingCommand.Create(
            new MenteeInfo(UUID.randomUUID(), UserType.INSTRUCTOR, "이강사"),
            mentoring.getMentoringId().mentoringId(),
            List.of(sessionSlot()),
            REQUEST_MESSAGE,null);

    assertThatThrownBy(() -> bookingService.createBooking(command))
        .isInstanceOf(RuntimeException.class);
  }

  private BookingCommand.Create defaultCommand(Mentoring mentoring) {
    return new BookingCommand.Create(
        new MenteeInfo(MENTEE_ID, UserType.STUDENT, MENTEE_NAME),
        mentoring.getMentoringId().mentoringId(),
        List.of(sessionSlot(SESSION_DATE_1)),
        REQUEST_MESSAGE,null);
  }

  @Test
  void getBooking_byMentee_success() {
    MentoringBooking booking = pendingBooking();
    UUID bookingId = booking.getMentoringBookingId().bookingId();
    given(bookingRepository.findById(any())).willReturn(Optional.of(booking));

    BookingResult.Detail result = bookingService.getBooking(bookingId, MENTEE_ID, UserType.STUDENT);

    log.info("==== 예약 상세 조회 결과 ====");
    log.info("bookingId: {}", result.bookingId());
    log.info("menteeId : {}", result.mentee().menteeId());
    log.info("status   : {}", result.status());

    assertThat(result.bookingId()).isEqualTo(bookingId);
    assertThat(result.mentee().menteeId()).isEqualTo(MENTEE_ID);
    assertThat(result.status()).isEqualTo(booking.getStatus());
    verify(bookingRepository).findById(any());
  }

  @Test
  void getBooking_byMentor_success() {
    MentoringBooking booking = pendingBooking();
    UUID bookingId = booking.getMentoringBookingId().bookingId();
    given(bookingRepository.findById(any())).willReturn(Optional.of(booking));

    BookingResult.Detail result = bookingService.getBooking(bookingId, MENTOR_ID, UserType.INSTRUCTOR);

    assertThat(result.bookingId()).isEqualTo(bookingId);
    assertThat(result.status()).isEqualTo(booking.getStatus());
    verify(bookingRepository).findById(any());
  }

  @Test
  void getBooking_notFound() {
    UUID unknownBookingId = UUID.randomUUID();
    given(bookingRepository.findById(any())).willReturn(Optional.empty());

    assertThatThrownBy(
            () -> bookingService.getBooking(unknownBookingId, MENTEE_ID, UserType.STUDENT))
        .isInstanceOf(BookingNotFoundException.class);
  }

  @Test
  void getBooking_byOtherUser_forbidden() {
    MentoringBooking booking = pendingBooking();
    UUID bookingId = booking.getMentoringBookingId().bookingId();
    UUID otherUserId = UUID.randomUUID();
    given(bookingRepository.findById(any())).willReturn(Optional.of(booking));

    assertThatThrownBy(() -> bookingService.getBooking(bookingId, otherUserId, UserType.STUDENT))
        .isInstanceOf(ForbiddenException.class)
        .hasMessageContaining("접근 권한");
  }

  @Test
  void getMyBookings_success() {
    MentoringBooking booking1 = pendingBooking();
    MentoringBooking booking2 = pendingBooking();
    Page<MentoringBooking> page = new PageImpl<>(List.of(booking1, booking2));
    given(bookingRepository.findByUser(any(), any())).willReturn(page);

    BookingSearchCondition condition =
        new BookingSearchCondition(MENTEE_ID, UserType.STUDENT, null, BookingSort.CREATED_AT_DESC);
    Page<BookingResult.Summary> result =
        bookingService.getMyBookings(condition, CommonPageRequest.of(0, 10));

    log.info("==== 내 예약 목록 조회 결과 ====");
    log.info("총 {}건", result.getTotalElements());

    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getTotalElements()).isEqualTo(2);
    assertThat(result.getContent().getFirst().bookingId())
        .isEqualTo(booking1.getMentoringBookingId().bookingId());
    assertThat(result.getContent().getFirst().mentoringTitle()).isEqualTo(TITLE);
    assertThat(result.getContent().getFirst().mentorName()).isEqualTo(MENTOR_NAME);
    assertThat(result.getContent().getFirst().menteeName()).isEqualTo(MENTEE_NAME);
    verify(bookingRepository).findByUser(any(), any());
  }

  @Test
  void getMyBookings_empty() {
    Page<MentoringBooking> emptyPage = new PageImpl<>(List.of());
    given(bookingRepository.findByUser(any(), any())).willReturn(emptyPage);

    BookingSearchCondition condition =
        new BookingSearchCondition(MENTEE_ID, UserType.STUDENT, null, BookingSort.CREATED_AT_DESC);
    Page<BookingResult.Summary> result =
        bookingService.getMyBookings(condition, CommonPageRequest.of(0, 10));

    assertThat(result.getContent()).isEmpty();
    assertThat(result.getTotalElements()).isZero();
    verify(bookingRepository).findByUser(any(), any());
  }
}
