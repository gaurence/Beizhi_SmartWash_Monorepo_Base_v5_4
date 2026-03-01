# ITER-11 Manual QA (Incremental Chunks)

## Chunk 1 — App Tech Tasks API baseline (`/app/tech/tasks`, accept, complete)

### What changed (files)
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/AppTechController.java`
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/AppTechService.java`
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/infrastructure/repository/InMemoryPlatformRepository.java`
- `backend/apps/api-server/src/test/java/com/beizhi/smartwash/PlatformGateIntegrationTest.java`

### What was verified (commands + result)
- `make verify-docs` -> PASS
- `make verify-openapi` -> PASS
- `make verify-asyncapi` -> PASS
- `make verify-page-coverage ITER=ITER-11` -> PASS
- `make verify-traceability ITER=ITER-11` -> PASS
- `make verify-visual ITER=ITER-11` -> PASS (`mode=mock`, Playwright disabled)
- `make backend-test` -> PASS
- `make frontend-build APPS="platform-admin store-admin technician-app cabinet-screen"` -> PASS

### How to manually test (role-based steps)
1. Technician (`tech01` / `Passw0rd!`):
- Request `GET /app/tech/tasks` with `Authorization: Bearer mock-jwt:tenant-a:tech01` and `X-Permissions: app.task.read`.
- Expected: only tenant-a tasks assigned to `tech01`; no tenant-b task leakage.
- Request `POST /app/tech/tasks/TASK-TENANT-A-0001/accept` with `Idempotency-Key: idem-manual-accept-1` and `X-Permissions: app.task.accept`.
- Repeat same request once.
- Expected: both responses return same business payload (`status=ACCEPTED`, same idempotent response content).
- Request `POST /app/tech/tasks/TASK-TENANT-A-0001/complete` with `Idempotency-Key: idem-manual-complete-1` and `X-Permissions: app.task.complete`.
- Repeat same request once.
- Expected: both responses stable and task ends in `COMPLETED`.

2. Unauthorized technician (`tech02` in same tenant):
- Request `POST /app/tech/tasks/TASK-TENANT-A-0001/complete` with `Authorization: Bearer mock-jwt:tenant-a:tech02`, `X-Permissions: app.task.complete`.
- Expected: `403 FORBIDDEN` (task assignee ABAC check).

3. Cross-tenant technician (`tenant-b`):
- Request `POST /app/tech/tasks/TASK-TENANT-A-0001/accept` with `Authorization: Bearer mock-jwt:tenant-b:tech01`, `X-Permissions: app.task.accept`.
- Expected: `404 NOT_FOUND` (tenant isolation).

4. Platform admin / audit reader:
- Request `GET /audit/logs` with `X-Tenant-Id: tenant-a`, `X-Permissions: audit.log.read`.
- Expected: append-only audit includes actions `APP_TECH_TASK_ACCEPT` and `APP_TECH_TASK_COMPLETE` with request context.

### Closed TODO for next chunk
- Closed on 2026-02-28: merged into consolidated completion pass.

