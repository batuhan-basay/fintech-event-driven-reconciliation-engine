CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE payments (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    idempotency_key VARCHAR(80)  NOT NULL,
    merchant_id     VARCHAR(64)  NOT NULL,
    customer_id     VARCHAR(64)  NOT NULL,
    amount_minor    BIGINT       NOT NULL CHECK (amount_minor > 0),
    currency        CHAR(3)      NOT NULL,
    status          VARCHAR(24)  NOT NULL,
    external_ref    VARCHAR(80),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_payments_idempotency UNIQUE (merchant_id, idempotency_key)
);

CREATE INDEX ix_payments_status_created ON payments (status, created_at);
CREATE INDEX ix_payments_external_ref   ON payments (external_ref);

CREATE TABLE ledger_entries (
    id            BIGSERIAL    PRIMARY KEY,
    payment_id    UUID         NOT NULL REFERENCES payments (id),
    account_id    VARCHAR(64)  NOT NULL,
    entry_type    VARCHAR(8)   NOT NULL CHECK (entry_type IN ('DEBIT','CREDIT')),
    amount_minor  BIGINT       NOT NULL CHECK (amount_minor > 0),
    currency      CHAR(3)      NOT NULL,
    posted_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX ix_ledger_payment       ON ledger_entries (payment_id);
CREATE INDEX ix_ledger_account_date  ON ledger_entries (account_id, posted_at DESC);
