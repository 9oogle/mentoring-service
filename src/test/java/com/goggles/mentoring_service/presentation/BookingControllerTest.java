package com.goggles.mentoring_service.presentation;

import static com.goggles.mentoring_service.domain.booking.BookingFixture.*;
import static com.goggles.mentoring_service.domain.mentoring.MentoringFixture.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goggles.mentoring_service.application.result.BookingResult;
import com.goggles.mentoring_service.application.service.BookingService;
import com.goggles.mentoring_service.domain._common.UserType;
import com.goggles.mentoring_service.domain.booking.MentoringBookingId;
import com.goggles.mentoring_service.domain.mentoring.MentoringId;
import com.goggles.mentoring_service.domain.mentoring.exception.MentoringNotFoundException;
import com.goggles.mentoring_service.infrastructure.config.WebMvcConfig;
import com.goggles.mentoring_service.presentation.dto.BookingRequest;
import com.goggles.mentoring_service.presentation.support.TestHeaders;
import com.goggles.mentoring_service.presentation.support.UserContextArgumentResolver;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BookingController.class)
@Import({WebMvcConfig.class, UserContextArgumentResolver.class})
@MockitoBean(types = JpaMetamodelMappingContext.class)
class BookingControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockitoBean private BookingService bookingService;

  @Test
  void createBooking_success() throws Exception {
    UUID bookingId = UUID.randomUUID();
    given(bookingService.createBooking(any()))
        .willReturn(BookingResult.Create.of(new MentoringBookingId(bookingId)));

    mockMvc
        .perform(
            post("/api/v1/mentoring-bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(TestHeaders.headersFor(UserType.STUDENT))
                .content(objectMapper.writeValueAsString(defaultRequest())))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.bookingId").value(bookingId.toString()));
  }

  @Test
  void createBooking_fails_without_mentoringId() throws Exception {
    String body =
        """
        {"bookingTimeSlots":[{"date":"2026-06-01","startTime":"10:00:00","endTime":"11:00:00"}]}
        """;
    mockMvc
        .perform(
            post("/api/v1/mentoring-bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(TestHeaders.headersFor(UserType.STUDENT))
                .content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createBooking_fails_with_empty_slots() throws Exception {
    BookingRequest.Create request = new BookingRequest.Create(UUID.randomUUID(), List.of(), null,
            null);

    mockMvc
        .perform(
            post("/api/v1/mentoring-bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(TestHeaders.headersFor(UserType.STUDENT))
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createBooking_mentoring_not_found() throws Exception {
    MentoringId mentoringId = new MentoringId(UUID.randomUUID());
    given(bookingService.createBooking(any()))
        .willThrow(new MentoringNotFoundException(mentoringId));

    mockMvc
        .perform(
            post("/api/v1/mentoring-bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(TestHeaders.headersFor(UserType.STUDENT))
                .content(objectMapper.writeValueAsString(defaultRequest())))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.title").value("Not Found"));
  }

  private BookingRequest.Create defaultRequest() {
    BookingRequest.BookingTimeSlot slot =
        new BookingRequest.BookingTimeSlot(SESSION_DATE, SESSION_START_TIME, SESSION_END_TIME);
    return new BookingRequest.Create(MENTOR_ID, List.of(slot), REQUEST_MESSAGE,null);
  }
}
