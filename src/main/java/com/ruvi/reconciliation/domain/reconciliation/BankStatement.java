package com.ruvi.reconciliation.domain.reconciliation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "bank_statements")
public class BankStatement {

    @Id
    private UUID id;

    @Column(name = "statement_date", nullable = false)
    private LocalDate statementDate;

    @Column(nullable = false, length = 64)
    private String source;

    @Column(name = "external_ref", nullable = false, length = 80)
    private String externalRef;

    @Column(name = "amount_minor", nullable = false)
    private long amountMinor;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "posted_at", nullable = false)
    private Instant postedAt;

    @CreationTimestamp
    @Column(name = "imported_at", nullable = false, updatable = false)
    private Instant importedAt;

    protected BankStatement() { }

    public static BankStatement of(String source, String externalRef, LocalDate statementDate,
                                   long amountMinor, String currency, Instant postedAt) {
        BankStatement b = new BankStatement();
        b.id = UUID.randomUUID();
        b.source = source;
        b.externalRef = externalRef;
        b.statementDate = statementDate;
        b.amountMinor = amountMinor;
        b.currency = currency;
        b.postedAt = postedAt;
        return b;
    }

    public UUID getId() { return id; }
    public LocalDate getStatementDate() { return statementDate; }
    public String getSource() { return source; }
    public String getExternalRef() { return externalRef; }
    public long getAmountMinor() { return amountMinor; }
    public String getCurrency() { return currency; }
    public Instant getPostedAt() { return postedAt; }
    public Instant getImportedAt() { return importedAt; }
}
