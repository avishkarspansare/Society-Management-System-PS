-- ============================================================
-- Society Ledger — PostgreSQL Schema Initialization
-- Runs automatically when the local Docker postgres container
-- starts for the first time (via docker-entrypoint-initdb.d).
--
-- Creates the same schema layout as Neon so local dev is
-- identical to production. Each microservice's Flyway migration
-- manages its own tables inside its schema.
-- ============================================================

-- Create all service schemas inside the neondb database
CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS society;
CREATE SCHEMA IF NOT EXISTS finance;
CREATE SCHEMA IF NOT EXISTS statement;
CREATE SCHEMA IF NOT EXISTS receipt;
CREATE SCHEMA IF NOT EXISTS audit;
CREATE SCHEMA IF NOT EXISTS query_service;
CREATE SCHEMA IF NOT EXISTS notification;
CREATE SCHEMA IF NOT EXISTS audit_log;

-- Grant all privileges to the application user on every schema
DO $$
DECLARE
    s text;
BEGIN
    FOREACH s IN ARRAY ARRAY['auth','society','finance','statement',
                              'receipt','audit','query_service',
                              'notification','audit_log']
    LOOP
        EXECUTE format('GRANT ALL PRIVILEGES ON SCHEMA %I TO sl_user', s);
        EXECUTE format(
            'ALTER DEFAULT PRIVILEGES IN SCHEMA %I GRANT ALL ON TABLES TO sl_user', s);
        EXECUTE format(
            'ALTER DEFAULT PRIVILEGES IN SCHEMA %I GRANT ALL ON SEQUENCES TO sl_user', s);
    END LOOP;
    RAISE NOTICE 'All schemas created and privileges granted to sl_user';
END $$;
