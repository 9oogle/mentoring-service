package com.goggles.mentoring_service.presentation.support;

import com.goggles.mentoring_service.domain.common.UserType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.UUID;

@Component
public class UserContextArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.getParameterType()
				.equals(UserContext.class);
	}

	@Override
	public UserContext resolveArgument(MethodParameter parameter,
			ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
			WebDataBinderFactory binderFactory) {
		HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
		return new UserContext(UUID.fromString(request.getHeader("X-User-Id")),
				UserType.valueOf(request.getHeader("X-User-Type")),
				request.getHeader("X-User-Name"), request.getHeader("X-User-Email"),
				request.getHeader("X-User-Field"));
	}
}
