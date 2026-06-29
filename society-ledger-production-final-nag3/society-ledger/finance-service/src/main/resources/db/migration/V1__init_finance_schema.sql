-- V1__init_finance_schema.sql
-- Finance Service schema

CREATE TABLE IF NOT EXISTS expense_categories (
    id          BIGSERIAL PRIMARY KEY,
    society_id  BIGINT       NOT NULL,
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    created_at  TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT uq_category_name UNIQUE (society_id, name)
);

CREATE INDEX IF NOT EXISTS idx_cat_society ON expense_categories(society_id);

-- Seed default categories (will be scoped per society at runtime)
-- Actual per-society categories created via API

CREATE TABLE IF NOT EXISTS expenses (
    id              BIGSERIAL PRIMARY KEY,
    society_id      BIGINT         NOT NULL,
    category_id     BIGINT         NOT NULL REFERENCES expense_categories(id),
    amount          NUMERIC(15, 2) NOT NULL,
    vendor_name     VARCHAR(255)   NOT NULL,
    description     TEXT           NOT NULL,
    expense_date    DATE           NOT NULL,
    proof_file_path VARCHAR(500),
    proof_file_name VARCHAR(255),
    status          VARCHAR(20)    NOT NULL DEFAULT 'DRAFT'
                    CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    created_by      BIGINT         NOT NULL,
    published_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ DEFAULT NOW(),
    updated_at      TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_expenses_society ON expenses(society_id);
CREATE INDEX IF NOT EXISTS idx_expenses_status  ON expenses(society_id, status);
CREATE INDEX IF NOT EXISTS idx_expenses_date    ON expenses(society_id, expense_date DESC);

CREATE TABLE IF NOT EXISTS monthly_financial_summary (
    id              BIGSERIAL PRIMARY KEY,
    society_id      BIGINT         NOT NULL,
    year            INT            NOT NULL,
    month           INT            NOT NULL CHECK (month BETWEEN 1 AND 12),
    total_income    NUMERIC(15, 2) DEFAULT 0,
    total_expenses  NUMERIC(15, 2) DEFAULT 0,
    closing_balance NUMERIC(15, 2) DEFAULT 0,
    pending_flats   INT            DEFAULT 0,
    generated_at    TIMESTAMPTZ DEFAULT NOW(),
    updated_at      TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT uq_summary_month UNIQUE (society_id, year, month)
);

CREATE INDEX IF NOT EXISTS idx_mfs_society_year ON monthly_financial_summary(society_id, year, month);

CREATE TABLE IF NOT EXISTS transparency_timeline (
    id            BIGSERIAL PRIMARY KEY,
    society_id    BIGINT       NOT NULL,
    event_type    VARCHAR(50)  NOT NULL,
    event_summary VARCHAR(500) NOT NULL,
    reference_id  BIGINT,
    reference_type VARCHAR(50),
    actor_user_id BIGINT,
    occurred_at   TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_timeline_society ON transparency_timeline(society_id, occurred_at DESC);

CREATE TABLE IF NOT EXISTS announcements (
    id          BIGSERIAL PRIMARY KEY,
    society_id  BIGINT       NOT NULL,
    title       VARCHAR(255) NOT NULL,
    body        TEXT         NOT NULL,
    category    VARCHAR(50),
    is_active   BOOLEAN     DEFAULT TRUE,
    created_by  BIGINT       NOT NULL,
    created_at  TIMESTAMPTZ DEFAULT NOW(),
    expires_at  TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_ann_society ON announcements(society_id, is_active);
