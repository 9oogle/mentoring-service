package com.goggles.mentoring_service.application.service;

import com.goggles.common.exception.ForbiddenException;
import com.goggles.common.pagination.CommonPageRequest;
import com.goggles.mentoring_service.application.command.BookingCommand;
import com.goggles.mentoring_service.application.command.MenteeInfo;
import com.goggles.mentoring_service.application.query.BookingSearchCondition;
import com.goggles.mentoring_service.application.result.BookingResult;
import com.goggles.mentoring_service.domain._common.UserType;
import com.goggles.mentoring_service.domain.booking.BookedMentoring;
import com.goggles.mentoring_service.domain.booking.Mentee;
import com.goggles.mentoring_service.domain.booking.MentoringBooking;
import com.goggles.mentoring_service.domain.booking.MentoringBookingId;
import com.goggles.mentoring_service.domain.booking.SessionSlot;
import com.goggles.mentoring_service.domain.booking.exception.BookingNotFoundException;
import com.goggles.mentoring_service.domain.booking.repository.MentoringBookingRepository;
import com.goggles.mentoring_service.domain.mentoring.Mentoring;
import com.goggles.mentoring_service.domain.mentoring.MentoringId;
import com.goggles.mentoring_service.domain.mentoring.exception.MentoringNotFoundException;
import com.goggles.mentoring_service.domain.mentoring.repository.MentoringRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingService {

  private final MentoringBookingRepository bookingRepository;
  private final MentoringRepository mentoringRepository;

  @Transactional
  public BookingResult.Create createBooking(BookingCommand.Create command) {
    MentoringId mentoringId = new MentoringId(command.mentoringId());
    Mentoring mentoring =
        mentoringRepository
            .findById(mentoringId)
            .orElseThrow(() -> new MentoringNotFoundException(mentoringId));

    MenteeInfo menteeInfo = command.menteeInfo();
    List<SessionSlot> slots = command.sessionSlots();
    mentoring.bookSession(slots);
    MentoringBooking booking =
        MentoringBooking.create(
            menteeInfo.menteeId(), menteeInfo.menteeUserType(), menteeInfo.menteeName(),
            mentoring, slots, command.requestMessage(), command.orderId());
    bookingRepository.save(booking);
    return BookingResult.Create.of(booking, mentoring);
  }

  @Transactional(readOnly = true)
  public BookingResult.Detail getBooking(UUID bookingId, UUID userId, UserType userType) {
    MentoringBooking booking =
        bookingRepository
            .findById(new MentoringBookingId(bookingId))
            .orElseThrow(() -> new BookingNotFoundException(new MentoringBookingId(bookingId)));

    checkAccess(booking, userId, userType);

    return BookingResult.Detail.from(booking);
  }

  @Transactional(readOnly = true)
  public Page<BookingResult.Summary> getMyBookings(
      BookingSearchCondition condition, CommonPageRequest pageRequest) {
    return bookingRepository
        .findByUser(condition, pageRequest.toPageable(Sort.unsorted()))
        .map(BookingResult.Summary::from);
  }

  private void checkAccess(MentoringBooking booking, UUID userId, UserType userType) {
    boolean isMentee = userType == UserType.STUDENT && booking.getMentee()
            .isMentee(userId);
    boolean isMentor = userType == UserType.INSTRUCTOR && booking.getBookedMentoring()
            .isMentor(userId);
    if (!isMentee && !isMentor) {
      throw new ForbiddenException("해당 예약에 접근 권한이 없습니다.");
    }
  }

  @Transactional
  public void paymentFailed(BookingCommand.PaymentFailed command) {
    MentoringBooking booking =
            bookingRepository.findById(command.mentoringBookingId())
                    .orElseThrow(() -> new BookingNotFoundException(command.mentoringBookingId()));
    booking.failPayment(command.failureReason(), LocalDateTime.now());
    bookingRepository.save(booking);
  }
}
