package com.ruvi.reconciliation.domain.payment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    private UUID id;

    @Column(name = "idempotency_key", nullable = false, length = 80)
    private String idempotencyKey;

    @Column(name = "merchant_id", nullable = false, length = 64)
    private String merchantId;

    @Column(name = "customer_id", nullable = false, length = 64)
    private String customerId;

    @Column(name = "amount_minor", nullable = false)
    private long amountMinor;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private PaymentStatus status;

    @Column(name = "external_ref", length = 80)
    private String externalRef;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Payment() { }

    public static Payment authorize(String idempotencyKey, String merchantId, String customerId,
                                    long amountMinor, String currency) {
        Payment p = new Payment();
        p.id = UUID.randomUUID();
        p.idempotencyKey = idempotencyKey;
        p.merchantId = merchantId;
        p.customerId = customerId;
        p.amountMinor = amountMinor;
        p.currency = currency;
        p.status = PaymentStatus.AUTHORIZED;
        p.externalRef = "BNK-" + p.id.toString().substring(0, 8).toUpperCase();
        return p;
    }

    public UUID getId() { return id; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getMerchantId() { return merchantId; }
    public String getCustomerId() { return customerId; }
    public long getAmountMinor() { return amountMinor; }
    public String getCurrency() { return currency; }
    public PaymentStatus getStatus() { return status; }
    public String getExternalRef() { return externalRef; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
