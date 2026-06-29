# Society Ledger — Financial Transparency Platform

A production-grade, multi-tenant SaaS platform for housing society financial management.

---

## 🏗️ Architecture

```
Internet → Nginx → React Frontend
                ↓
         API Gateway :8080 (JWT Validation)
                ↓
    ┌─────────────────────────────────────┐
    │  Auth    Society  Finance  Statement │
    │ :8081    :8082    :8083    :8084    │
    │  Receipt  Audit   Query   Notify    │
    │ :8085    :8086    :8087   :8088    │
    └─────────────────────────────────────┘
                ↓               ↓
          PostgreSQL           Kafka
         (per service)       (4 topics)
```

### Services
| Service | Port | Database | Description |
|---|---|---|---|
| API Gateway | 8080 | — | JWT validation, routing |
| Auth Service | 8081 | auth_db | Registration, login, OTP |
| Society Service | 8082 | society_db | Societies, wings, flats, families |
| Finance Service | 8083 | finance_db | Expenses, summaries, announcements |
| Statement Service | 8084 | statement_db | Bank statement parsing, payment matching |
| Receipt Service | 8085 | receipt_db | PDF receipt generation |
| Audit Service | 8086 | audit_db | Audit report management |
| Query Service | 8087 | query_db | Public Q&A module |
| Notification Service | 8088 | notification_db | Kafka consumer, email dispatch |

### Kafka Topics
| Topic | Producer | Consumer | Trigger |
|---|---|---|---|
| `receipt.generated` | Statement Service | Notification | Payment matched |
| `expense.published` | Finance Service | Notification | Expense published |
| `audit.uploaded` | Audit Service | Notification | Audit report added |
| `query.answered` | Query Service | Notification | Admin responds to query |

---

## ⚡ Quick Start (Docker)

### Prerequisites
- Docker Desktop 4.x+ / Docker Engine 24+
- 8 GB RAM minimum (16 GB recommended)
- Ports 80, 8080–8088, 5432, 9092 free

### 1. Clone and configure

```bash
git clone https://github.com/yourorg/society-ledger.git
cd society-ledger
cp .env.example .env
```

Edit `.env`:
```env
# REQUIRED — generate a secure random string:
# openssl rand -base64 64
JWT_SECRET=your-256-bit-secret-here

DB_PASSWORD=choose-a-strong-db-password

# Optional — email notifications
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=noreply@yoursociety.com
MAIL_PASSWORD=your-app-password
MAIL_ENABLED=true
```

### 2. Start everything

```bash
docker compose up --build
```

First start takes ~3–5 minutes (Maven downloads, image builds).

### 3. Access the application

| URL | What |
|---|---|
| http://localhost | React frontend |
| http://localhost:8080/api/v1 | API Gateway |
| http://localhost:8081/actuator/health | Auth health |

### 4. First-time setup

The database is initialized automatically by Flyway. You need to:

1. **Create your first society** (directly in DB for bootstrap):
```sql
\c society_db
INSERT INTO societies (society_name, city, state, contact_email, is_active)
VALUES ('Sunrise Heights', 'Mumbai', 'Maharashtra', 'admin@sunriseheights.com', true);

INSERT INTO wings (society_id, wing_name) VALUES (1, 'A'), (1, 'B'), (1, 'C');

INSERT INTO flats (society_id, wing_id, flat_number, payment_reference_code, is_occupied)
VALUES
  (1, 1, '101', 'A101-MNT', true),
  (1, 1, '102', 'A102-MNT', true),
  (1, 2, '201', 'B201-MNT', true);
```

2. **Register admin via API:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "societyId": 1,
    "flatId": 1,
    "email": "admin@sunriseheights.com",
    "password": "Admin@1234",
    "confirmPassword": "Admin@1234"
  }'
```

> **Note:** The first registered user gets RESIDENT role. Promote to ADMIN directly in DB:
> ```sql
> \c auth_db
> UPDATE user_accounts SET role = 'ADMIN' WHERE email = 'admin@sunriseheights.com';
> ```

3. **Login:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@sunriseheights.com", "password": "Admin@1234"}'
```

---

## 🛠️ Local Development (Without Docker)

### Prerequisites
- Java 21 (Temurin/Liberica)
- Maven 3.9+
- PostgreSQL 16
- Apache Kafka 3.6 (or use Docker just for infra)

### Start infra only

```bash
# Start only Postgres and Kafka via Docker
docker compose up postgres kafka zookeeper -d
```

### Create databases

```bash
psql -U postgres -h localhost -f init-dbs.sql
```

### Run a service

```bash
# Build common-lib first
mvn install -pl common-lib -am -q

# Start auth service
cd auth-service
mvn spring-boot:run -Dspring-boot.run.profiles=default
```

### Run all services (parallel)

```bash
# From project root — starts all 9 services
for svc in auth-service society-service finance-service statement-service \
           receipt-service audit-service query-service notification-service; do
  (cd $svc && mvn spring-boot:run &)
done
cd api-gateway && mvn spring-boot:run
```

### Run frontend

```bash
cd society-ledger-ui
npm install
npm run dev
# Opens at http://localhost:5173
```

---

## 🔒 Security

- **JWT**: HS256, 15-minute access tokens, 7-day refresh tokens with rotation
- **Passwords**: BCrypt with cost factor 12
- **OTP**: 6-digit SecureRandom, 10-minute expiry, max 3 requests/hour
- **Multi-tenancy**: Every query scoped by `society_id` from JWT claims
- **Gateway-auth pattern**: Gateway validates JWT, downstream services read pre-validated headers
- **Internal APIs** (`/api/v1/internal/**`): Secured by network policy in production; `permitAll` in app config

### Production hardening checklist
- [ ] Set strong `JWT_SECRET` (64+ chars) from secrets manager
- [ ] Enable TLS on all service-to-service communication
- [ ] Restrict `INTERNAL` endpoints to cluster network only (Kubernetes NetworkPolicy or Istio)
- [ ] Set `MAIL_ENABLED=true` and configure real SMTP
- [ ] Configure Kafka with authentication (SASL/SCRAM)
- [ ] Use PostgreSQL with SSL
- [ ] Enable audit logging aggregation (forward to ELK/Loki)

---

## 📁 Project Structure

```
society-ledger/
├── pom.xml                    # Parent Maven POM
├── common-lib/                # Shared DTOs, exceptions, security
├── api-gateway/               # Spring Cloud Gateway
├── auth-service/              # JWT auth, OTP, user management
├── society-service/           # Societies, wings, flats, families
├── finance-service/           # Expenses, summaries, announcements
├── statement-service/         # Bank parsing, payment matching
├── receipt-service/           # PDF receipt generation (OpenPDF)
├── audit-service/             # Audit reports
├── query-service/             # Public Q&A
├── notification-service/      # Kafka consumer, email dispatch
├── society-ledger-ui/         # React + MUI frontend
├── docker-compose.yml
├── init-dbs.sql
└── .env.example
```

---

## 🏦 Bank Statement Upload

**Currently supported:** Bank of Baroda (BOB) — CSV format

**Adding a new bank** (Strategy Pattern):
1. Create `XyzStatementParser implements StatementParser` annotated with `@Component`
2. Implement `getBankCode()` returning the bank identifier (e.g. `"HDFC"`)
3. Implement `parse(InputStream, String)` mapping rows to `ParsedTransaction`
4. No changes to `StatementParserFactory` — it auto-discovers via Spring DI

**BoB CSV format expected:**
```
Date,Value Date,Description,Ref No./Cheque No.,Debit,Credit,Balance
01-01-2024,01-01-2024,NEFT B403-MNT,NEFT123,,2500.00,52500.00
```

**Payment Reference format:**
```
{WingPrefix}{FlatNumber}-MNT
Examples: A101-MNT, B403-MNT, C201-MNT
```

---

## 🧪 Running Tests

```bash
# All tests
mvn test

# Specific service
cd statement-service && mvn test

# Specific test class
mvn test -Dtest=BobStatementParserTest -pl statement-service
```

---

## 📊 API Documentation

Once services are running, each service exposes health at `/actuator/health`.

Base URL: `http://localhost:8080/api/v1`

Key endpoints:

```
# Auth
POST   /auth/login
POST   /auth/register
POST   /auth/forgot-password/initiate
POST   /auth/forgot-password/verify-otp
POST   /auth/forgot-password/reset

# Society
GET    /societies/{societyId}
GET    /societies/{societyId}/wings
POST   /societies/{societyId}/wings
GET    /societies/{societyId}/flats
POST   /societies/{societyId}/flats
GET    /societies/{societyId}/flats/{flatId}/family-members

# Finance
GET    /finance/{societyId}/dashboard
GET    /finance/{societyId}/expenses
POST   /finance/{societyId}/expenses
PATCH  /finance/{societyId}/expenses/{id}/publish
GET    /finance/{societyId}/monthly-summary
GET    /finance/{societyId}/timeline

# Statements
POST   /statements/{societyId}/upload      (multipart)
GET    /statements/{societyId}/transactions/unmatched
POST   /statements/{societyId}/transactions/{txnId}/manual-match

# Receipts
GET    /receipts/{societyId}/flat/{flatId}
GET    /receipts/{societyId}/{receiptId}/download

# Audit
POST   /audit/{societyId}/reports          (multipart)
GET    /audit/{societyId}/reports

# Queries
GET    /queries/{societyId}
POST   /queries/{societyId}
POST   /queries/{societyId}/{queryId}/respond
```

---

## 🚀 Production Deployment

### Kubernetes (recommended for production)

Each service has a Dockerfile. Build and push images:

```bash
# Build all images
docker compose build

# Tag and push
docker tag society-ledger-auth-service your-registry.com/sl-auth:1.0.0
docker push your-registry.com/sl-auth:1.0.0
# ... repeat for each service
```

Apply K8s manifests (create in `k8s/` directory):
- `Deployment` per service
- `Service` (ClusterIP) per microservice
- `Ingress` for API Gateway and Frontend
- `ConfigMap` for application.yml overrides
- `Secret` for DB passwords and JWT secret
- `NetworkPolicy` to restrict internal API access

### Environment variables (production)

| Variable | Required | Description |
|---|---|---|
| `JWT_SECRET` | ✅ | Min 256-bit string, from secrets manager |
| `DB_PASSWORD` | ✅ | PostgreSQL password |
| `SPRING_DATASOURCE_URL` | ✅ | Per-service DB JDBC URL |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | ✅ | Kafka broker list |
| `SOCIETY_SERVICE_URL` | ✅ | Internal society-service URL |
| `STATEMENT_SERVICE_URL` | ✅ | Internal statement-service URL |
| `MAIL_HOST` | For notifications | SMTP host |
| `MAIL_USERNAME` | For notifications | SMTP username |
| `MAIL_PASSWORD` | For notifications | SMTP password |
| `MAIL_ENABLED` | | `true` to enable email |
| `RECEIPT_STORAGE_DIR` | | PDF storage path (use PVC in K8s) |
| `AUDIT_STORAGE_DIR` | | Audit file storage path |

---

## 📄 License

MIT — see LICENSE file.
