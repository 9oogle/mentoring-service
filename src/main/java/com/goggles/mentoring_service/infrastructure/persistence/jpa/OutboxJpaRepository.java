package com.goggles.mentoring_service.infrastructure.persistence.jpa;

import com.goggles.common.domain.Outbox;
import com.goggles.common.domain.OutboxRepository;
import com.goggles.common.domain.OutboxStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OutboxJpaRepository extends OutboxRepository {

	@Override
	@Query(value = "SELECT * FROM p_outbox WHERE status = :#{#status.name()} AND updated_at < :threshold",
			nativeQuery = true)
	List<Outbox> findByStatusAndUpdatedAtBefore(@Param("status") OutboxStatus status, @Param("threshold") LocalDateTime threshold);
}