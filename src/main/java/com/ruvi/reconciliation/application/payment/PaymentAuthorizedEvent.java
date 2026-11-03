package com.ruvi.reconciliation.application.payment;

import java.time.Instant;
import java.util.UUID;

public record PaymentAuthorizedEvent(
        UUID paymentId,
        String merchantId,
        String customerId,
        long amountMinor,
        String currency,
        String externalRef,
        Instant occurredAt
) { }
