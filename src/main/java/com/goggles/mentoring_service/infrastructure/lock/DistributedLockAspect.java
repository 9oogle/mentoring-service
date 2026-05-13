package com.goggles.mentoring_service.infrastructure.lock;

import com.goggles.mentoring_service.application.lock.DistributedLock;
import com.goggles.mentoring_service.infrastructure.exception.DistributionLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Slf4j
@Aspect
@Order(Ordered.LOWEST_PRECEDENCE - 1)
@RequiredArgsConstructor
public class DistributedLockAspect {

	private static final String LOCK_PREFIX = "lock:";

	private final RedissonClient redissonClient;

	@Around("@annotation(distributedLock)")
	public Object lock(ProceedingJoinPoint joinPoint, DistributedLock distributedLock)
			throws Throwable {
		String key = LOCK_PREFIX + LockKeyParser.parse(distributedLock.key(), joinPoint);
		RLock lock = redissonClient.getLock(key);

		boolean acquired = tryAcquire(lock, distributedLock);
		if (!acquired) {
			log.warn("Distributed lock acquisition timed out. key={}", key);
			throw new DistributionLockException("예약 처리 중입니다. 잠시 후 다시 시도해주세요.");
		}

		try {
			return joinPoint.proceed();
		} finally {
			if (lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
		}
	}

	private boolean tryAcquire(RLock lock, DistributedLock distributedLock)
			throws InterruptedException {
		if (distributedLock.leaseTime() < 0) {
			return lock.tryLock(distributedLock.waitTime(), distributedLock.timeUnit());
		}
		return lock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(),
				distributedLock.timeUnit());
	}
}
