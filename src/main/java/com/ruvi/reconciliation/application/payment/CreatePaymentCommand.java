package com.ruvi.reconciliation.application.payment;

public record CreatePaymentCommand(
        String idempotencyKey,
        String merchantId,
        String customerId,
        long amountMinor,
        String currency
) { }
