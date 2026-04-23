package com.goggles.mentoring_service.domain.booking;

import com.goggles.mentoring_service.domain.booking.exception.InvalidBookingStatusTransitionException;

public enum BookingStatus {
	PENDING, PAYMENT_COMPLETED, PAYMENT_FAILED, ACCEPTED, REJECTED, CANCELED;

	private boolean canTransitionTo(BookingStatus newStatus) {
		return switch (this) {
			case PENDING -> newStatus == PAYMENT_COMPLETED || newStatus == PAYMENT_FAILED ||
					newStatus == CANCELED;
			case PAYMENT_COMPLETED ->
					newStatus == ACCEPTED || newStatus == REJECTED || newStatus == CANCELED;
			case ACCEPTED -> newStatus == CANCELED;
			default -> false;
		};
	}

	public void checkTransitionValidation(BookingStatus newStatus) {
		if (!canTransitionTo(newStatus)) {
			if (this == PAYMENT_FAILED) {
				throw InvalidBookingStatusTransitionException.cannotModifyPaymentFailed();
			}
			throw switch (newStatus) {
				case PAYMENT_COMPLETED -> InvalidBookingStatusTransitionException.cannotCompletePayment(this);
				case ACCEPTED -> InvalidBookingStatusTransitionException.cannotAccept(this);
				case REJECTED, CANCELED -> InvalidBookingStatusTransitionException.alreadyClosedBooking(this);
				default -> throw new IllegalStateException("정의되지 않은 상태 전이입니다: " + newStatus);
			};
		}
	}

}