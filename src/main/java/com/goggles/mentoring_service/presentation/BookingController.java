package com.goggles.mentoring_service.presentation;

import com.goggles.common.pagination.CommonPageRequest;
import com.goggles.common.pagination.CommonPageResponse;
import com.goggles.mentoring_service.application.command.BookingCommand;
import com.goggles.mentoring_service.application.result.BookingResult;
import com.goggles.mentoring_service.application.service.BookingService;
import com.goggles.mentoring_service.domain.booking.BookingSearchCondition;
import com.goggles.mentoring_service.domain.booking.BookingSort;
import com.goggles.mentoring_service.domain.booking.BookingStatus;
import com.goggles.mentoring_service.presentation.dto.BookingRequest;
import com.goggles.mentoring_service.presentation.dto.BookingResponse;
import com.goggles.mentoring_service.presentation.support.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/mentoring-bookings")
@RequiredArgsConstructor
public class BookingController {

	private final BookingService bookingService;

	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping
	public BookingResponse.Create createBooking(UserContext userContext,
			@Valid @RequestBody BookingRequest.Create request) {
		BookingCommand.Create command = request.toCommand(userContext);
		BookingResult.Create result = bookingService.createBooking(command);
		return BookingResponse.Create.of(result);
	}

	@GetMapping("/{bookingId}")
	public BookingResponse.Detail getBooking(UserContext userContext,
			@PathVariable UUID bookingId) {
		BookingResult.Detail result =
				bookingService.getBooking(bookingId, userContext.userId(), userContext.userType());
		return BookingResponse.Detail.of(result);
	}

	@GetMapping
	public CommonPageResponse<BookingResponse.Summary> getMyBookings(UserContext userContext,
			@RequestParam(required = false) BookingStatus status,
			@RequestParam(required = false) BookingSort sort, CommonPageRequest pageRequest) {
		BookingSearchCondition condition =
				new BookingSearchCondition(userContext.userId(), userContext.userType(), status,
						sort);
		Page<BookingResult.Summary> page = bookingService.getMyBookings(condition, pageRequest);
		return CommonPageResponse.of(page.map(BookingResponse.Summary::of));
	}

	@ResponseStatus(HttpStatus.OK)
	@PostMapping("/{bookingId}/acceptance")
	public void acceptBooking(UserContext userContext, @PathVariable UUID bookingId) {
		BookingCommand.Accept command =
				new BookingCommand.Accept(bookingId, userContext.userId(), userContext.userType());
		bookingService.acceptBooking(command);
	}

	@ResponseStatus(HttpStatus.OK)
	@PostMapping("/{bookingId}/rejection")
	public void rejectBooking(UserContext userContext, @PathVariable UUID bookingId,
			@Valid @RequestBody BookingRequest.Reject request) {
		BookingCommand.Reject command = request.toCommand(bookingId, userContext);
		bookingService.rejectBooking(command);
	}

	@ResponseStatus(HttpStatus.OK)
	@PostMapping("/{bookingId}/cancellation")
	public void cancelBooking(UserContext userContext, @PathVariable UUID bookingId,
			@Valid @RequestBody BookingRequest.Cancel request) {
		BookingCommand.Cancel command = request.toCommand(bookingId, userContext);
		bookingService.cancelBooking(command);
	}
}
