package com.goggles.mentoring_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
@ConfigurationPropertiesScan
@EntityScan(basePackages = {"com.goggles.mentoring_service", "com.goggles.common.domain"})
public class MentoringServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MentoringServerApplication.class, args);
	}

}
