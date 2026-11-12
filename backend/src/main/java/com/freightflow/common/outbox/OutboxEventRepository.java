package com.freightflow.common.outbox;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    @Query("select e from OutboxEvent e where e.status = com.freightflow.common.outbox.OutboxEvent.OutboxStatus.PENDING order by e.createdAt")
    List<OutboxEvent> findPending(Pageable pageable);
}
