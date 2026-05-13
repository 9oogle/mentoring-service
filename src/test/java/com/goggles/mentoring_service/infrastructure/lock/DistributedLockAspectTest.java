package com.goggles.mentoring_service.infrastructure.lock;

import com.goggles.mentoring_service.application.lock.DistributedLock;
import com.goggles.mentoring_service.infrastructure.exception.DistributionLockException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.ObjectProvider;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class DistributedLockAspectTest {

	private RedissonClient redissonClient;
	private DistributedLockAspect aspect;

	@BeforeEach
	void setUp() {
		redissonClient = mock(RedissonClient.class);
		ObjectProvider<RedissonClient> provider = mock(ObjectProvider.class);
		when(provider.getIfAvailable()).thenReturn(redissonClient);
		aspect = new DistributedLockAspect(provider);
	}

	@Test
	void lock_whenRedisUnavailable_proceedsWithoutLock() throws Throwable {
		ObjectProvider<RedissonClient> emptyProvider = mock(ObjectProvider.class);
		when(emptyProvider.getIfAvailable()).thenReturn(null);
		DistributedLockAspect noRedisAspect = new DistributedLockAspect(emptyProvider);

		ProceedingJoinPoint joinPoint = mockJoinPoint("result", new String[]{}, new Object[]{});
		DistributedLock annotation = mockAnnotation("static-key", 5L, -1L);

		Object result = noRedisAspect.lock(joinPoint, annotation);

		assertThat(result).isEqualTo("result");
		verifyNoInteractions(redissonClient);
	}

	@Test
	void lock_whenAcquired_executesAndReturnsResult() throws Throwable {
		RLock rLock = mockAcquiredLock();
		when(redissonClient.getLock("lock:booking:test-id")).thenReturn(rLock);

		// joinPoint에 'id' 파라미터를 포함해야 SpEL이 올바르게 평가됨
		ProceedingJoinPoint joinPoint =
				mockJoinPoint("expected", new String[]{"id"}, new Object[]{"test-id"});
		DistributedLock annotation = mockAnnotation("'booking:' + #id", 5L, -1L);

		Object result = aspect.lock(joinPoint, annotation);

		assertThat(result).isEqualTo("expected");
		verify(rLock).unlock();
	}

	@Test
	void lock_whenNotAcquired_throwsConflictException() throws Throwable {
		RLock rLock = mockFailedLock();
		when(redissonClient.getLock(anyString())).thenReturn(rLock);

		ProceedingJoinPoint joinPoint = mockJoinPoint("result", new String[]{}, new Object[]{});
		DistributedLock annotation = mockAnnotation("static-key", 1L, -1L);

		assertThatThrownBy(() -> aspect.lock(joinPoint, annotation))
				.isInstanceOf(DistributionLockException.class);

		verify(joinPoint, never()).proceed();
		verify(rLock, never()).unlock();
	}

	@Test
	void lock_whenMethodThrows_unlocksAndRethrows() throws Throwable {
		RLock rLock = mockAcquiredLock();
		when(redissonClient.getLock(anyString())).thenReturn(rLock);

		ProceedingJoinPoint joinPoint = mockJoinPoint(null, new String[]{}, new Object[]{});
		when(joinPoint.proceed()).thenThrow(new RuntimeException("도메인 오류"));
		DistributedLock annotation = mockAnnotation("static-key", 5L, -1L);

		assertThatThrownBy(() -> aspect.lock(joinPoint, annotation))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("도메인 오류");

		verify(rLock).unlock();
	}

	@Test
	void lock_withExplicitLeaseTime_usesThreeParamTryLock() throws Throwable {
		RLock rLock = mock(RLock.class);
		when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
		when(rLock.isHeldByCurrentThread()).thenReturn(true);
		when(redissonClient.getLock(anyString())).thenReturn(rLock);

		ProceedingJoinPoint joinPoint = mockJoinPoint("result", new String[]{}, new Object[]{});
		DistributedLock annotation = mockAnnotation("static-key", 5L, 30L);

		aspect.lock(joinPoint, annotation);

		verify(rLock).tryLock(5L, 30L, TimeUnit.SECONDS);
		verify(rLock, never()).tryLock(anyLong(), any(TimeUnit.class));
	}

	// ── helpers ──────────────────────────────────────────────────────────────

	private RLock mockAcquiredLock() throws InterruptedException {
		RLock rLock = mock(RLock.class);
		when(rLock.tryLock(anyLong(), any(TimeUnit.class))).thenReturn(true);
		when(rLock.isHeldByCurrentThread()).thenReturn(true);
		return rLock;
	}

	private RLock mockFailedLock() throws InterruptedException {
		RLock rLock = mock(RLock.class);
		when(rLock.tryLock(anyLong(), any(TimeUnit.class))).thenReturn(false);
		when(rLock.isHeldByCurrentThread()).thenReturn(false);
		return rLock;
	}

	private ProceedingJoinPoint mockJoinPoint(Object returnValue, String[] paramNames,
			Object[] args) throws Throwable {
		MethodSignature signature = mock(MethodSignature.class);
		when(signature.getParameterNames()).thenReturn(paramNames);

		ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
		when(joinPoint.getSignature()).thenReturn(signature);
		when(joinPoint.getArgs()).thenReturn(args);
		when(joinPoint.proceed()).thenReturn(returnValue);
		return joinPoint;
	}

	private DistributedLock mockAnnotation(String key, long waitTime, long leaseTime) {
		DistributedLock annotation = mock(DistributedLock.class);
		when(annotation.key()).thenReturn(key);
		when(annotation.waitTime()).thenReturn(waitTime);
		when(annotation.leaseTime()).thenReturn(leaseTime);
		when(annotation.timeUnit()).thenReturn(TimeUnit.SECONDS);
		return annotation;
	}
}