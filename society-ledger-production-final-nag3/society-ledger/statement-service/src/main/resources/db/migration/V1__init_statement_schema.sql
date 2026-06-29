-- V1__init_statement_schema.sql
-- Statement Service schema

CREATE TABLE IF NOT EXISTS bank_statements (
    id               BIGSERIAL PRIMARY KEY,
    society_id       BIGINT      NOT NULL,
    bank_name        VARCHAR(50) NOT NULL,
    account_number   VARCHAR(50),
    statement_month  INT,
    statement_year   INT,
    file_path        VARCHAR(500),
    file_name        VARCHAR(255),
    upload_status    VARCHAR(20) NOT NULL DEFAULT 'PROCESSING'
                     CHECK (upload_status IN ('PROCESSING', 'COMPLETED', 'FAILED')),
    uploaded_by      BIGINT      NOT NULL,
    uploaded_at      TIMESTAMPTZ DEFAULT NOW(),
    processed_at     TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_stmt_society ON bank_statements(society_id);
CREATE INDEX IF NOT EXISTS idx_stmt_year    ON bank_statements(society_id, statement_year, statement_month);

CREATE TABLE IF NOT EXISTS bank_transactions (
    id               BIGSERIAL PRIMARY KEY,
    statement_id     BIGINT         NOT NULL REFERENCES bank_statements(id) ON DELETE CASCADE,
    society_id       BIGINT         NOT NULL,
    transaction_date DATE           NOT NULL,
    value_date       DATE,
    description      TEXT,
    reference_number VARCHAR(255),
    credit_amount    NUMERIC(15, 2) DEFAULT 0,
    debit_amount     NUMERIC(15, 2) DEFAULT 0,
    balance          NUMERIC(15, 2),
    match_status     VARCHAR(20)    NOT NULL DEFAULT 'UNMATCHED'
                     CHECK (match_status IN ('MATCHED', 'UNMATCHED', 'MANUALLY_MATCHED')),
    created_at       TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_txn_statement   ON bank_transactions(statement_id);
CREATE INDEX IF NOT EXISTS idx_txn_society     ON bank_transactions(society_id);
CREATE INDEX IF NOT EXISTS idx_txn_match_status ON bank_transactions(society_id, match_status);
CREATE INDEX IF NOT EXISTS idx_txn_ref         ON bank_transactions(reference_number);
CREATE INDEX IF NOT EXISTS idx_txn_date        ON bank_transactions(society_id, transaction_date DESC);

CREATE TABLE IF NOT EXISTS payment_records (
    id                BIGSERIAL PRIMARY KEY,
    society_id        BIGINT         NOT NULL,
    flat_id           BIGINT         NOT NULL,
    transaction_id    BIGINT         REFERENCES bank_transactions(id),
    payment_month     INT            NOT NULL,
    payment_year      INT            NOT NULL,
    amount            NUMERIC(15, 2) NOT NULL,
    payment_date      DATE           NOT NULL,
    payment_reference VARCHAR(100),
    payment_type      VARCHAR(30)    NOT NULL DEFAULT 'MAINTENANCE',
    match_type        VARCHAR(20)    NOT NULL CHECK (match_type IN ('AUTO', 'MANUAL')),
    created_at        TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT uq_payment_flat_month UNIQUE (flat_id, payment_year, payment_month, payment_type)
);

CREATE INDEX IF NOT EXISTS idx_payment_society ON payment_records(society_id);
CREATE INDEX IF NOT EXISTS idx_payment_flat    ON payment_records(flat_id);
CREATE INDEX IF NOT EXISTS idx_payment_month   ON payment_records(society_id, payment_year, payment_month);

CREATE TABLE IF NOT EXISTS unmatched_transactions (
    id             BIGSERIAL PRIMARY KEY,
    transaction_id BIGINT NOT NULL REFERENCES bank_transactions(id) ON DELETE CASCADE,
    society_id     BIGINT NOT NULL,
    reason         VARCHAR(255),
    resolved       BOOLEAN     DEFAULT FALSE,
    resolved_by    BIGINT,
    resolved_at    TIMESTAMPTZ,
    created_at     TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_unmatched_society ON unmatched_transactions(society_id, resolved);
