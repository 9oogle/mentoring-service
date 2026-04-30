package com.goggles.mentoring_service.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;
import java.util.UUID;

@Configuration
public class AuditConfig {

	@Bean
	@ConditionalOnMissingBean(AuditorAware.class)
	public AuditorAware<UUID> auditorAware() {
		return () -> {
			ServletRequestAttributes attrs =
					(ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
			if (attrs == null) {
				return Optional.empty();
			}
			HttpServletRequest request = attrs.getRequest();
			String userId = request.getHeader("X-User-Id");
			if (userId == null || userId.isBlank()) {
				return Optional.empty();
			}
			try {
				return Optional.of(UUID.fromString(userId));
			} catch (IllegalArgumentException e) {
				return Optional.empty();
			}
		};
	}
}
