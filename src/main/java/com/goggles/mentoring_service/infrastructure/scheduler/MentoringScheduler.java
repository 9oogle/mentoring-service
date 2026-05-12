package com.goggles.mentoring_service.infrastructure.scheduler;

import com.goggles.mentoring_service.application.service.BookingService;
import com.goggles.mentoring_service.application.service.MentoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class MentoringScheduler {

	private final BookingService bookingService;
	private final MentoringService mentoringService;

	@Scheduled(cron = "0 0 0 * * *")
	public void generateDailyRepeatSessions() {
		LocalDate tomorrow = LocalDate.now().plusDays(1);
		try {
			mentoringService.generateDailyRepeatSessions(tomorrow);
			log.info("[스케쥴러] 반복 세션 생성 완료: {}", tomorrow);
		} catch (Exception e) {
			log.error("[스케쥴러] 반복 세션 생성 실패: {}", tomorrow, e);
		}
	}

	@Scheduled(cron = "0 5 0 * * *")
	public void cleanupExpiredSessions() {
		LocalDate today = LocalDate.now();
		try {
			mentoringService.cleanupExpiredSessions(today);
			log.info("[스케쥴러] 만료 슬롯 정리 완료: {} 이전", today);
		} catch (Exception e) {
			log.error("[스케쥴러] 만료 슬롯 정리 실패", e);
		}
	}
	
	@Scheduled(cron = "0 0 * * * *")
	public void autoCancelUnapprovedBookings() {
		LocalDateTime threshold = LocalDateTime.now().plusHours(24);
		try {
			int count = bookingService.autoCancelUnapproved(threshold);
			if (count > 0) {
				log.info("[스케쥴러] 미승인 예약 자동 취소: {}건", count);
			}
		} catch (Exception e) {
			log.error("[스케쥴러] 미승인 예약 자동 취소 실패", e);
		}
	}
}