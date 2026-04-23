package com.goggles.mentoring_service.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goggles.mentoring_service.application.service.MentoringService;
import com.goggles.mentoring_service.domain.mentoring.Format;
import com.goggles.mentoring_service.domain.mentoring.MentoringType;
import com.goggles.mentoring_service.infrastructure.config.WebMvcConfig;
import com.goggles.mentoring_service.presentation.dto.MentoringRequest;
import com.goggles.mentoring_service.presentation.support.UserContextArgumentResolver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import java.util.List;

import static com.goggles.mentoring_service.domain.mentoring.MentoringFixture.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MentoringController.class)
@Import({WebMvcConfig.class, UserContextArgumentResolver.class})
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

        mockMvc.perform(post("/api/v1/mentorings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(userHeaders())
                        .content(objectMapper.writeValueAsString(defaultRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mentoringId").value(mentoringId.toString()));
    }

    @Test
    void createMentoring_invalid_request() throws Exception {
        mockMvc.perform(post("/api/v1/mentorings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(userHeaders())
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    private HttpHeaders userHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Id", MENTOR_ID.toString());
        headers.add("X-User-Type", MENTOR_TYPE.name());
        headers.add("X-User-Name", MENTOR_NAME);
        headers.add("X-User-Email", MENTOR_EMAIL);
        headers.add("X-User-Field", MENTOR_FIELD);
        return headers;
    }

    private MentoringRequest.Create defaultRequest() {
        return new MentoringRequest.Create(
                CATEGORY_ID, TITLE, SUBTITLE, DESCRIPTION,
                DURATION, MentoringType.ONE_ON_ONE, Format.SINGLE,
                SESSION_COUNT, MAX_PARTICIPANTS, false, PRICE, null,
                List.of(new MentoringRequest.Create.SessionDto(SESSION_DATE_1, START_TIME, END_TIME)),
                List.of(new MentoringRequest.Create.RepeatPatternDto(java.time.DayOfWeek.MONDAY, START_TIME, END_TIME))
        );
    }
}
