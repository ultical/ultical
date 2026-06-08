# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

UltiCal is a tournament and team management platform for Ultimate Frisbee, backed by the German Discsport Federation (DFV). Java/Dropwizard backend with an AngularJS frontend.

## Architecture

- **Backend** (`backend/`): Dropwizard 1.3.7 REST API, Java 8, MyBatis ORM, MySQL 8, Liquibase migrations
- **Frontend** (`web/`): AngularJS with Bower dependencies, Angular UI Router, Bootstrap, German-only i18n
- **Docker** (`docker/`): Development environment via Docker Compose

### Backend Structure (`backend/src/main/java/de/ultical/backend/`)

- `api/` — REST resources (EventsResource, TournamentResource, TeamResource, AuthResource, etc.)
- `app/` — Application bootstrap, configuration, Dropwizard setup
- `data/` — MyBatis mappers and data access
- `model/` — Domain models (uses Lombok)
- `jobs/` — Scheduled tasks (DfvDataSync)

### Key Patterns

- JSON serialization uses JSOG (handles circular references)
- Authentication: Basic Auth with jBCrypt password hashing
- Role-based authorization: overall admins + tournament-specific permissions
- MyBatis for SQL mapping (not JPA/Hibernate)

## Build & Run

### Docker Development (recommended)

```bash
cd docker
docker compose -f compose.dev.yaml up --build
```

Services: web (:8742), backend (:8765), MySQL (:3307), MailHog (:8025), phpMyAdmin (:8750)

### Backend Only

```bash
cd backend
mvn clean package           # Build
mvn test                    # Run tests
mvn liquibase:update        # Apply DB migrations
```

### Frontend

```bash
cd web
bower install
```

## Testing

- Unit tests: Maven Surefire (`mvn test`)
- Integration tests: Maven Failsafe
- Tests use in-memory Derby database
- Tests run single-threaded for database integrity
- MailHog captures emails in development

## Configuration

- `backend/src/main/resources/default.yaml` — Local dev config
- `backend/src/main/resources/default.docker.yaml` — Docker config
- `backend/src/main/resources/default.docker.dev.yaml` — Docker dev config
- `web/config/config.js` — Frontend config (copy from `config.js.dist`)
- `backend/src/main/resources/database/liquibase.properties` — Migration config

## Database

- MySQL 8.0, default credentials: ultical/ultical
- Schema migrations in `backend/src/main/resources/database/db.changelog-1.0.xml`
- Dev port 3307 (mapped to 3306 in container)

## CI

Travis CI runs `mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent test sonar:sonar` with SonarCloud integration.
