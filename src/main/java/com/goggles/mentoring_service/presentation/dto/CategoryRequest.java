package com.goggles.mentoring_service.presentation.dto;

import com.goggles.mentoring_service.application.command.CategoryCommand;
import com.goggles.mentoring_service.presentation.support.UserContext;
import jakarta.validation.constraints.*;


public class CategoryRequest {

	public record Create( @NotBlank @Size(max = 100) String title,
						  @NotBlank @Size(max = 10) String code,@NotNull int sortOrder) {
		public CategoryCommand.Create toCommand(UserContext userContext) {
			return new CategoryCommand.Create(title(), code(), sortOrder(), userContext.userId(),
					userContext.userType());
		}}

}
