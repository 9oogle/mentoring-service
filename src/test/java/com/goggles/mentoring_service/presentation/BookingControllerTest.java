package com.goggles.mentoring_service.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goggles.common.exception.ForbiddenException;
import com.goggles.common.pagination.CommonPageRequestArgumentResolver;
import com.goggles.mentoring_service.application.result.BookingResult;
import com.goggles.mentoring_service.application.service.BookingService;
import com.goggles.mentoring_service.domain._common.UserType;
import com.goggles.mentoring_service.domain.booking.MentoringBookingId;
import com.goggles.mentoring_service.domain.booking.exception.BookingNotFoundException;
import com.goggles.mentoring_service.domain.booking.exception.InvalidBookingStatusTransitionException;
import com.goggles.mentoring_service.domain.booking.exception.UnauthorizedBookingAccessException;
import com.goggles.mentoring_service.domain.mentoring.MentoringId;
import com.goggles.mentoring_service.domain.mentoring.exception.MentoringNotFoundException;
import com.goggles.mentoring_service.infrastructure.config.WebMvcConfig;
import com.goggles.mentoring_service.presentation.dto.BookingRequest;
import com.goggles.mentoring_service.presentation.support.TestHeaders;
import com.goggles.mentoring_service.presentation.support.UserContextArgumentResolver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.goggles.mentoring_service.domain.booking.BookingFixture.*;
import static com.goggles.mentoring_service.domain.mentoring.MentoringFixture.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@Import({WebMvcConfig.class, UserContextArgumentResolver.class,
		BookingControllerTest.PageResolverConfig.class})
@MockitoBean(types = JpaMetamodelMappingContext.class)
class BookingControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@MockitoBean
	private BookingService bookingService;

	@Test
	void createBooking_success() throws Exception {
		UUID bookingId = UUID.randomUUID();
		given(bookingService.createBooking(any())).willReturn(
				new BookingResult.Create(bookingId, MENTORING_ID, TITLE, PRICE, MENTOR_ID,
						MENTOR_NAME));

		mockMvc.perform(post("/api/v1/mentoring-bookings").contentType(MediaType.APPLICATION_JSON)
						.headers(TestHeaders.headersFor(UserType.STUDENT))
						.content(objectMapper.writeValueAsString(defaultRequest())))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.enrollmentId").value(bookingId.toString()));
	}

	@Test
	void createBooking_fails_without_mentoringId() throws Exception {
		String body = """
				{"bookingTimeSlots":[{"date":"2026-06-01","startTime":"10:00:00","endTime":"11:00:00"}]}
				""";
		mockMvc.perform(post("/api/v1/mentoring-bookings").contentType(MediaType.APPLICATION_JSON)
						.headers(TestHeaders.headersFor(UserType.STUDENT))
						.content(body))
				.andExpect(status().isBadRequest());
	}

	@Test
	void createBooking_fails_with_empty_slots() throws Exception {
		BookingRequest.Create request =
				new BookingRequest.Create(UUID.randomUUID(), List.of(), null, null);

		mockMvc.perform(post("/api/v1/mentoring-bookings").contentType(MediaType.APPLICATION_JSON)
						.headers(TestHeaders.headersFor(UserType.STUDENT))
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void createBooking_mentoring_not_found() throws Exception {
		MentoringId mentoringId = new MentoringId(UUID.randomUUID());
		given(bookingService.createBooking(any())).willThrow(
				new MentoringNotFoundException(mentoringId));

		mockMvc.perform(post("/api/v1/mentoring-bookings").contentType(MediaType.APPLICATION_JSON)
						.headers(TestHeaders.headersFor(UserType.STUDENT))
						.content(objectMapper.writeValueAsString(defaultRequest())))
				.andDo(print())
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("Not Found"));
	}

	@Test
	void getBooking_success() throws Exception {
		BookingResult.Detail detail = bookingDetail();
		given(bookingService.getBooking(any(), any(), any())).willReturn(detail);

		mockMvc.perform(get("/api/v1/mentoring-bookings/{bookingId}", detail.bookingId()).headers(
						TestHeaders.headersFor(UserType.STUDENT)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.bookingId").value(detail.bookingId()
						.toString()))
				.andExpect(jsonPath("$.status").value(detail.status()
						.name()))
				.andExpect(jsonPath("$.mentoring.title").value(detail.mentoring()
						.title()))
				.andExpect(jsonPath("$.mentee.menteeId").value(detail.mentee()
						.menteeId()
						.toString()))
				.andExpect(jsonPath("$.requestMessage").value(detail.requestMessage()))
				.andExpect(jsonPath("$.bookedTimes").isArray());
	}

	@Test
	void getBooking_notFound() throws Exception {
		UUID bookingId = UUID.randomUUID();
		given(bookingService.getBooking(any(), any(), any())).willThrow(
				new BookingNotFoundException(new MentoringBookingId(bookingId)));

		mockMvc.perform(get("/api/v1/mentoring-bookings/{bookingId}", bookingId).headers(
						TestHeaders.headersFor(UserType.STUDENT)))
				.andDo(print())
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("Not Found"));
	}

	@Test
	void getBooking_forbidden() throws Exception {
		UUID bookingId = UUID.randomUUID();
		given(bookingService.getBooking(any(), any(), any())).willThrow(
				new ForbiddenException("해당 예약에 접근 권한이 없습니다."));

		mockMvc.perform(get("/api/v1/mentoring-bookings/{bookingId}", bookingId).headers(
						TestHeaders.headersFor(UserType.STUDENT)))
				.andDo(print())
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.title").value("Forbidden"));
	}

	@Test
	void getMyBookings_success() throws Exception {
		BookingResult.Summary summary = bookingSummary();
		Page<BookingResult.Summary> page = new PageImpl<>(List.of(summary));
		given(bookingService.getMyBookings(any(), any())).willReturn(page);

		mockMvc.perform(
						get("/api/v1/mentoring-bookings").headers(TestHeaders.headersFor(UserType.STUDENT))
								.param("page", "0")
								.param("size", "10"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(1))
				.andExpect(jsonPath("$.content[0].bookingId").value(summary.bookingId()
						.toString()))
				.andExpect(jsonPath("$.content[0].status").value(summary.status()
						.name()))
				.andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.first").value(true))
				.andExpect(jsonPath("$.last").value(true));
	}

	@Test
	void getMyBookings_empty() throws Exception {
		Page<BookingResult.Summary> emptyPage = new PageImpl<>(List.of());
		given(bookingService.getMyBookings(any(), any())).willReturn(emptyPage);

		mockMvc.perform(
						get("/api/v1/mentoring-bookings").headers(TestHeaders.headersFor(UserType.STUDENT))
								.param("page", "0")
								.param("size", "10"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(0))
				.andExpect(jsonPath("$.totalElements").value(0));
	}

	@Test
	void acceptBooking_success() throws Exception {
		UUID bookingId = UUID.randomUUID();
		willDoNothing().given(bookingService)
				.acceptBooking(any());

		mockMvc.perform(
						post("/api/v1/mentoring-bookings/{bookingId}/acceptance", bookingId).headers(
								TestHeaders.headersFor(UserType.INSTRUCTOR)))
				.andDo(print())
				.andExpect(status().isOk());
	}

	@Test
	void acceptBooking_booking_not_found() throws Exception {
		UUID bookingId = UUID.randomUUID();
		willThrow(new BookingNotFoundException(new MentoringBookingId(bookingId))).given(
						bookingService)
				.acceptBooking(any());

		mockMvc.perform(
						post("/api/v1/mentoring-bookings/{bookingId}/acceptance", bookingId).headers(
								TestHeaders.headersFor(UserType.INSTRUCTOR)))


				.andDo(print())
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("Not Found"));
	}

	@Test
	void rejectBooking_success() throws Exception {
		UUID bookingId = UUID.randomUUID();
		willDoNothing().given(bookingService)
				.rejectBooking(any());

		mockMvc.perform(
						post("/api/v1/mentoring-bookings/{bookingId}/rejection", bookingId).contentType(
										MediaType.APPLICATION_JSON)
								.headers(TestHeaders.headersFor(UserType.INSTRUCTOR))
								.content(objectMapper.writeValueAsString(Map.of("reason", REJECT_REASON))))
				.andDo(print())
				.andExpect(status().isOk());
	}

	@Test
	void rejectBooking_fails_without_reason() throws Exception {
		UUID bookingId = UUID.randomUUID();

		mockMvc.perform(
						post("/api/v1/mentoring-bookings/{bookingId}/rejection", bookingId).contentType(
										MediaType.APPLICATION_JSON)
								.headers(TestHeaders.headersFor(UserType.INSTRUCTOR))
								.content("{}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void cancelBooking_success() throws Exception {
		UUID bookingId = UUID.randomUUID();
		willDoNothing().given(bookingService)
				.cancelBooking(any());

		mockMvc.perform(
						post("/api/v1/mentoring-bookings/{bookingId}/cancellation", bookingId).contentType(
										MediaType.APPLICATION_JSON)
								.headers(TestHeaders.headersFor(UserType.STUDENT))
								.content(objectMapper.writeValueAsString(Map.of("reason", CANCEL_REASON))))
				.andDo(print())
				.andExpect(status().isOk());
	}

	@Test
	void cancelBooking_fails_without_reason() throws Exception {
		UUID bookingId = UUID.randomUUID();

		mockMvc.perform(
						post("/api/v1/mentoring-bookings/{bookingId}/cancellation", bookingId).contentType(
										MediaType.APPLICATION_JSON)
								.headers(TestHeaders.headersFor(UserType.STUDENT))
								.content("{}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void rejectBooking_booking_not_found() throws Exception {
		UUID bookingId = UUID.randomUUID();
		willThrow(new BookingNotFoundException(new MentoringBookingId(bookingId))).given(
						bookingService)
				.rejectBooking(any());

		mockMvc.perform(
						post("/api/v1/mentoring-bookings/{bookingId}/rejection", bookingId).contentType(
										MediaType.APPLICATION_JSON)
								.headers(TestHeaders.headersFor(UserType.INSTRUCTOR))
								.content(objectMapper.writeValueAsString(Map.of("reason", REJECT_REASON))))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("Not Found"));
	}

	@Test
	void cancelBooking_booking_not_found() throws Exception {
		UUID bookingId = UUID.randomUUID();
		willThrow(new BookingNotFoundException(new MentoringBookingId(bookingId))).given(
						bookingService)
				.cancelBooking(any());

		mockMvc.perform(
						post("/api/v1/mentoring-bookings/{bookingId}/cancellation", bookingId).contentType(
										MediaType.APPLICATION_JSON)
								.headers(TestHeaders.headersFor(UserType.STUDENT))
								.content(objectMapper.writeValueAsString(Map.of("reason", CANCEL_REASON))))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("Not Found"));
	}

	@Test
	void acceptBooking_unauthorized() throws Exception {
		UUID bookingId = UUID.randomUUID();
		willThrow(UnauthorizedBookingAccessException.noPermissionToProcess()).given(bookingService)
				.acceptBooking(any());

		mockMvc.perform(
						post("/api/v1/mentoring-bookings/{bookingId}/acceptance", bookingId).headers(
								TestHeaders.headersFor(UserType.INSTRUCTOR)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.title").value("Forbidden"));
	}

	@Test
	void rejectBooking_unauthorized() throws Exception {
		UUID bookingId = UUID.randomUUID();
		willThrow(UnauthorizedBookingAccessException.noPermissionToProcess()).given(bookingService)
				.rejectBooking(any());

		mockMvc.perform(
						post("/api/v1/mentoring-bookings/{bookingId}/rejection", bookingId).contentType(
										MediaType.APPLICATION_JSON)
								.headers(TestHeaders.headersFor(UserType.STUDENT))
								.content(objectMapper.writeValueAsString(Map.of("reason", REJECT_REASON))))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.title").value("Forbidden"));
	}

	@Test
	void cancelBooking_unauthorized() throws Exception {
		UUID bookingId = UUID.randomUUID();
		willThrow(UnauthorizedBookingAccessException.noPermissionToCancel()).given(bookingService)
				.cancelBooking(any());

		mockMvc.perform(
						post("/api/v1/mentoring-bookings/{bookingId}/cancellation", bookingId).contentType(
										MediaType.APPLICATION_JSON)
								.headers(TestHeaders.headersFor(UserType.STUDENT))
								.content(objectMapper.writeValueAsString(Map.of("reason", CANCEL_REASON))))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.title").value("Forbidden"));
	}

	@Test
	void acceptBooking_invalid_status() throws Exception {
		UUID bookingId = UUID.randomUUID();
		willThrow(InvalidBookingStatusTransitionException.cannotAccept(
				com.goggles.mentoring_service.domain.booking.BookingStatus.PENDING)).given(
						bookingService)
				.acceptBooking(any());

		mockMvc.perform(
						post("/api/v1/mentoring-bookings/{bookingId}/acceptance", bookingId).headers(
								TestHeaders.headersFor(UserType.INSTRUCTOR)))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Invalid mentoring request"));
	}

	private BookingRequest.Create defaultRequest() {
		BookingRequest.BookingTimeSlot slot =
				new BookingRequest.BookingTimeSlot(SESSION_DATE, SESSION_START_TIME,
						SESSION_END_TIME);
		return new BookingRequest.Create(MENTOR_ID, List.of(slot), REQUEST_MESSAGE, null);
	}

	@TestConfiguration
	static class PageResolverConfig implements WebMvcConfigurer {
		@Override
		public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
			resolvers.add(new CommonPageRequestArgumentResolver());
		}
	}
}
