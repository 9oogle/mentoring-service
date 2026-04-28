package com.goggles.mentoring_service.presentation;

import static com.goggles.mentoring_service.presentation.support.TestHeaders.headersFor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.goggles.common.exception.ForbiddenException;
import com.goggles.mentoring_service.application.result.CategoryResult;
import com.goggles.mentoring_service.application.service.CategoryService;
import com.goggles.mentoring_service.domain._common.UserType;
import com.goggles.mentoring_service.infrastructure.config.WebMvcConfig;
import com.goggles.mentoring_service.presentation.support.UserContextArgumentResolver;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminController.class)
@Import({WebMvcConfig.class, UserContextArgumentResolver.class})
@MockitoBean(types = JpaMetamodelMappingContext.class)
class AdminControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private CategoryService categoryService;

  @Test
  void getAllCategories_success() throws Exception {
    UUID activeId = UUID.randomUUID();
    UUID inactiveId = UUID.randomUUID();
    String activeName = UUID.randomUUID().toString();
    String activeCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

    given(categoryService.getAllCategories(any()))
        .willReturn(
            List.of(
                new CategoryResult.Info(activeId, activeName, activeCode, 0, true),
                new CategoryResult.Info(
                    inactiveId,
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                    null,
                    false)));

    mockMvc
        .perform(get("/api/v1/admin/mentorings-categories").headers(headersFor(UserType.MASTER)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.categories").isArray())
        .andExpect(jsonPath("$.categories.length()").value(2))
        .andExpect(jsonPath("$.categories[0].categoryId").value(activeId.toString()))
        .andExpect(jsonPath("$.categories[0].name").value(activeName))
        .andExpect(jsonPath("$.categories[0].active").value(true))
        .andExpect(jsonPath("$.categories[0].sortOrder").value(0))
        .andExpect(jsonPath("$.categories[1].active").value(false))
        .andExpect(jsonPath("$.categories[1].sortOrder").isEmpty());
  }

  @Test
  void getAllCategories_forbidden_if_not_master() throws Exception {
    String errorMessage = "관리자 권한이 필요합니다.";
    willThrow(new ForbiddenException(errorMessage)).given(categoryService).getAllCategories(any());

    mockMvc
        .perform(
            get("/api/v1/admin/mentorings-categories").headers(headersFor(UserType.INSTRUCTOR)))
        .andDo(print())
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.title").value("Forbidden"))
        .andExpect(jsonPath("$.detail").value(errorMessage));
  }
}
