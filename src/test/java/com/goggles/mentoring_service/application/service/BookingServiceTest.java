package com.goggles.mentoring_service.application.service;

import static com.goggles.mentoring_service.domain.booking.BookingFixture.*;
import static com.goggles.mentoring_service.domain.mentoring.MentoringFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.goggles.mentoring_service.application.command.BookingCommand;
import com.goggles.mentoring_service.application.command.MenteeInfo;
import com.goggles.mentoring_service.application.result.BookingResult;
import com.goggles.mentoring_service.domain._common.UserType;
import com.goggles.mentoring_service.domain.booking.repository.MentoringBookingRepository;
import com.goggles.mentoring_service.domain.mentoring.Mentoring;
import com.goggles.mentoring_service.domain.mentoring.exception.MentoringNotFoundException;
import com.goggles.mentoring_service.domain.mentoring.repository.MentoringRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
    log.info("bookingId: {}", result.bookingId());

    assertThat(result.bookingId()).isNotNull();
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
}
