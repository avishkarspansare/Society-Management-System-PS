-- V1__init_auth_schema.sql
-- Auth Service schema

CREATE TABLE IF NOT EXISTS user_accounts (
    id            BIGSERIAL PRIMARY KEY,
    society_id    BIGINT    NOT NULL,
    flat_id       BIGINT    NOT NULL UNIQUE,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN', 'RESIDENT')),
    is_active     BOOLEAN  DEFAULT TRUE,
    created_at    TIMESTAMPTZ DEFAULT NOW(),
    updated_at    TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_user_society   ON user_accounts(society_id);
CREATE INDEX IF NOT EXISTS idx_user_flat      ON user_accounts(flat_id);
CREATE INDEX IF NOT EXISTS idx_user_email     ON user_accounts(email);

CREATE TABLE IF NOT EXISTS otp_tokens (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    otp_code   VARCHAR(6)  NOT NULL,
    otp_type   VARCHAR(30) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    is_used    BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_otp_user ON otp_tokens(user_id);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    token      VARCHAR(512) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ  NOT NULL,
    revoked    BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_refresh_user  ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_token ON refresh_tokens(token);
