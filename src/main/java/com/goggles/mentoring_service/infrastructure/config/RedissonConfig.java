package com.goggles.mentoring_service.infrastructure.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SentinelServersConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class RedissonConfig {

	@Bean(destroyMethod = "shutdown")
	@ConditionalOnProperty(prefix = "spring.data.redis.sentinel", name = "master")
	public RedissonClient redissonSentinelClient(Environment env) {
		Config config = new Config();
		String master = env.getProperty("spring.data.redis.sentinel.master");
		String[] nodes = env.getProperty("spring.data.redis.sentinel.nodes", String[].class,
				new String[0]);
		String password = env.getProperty("spring.data.redis.password");

		SentinelServersConfig sentinelConfig = config.useSentinelServers().setMasterName(master);
		for (String node : nodes) {
			sentinelConfig.addSentinelAddress("redis://" + node);
		}
		if (password != null) {
			sentinelConfig.setPassword(password);
			sentinelConfig.setSentinelPassword(password);
		}
		return Redisson.create(config);
	}

	@Bean(destroyMethod = "shutdown")
	@ConditionalOnProperty(prefix = "spring.data.redis", name = "host")
	@ConditionalOnMissingBean(RedissonClient.class)
	public RedissonClient redissonSingleClient(Environment env) {
		Config config = new Config();
		String host = env.getProperty("spring.data.redis.host", "localhost");
		String port = env.getProperty("spring.data.redis.port", "6379");
		config.useSingleServer().setAddress("redis://" + host + ":" + port);
		return Redisson.create(config);
	}
}