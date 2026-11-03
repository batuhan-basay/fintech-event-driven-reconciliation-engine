CREATE TABLE saga_instances (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    saga_type       VARCHAR(64)  NOT NULL,
    correlation_id  VARCHAR(80)  NOT NULL,
    current_step    VARCHAR(64)  NOT NULL,
    status          VARCHAR(16)  NOT NULL
                     CHECK (status IN ('STARTED','COMPENSATING','COMPLETED','FAILED')),
    state_payload   JSONB        NOT NULL DEFAULT '{}'::jsonb,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_saga_correlation UNIQUE (saga_type, correlation_id)
);

CREATE INDEX ix_saga_status ON saga_instances (status, updated_at);
