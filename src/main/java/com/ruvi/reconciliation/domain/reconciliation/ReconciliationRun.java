package com.ruvi.reconciliation.domain.reconciliation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "reconciliation_runs")
public class ReconciliationRun {

    @Id
    private UUID id;

    @Column(name = "run_date", nullable = false)
    private LocalDate runDate;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ReconciliationStatus status;

    @Column(name = "matched_count", nullable = false)
    private int matchedCount;

    @Column(name = "discrepancy_count", nullable = false)
    private int discrepancyCount;

    protected ReconciliationRun() { }

    public static ReconciliationRun start(LocalDate runDate) {
        ReconciliationRun r = new ReconciliationRun();
        r.id = UUID.randomUUID();
        r.runDate = runDate;
        r.startedAt = Instant.now();
        r.status = ReconciliationStatus.RUNNING;
        r.matchedCount = 0;
        r.discrepancyCount = 0;
        return r;
    }

    public void complete(int matched, int discrepancies) {
        this.matchedCount = matched;
        this.discrepancyCount = discrepancies;
        this.status = ReconciliationStatus.COMPLETED;
        this.finishedAt = Instant.now();
    }

    public void fail() {
        this.status = ReconciliationStatus.FAILED;
        this.finishedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public LocalDate getRunDate() { return runDate; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getFinishedAt() { return finishedAt; }
    public ReconciliationStatus getStatus() { return status; }
    public int getMatchedCount() { return matchedCount; }
    public int getDiscrepancyCount() { return discrepancyCount; }
}
