package com.goggles.mentoring_service.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goggles.common.pagination.CommonPageRequestArgumentResolver;
import com.goggles.mentoring_service.application.result.MentoringResult;
import com.goggles.mentoring_service.application.service.MentoringService;
import com.goggles.mentoring_service.domain.mentoring.*;
import com.goggles.mentoring_service.domain.mentoring.exception.MentoringNotFoundException;
import com.goggles.mentoring_service.infrastructure.config.WebMvcConfig;
import com.goggles.mentoring_service.presentation.dto.MentoringRequest;
import com.goggles.mentoring_service.presentation.support.UserContextArgumentResolver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

import static com.goggles.mentoring_service.domain.mentoring.MentoringFixture.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(MentoringController.class)
@Import({WebMvcConfig.class, UserContextArgumentResolver.class,
		MentoringControllerTest.PageResolverConfig.class})
@MockitoBean(types = JpaMetamodelMappingContext.class)
class MentoringControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@MockitoBean
	private MentoringService mentoringService;

	@Test
	void createMentoring_success() throws Exception {
		UUID mentoringId = UUID.randomUUID();
		given(mentoringService.createMentoring(any())).willReturn(mentoringId);

		mockMvc.perform(post("/api/v1/mentorings").contentType(MediaType.APPLICATION_JSON)
						.headers(userHeaders())
						.content(objectMapper.writeValueAsString(defaultRequest())))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.mentoringId").value(mentoringId.toString()));
	}

	@Test
	void createMentoring_invalid_request() throws Exception {
		mockMvc.perform(post("/api/v1/mentorings").contentType(MediaType.APPLICATION_JSON)
						.headers(userHeaders())
						.content("{}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void getMentoring_success() throws Exception {
		UUID mentoringId = UUID.randomUUID();
		MentoringResult.Detail detail =
				new MentoringResult.Detail(mentoringId, TITLE, SUBTITLE, DESCRIPTION,
						new MentoringResult.Detail.MentorInfo(MENTOR_NAME, MENTOR_FIELD),
						new MentoringResult.Detail.CategoryInfo(CATEGORY_NAME, CATEGORY_CODE),
						MentoringStatus.INACTIVE, Format.SINGLE, MentoringType.ONE_ON_ONE,
						MentoringDuration.MINUTES_60, SESSION_COUNT, MAX_PARTICIPANTS, false, PRICE,
						null);
		given(mentoringService.getMentoring(mentoringId)).willReturn(detail);

		mockMvc.perform(get("/api/v1/mentorings/{mentoringId}", mentoringId))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.mentoringId").value(mentoringId.toString()))
				.andExpect(jsonPath("$.title").value(TITLE))
				.andExpect(jsonPath("$.subtitle").value(SUBTITLE))
				.andExpect(jsonPath("$.status").value(MentoringStatus.INACTIVE.name()))
				.andExpect(jsonPath("$.price").value(PRICE))
				.andExpect(jsonPath("$.mentor.name").value(MENTOR_NAME))
				.andExpect(jsonPath("$.mentor.field").value(MENTOR_FIELD))
				.andExpect(jsonPath("$.category.name").value(CATEGORY_NAME))
				.andExpect(jsonPath("$.category.code").value(CATEGORY_CODE));
	}

	@Test
	void getMentoring_not_found() throws Exception {
		UUID unknownId = UUID.randomUUID();
		given(mentoringService.getMentoring(unknownId)).willThrow(
				new MentoringNotFoundException(new MentoringId(unknownId)));

		mockMvc.perform(get("/api/v1/mentorings/{mentoringId}", unknownId))
				.andDo(print())
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("Not Found"))
				.andExpect(jsonPath("$.detail").value(
						org.hamcrest.Matchers.containsString(unknownId.toString())));
	}

	@Test
	void getMentoringSchedules_success() throws Exception {
		UUID mentoringId = UUID.randomUUID();
		MentoringResult.Schedules schedules = new MentoringResult.Schedules(
				List.of(new MentoringResult.Schedules.RepeatPatternDto(DayOfWeek.MONDAY, START_TIME,
								END_TIME),
						new MentoringResult.Schedules.RepeatPatternDto(DayOfWeek.WEDNESDAY,
								START_TIME, END_TIME)),
				List.of(new MentoringResult.Schedules.SessionDto(SESSION_DATE_1, START_TIME,
								END_TIME, SessionStatus.AVAILABLE),
						new MentoringResult.Schedules.SessionDto(SESSION_DATE_2, START_TIME,
								END_TIME, SessionStatus.AVAILABLE)));
		given(mentoringService.getMentoringSchedules(mentoringId)).willReturn(schedules);

		mockMvc.perform(get("/api/v1/mentorings/{mentoringId}/schedules", mentoringId))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.repeatPatterns").isArray())
				.andExpect(jsonPath("$.repeatPatterns.length()").value(2))
				.andExpect(jsonPath("$.repeatPatterns[0].dayOfWeek").value("MONDAY"))
				.andExpect(jsonPath("$.repeatPatterns[1].dayOfWeek").value("WEDNESDAY"))
				.andExpect(jsonPath("$.sessions").isArray())
				.andExpect(jsonPath("$.sessions.length()").value(2))
				.andExpect(jsonPath("$.sessions[0].date").value(SESSION_DATE_1.toString()))
				.andExpect(jsonPath("$.sessions[0].status").value(SessionStatus.AVAILABLE.name()));
	}

	@Test
	void getMentoringSchedules_not_found() throws Exception {
		UUID unknownId = UUID.randomUUID();
		given(mentoringService.getMentoringSchedules(unknownId)).willThrow(
				new MentoringNotFoundException(new MentoringId(unknownId)));

		mockMvc.perform(get("/api/v1/mentorings/{mentoringId}/schedules", unknownId))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	void searchMentorings_success() throws Exception {
		UUID mentoringId = UUID.randomUUID();
		MentoringResult.Summary summary =
				new MentoringResult.Summary(mentoringId, TITLE, SUBTITLE, MENTOR_NAME,
						CATEGORY_NAME, MentoringType.ONE_ON_ONE, Format.SINGLE, PRICE,
						MentoringStatus.INACTIVE);
		Page<MentoringResult.Summary> page =
				new PageImpl<>(List.of(summary), PageRequest.of(0, 10), 1);
		given(mentoringService.searchMentorings(any(), any())).willReturn(page);

		mockMvc.perform(get("/api/v1/mentorings").param("page", "0")
						.param("size", "10"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.content.length()").value(1))
				.andExpect(jsonPath("$.content[0].mentoringId").value(mentoringId.toString()))
				.andExpect(jsonPath("$.content[0].title").value(TITLE))
				.andExpect(jsonPath("$.content[0].mentorName").value(MENTOR_NAME))
				.andExpect(jsonPath("$.content[0].price").value(PRICE))
				.andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.totalPages").value(1))
				.andExpect(jsonPath("$.first").value(true))
				.andExpect(jsonPath("$.last").value(true));
	}

	@Test
	void searchMentorings_with_all_filters() throws Exception {
		Page<MentoringResult.Summary> emptyPage =
				new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
		given(mentoringService.searchMentorings(any(), any())).willReturn(emptyPage);

		mockMvc.perform(get("/api/v1/mentorings").param("keyword", "스프링")
						.param("categoryId", CATEGORY_ID.toString())
						.param("mentorId", MENTOR_ID.toString())
						.param("status", MentoringStatus.INACTIVE.name())
						.param("mentoringType", MentoringType.ONE_ON_ONE.name())
						.param("sortBy", MentoringSort.PRICE_ASC.name())
						.param("page", "0")
						.param("size", "10"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.totalElements").value(0));
	}

	private HttpHeaders userHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-User-Id", MENTOR_ID.toString());
		headers.add("X-User-Role", MENTOR_TYPE.name());
		headers.add("X-User-Name", MENTOR_NAME);
		headers.add("X-User-Email", MENTOR_EMAIL);
		headers.add("X-User-Field", MENTOR_FIELD);
		return headers;
	}

	private MentoringRequest.Create defaultRequest() {
		return new MentoringRequest.Create(CATEGORY_ID, TITLE, SUBTITLE, DESCRIPTION, DURATION,
				MentoringType.ONE_ON_ONE, Format.SINGLE, SESSION_COUNT, MAX_PARTICIPANTS, false,
				PRICE, null,
				List.of(new MentoringRequest.Create.SessionDto(SESSION_DATE_1, START_TIME,
						END_TIME)),
				List.of(new MentoringRequest.Create.RepeatPatternDto(java.time.DayOfWeek.MONDAY,
						START_TIME, END_TIME)));
	}

	@TestConfiguration
	static class PageResolverConfig implements WebMvcConfigurer {
		@Override
		public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
			resolvers.add(new CommonPageRequestArgumentResolver());
		}
	}
}
