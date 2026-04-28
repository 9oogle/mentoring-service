package com.goggles.mentoring_service.domain.booking;

import static com.goggles.mentoring_service.domain.booking.BookingFixture.*;
import static com.goggles.mentoring_service.domain.mentoring.MentoringFixture.MENTOR_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.goggles.mentoring_service.domain._common.UserType;
import com.goggles.mentoring_service.domain.booking.exception.CancellationDeadlineExceededException;
import com.goggles.mentoring_service.domain.booking.exception.CancellationReasonRequiredException;
import com.goggles.mentoring_service.domain.booking.exception.InvalidBookingStatusTransitionException;
import com.goggles.mentoring_service.domain.booking.exception.UnauthorizedBookingAccessException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MentoringBookingTest {

  @Test
  void create_success() {
    MentoringBooking booking = pendingBooking();

    assertThat(booking.getMentoringBookingId()).isNotNull();
    assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING);
  }

  @Test
  void create_with_multiple_sessions() {
    MentoringBooking booking =
        MentoringBooking.create(
            MENTEE_ID, UserType.STUDENT, MENTEE_NAME,
            mentoring(), List.of(sessionSlot(), sessionSlot2()), REQUEST_MESSAGE, UUID.randomUUID());

    assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING);
  }

  @Test
  void mentee_must_be_student() {
    UUID instructorId = UUID.randomUUID();

    assertThatThrownBy(
            () ->
                MentoringBooking.create(
                    instructorId, UserType.INSTRUCTOR, "이강사",
                    mentoring(), List.of(sessionSlot()), REQUEST_MESSAGE, UUID.randomUUID()))
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  void completePayment_success() {
    MentoringBooking booking = pendingBooking();
    booking.completePayment();

    assertThat(booking.getStatus()).isEqualTo(BookingStatus.PAYMENT_COMPLETED);
  }

  @Test
  void failPayment_success() {
    MentoringBooking booking = pendingBooking();
    booking.failPayment( "결제 실패 사유");

    assertThat(booking.getStatus()).isEqualTo(BookingStatus.PAYMENT_FAILED);
  }

  @Test
  void completePayment_from_invalid_status() {
    MentoringBooking booking = paymentCompletedBooking();

    assertThatThrownBy(booking::completePayment)
        .isInstanceOf(InvalidBookingStatusTransitionException.class);
  }

  @Test
  void accept_success() {
    MentoringBooking booking = paymentCompletedBooking();
    booking.accept(MENTOR_ID, UserType.INSTRUCTOR);

    assertThat(booking.getStatus()).isEqualTo(BookingStatus.ACCEPTED);
  }

  @Test
  void accept_by_non_mentor() {
    MentoringBooking booking = paymentCompletedBooking();

    assertThatThrownBy(() -> booking.accept(UUID.randomUUID(), UserType.INSTRUCTOR))
        .isInstanceOf(UnauthorizedBookingAccessException.class);
  }

  @Test
  void accept_by_student() {
    MentoringBooking booking = paymentCompletedBooking();

    assertThatThrownBy(() -> booking.accept(MENTEE_ID, UserType.STUDENT))
        .isInstanceOf(UnauthorizedBookingAccessException.class);
  }

  @Test
  void accept_from_pending_status() {
    MentoringBooking booking = pendingBooking();

    assertThatThrownBy(() -> booking.accept(MENTOR_ID, UserType.INSTRUCTOR))
        .isInstanceOf(InvalidBookingStatusTransitionException.class);
  }

  @Test
  void reject_success() {
    MentoringBooking booking = paymentCompletedBooking();
    booking.reject(MENTOR_ID, UserType.INSTRUCTOR, REJECT_REASON, LocalDateTime.now());

    assertThat(booking.getStatus()).isEqualTo(BookingStatus.REJECTED);
    assertThat(booking.getCloseReason()).isEqualTo(REJECT_REASON);
    assertThat(booking.getClosedBy()).isEqualTo(MENTOR_ID);
  }

  @Test
  void reject_without_reason() {
    MentoringBooking booking = paymentCompletedBooking();

    assertThatThrownBy(
            () -> booking.reject(MENTOR_ID, UserType.INSTRUCTOR, "", LocalDateTime.now()))
        .isInstanceOf(CancellationReasonRequiredException.class);
  }

  @Test
  void reject_by_non_mentor() {
    MentoringBooking booking = paymentCompletedBooking();

    assertThatThrownBy(
            () ->
                booking.reject(
                    UUID.randomUUID(), UserType.INSTRUCTOR, REJECT_REASON, LocalDateTime.now()))
        .isInstanceOf(UnauthorizedBookingAccessException.class);
  }

  @Test
  void cancel_by_mentee_success() {
    MentoringBooking booking = paymentCompletedBooking();
    booking.cancel(MENTEE_ID, UserType.STUDENT, CANCEL_REASON, BEFORE_DEADLINE);

    assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELED);
    assertThat(booking.getClosedBy()).isEqualTo(MENTEE_ID);
  }

  @Test
  void cancel_by_mentor_success() {
    MentoringBooking booking = acceptedBooking();
    booking.cancel(MENTOR_ID, UserType.INSTRUCTOR, CANCEL_REASON, BEFORE_DEADLINE);

    assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELED);
  }

  @Test
  void cancel_without_reason() {
    MentoringBooking booking = paymentCompletedBooking();

    assertThatThrownBy(() -> booking.cancel(MENTEE_ID, UserType.STUDENT, "", BEFORE_DEADLINE))
        .isInstanceOf(CancellationReasonRequiredException.class);
  }

  @Test
  void cancel_after_deadline() {
    MentoringBooking booking = paymentCompletedBooking();

    assertThatThrownBy(
            () -> booking.cancel(MENTEE_ID, UserType.STUDENT, CANCEL_REASON, AFTER_DEADLINE))
        .isInstanceOf(CancellationDeadlineExceededException.class);
  }

  @Test
  void cancel_by_unauthorized_user() {
    MentoringBooking booking = paymentCompletedBooking();

    assertThatThrownBy(
            () ->
                booking.cancel(UUID.randomUUID(), UserType.STUDENT, CANCEL_REASON, BEFORE_DEADLINE))
        .isInstanceOf(UnauthorizedBookingAccessException.class);
  }

  @Test
  void cancel_already_rejected() {
    MentoringBooking booking = paymentCompletedBooking();
    booking.reject(MENTOR_ID, UserType.INSTRUCTOR, REJECT_REASON, LocalDateTime.now());

    assertThatThrownBy(
            () -> booking.cancel(MENTEE_ID, UserType.STUDENT, CANCEL_REASON, BEFORE_DEADLINE))
        .isInstanceOf(InvalidBookingStatusTransitionException.class);
  }
}
