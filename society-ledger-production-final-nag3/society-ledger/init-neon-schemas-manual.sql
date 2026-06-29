-- ============================================================
-- Society Ledger — Neon PostgreSQL Schema Initialization
-- Run ONCE on your Neon database to create all schemas.
-- Each microservice then manages its own tables via Flyway.
-- ============================================================

-- Create schemas (Neon uses one DB, multiple schemas)
CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS society;
CREATE SCHEMA IF NOT EXISTS finance;
CREATE SCHEMA IF NOT EXISTS statement;
CREATE SCHEMA IF NOT EXISTS receipt;
CREATE SCHEMA IF NOT EXISTS audit;
CREATE SCHEMA IF NOT EXISTS query_service;
CREATE SCHEMA IF NOT EXISTS notification;
CREATE SCHEMA IF NOT EXISTS audit_log;

-- Grant privileges on all schemas to the application user
-- Replace 'neondb_owner' with your Neon role name if different
DO $$
DECLARE
    schema_name text;
    app_user text := current_user;
BEGIN
    FOR schema_name IN
        SELECT unnest(ARRAY['auth','society','finance','statement',
                            'receipt','audit','query_service',
                            'notification','audit_log'])
    LOOP
        EXECUTE format('GRANT ALL PRIVILEGES ON SCHEMA %I TO %I', schema_name, app_user);
        EXECUTE format('ALTER DEFAULT PRIVILEGES IN SCHEMA %I GRANT ALL ON TABLES TO %I', schema_name, app_user);
        EXECUTE format('ALTER DEFAULT PRIVILEGES IN SCHEMA %I GRANT ALL ON SEQUENCES TO %I', schema_name, app_user);
    END LOOP;
    RAISE NOTICE 'All schemas created and privileges granted.';
END $$;
