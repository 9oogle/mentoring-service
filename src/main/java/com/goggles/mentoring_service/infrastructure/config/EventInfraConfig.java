package com.goggles.mentoring_service.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goggles.common.domain.OutboxRepository;
import com.goggles.common.event.OutboxEventListener;
import com.goggles.common.event.OutboxStatusUpdater;
import com.goggles.common.event.scheduler.OutboxRelayScheduler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class EventInfraConfig {

	@Bean
	@ConditionalOnBean(KafkaTemplate.class)
	public OutboxStatusUpdater outboxStatusUpdater(OutboxRepository outboxRepository,
			KafkaTemplate<String, Object> kafkaTemplate) {
		return new OutboxStatusUpdater(outboxRepository, kafkaTemplate);
	}

	@Bean
	@ConditionalOnBean(KafkaTemplate.class)
	public OutboxEventListener outboxEventListener(OutboxRepository outboxRepository,
			KafkaTemplate<String, Object> kafkaTemplate, ObjectMapper objectMapper,
			OutboxStatusUpdater outboxStatusUpdater) {
		return new OutboxEventListener(outboxRepository, kafkaTemplate, objectMapper,
				outboxStatusUpdater);
	}

	@Bean
	@ConditionalOnBean(KafkaTemplate.class)
	public OutboxRelayScheduler outboxRelayScheduler(OutboxRepository outboxRepository,
			KafkaTemplate<String, Object> kafkaTemplate, OutboxStatusUpdater outboxStatusUpdater) {
		return new OutboxRelayScheduler(outboxRepository, kafkaTemplate, outboxStatusUpdater);
	}
}
