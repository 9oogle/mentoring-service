package com.goggles.mentoring_service.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kafka.topics")
public record KafkaTopicProperties(Booking booking) {

	public record Booking(
			String paymentCompleted,
			String paymentFailed,
			String accepted,
			String rejected,
			String canceled,
			String requested,
			String pendingApproval
	) {}
}