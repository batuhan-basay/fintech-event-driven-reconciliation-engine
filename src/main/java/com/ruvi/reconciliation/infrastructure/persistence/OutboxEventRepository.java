package com.ruvi.reconciliation.infrastructure.persistence;

import com.ruvi.reconciliation.domain.outbox.OutboxEvent;
import com.ruvi.reconciliation.domain.outbox.OutboxStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@jakarta.persistence.QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    @Query("""
           select e from OutboxEvent e
           where e.status in :statuses and e.nextAttemptAt <= :now
           order by e.createdAt asc
           """)
    List<OutboxEvent> lockBatchForDispatch(@Param("statuses") List<OutboxStatus> statuses,
                                           @Param("now") Instant now,
                                           Pageable pageable);
}
