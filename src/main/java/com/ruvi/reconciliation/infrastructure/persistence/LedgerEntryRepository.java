package com.ruvi.reconciliation.infrastructure.persistence;

import com.ruvi.reconciliation.domain.ledger.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {
    List<LedgerEntry> findByPaymentId(UUID paymentId);
}
