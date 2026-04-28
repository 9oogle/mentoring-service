package com.goggles.mentoring_service.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.goggles.mentoring_service.presentation.support.TestHeaders.headersFor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@Import({WebMvcConfig.class, UserContextArgumentResolver.class})
@MockitoBean(types = JpaMetamodelMappingContext.class)
class CategoryControllerTest {

  @Autowired private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private CategoryService categoryService;

	@Test
	void createCategory_returns_201_with_category_id() throws Exception {
		UUID categoryId = UUID.randomUUID();
		given(categoryService.createCategory(any())).willReturn(categoryId);

		mockMvc.perform(post("/api/v1/mentoring-categories")
						.contentType(MediaType.APPLICATION_JSON)
						.headers(headersFor(UserType.MASTER))
						.content(objectMapper.writeValueAsString(
								Map.of("title", "Java 백엔드", "code", "JAVA", "sortOrder", 0))))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.categoryId").value(categoryId.toString()));
	}

	@Test
	void createCategory_returns_403_when_not_master() throws Exception {
		String errorMessage = "관리자 권한이 필요합니다";
		willThrow(new ForbiddenException(errorMessage)).given(categoryService).createCategory(any());

		mockMvc.perform(post("/api/v1/mentoring-categories")
						.contentType(MediaType.APPLICATION_JSON)
						.headers(headersFor(UserType.INSTRUCTOR))
						.content(objectMapper.writeValueAsString(
								Map.of("title", "Java 백엔드", "code", "JAVA", "sortOrder", 0))))
				.andDo(print())
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.title").value("Forbidden"))
				.andExpect(jsonPath("$.detail").value(errorMessage));
	}

	@Test
	void createCategory_returns_400_when_title_blank() throws Exception {
		mockMvc.perform(post("/api/v1/mentoring-categories")
						.contentType(MediaType.APPLICATION_JSON)
						.headers(headersFor(UserType.MASTER))
						.content(objectMapper.writeValueAsString(
								Map.of("title", "", "code", "JAVA", "sortOrder", 0))))
				.andDo(print())
				.andExpect(status().isBadRequest());
	}

	@Test
	void createCategory_returns_400_when_code_too_long() throws Exception {
		mockMvc.perform(post("/api/v1/mentoring-categories")
						.contentType(MediaType.APPLICATION_JSON)
						.headers(headersFor(UserType.MASTER))
						.content(objectMapper.writeValueAsString(
								Map.of("title", "Java 백엔드", "code", "TOOLONGCODE", "sortOrder", 0))))
				.andDo(print())
				.andExpect(status().isBadRequest());
	}


	@Test
	void getActiveCategories_returns_200_with_category_list() throws Exception {
		UUID id1 = UUID.randomUUID();
		UUID id2 = UUID.randomUUID();
		String name1 = UUID.randomUUID()
				.toString();
		String name2 = UUID.randomUUID()
				.toString();
		String code1 = UUID.randomUUID()
				.toString()
				.substring(0, 8)
				.toUpperCase();
		String code2 = UUID.randomUUID()
				.toString()
				.substring(0, 8)
				.toUpperCase();

    given(categoryService.getActiveCategories())
        .willReturn(
            List.of(
                new CategoryResult.Info(id1, name1, code1, 0, true),
                new CategoryResult.Info(id2, name2, code2, 1, true)));

		mockMvc.perform(get("/api/v1/mentoring-categories"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.categories").isArray())
				.andExpect(jsonPath("$.categories.length()").value(2))
				.andExpect(jsonPath("$.categories[0].categoryId").value(id1.toString()))
				.andExpect(jsonPath("$.categories[0].name").value(name1))
				.andExpect(jsonPath("$.categories[0].code").value(code1))
				.andExpect(jsonPath("$.categories[0].sortOrder").value(0))
				.andExpect(jsonPath("$.categories[0].active").doesNotExist())
				.andExpect(jsonPath("$.categories[1].name").value(name2));
	}

  @Test
  void getActiveCategories_empty() throws Exception {
    given(categoryService.getActiveCategories()).willReturn(List.of());

		mockMvc.perform(get("/api/v1/mentoring-categories"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.categories").isArray())
				.andExpect(jsonPath("$.categories").isEmpty());
	}
}
