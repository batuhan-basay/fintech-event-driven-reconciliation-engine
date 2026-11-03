CREATE TABLE bank_statements (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    statement_date  DATE         NOT NULL,
    source          VARCHAR(64)  NOT NULL,
    external_ref    VARCHAR(80)  NOT NULL,
    amount_minor    BIGINT       NOT NULL,
    currency        CHAR(3)      NOT NULL,
    posted_at       TIMESTAMPTZ  NOT NULL,
    imported_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_bank_statement_source_ref UNIQUE (source, external_ref)
);

CREATE INDEX ix_bank_statement_date ON bank_statements (statement_date);

CREATE TABLE reconciliation_runs (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    run_date        DATE         NOT NULL,
    started_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    finished_at     TIMESTAMPTZ,
    status          VARCHAR(16)  NOT NULL DEFAULT 'RUNNING'
                     CHECK (status IN ('RUNNING','COMPLETED','FAILED')),
    matched_count   INT          NOT NULL DEFAULT 0,
    discrepancy_count INT        NOT NULL DEFAULT 0
);

CREATE TABLE discrepancies (
    id                 UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    run_id             UUID        NOT NULL REFERENCES reconciliation_runs (id),
    discrepancy_type   VARCHAR(32) NOT NULL
                       CHECK (discrepancy_type IN
                           ('MISSING_IN_LEDGER','MISSING_IN_STATEMENT','AMOUNT_MISMATCH','CURRENCY_MISMATCH')),
    payment_id         UUID        REFERENCES payments (id),
    bank_statement_id  UUID        REFERENCES bank_statements (id),
    expected_amount    BIGINT,
    actual_amount      BIGINT,
    currency           CHAR(3),
    details            JSONB       NOT NULL DEFAULT '{}'::jsonb,
    detected_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX ix_discrepancies_run  ON discrepancies (run_id);
CREATE INDEX ix_discrepancies_type ON discrepancies (discrepancy_type);
