package com.goggles.mentoring_service.presentation;

import com.goggles.mentoring_service.application.command.BookingCommand;
import com.goggles.mentoring_service.application.result.BookingResult;
import com.goggles.mentoring_service.application.service.BookingService;
import com.goggles.mentoring_service.domain._common.UserType;
import com.goggles.mentoring_service.presentation.dto.BookingRequest;
import com.goggles.mentoring_service.presentation.dto.BookingResponse;
import com.goggles.mentoring_service.presentation.support.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/internal/v1/")
@RequiredArgsConstructor
public class InternalMentoringController {
	private final BookingService bookingService;

	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping("mentoring-booking")
	public BookingResponse.Create processMentoringBooking(UserContext userContext,
			@Valid @RequestBody BookingRequest.Create request) {
		BookingCommand.Create command = request.toCommand(userContext);
		BookingResult.Create result = bookingService.createBooking(command);
		return BookingResponse.Create.of(result);
	}

	@PatchMapping("mentoring-booking/{bookingId}/rollback")
	public void rollbackMentoringBooking(@PathVariable UUID bookingId,
			@Valid @RequestBody BookingRequest.Rollback request) {
		bookingService.rollbackBooking(request.toCommand(bookingId));
	}

	@PatchMapping("mentoring-booking/{bookingId}/cancellation")
	public void cancelMentoringBooking(@PathVariable UUID bookingId,
			@RequestHeader("X-User-Id") UUID userId,
			@RequestHeader("X-User-Role") UserType userType,
			@Valid @RequestBody BookingRequest.Cancellation request) {
		bookingService.cancelBookingByOrder(request.toCommand(bookingId, userId, userType));
	}

	@PatchMapping("mentoring-booking/payment-completed")
	public void processMentoringBookingPaymentCompleted(
			@Valid @RequestBody BookingRequest.PaymentCompleted request) {
		bookingService.paymentCompleted(request.toCommand());
	}

	@PatchMapping("mentoring-booking/payment-failed")
	public void processMentoringBookingPaymentFailed(
			@Valid @RequestBody BookingRequest.PaymentFailed request) {
		BookingCommand.PaymentFailed command = request.toCommand();
		bookingService.paymentFailed(command);
	}
}
