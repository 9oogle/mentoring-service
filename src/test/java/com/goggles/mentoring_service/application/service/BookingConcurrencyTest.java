package com.goggles.mentoring_service.application.service;

import com.goggles.mentoring_service.application.command.BookingCommand;
import com.goggles.mentoring_service.application.command.MenteeInfo;
import com.goggles.mentoring_service.config.TestAuditConfig;
import com.goggles.mentoring_service.domain._common.UserType;
import com.goggles.mentoring_service.domain.booking.SessionSlot;
import com.goggles.mentoring_service.domain.category.MentoringCategory;
import com.goggles.mentoring_service.domain.category.repository.MentoringCategoryRepository;
import com.goggles.mentoring_service.domain.mentoring.*;
import com.goggles.mentoring_service.domain.mentoring.repository.MentoringRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.goggles.mentoring_service.domain.mentoring.MentoringFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(TestAuditConfig.class)
@ActiveProfiles("test")
class BookingConcurrencyTest {

	@MockitoBean
	private RedissonClient redissonClient;

	@Autowired
	private BookingService bookingService;
	@Autowired
	private MentoringRepository mentoringRepository;
	@Autowired
	private MentoringCategoryRepository categoryRepository;

	private UUID mentoringId;
	private final ConcurrentHashMap<String, java.util.concurrent.locks.ReentrantLock> lockMap = new ConcurrentHashMap<>();

	@BeforeEach
	void setUp() throws InterruptedException {
		lockMap.clear();
		setupFakeLock();
		mentoringId = saveMentoringWithSession();
	}

	@Test
	void createBooking_concurrentRequests_onlyOneSucceeds() throws InterruptedException {
		int threadCount = 5;
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		CountDownLatch ready = new CountDownLatch(threadCount);
		CountDownLatch start = new CountDownLatch(1);
		AtomicInteger successCount = new AtomicInteger();

		for (int i = 0; i < threadCount; i++) {
			UUID menteeId = UUID.randomUUID();
			executor.submit(() -> {
				ready.countDown();
				try {
					start.await();
					BookingCommand.Create command = bookingCommand(mentoringId, menteeId);
					bookingService.createBooking(command);
					successCount.incrementAndGet();
				} catch (Exception ignored) {
				}
			});
		}

		ready.await();
		start.countDown();
		executor.shutdown();
		boolean finished = executor.awaitTermination(30, TimeUnit.SECONDS);
		assertThat(finished).as("모든 스레드가 30초 내에 완료되어야 합니다").isTrue();

		assertThat(successCount.get()).isEqualTo(1);
	}

	// ── helpers ──────────────────────────────────────────────────────────────

	private void setupFakeLock() throws InterruptedException {
		when(redissonClient.getLock(anyString())).thenAnswer(inv -> {
			String key = inv.getArgument(0);
			java.util.concurrent.locks.ReentrantLock javaLock =
					lockMap.computeIfAbsent(key, k -> new java.util.concurrent.locks.ReentrantLock());

			RLock rLock = mock(RLock.class);
			when(rLock.tryLock(anyLong(), any(TimeUnit.class))).thenAnswer(i ->
					javaLock.tryLock(i.<Long>getArgument(0), i.getArgument(1)));
			when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenAnswer(i ->
					javaLock.tryLock(i.<Long>getArgument(0), i.<TimeUnit>getArgument(2)));
			when(rLock.isHeldByCurrentThread()).thenAnswer(i -> javaLock.isHeldByCurrentThread());
			doAnswer(i -> {
				if (javaLock.isHeldByCurrentThread()) javaLock.unlock();
				return null;
			}).when(rLock).unlock();

			return rLock;
		});
	}

	private UUID saveMentoringWithSession() {
		MentoringCategory category =
				MentoringCategory.create(MENTOR_ID, UserType.MASTER, CATEGORY_NAME, CATEGORY_CODE);
		categoryRepository.save(category);

		Mentoring mentoring = defaultMentoringBuilder()
				.categoryId(category.getMentoringCategoryId().categoryId())
				.categoryName(category.getName())
				.categoryCode(category.getCode())
				.format(Format.SINGLE)
				.mentoringType(MentoringType.ONE_ON_ONE)
				.sessions(List.of(session(SESSION_DATE_1)))
				.build();
		mentoringRepository.save(mentoring);
		return mentoring.getMentoringId().mentoringId();
	}

	private BookingCommand.Create bookingCommand(UUID mentoringId, UUID menteeId) {
		MenteeInfo menteeInfo = new MenteeInfo(menteeId, UserType.STUDENT, "멘티");
		SessionSlot slot = sessionSlot(SESSION_DATE_1);
		return new BookingCommand.Create(menteeInfo, mentoringId, List.of(slot), "테스트",
				UUID.randomUUID());
	}
}