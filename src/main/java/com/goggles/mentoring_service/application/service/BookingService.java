package com.goggles.mentoring_service.application.service;

import com.goggles.common.exception.ForbiddenException;
import com.goggles.common.pagination.CommonPageRequest;
import com.goggles.mentoring_service.application.command.BookingCommand;
import com.goggles.mentoring_service.application.command.MenteeInfo;
import com.goggles.mentoring_service.application.query.BookingQuery;
import com.goggles.mentoring_service.application.result.BookingResult;
import com.goggles.mentoring_service.domain._common.UserType;
import com.goggles.mentoring_service.domain.booking.*;
import com.goggles.mentoring_service.domain.booking.event.BookingEvent;
import com.goggles.mentoring_service.domain.booking.exception.BookingNotFoundException;
import com.goggles.mentoring_service.domain.booking.repository.MentoringBookingRepository;
import com.goggles.mentoring_service.domain.mentoring.Mentoring;
import com.goggles.mentoring_service.domain.mentoring.MentoringId;
import com.goggles.mentoring_service.domain.mentoring.exception.MentoringNotFoundException;
import com.goggles.mentoring_service.domain.mentoring.repository.MentoringRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

	private final MentoringBookingRepository bookingRepository;
	private final MentoringRepository mentoringRepository;
	private final BookingEvent events;

	@Transactional
	public BookingResult.Create createBooking(BookingCommand.Create command) {
		MentoringId mentoringId = new MentoringId(command.mentoringId());
		Mentoring mentoring = mentoringRepository.findById(mentoringId)
				.orElseThrow(() -> new MentoringNotFoundException(mentoringId));

		MenteeInfo menteeInfo = command.menteeInfo();
		List<SessionSlot> slots = command.sessionSlots();
		mentoring.bookSession(slots);
		MentoringBooking booking =
				MentoringBooking.create(menteeInfo.menteeId(), menteeInfo.menteeUserType(),
						menteeInfo.menteeName(), mentoring, slots, command.requestMessage(),
						command.orderId());
		bookingRepository.save(booking);
		return BookingResult.Create.of(booking, mentoring);
	}

	@Transactional(readOnly = true)
	public BookingResult.Detail getBooking(UUID bookingId, UUID userId, UserType userType) {
		MentoringBooking booking = bookingRepository.findById(new MentoringBookingId(bookingId))
				.orElseThrow(() -> new BookingNotFoundException(new MentoringBookingId(bookingId)));

		checkAccess(booking, userId, userType);

		return BookingResult.Detail.from(booking);
	}

	@Transactional(readOnly = true)
	public Page<BookingResult.Summary> getMyBookings(BookingQuery.GetMyBookings query,
			CommonPageRequest pageRequest) {
		BookingSearchCondition condition =
				new BookingSearchCondition(query.userId(), query.userType(), query.status(),
						query.sort());
		return bookingRepository.findByUser(condition, pageRequest.toPageable(Sort.unsorted()))
				.map(BookingResult.Summary::from);
	}

	@Transactional
	public void paymentCompleted(BookingCommand.PaymentCompleted command) {
		MentoringBooking booking = bookingRepository.findById(command.mentoringBookingId())
				.orElseThrow(() -> new BookingNotFoundException(command.mentoringBookingId()));
		booking.completePayment(events);
		bookingRepository.save(booking);
	}

	@Transactional
	public void paymentFailed(BookingCommand.PaymentFailed command) {
		MentoringBooking booking = bookingRepository.findById(command.mentoringBookingId())
				.orElseThrow(() -> new BookingNotFoundException(command.mentoringBookingId()));
		booking.failPayment(command.failureReason(), LocalDateTime.now(), events);
		bookingRepository.save(booking);
	}

	@Transactional
	public void acceptBooking(BookingCommand.Accept command) {
		MentoringBookingId id = new MentoringBookingId(command.bookingId());
		MentoringBooking booking = bookingRepository.findById(id)
				.orElseThrow(() -> new BookingNotFoundException(id));
		booking.accept(command.userId(), command.userType(), events);
	}

	@Transactional
	public void rejectBooking(BookingCommand.Reject command) {
		MentoringBookingId id = new MentoringBookingId(command.bookingId());
		MentoringBooking booking = bookingRepository.findById(id)
				.orElseThrow(() -> new BookingNotFoundException(id));
		booking.reject(command.userId(), command.userType(), command.reason(), LocalDateTime.now(), events);
	}

	@Transactional
	public void cancelBooking(BookingCommand.Cancel command) {
		MentoringBookingId id = new MentoringBookingId(command.bookingId());
		MentoringBooking booking = bookingRepository.findById(id)
				.orElseThrow(() -> new BookingNotFoundException(id));
		booking.cancel(command.userId(), command.userType(), command.reason(), LocalDateTime.now(), events);
	}


	@Transactional(readOnly = true)
	public BookingResult.SessionList getBookingSessions(UUID bookingId, UUID userId,
			UserType userType) {
		MentoringBooking booking = findBooking(bookingId);
		checkAccess(booking, userId, userType);
		return BookingResult.SessionList.from(booking);
	}


	@Transactional
	public void completeSession(BookingCommand.CompleteSession command) {
		MentoringBooking booking = findBooking(command.bookingId());
		booking.completeSession(command.sessionId(), command.userId(), command.userType());
	}

	@Transactional
	public void rescheduleSession(BookingCommand.RescheduleSession command) {
		MentoringBooking booking = findBooking(command.bookingId());
		MentoringId mentoringId = new MentoringId(booking.getBookedMentoring()
				.getMentoringId());
		Mentoring mentoring = mentoringRepository.findById(mentoringId)
				.orElseThrow(() -> new MentoringNotFoundException(mentoringId));

		LocalTime newEndTime = mentoring.getSlotEndTime(command.newDate(), command.newStartTime());
		SessionReschedule reschedule =
				booking.rescheduleSession(command.sessionId(), command.newDate(),
						command.newStartTime(), newEndTime, command.userId(), command.userType(),
						LocalDateTime.now());

		mentoring.unbookSession(reschedule.oldSlot()
				.date(), reschedule.oldSlot()
				.startTime());
		mentoring.bookSession(command.newDate(), command.newStartTime());
	}

	private MentoringBooking findBooking(UUID bookingId) {
		return bookingRepository.findById(new MentoringBookingId(bookingId))
				.orElseThrow(() -> new BookingNotFoundException(new MentoringBookingId(bookingId)));
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
	public void paymentFailed(BookingCommand.PaymentFailed command, BookingEvent events) {
		MentoringBooking booking = bookingRepository.findById(command.mentoringBookingId())
				.orElseThrow(() -> new BookingNotFoundException(command.mentoringBookingId()));
		booking.failPayment(command.failureReason(), LocalDateTime.now(), events);
		bookingRepository.save(booking);
	}
}
