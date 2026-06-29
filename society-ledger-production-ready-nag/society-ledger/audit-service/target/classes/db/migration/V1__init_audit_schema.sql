-- V1__init_audit_schema.sql
-- Audit Service schema

CREATE TABLE IF NOT EXISTS audit_reports (
    id                BIGSERIAL PRIMARY KEY,
    society_id        BIGINT      NOT NULL,
    audit_year        INT         NOT NULL,
    auditor_name      VARCHAR(255) NOT NULL,
    auditor_firm      VARCHAR(255),
    compliance_status VARCHAR(20)  NOT NULL
                      CHECK (compliance_status IN ('COMPLIANT', 'NON_COMPLIANT', 'PENDING')),
    remarks           TEXT,
    issues_found      TEXT,
    report_file_path  VARCHAR(500),
    report_file_name  VARCHAR(255),
    uploaded_by       BIGINT NOT NULL,
    uploaded_at       TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_audit_society ON audit_reports(society_id);
CREATE INDEX IF NOT EXISTS idx_audit_year    ON audit_reports(society_id, audit_year DESC);
