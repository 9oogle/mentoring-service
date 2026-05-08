package com.goggles.mentoring_service.domain.booking.exception;

import com.goggles.mentoring_service.domain._common.MentoringValidationException;
import com.goggles.mentoring_service.domain.booking.BookingStatus;

public class InvalidBookingStatusTransitionException extends MentoringValidationException {

	private InvalidBookingStatusTransitionException(String message) {
		super(message);
	}

	public static InvalidBookingStatusTransitionException cannotCompletePayment(
			BookingStatus current) {
		return new InvalidBookingStatusTransitionException(
				String.format("결제 완료 처리는 PENDING 상태에서만 가능합니다. 현재 상태: %s", current));
	}

	public static InvalidBookingStatusTransitionException cannotAccept(BookingStatus current) {
		return new InvalidBookingStatusTransitionException(
				String.format("예약 승인은 PAYMENT_COMPLETED 상태에서만 가능합니다. 현재 상태: %s", current));
	}

	public static InvalidBookingStatusTransitionException cannotModifyPaymentFailed() {
		return new InvalidBookingStatusTransitionException("결제 실패한 예약은 변경할 수 없습니다.");
	}

	public static InvalidBookingStatusTransitionException cannotReject(BookingStatus current) {
		return new InvalidBookingStatusTransitionException(
				String.format("예약 거절은 PAYMENT_COMPLETED 상태에서만 가능합니다. 현재 상태: %s", current));
	}

	public static InvalidBookingStatusTransitionException alreadyClosedBooking(
			BookingStatus current) {
		return new InvalidBookingStatusTransitionException(
				String.format("이미 종료된 예약은 변경할 수 없습니다. 현재 상태: %s", current));
	}

	public static InvalidBookingStatusTransitionException cannotCancelByOrder(BookingStatus current) {
		return new InvalidBookingStatusTransitionException(
				String.format("주문 취소는 PAYMENT_COMPLETED 또는 ACCEPTED 상태에서만 가능합니다. 현재 상태: %s",
						current));
	}
}