package com.goggles.mentoring_service.infrastructure.persistence.jpa;

import com.goggles.common.domain.OutboxRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OutboxJpaRepository extends OutboxRepository {}