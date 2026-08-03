# Manager Scheduler Lite architecture audit

## Baseline (before branch changes)

- Frontend tests: 4 passed.
- Frontend production build: passed.
- Frontend lint: failed with 15 errors and 6 warnings in pre-existing React hook usage, primarily hidden assistant/HR pages plus the legacy manager calendar.
- Backend tests: failed (351 run, 5 failures, 131 errors). Repository tests could not reach the configured PostgreSQL test database; additional pre-existing failures included stale service expectations and incomplete MVC test mocks.
- Backend compilation/build was evaluated separately from the database-dependent suite.

## Existing code reused

- JWT authentication and Spring method authorization.
- `Office` for practice locations.
- `Calendar` as the office/month draft or finalized container.
- `Schedule` as a dated assignment record.
- `ScheduleTeam` as a per-date editable team copy. It remains intentionally separate from reusable templates.
- Existing calendar creation, editing, publishing, employee assignment, and overview/print data paths.

## Additions

- `SchedulingResource` stores doctors and assistants without forcing a login account. It supports active state, offices, workdays, color, and notes.
- `ReusableTeam` stores a doctor, relational assistant membership, default office, color, notes, and active state.
- Manager-only CRUD APIs validate resource types, active state, office IDs, team names, doctor requirements, and duplicate assistant membership.
- The frontend exposes only the manager dashboard, schedule, teams, doctors, assistants, print, and logout workflow.

## Intentionally retained

Legacy assistant, HR, notification, availability, time-off, and employee-account backend code remains in this branch to avoid a destructive data-model rewrite. Its frontend routes are no longer linked in the manager experience, and unsupported paths redirect to the manager dashboard. The original full application remains available on the other branches.

## Persistence note

This project currently uses Hibernate schema management rather than Flyway or Liquibase. The two new tables and relational join tables are therefore created by the configured JPA schema policy; no framework-specific migration file was added.
