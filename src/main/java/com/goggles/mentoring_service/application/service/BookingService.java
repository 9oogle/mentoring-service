package com.goggles.mentoring_service.application.service;

import com.goggles.mentoring_service.application.command.BookingCommand;
import com.goggles.mentoring_service.application.command.MenteeInfo;
import com.goggles.mentoring_service.application.result.BookingResult;
import com.goggles.mentoring_service.domain.booking.MentoringBooking;
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
import lombok.RequiredArgsConstructor;
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

  @Transactional
  public void paymentFailed(BookingCommand.PaymentFailed command) {
    MentoringBooking booking =
            bookingRepository.findById(command.mentoringBookingId())
                    .orElseThrow(() -> new BookingNotFoundException(command.mentoringBookingId()));
    booking.failPayment(command.failureReason(), LocalDateTime.now());
    bookingRepository.save(booking);
  }
}
