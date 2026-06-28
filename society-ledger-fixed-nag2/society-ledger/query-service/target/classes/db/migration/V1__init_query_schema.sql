-- V1__init_query_schema.sql
-- Query Service schema

CREATE TABLE IF NOT EXISTS public_queries (
    id         BIGSERIAL PRIMARY KEY,
    society_id BIGINT       NOT NULL,
    flat_id    BIGINT       NOT NULL,
    asked_by   BIGINT       NOT NULL,
    subject    VARCHAR(255) NOT NULL,
    body       TEXT         NOT NULL,
    status     VARCHAR(20)  NOT NULL DEFAULT 'OPEN'
               CHECK (status IN ('OPEN', 'ANSWERED', 'CLOSED')),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_query_society ON public_queries(society_id, status);
CREATE INDEX IF NOT EXISTS idx_query_flat    ON public_queries(flat_id);
CREATE INDEX IF NOT EXISTS idx_query_created ON public_queries(society_id, created_at DESC);

CREATE TABLE IF NOT EXISTS query_responses (
    id           BIGSERIAL PRIMARY KEY,
    query_id     BIGINT NOT NULL REFERENCES public_queries(id) ON DELETE CASCADE,
    society_id   BIGINT NOT NULL,
    responded_by BIGINT NOT NULL,
    response     TEXT   NOT NULL,
    created_at   TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_qresp_query ON query_responses(query_id);
