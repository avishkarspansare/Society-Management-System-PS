-- Society Ledger Database Initialization Script
-- Run once on PostgreSQL 16

-- Create all service databases
CREATE DATABASE auth_db;
CREATE DATABASE society_db;
CREATE DATABASE finance_db;
CREATE DATABASE statement_db;
CREATE DATABASE receipt_db;
CREATE DATABASE audit_db;
CREATE DATABASE query_db;
CREATE DATABASE notification_db;
CREATE DATABASE audit_log_db;

-- Grant privileges to application user
GRANT ALL PRIVILEGES ON DATABASE auth_db        TO sl_user;
GRANT ALL PRIVILEGES ON DATABASE society_db     TO sl_user;
GRANT ALL PRIVILEGES ON DATABASE finance_db     TO sl_user;
GRANT ALL PRIVILEGES ON DATABASE statement_db   TO sl_user;
GRANT ALL PRIVILEGES ON DATABASE receipt_db     TO sl_user;
GRANT ALL PRIVILEGES ON DATABASE audit_db       TO sl_user;
GRANT ALL PRIVILEGES ON DATABASE query_db       TO sl_user;
GRANT ALL PRIVILEGES ON DATABASE notification_db TO sl_user;
GRANT ALL PRIVILEGES ON DATABASE audit_log_db   TO sl_user;

-- Seed subscription plans in society_db
\c society_db;
INSERT INTO subscription_plans (plan_name, max_flats, max_storage_gb, monthly_price) VALUES
  ('FREE',     50,   5,    0.00),
  ('STANDARD', 200,  25,  999.00),
  ('PREMIUM',  1000, 100, 2999.00)
ON CONFLICT (plan_name) DO NOTHING;
