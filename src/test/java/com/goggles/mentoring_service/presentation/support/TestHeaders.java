package com.goggles.mentoring_service.presentation.support;

import com.goggles.mentoring_service.domain._common.UserType;
import org.springframework.http.HttpHeaders;

import java.util.UUID;

public class TestHeaders {

	public static HttpHeaders headersFor(UserType userType) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-User-Id", UUID.randomUUID()
				.toString());
		headers.add("X-User-Type", userType.name());
		headers.add("X-User-Name", UUID.randomUUID()
				.toString());
		headers.add("X-User-Email", UUID.randomUUID()
				.toString());
		headers.add("X-User-Field", UUID.randomUUID()
				.toString());
		return headers;
	}
}
