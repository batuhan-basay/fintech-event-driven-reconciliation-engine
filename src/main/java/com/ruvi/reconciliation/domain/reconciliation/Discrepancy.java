package com.ruvi.reconciliation.domain.reconciliation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "discrepancies")
public class Discrepancy {

    @Id
    private UUID id;

    @Column(name = "run_id", nullable = false)
    private UUID runId;

    @Enumerated(EnumType.STRING)
    @Column(name = "discrepancy_type", nullable = false, length = 32)
    private DiscrepancyType discrepancyType;

    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(name = "bank_statement_id")
    private UUID bankStatementId;

    @Column(name = "expected_amount")
    private Long expectedAmount;

    @Column(name = "actual_amount")
    private Long actualAmount;

    @Column(length = 3)
    private String currency;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String details;

    @CreationTimestamp
    @Column(name = "detected_at", nullable = false, updatable = false)
    private Instant detectedAt;

    protected Discrepancy() { }

    public static Discrepancy missingInLedger(UUID runId, UUID bankStatementId, long actual, String currency) {
        Discrepancy d = base(runId, DiscrepancyType.MISSING_IN_LEDGER);
        d.bankStatementId = bankStatementId;
        d.actualAmount = actual;
        d.currency = currency;
        return d;
    }

    public static Discrepancy missingInStatement(UUID runId, UUID paymentId, long expected, String currency) {
        Discrepancy d = base(runId, DiscrepancyType.MISSING_IN_STATEMENT);
        d.paymentId = paymentId;
        d.expectedAmount = expected;
        d.currency = currency;
        return d;
    }

    public static Discrepancy amountMismatch(UUID runId, UUID paymentId, UUID bankStatementId,
                                             long expected, long actual, String currency) {
        Discrepancy d = base(runId, DiscrepancyType.AMOUNT_MISMATCH);
        d.paymentId = paymentId;
        d.bankStatementId = bankStatementId;
        d.expectedAmount = expected;
        d.actualAmount = actual;
        d.currency = currency;
        return d;
    }

    private static Discrepancy base(UUID runId, DiscrepancyType type) {
        Discrepancy d = new Discrepancy();
        d.id = UUID.randomUUID();
        d.runId = runId;
        d.discrepancyType = type;
        d.details = "{}";
        return d;
    }

    public UUID getId() { return id; }
    public UUID getRunId() { return runId; }
    public DiscrepancyType getDiscrepancyType() { return discrepancyType; }
    public UUID getPaymentId() { return paymentId; }
    public UUID getBankStatementId() { return bankStatementId; }
    public Long getExpectedAmount() { return expectedAmount; }
    public Long getActualAmount() { return actualAmount; }
    public String getCurrency() { return currency; }
    public String getDetails() { return details; }
    public Instant getDetectedAt() { return detectedAt; }
}
