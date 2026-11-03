package com.ruvi.reconciliation.infrastructure.persistence;

import com.ruvi.reconciliation.domain.reconciliation.ReconciliationRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReconciliationRunRepository extends JpaRepository<ReconciliationRun, UUID> {
}
