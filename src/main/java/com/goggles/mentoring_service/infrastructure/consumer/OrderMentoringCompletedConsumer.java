package com.goggles.mentoring_service.infrastructure.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goggles.common.event.annotation.IdempotentConsumer;
import com.goggles.mentoring_service.application.command.BookingCommand;
import com.goggles.mentoring_service.application.service.BookingService;
import com.goggles.mentoring_service.domain.booking.MentoringBookingId;
import com.goggles.mentoring_service.infrastructure.consumer.event.OrderMentoringCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderMentoringCompletedConsumer {

	private final BookingService bookingService;
	private final ObjectMapper objectMapper;

	@KafkaListener(topics = "${kafka.topics.order.mentoring-completed}",
			groupId = "${spring.kafka.consumer.group-id}")
	@IdempotentConsumer("ORDER_MENTORING_COMPLETED")
	public void consume(ConsumerRecord<String, String> record) {
		try {
			OrderMentoringCompletedEvent event =
					objectMapper.readValue(record.value(), OrderMentoringCompletedEvent.class);

			BookingCommand.PaymentCompleted command = new BookingCommand.PaymentCompleted(
					new MentoringBookingId(event.enrollmentId()), event.orderId());

			bookingService.paymentCompleted(command);
		} catch (Exception e) {
			log.error("멘토링 주문 완료 이벤트 처리 실패. offset={}, value={}", record.offset(),
					record.value(), e);
			throw new RuntimeException(e);
		}
	}
}