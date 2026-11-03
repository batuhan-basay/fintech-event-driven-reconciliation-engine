package com.ruvi.reconciliation.domain.reconciliation;

public enum DiscrepancyType {
    MISSING_IN_LEDGER,
    MISSING_IN_STATEMENT,
    AMOUNT_MISMATCH,
    CURRENCY_MISMATCH
}
