CREATE TABLE outbox_events (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type  VARCHAR(64)  NOT NULL,
    aggregate_id    VARCHAR(80)  NOT NULL,
    event_type      VARCHAR(96)  NOT NULL,
    payload         JSONB        NOT NULL,
    headers         JSONB        NOT NULL DEFAULT '{}'::jsonb,
    status          VARCHAR(16)  NOT NULL DEFAULT 'PENDING'
                     CHECK (status IN ('PENDING','DISPATCHING','PUBLISHED','FAILED')),
    attempts        INT          NOT NULL DEFAULT 0,
    last_error      TEXT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    next_attempt_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    published_at    TIMESTAMPTZ
);

CREATE INDEX ix_outbox_pending ON outbox_events (status, next_attempt_at)
    WHERE status IN ('PENDING','FAILED');
CREATE INDEX ix_outbox_aggregate ON outbox_events (aggregate_type, aggregate_id);
