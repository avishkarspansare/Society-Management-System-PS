-- V1__init_receipt_schema.sql
-- Receipt Service schema

CREATE TABLE IF NOT EXISTS receipts (
    id             BIGSERIAL PRIMARY KEY,
    society_id     BIGINT         NOT NULL,
    flat_id        BIGINT         NOT NULL,
    payment_id     BIGINT         NOT NULL UNIQUE,
    receipt_number VARCHAR(50)    NOT NULL UNIQUE,
    amount         NUMERIC(15, 2) NOT NULL,
    payment_month  INT            NOT NULL,
    payment_year   INT            NOT NULL,
    pdf_file_path  VARCHAR(500),
    resident_name  VARCHAR(255),
    flat_number    VARCHAR(50),
    society_name   VARCHAR(255),
    generated_at   TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_receipt_society ON receipts(society_id);
CREATE INDEX IF NOT EXISTS idx_receipt_flat    ON receipts(flat_id);
CREATE INDEX IF NOT EXISTS idx_receipt_payment ON receipts(payment_id);
CREATE INDEX IF NOT EXISTS idx_receipt_number  ON receipts(receipt_number);
