package com.ruvi.reconciliation.infrastructure.persistence;

import com.ruvi.reconciliation.domain.reconciliation.Discrepancy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DiscrepancyRepository extends JpaRepository<Discrepancy, UUID> {
    List<Discrepancy> findByRunId(UUID runId);
}
