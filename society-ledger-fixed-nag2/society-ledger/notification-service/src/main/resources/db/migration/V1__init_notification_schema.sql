-- V1__init_notification_schema.sql
-- Notification Service schema

CREATE TABLE IF NOT EXISTS notification_log (
    id              BIGSERIAL PRIMARY KEY,
    society_id      BIGINT       NOT NULL,
    flat_id         BIGINT,
    recipient_email VARCHAR(255) NOT NULL,
    subject         VARCHAR(500) NOT NULL,
    body            TEXT         NOT NULL,
    channel         VARCHAR(30)  NOT NULL DEFAULT 'EMAIL',
    event_type      VARCHAR(50)  NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'SENT'
                    CHECK (status IN ('SENT', 'FAILED', 'SKIPPED')),
    error_message   TEXT,
    sent_at         TIMESTAMPTZ  DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_notif_society ON notification_log(society_id);
CREATE INDEX IF NOT EXISTS idx_notif_flat    ON notification_log(flat_id);
CREATE INDEX IF NOT EXISTS idx_notif_event   ON notification_log(event_type, sent_at DESC);
