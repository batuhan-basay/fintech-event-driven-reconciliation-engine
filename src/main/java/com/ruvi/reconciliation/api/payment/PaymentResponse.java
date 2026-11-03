package com.ruvi.reconciliation.api.payment;

import com.ruvi.reconciliation.domain.payment.Payment;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(name = "PaymentResponse")
public record PaymentResponse(
        UUID paymentId,
        String merchantId,
        String customerId,
        long amountMinor,
        String currency,
        String status,
        String externalRef,
        Instant createdAt,
        boolean replayed
) {
    public static PaymentResponse of(Payment p, boolean replayed) {
        return new PaymentResponse(
                p.getId(), p.getMerchantId(), p.getCustomerId(),
                p.getAmountMinor(), p.getCurrency(), p.getStatus().name(),
                p.getExternalRef(), p.getCreatedAt(), replayed
        );
    }
}
