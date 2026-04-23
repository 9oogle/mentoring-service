package com.goggles.mentoring_service.domain.mentoring;

import java.time.LocalTime;

public final class SessionPolicy {

	public static final LocalTime BUSINESS_START = LocalTime.of(6, 0);
	public static final LocalTime BUSINESS_END = LocalTime.of(22, 0);

	private SessionPolicy() {}
}