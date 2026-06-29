# Society Ledger

A **production-grade, multi-tenant SaaS platform** for financial transparency in housing societies.

## Architecture

```
api-gateway (8080)
├── auth-service        (8081) — JWT issuance, OTP password reset
├── society-service     (8082) — Wings, Flats, Family Members, Activity Audit Log
├── finance-service     (8083) — Expenses, Monthly Summary, Timeline, Announcements
├── statement-service   (8084) — Bank statement upload, CSV parsing, payment matching
├── receipt-service     (8085) — Auto-receipt generation via Kafka
├── audit-service       (8086) — Audit report upload/publish
├── query-service       (8087) — Resident queries & admin answers
└── notification-service(8088) — Email notifications (receipt, query, expense events)
```

## Tech Stack

| Layer | Tech |
|-------|------|
| Backend | Java 21, Spring Boot 3, Spring Security, OpenFeign |
| Database | PostgreSQL 16 (one DB per service) |
| Messaging | Apache Kafka (local Docker) |
| Frontend | React 18 (Vite), Material UI v5 |
| Infrastructure | Docker Compose |

## Quick Start

```bash
# 1. Clone and build common-lib first
cd common-lib && mvn install -DskipTests && cd ..

# 2. Start infrastructure
docker compose up -d postgres kafka kafka-ui

# 3. Start all services (or run individually via IntelliJ)
docker compose up --build

# 4. UI
cd society-ledger-ui && npm install && npm run dev
```

The API Gateway is available at `http://localhost:8080`.
Kafka UI is at `http://localhost:8090`.

## Key Business Flow

```
Admin uploads Bank Statement (CSV)
        ↓
statement-service parses CSV  → stores BankTransaction rows
        ↓
PaymentMatchingService matches credits → Reference Code Match → Description Match
        ↓
    MATCHED?
   YES ──────────────── Kafka: payment.matched ──────→ receipt-service generates Receipt
                                                               ↓
                                                    Kafka: receipt.generated ──→ notification-service sends email
   NO  ──────────────── Kafka: payment.unmatched ──→ Admin manually maps via UI
```

## Service Ports

| Service | Port |
|---------|------|
| API Gateway | 8080 |
| Auth | 8081 |
| Society | 8082 |
| Finance | 8083 |
| Statement | 8084 |
| Receipt | 8085 |
| Audit | 8086 |
| Query | 8087 |
| Notification | 8088 |
| PostgreSQL | 5432 |
| Kafka | 9092 |
| Kafka UI | 8090 |

## Environment Variables

| Variable | Description |
|----------|-------------|
| `JWT_SECRET` | HS256 signing key (min 256-bit) |
| `DB_USERNAME` / `DB_PASSWORD` | PostgreSQL credentials |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | Kafka bootstrap |
| `MAIL_HOST` / `MAIL_USERNAME` / `MAIL_PASSWORD` | SMTP for notifications |
| `FILE_STORAGE_PATH` | Base path for uploaded files |

## Roles

- **ADMIN** — Committee member: uploads statements, manages expenses, answers queries
- **RESIDENT** — Flat resident: views finances, receipts, audit reports, raises queries

## Bank Support

- **Phase 1**: Bank of Baroda (BoB) CSV format
- Future: SBI, HDFC, ICICI, Axis (Strategy Pattern implemented)
