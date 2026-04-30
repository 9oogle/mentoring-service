package com.goggles.mentoring_service.presentation;

import com.goggles.mentoring_service.application.result.CategoryResult;
import com.goggles.mentoring_service.application.service.CategoryService;
import com.goggles.mentoring_service.infrastructure.config.WebMvcConfig;
import com.goggles.mentoring_service.presentation.support.UserContextArgumentResolver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@Import({WebMvcConfig.class, UserContextArgumentResolver.class})
@MockitoBean(types = JpaMetamodelMappingContext.class)
class CategoryControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CategoryService categoryService;

	@Test
	void getActiveCategories_success() throws Exception {
		UUID id1 = UUID.randomUUID();
		UUID id2 = UUID.randomUUID();
		String name1 = UUID.randomUUID()
				.toString();
		String code1 = UUID.randomUUID()
				.toString()
				.substring(0, 8)
				.toUpperCase();

		given(categoryService.getActiveCategories()).willReturn(
				List.of(new CategoryResult.Info(id1, name1, code1, 0, true),
						new CategoryResult.Info(id2, UUID.randomUUID()
								.toString(), UUID.randomUUID()
								.toString()
								.substring(0, 8)
								.toUpperCase(), 1, true)));

		mockMvc.perform(get("/api/v1/mentoring-categories"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.categories").isArray())
				.andExpect(jsonPath("$.categories.length()").value(2))
				.andExpect(jsonPath("$.categories[0].categoryId").value(id1.toString()))
				.andExpect(jsonPath("$.categories[0].name").value(name1))
				.andExpect(jsonPath("$.categories[0].code").value(code1))
				.andExpect(jsonPath("$.categories[0].sortOrder").value(0));
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
