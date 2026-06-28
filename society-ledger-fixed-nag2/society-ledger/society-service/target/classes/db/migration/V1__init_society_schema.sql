-- V1__init_society_schema.sql
-- Society Service schema

CREATE TABLE IF NOT EXISTS subscription_plans (
    id              BIGSERIAL PRIMARY KEY,
    plan_name       VARCHAR(50)    NOT NULL UNIQUE,
    max_flats       INT            NOT NULL,
    max_storage_gb  INT            NOT NULL,
    monthly_price   NUMERIC(10, 2) NOT NULL,
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMPTZ DEFAULT NOW()
);

INSERT INTO subscription_plans (plan_name, max_flats, max_storage_gb, monthly_price)
VALUES
    ('FREE',     50,   5,    0.00),
    ('STANDARD', 200,  25,   999.00),
    ('PREMIUM',  1000, 100,  2999.00)
ON CONFLICT (plan_name) DO NOTHING;

CREATE TABLE IF NOT EXISTS societies (
    id                  BIGSERIAL PRIMARY KEY,
    society_name        VARCHAR(255) NOT NULL,
    registration_number VARCHAR(100),
    address             TEXT,
    city                VARCHAR(100),
    state               VARCHAR(100),
    pincode             VARCHAR(10),
    contact_email       VARCHAR(255),
    contact_phone       VARCHAR(20),
    plan_id             BIGINT REFERENCES subscription_plans(id),
    is_active           BOOLEAN DEFAULT TRUE,
    created_at          TIMESTAMPTZ DEFAULT NOW(),
    updated_at          TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS wings (
    id         BIGSERIAL PRIMARY KEY,
    society_id BIGINT NOT NULL REFERENCES societies(id) ON DELETE CASCADE,
    wing_name  VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT uq_wing_name UNIQUE (society_id, wing_name)
);

CREATE INDEX IF NOT EXISTS idx_wings_society ON wings(society_id);

CREATE TABLE IF NOT EXISTS flats (
    id                     BIGSERIAL PRIMARY KEY,
    society_id             BIGINT      NOT NULL REFERENCES societies(id) ON DELETE CASCADE,
    wing_id                BIGINT      NOT NULL REFERENCES wings(id) ON DELETE CASCADE,
    flat_number            VARCHAR(20) NOT NULL,
    floor_number           INT,
    area_sqft              NUMERIC(8, 2),
    payment_reference_code VARCHAR(50) NOT NULL UNIQUE,
    is_occupied            BOOLEAN DEFAULT TRUE,
    created_at             TIMESTAMPTZ DEFAULT NOW(),
    updated_at             TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT uq_flat_number UNIQUE (society_id, wing_id, flat_number)
);

CREATE INDEX IF NOT EXISTS idx_flats_society  ON flats(society_id);
CREATE INDEX IF NOT EXISTS idx_flats_wing     ON flats(wing_id);
CREATE INDEX IF NOT EXISTS idx_flats_ref_code ON flats(payment_reference_code);

CREATE TABLE IF NOT EXISTS family_members (
    id            BIGSERIAL PRIMARY KEY,
    flat_id       BIGINT NOT NULL REFERENCES flats(id) ON DELETE CASCADE,
    society_id    BIGINT NOT NULL REFERENCES societies(id) ON DELETE CASCADE,
    full_name     VARCHAR(255) NOT NULL,
    relation      VARCHAR(50)  NOT NULL
                  CHECK (relation IN ('OWNER','SPOUSE','CHILD','PARENT','TENANT','OTHER')),
    date_of_birth DATE,
    phone         VARCHAR(20),
    is_primary    BOOLEAN DEFAULT FALSE,
    created_at    TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_family_flat    ON family_members(flat_id);
CREATE INDEX IF NOT EXISTS idx_family_society ON family_members(society_id);

CREATE TABLE IF NOT EXISTS activity_audit_log (
    id          BIGSERIAL PRIMARY KEY,
    society_id  BIGINT NOT NULL,
    user_id     BIGINT,
    action      VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id   BIGINT,
    description TEXT,
    ip_address  VARCHAR(45),
    occurred_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_aal_society     ON activity_audit_log(society_id);
CREATE INDEX IF NOT EXISTS idx_aal_user        ON activity_audit_log(user_id);
CREATE INDEX IF NOT EXISTS idx_aal_action      ON activity_audit_log(action);
CREATE INDEX IF NOT EXISTS idx_aal_occurred_at ON activity_audit_log(occurred_at DESC);
