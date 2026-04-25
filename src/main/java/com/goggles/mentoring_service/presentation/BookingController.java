package com.goggles.mentoring_service.presentation;

import com.goggles.mentoring_service.application.command.BookingCommand;
import com.goggles.mentoring_service.application.result.BookingResult;
import com.goggles.mentoring_service.application.service.BookingService;
import com.goggles.mentoring_service.domain.booking.BookingStatus;
import com.goggles.mentoring_service.presentation.dto.BookingRequest;
import com.goggles.mentoring_service.presentation.dto.BookingResponse;
import com.goggles.mentoring_service.presentation.support.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mentoring-bookings")
@RequiredArgsConstructor
public class BookingController {

  private final BookingService bookingService;

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public BookingResponse.Create createBooking(
      UserContext userContext, @Valid @RequestBody BookingRequest.Create request) {
    BookingCommand.Create command = request.toCommand(userContext);
    BookingResult.Create result = bookingService.createBooking(command);
    return BookingResponse.Create.of(result);
  }

  @GetMapping("/{bookingId}")
  public BookingResponse.Detail getBooking(
      UserContext userContext, @PathVariable UUID bookingId) {
    BookingResult.Detail result =
        bookingService.getBooking(bookingId, userContext.userId(), userContext.userType());
    return BookingResponse.Detail.of(result);
  }
}