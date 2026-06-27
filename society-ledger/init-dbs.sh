#!/bin/bash
# Creates all required databases for Society Ledger.
# Runs automatically as PostgreSQL entrypoint init script.
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE DATABASE auth_db;
    CREATE DATABASE society_db;
    CREATE DATABASE finance_db;
    CREATE DATABASE statement_db;
    CREATE DATABASE receipt_db;
    CREATE DATABASE audit_db;
    CREATE DATABASE query_db;
    CREATE DATABASE notification_db;
    GRANT ALL PRIVILEGES ON DATABASE auth_db         TO $POSTGRES_USER;
    GRANT ALL PRIVILEGES ON DATABASE society_db      TO $POSTGRES_USER;
    GRANT ALL PRIVILEGES ON DATABASE finance_db      TO $POSTGRES_USER;
    GRANT ALL PRIVILEGES ON DATABASE statement_db    TO $POSTGRES_USER;
    GRANT ALL PRIVILEGES ON DATABASE receipt_db      TO $POSTGRES_USER;
    GRANT ALL PRIVILEGES ON DATABASE audit_db        TO $POSTGRES_USER;
    GRANT ALL PRIVILEGES ON DATABASE query_db        TO $POSTGRES_USER;
    GRANT ALL PRIVILEGES ON DATABASE notification_db TO $POSTGRES_USER;
EOSQL
