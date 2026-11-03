package com.ruvi.reconciliation.domain.outbox;

public enum OutboxStatus {
    PENDING,
    DISPATCHING,
    PUBLISHED,
    FAILED
}
