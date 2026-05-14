package com.goggles.mentoring_service.infrastructure.config;

import org.redisson.api.RedissonClient;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Map;

@Configuration
@EnableCaching
public class MentoringCacheConfig {

	static final String MENTORING      = "mentoring";
	static final String MENTORING_LIST = "mentoring-list";

	// 로컬: in-memory 캐시 (직렬화 불필요, 테스트에 최적)
	@Bean
	@Profile("local")
	public CacheManager localCacheManager() {
		return new ConcurrentMapCacheManager(MENTORING, MENTORING_LIST);
	}

	// prod: Redisson 기반 분산 캐시
	@Bean
	@Profile("prod")
	public CacheManager redisCacheManager(RedissonClient redissonClient) {
		return new RedissonSpringCacheManager(redissonClient, Map.of(
			MENTORING,      new org.redisson.spring.cache.CacheConfig(10 * 60_000L, 5 * 60_000L),
			MENTORING_LIST, new org.redisson.spring.cache.CacheConfig(60_000L, 30_000L)
		));
	}
}