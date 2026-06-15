# Deploying UltiCal to Runway (runway.horse)

This guide deploys UltiCal as **three pieces** on Runway:

| Piece | What | Runway resource |
|-------|------|-----------------|
| Database | PostgreSQL (the app is DB-agnostic since the *"selectable DB engine"* change) | managed **service** (`runway service`) |
| Backend | Dropwizard REST API, listens on `:8765` | **app** built from `backend/Dockerfile` |
| Frontend | AngularJS SPA served by nginx on `:8080` | **app** built from `web/Dockerfile` |

> Why Postgres? Runway's managed database is PostgreSQL only (no MySQL), and its
> ingress is HTTP(S)-only today (no cross-app TCP), so a self-hosted MySQL
> container can't be reached on `:3306`. The backend now runs on Postgres *or*
> MySQL purely via env vars, so we target Runway's managed Postgres.

You run every command below from a machine that has **Docker**, **Java 8 + Maven**
(for migrations), and the **Runway CLI**. (The dev sandbox these files were
authored in has none of those, which is why this is a runbook rather than an
automated deploy.)

---

## 0. One-time setup

```bash
# Install the Runway CLI (Linux/macOS)
bash <(curl -s https://www.runway.horse/install.sh)

# Register at https://ui.runway.horse/register, then:
runway login
runway whoami        # confirm you're authenticated
```

---

## 1. Create the managed PostgreSQL service

```bash
runway service create ultical-db          # add -p <plan> if prompted
runway service dsn ultical-db             # prints the connection DSN
```

`runway service dsn` returns a URL like:

```
postgres://USER:PASSWORD@HOST:PORT/DBNAME
```

Keep these four parts handy. JDBC needs them in this form:

```
jdbc:postgresql://HOST:PORT/DBNAME      # note the "jdbc:postgresql://" prefix
```

---

## 2. Apply the database schema (Liquibase)

The app does **not** migrate on startup — run Liquibase once against the new DB
(and again whenever `db.changelog-1.0.xml` changes). The Postgres driver is
already on the plugin classpath.

```bash
cd backend
# liquibase.properties is gitignored; the plugin still needs the file to exist.
# Seed it from the template (the -D flags below override its values anyway).
cp -n src/main/resources/database/liquibase.properties.dist \
      src/main/resources/database/liquibase.properties

mvn liquibase:update \
  -Dliquibase.driver=org.postgresql.Driver \
  -Dliquibase.url="jdbc:postgresql://HOST:PORT/DBNAME" \
  -Dliquibase.username=USER \
  -Dliquibase.password=PASSWORD
```

This creates all tables and runs the `dbms="postgresql"` data-migration
changesets. (The MySQL-only `utf8mb4` changesets are skipped automatically —
Postgres is UTF-8 native.)

---

## 3. Deploy the backend app

### 3a. Build & push the image

Runway pulls images from a registry. Any registry Runway can read works; the
example uses GitHub Container Registry (GHCR). Replace `OWNER` with your org/user.

```bash
cd backend
docker build -t ghcr.io/OWNER/ultical-backend:latest .
echo "$GHCR_TOKEN" | docker login ghcr.io -u OWNER --password-stdin
docker push ghcr.io/OWNER/ultical-backend:latest
```

### 3b. Create the app and its config

```bash
runway app create ultical-api

# Database (engine chosen here — Postgres):
runway app config set DB_DRIVER=org.postgresql.Driver
runway app config set DB_URL="jdbc:postgresql://HOST:PORT/DBNAME"
runway app config set DB_USER=USER
runway app config set DB_PASSWORD=PASSWORD

# App settings (set FRONTEND_URL after step 4 once you know the frontend URL):
runway app config set CORS_ENABLED=true
runway app config set ADMIN_EMAIL=you@example.org
runway app config set DFV_API_SECRET=...        # if you use the DFV sync
runway app config set RECAPTCHA_SECRET=...
runway app config set SMTP_HOST=...  SMTP_PORT=587  SMTP_USER=...  SMTP_PASSWORD=...  SMTP_SENDER=no-reply@your-domain
```

> If the registry is private, first:
> `runway registry add https://ghcr.io OWNER` then
> `runway app registry set https://ghcr.io --app=ultical-api`.

### 3c. Deploy

```bash
runway app deploy image ghcr.io/OWNER/ultical-backend:latest --app=ultical-api
```

The backend is now at `https://ultical-api.pqapp.dev` (HTTPS is automatic).
Runway routes 80/443 → the image's `EXPOSE 8765`.

---

## 4. Deploy the frontend app

The backend URL is baked in at build time via `--build-arg` (the SPA config is a
static JS file the browser loads).

```bash
cd web
docker build --build-arg BACKEND_URL=https://ultical-api.pqapp.dev \
  -t ghcr.io/OWNER/ultical-web:latest .
docker push ghcr.io/OWNER/ultical-web:latest

runway app create ultical-web
runway app deploy image ghcr.io/OWNER/ultical-web:latest --app=ultical-web
```

The frontend is now at `https://ultical-web.pqapp.dev` (nginx `EXPOSE 8080`).

---

## 5. Close the loop (CORS)

Point the backend at the real frontend origin so CORS allows it, then restart:

```bash
runway app config set FRONTEND_URL=https://ultical-web.pqapp.dev --app=ultical-api
runway app restart ultical-api
```

(`CORS_ENABLED=true` is already set; the backend's CORS filter allows all
origins, and `FRONTEND_URL` is used for links in generated emails.)

---

## 6. Custom domains (optional)

```bash
runway app domain add ultical.your-domain.org   --app=ultical-web
runway app domain add api.your-domain.org       --app=ultical-api
```

Add the DNS record Runway prints (CNAME → `paas-prod-ip.runway.horse.`, or an
A record → `5.57.43.177`) plus the one-time TXT ownership record. HTTPS is
auto-provisioned. If you use a custom backend domain, **rebuild the frontend**
with `--build-arg BACKEND_URL=https://api.your-domain.org` and update
`FRONTEND_URL` on the backend.

---

## 7. Verify

```bash
curl -i https://ultical-api.pqapp.dev/                 # backend reachable
runway app config ls --app=ultical-api                 # env looks right
runway service stats ultical-db                        # DB healthy
# open https://ultical-web.pqapp.dev in a browser and exercise login/events
```

---

## Verify locally first (recommended before any deploy)

```bash
cd backend
mvn test            # unit/integration tests (still run on in-memory Derby)
mvn package         # produces target/ultical-backend.jar

# Smoke-test the Postgres path with a throwaway local DB:
docker run --rm -d --name ult-pg -e POSTGRES_USER=ultical \
  -e POSTGRES_PASSWORD=ultical -e POSTGRES_DB=ultical -p 5432:5432 postgres:16
mvn liquibase:update -Dliquibase.driver=org.postgresql.Driver \
  -Dliquibase.url="jdbc:postgresql://localhost:5432/ultical" \
  -Dliquibase.username=ultical -Dliquibase.password=ultical
DB_URL="jdbc:postgresql://localhost:5432/ultical" \
  java -jar target/ultical-backend.jar server src/main/resources/default.docker.yaml
```

---

## Notes & caveats

- **DB engine is just config.** To run the backend on MySQL instead, set
  `DB_DRIVER=com.mysql.cj.jdbc.Driver` and a `DB_URL=jdbc:mysql://…` — no rebuild.
- **Frontend `bower install` is the least-tested step.** Bower is legacy; if a
  package fails to resolve during `docker build`, that's the place to look. The
  Java backend changes, by contrast, only touch portable SQL + config.
- **Migrations are manual** (step 2). Re-run `mvn liquibase:update` after pulling
  changelog changes, before deploying a backend that expects the new schema.
- **`runway app deploy image`** assumes a pre-pushed image. Runway also supports
  a managed registry and source-based deploys — check `runway app --help` /
  `runway registry --help` if you prefer those.
- **Existing MySQL installs are unaffected:** no existing Liquibase changeset was
  modified (only new `dbms="postgresql"` siblings were added), so checksums on a
  live MySQL database stay valid.
