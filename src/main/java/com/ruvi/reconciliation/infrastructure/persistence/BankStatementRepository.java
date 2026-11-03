package com.ruvi.reconciliation.infrastructure.persistence;

import com.ruvi.reconciliation.domain.reconciliation.BankStatement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BankStatementRepository extends JpaRepository<BankStatement, UUID> {
    List<BankStatement> findByStatementDate(LocalDate statementDate);
}
