# ITER-16 Manual QA Notes

## Chunk 1 (Technician notification preferences backend API)

### What changed (files)
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/AppTechController.java`
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/AppTechService.java`
- `backend/apps/api-server/src/test/java/com/beizhi/smartwash/PlatformGateIntegrationTest.java`
- `docs/qa/manual/ITER-16.md`

### Implementation summary
- Added technician notification preference APIs:
  - `GET /app/tech/notification-prefs`
  - `PUT /app/tech/notification-prefs`
- Enforced RBAC checks:
  - `app.tech.notifications.read`
  - `app.tech.notifications.update`
- Implemented tenant+user scoped preference storage in application service (`tenantId::userId`).
- Added idempotent update flow using `Idempotency-Key` on `PUT /app/tech/notification-prefs`.
- Added append-only audit logging for preference updates:
  - `APP_TECH_NOTIFICATION_PREFS_UPDATE`
- Added integration coverage for allow/deny, tenant isolation, idempotency replay stability, and audit existence.

### Verified (commands + result)
- `make verify-docs` -> PASS
- `make verify-openapi` -> PASS
- `make verify-asyncapi` -> PASS
- `make verify-page-coverage ITER=ITER-16` -> PASS
- `make verify-traceability ITER=ITER-16` -> PASS
- `make verify-visual ITER=ITER-16` -> PASS (mock mode)
- `make backend-test` -> PASS
- `make frontend-build APPS="platform-admin platform-ops store-admin customer-app technician-app"` -> PASS

### VCS note
- `git add`/`git commit` is blocked in current execution sandbox (`.git/index.lock: Operation not permitted`), so this chunk is verified but not committed by automation.

## Manual test steps (role-based)

### Role: TECHNICIAN (allow path)
1. Read prefs:
   - `GET /app/tech/notification-prefs`
   - headers: `Authorization: Bearer mock-jwt:tenant-a:tech01`, `X-Permissions: app.tech.notifications.read`
2. Expect `tenantId=tenant-a`, `userId=tech01`, default preference payload.
3. Update prefs:
   - `PUT /app/tech/notification-prefs`
   - headers: `Authorization: Bearer mock-jwt:tenant-a:tech01`, `X-Permissions: app.tech.notifications.update`, `Idempotency-Key: idem-tech-prefs-001`
   - body:
     - `pushEnabled=false`
     - `quietHoursEnabled=true`
     - `quietHoursStart=23:30`
     - `quietHoursEnd=06:30`
4. Replay the same request with same idempotency key; expect stable response (`requestId` unchanged).

### Role: TECHNICIAN (deny path)
1. Call `GET /app/tech/notification-prefs` without `X-Permissions`.
2. Expect `403 Forbidden`.

### Role: TECHNICIAN (tenant isolation)
1. Read tenant-a prefs after update (as above), confirm `pushEnabled=false`.
2. Read with `Authorization: Bearer mock-jwt:tenant-b:tech01` and `X-Permissions: app.tech.notifications.read`.
3. Expect independent tenant-b data (`tenantId=tenant-b` and default `pushEnabled=true`).

### Role: AUDITOR / PLATFORM_ADMIN
1. Call `GET /audit/logs` with `X-Tenant-Id: tenant-a`, `X-Permissions: audit.log.read`.
2. Expect action `APP_TECH_NOTIFICATION_PREFS_UPDATE` present.
3. Verify audit record contains request context fields (`requestId`, tenant, actor, resource, result).

## Closed TODO for next chunk
- Closed on 2026-02-28: merged into consolidated completion pass.

