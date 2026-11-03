package com.ruvi.reconciliation.application.payment;

import com.ruvi.reconciliation.application.outbox.OutboxWriter;
import com.ruvi.reconciliation.domain.ledger.EntryType;
import com.ruvi.reconciliation.domain.ledger.LedgerEntry;
import com.ruvi.reconciliation.domain.payment.Payment;
import com.ruvi.reconciliation.infrastructure.persistence.LedgerEntryRepository;
import com.ruvi.reconciliation.infrastructure.persistence.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class CreatePaymentHandler {

    private static final String MERCHANT_ACCOUNT_PREFIX = "merchant:";
    private static final String CUSTOMER_ACCOUNT_PREFIX = "customer:";

    private final PaymentRepository paymentRepository;
    private final LedgerEntryRepository ledgerRepository;
    private final OutboxWriter outboxWriter;
    private final IdempotencyService idempotency;

    public CreatePaymentHandler(PaymentRepository paymentRepository,
                                LedgerEntryRepository ledgerRepository,
                                OutboxWriter outboxWriter,
                                IdempotencyService idempotency) {
        this.paymentRepository = paymentRepository;
        this.ledgerRepository = ledgerRepository;
        this.outboxWriter = outboxWriter;
        this.idempotency = idempotency;
    }

    @Transactional
    public PaymentResult handle(CreatePaymentCommand cmd) {
        var existing = paymentRepository
                .findByMerchantIdAndIdempotencyKey(cmd.merchantId(), cmd.idempotencyKey());
        if (existing.isPresent()) {
            return new PaymentResult(existing.get(), true);
        }

        if (!idempotency.tryAcquire(cmd.merchantId(), cmd.idempotencyKey())) {
            throw new IdempotencyException(
                    "A concurrent payment with the same idempotency key is already in progress");
        }

        Payment payment = Payment.authorize(
                cmd.idempotencyKey(), cmd.merchantId(), cmd.customerId(),
                cmd.amountMinor(), cmd.currency());
        paymentRepository.save(payment);

        ledgerRepository.save(LedgerEntry.of(payment.getId(),
                CUSTOMER_ACCOUNT_PREFIX + cmd.customerId(), EntryType.DEBIT,
                cmd.amountMinor(), cmd.currency()));
        ledgerRepository.save(LedgerEntry.of(payment.getId(),
                MERCHANT_ACCOUNT_PREFIX + cmd.merchantId(), EntryType.CREDIT,
                cmd.amountMinor(), cmd.currency()));

        PaymentAuthorizedEvent event = new PaymentAuthorizedEvent(
                payment.getId(), payment.getMerchantId(), payment.getCustomerId(),
                payment.getAmountMinor(), payment.getCurrency(),
                payment.getExternalRef(), Instant.now());

        outboxWriter.write("Payment", payment.getId().toString(),
                "payment.authorized", event);

        idempotency.commit(cmd.merchantId(), cmd.idempotencyKey(), payment.getId().toString());

        return new PaymentResult(payment, false);
    }

    public record PaymentResult(Payment payment, boolean replayed) { }
}
