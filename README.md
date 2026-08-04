# DentalWave Manager Scheduler

DentalWave is a manager-focused monthly scheduling application for dental offices. Managers can generate and edit schedules, add assistant-specific notes such as `out@2`, publish schedules, and print a compact Monday-through-Thursday landscape calendar.

## Technology

- Frontend: React 19, Vite, and React Router
- Backend: Java 21, Spring Boot 3.4, Spring Security, and JWT authentication
- Database: PostgreSQL
- Password storage: BCrypt hashes

## Local development

Prerequisites: Java 21, Node.js/npm, and PostgreSQL.

Create a local database, then create `DentalWave/src/main/resources/application-local.properties` (this file is ignored by Git):

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/dentalwave
spring.datasource.username=YOUR_DATABASE_USERNAME
spring.datasource.password=YOUR_DATABASE_PASSWORD
app.jwt-secret=YOUR_BASE64_SECRET
app.seed-data=true
```

`app.jwt-secret` must be Base64-encoded and decode to at least 32 bytes. Generate one with a trusted password/secret generator; never commit it.

Start the backend:

```bash
cd DentalWave
./mvnw spring-boot:run
```

Start the frontend in a second terminal:

```bash
cd DentalWave-frontend
npm ci
npm run dev
```

The development frontend uses `http://localhost:8080` for API requests unless `VITE_API_BASE_URL` is set.

## Verification commands

```bash
cd DentalWave-frontend
npm ci
npm test
npm run lint
npm run build
npm audit --omit=dev
```

```bash
cd DentalWave
./mvnw test
./mvnw package -DskipTests
```

Skipping tests is useful only to inspect the packaged artifact. It is not a substitute for a passing test suite before release.

## Production configuration

Build the frontend with `npm run build` and the backend with `./mvnw package`. The backend production profile requires:

| Variable | Required | Purpose |
|---|---:|---|
| `SPRING_PROFILES_ACTIVE=production` | Yes | Enables production-safe configuration |
| `DB_URL` | Yes | PostgreSQL JDBC URL |
| `DB_USERNAME` | Yes | Database account |
| `DB_PASSWORD` | Yes | Database password |
| `JWT_SECRET` | Yes | Base64 secret decoding to at least 32 bytes |
| `JWT_EXPIRATION_MS` | No | Token lifetime; defaults to 7 days |
| `CORS_ALLOWED_ORIGINS` | When cross-origin | Comma-separated frontend origins |
| `VITE_API_BASE_URL` | When separately hosted | Backend URL embedded during the frontend build |
| `MAIL_USERNAME` | Only for email | SMTP username |
| `MAIL_PASSWORD` | Only for email | SMTP password |

Do not enable `SEED_DATA` in production. It is an explicit development-only switch.

Start the packaged backend with:

```bash
java -jar DentalWave/target/assistant-scheduler-0.0.1-SNAPSHOT.jar --spring.profiles.active=production
```

The production profile uses `spring.jpa.hibernate.ddl-auto=validate`; the database schema must already exist. This repository does not yet include a migration system, so database migrations must be added before a managed production release.

The frontend `dist` directory is not automatically bundled into the Spring Boot JAR. Deploy it to a static web host with SPA fallback routing, or add a build step that copies it into Spring Boot static resources. If frontend and backend use different origins, set both `VITE_API_BASE_URL` and `CORS_ALLOWED_ORIGINS`.

## Windows USB deployment status

The application is not yet a self-contained Windows USB application. It currently requires Java 21, a separately running PostgreSQL database, and separate frontend/backend processes. A true plug-in-and-run USB release still needs:

1. A portable data strategy, such as an embedded database stored on the USB, with a tested migration path from PostgreSQL.
2. A bundled Windows Java runtime and launcher.
3. A production build that serves the frontend and API together.
4. Backup/recovery behavior for USB removal, drive-letter changes, and corrupted files.
5. Passing frontend lint, backend tests against an isolated test database, and final security review.

Do not distribute the current build as the final USB edition until those items are complete.
