package com.ruvi.reconciliation.domain.ledger;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Column(name = "account_id", nullable = false, length = 64)
    private String accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 8)
    private EntryType entryType;

    @Column(name = "amount_minor", nullable = false)
    private long amountMinor;

    @Column(nullable = false, length = 3)
    private String currency;

    @CreationTimestamp
    @Column(name = "posted_at", nullable = false, updatable = false)
    private Instant postedAt;

    protected LedgerEntry() { }

    public static LedgerEntry of(UUID paymentId, String accountId, EntryType type, long amountMinor, String currency) {
        LedgerEntry e = new LedgerEntry();
        e.paymentId = paymentId;
        e.accountId = accountId;
        e.entryType = type;
        e.amountMinor = amountMinor;
        e.currency = currency;
        return e;
    }

    public Long getId() { return id; }
    public UUID getPaymentId() { return paymentId; }
    public String getAccountId() { return accountId; }
    public EntryType getEntryType() { return entryType; }
    public long getAmountMinor() { return amountMinor; }
    public String getCurrency() { return currency; }
    public Instant getPostedAt() { return postedAt; }
}
