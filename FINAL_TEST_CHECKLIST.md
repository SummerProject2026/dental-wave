# Final Test Checklist

Audit date: August 30, 2026

Legend: `[x]` verified, `[ ]` not verified or still requires a manual release check.

## Automated gates

- [x] Frontend tests: 16 passed, 0 failed, 0 skipped (`npm test -- --run`)
- [x] Frontend lint exits successfully with 0 errors; 6 hook-dependency warnings remain (`npm run lint`)
- [x] Frontend production build succeeds (`npm run build`)
- [x] Frontend production dependency audit reports 0 known vulnerabilities (`npm audit --omit=dev`)
- [x] Backend tests: 369 passed, 0 failed, 0 errored, 0 skipped (`./mvnw test`)
- [x] Backend package succeeds and includes the built React application (`./mvnw -DskipTests package`)
- [x] Portable Windows launcher path tests: 8 passed (`python3 -m unittest discover -s portable/tests -p 'test_*.py'`)
- [ ] Backend dependency audit: no dependency-security scanner is configured

## Core workflow

- [x] Portable application starts with a disposable file-backed H2 database
- [x] Portable manager login and protected dashboard load successfully
- [ ] Populate representative doctors and assistants and create a complete sample schedule
- [ ] Verify a one-date location override without changing the recurring doctor rule
- [ ] Verify assistant assignment through the complete browser workflow
- [x] Assignment-specific assistant side-note storage has backend and frontend utility coverage
- [x] Notes and announcements carry through the implemented print workflow
- [ ] Verify save, browser refresh, backend restart, and H2 reload persistence with representative data
- [x] Print Preview opens and the approved preview was visually reviewed
- [x] Small, Medium, and Large assistant-name settings are implemented in the print workflow
- [ ] Perform an actual physical print on US Letter paper

## Print sanity

- [x] CSS requests US Letter landscape
- [x] Preview is constrained to one printer-safe page
- [x] Print controls are hidden by print CSS
- [x] Complete borders and aligned location columns appear in the reviewed preview
- [x] Long-name scaling has automated coverage
- [ ] Verify clipping, overlap, and one-page output on the target physical printer

## Security and safety

- [x] Manager frontend routes require a token and manager/admin role
- [x] Protected APIs require authentication; manager schedule mutations use backend role checks
- [x] Ownership and security controller coverage passes within the backend suite
- [x] JWT secrets are externally configured and invalid or missing portable secrets fail startup
- [x] Passwords use BCrypt and no plaintext production credential was found in source control
- [x] Public registration returns 403; administrator-controlled account creation remains protected
- [x] CORS origins are configuration-driven; no authenticated wildcard origin was found
- [x] Production seed data is disabled
- [x] Production persistence uses schema validation rather than destructive create/drop behavior
- [x] Production error details are suppressed
- [x] No password, JWT, or database-secret logging was found
- [ ] Complete a manual invalid-input and identifier-tampering review before any production deployment

## Release decision

**AUTOMATED GATES PASS — MANUAL RELEASE CHECKS REMAIN.** The source tree, frontend build, backend package, tests, and portable launcher path checks pass. Before distributing the Windows bundle as a production release, complete a representative end-to-end scheduling and restart-persistence exercise, test the launchers on Windows, and verify output on the target physical printer.
