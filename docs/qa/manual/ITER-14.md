# ITER-14 Manual QA Log

## Chunk 01 - Technician Role Mode API Slice
Date: 2026-02-28

### What changed (files)
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/AppTechController.java`
  - Added `GET /app/tech/profile`.
  - Added `POST /app/tech/role-mode-switch` (with optional `Idempotency-Key`).
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/AppTechService.java`
  - Added profile query with permission check: `app.tech.profile.read`.
  - Added role mode switch with permission check: `app.tech.role-mode.switch`.
  - Added tenant+user scoped in-memory role mode state.
  - Added idempotent switch execution and audit write (`APP_TECH_ROLE_MODE_SWITCH`).
- `docs/contracts/openapi.yaml`
  - Added `getAppTechProfile` for `GET /app/tech/profile`.
  - Added `postAppTechRoleModeSwitch` for `POST /app/tech/role-mode-switch`.
- `docs/traceability/page_to_api.md`
  - Updated `TA_02_2_Switch_Role_Mode` mapping to `app-tech` with endpoints:
    - `GET /app/tech/profile`
    - `POST /app/tech/role-mode-switch`

### What was verified (commands + result)
- `make verify-docs` -> PASS
- `make verify-openapi` -> PASS
- `make verify-asyncapi` -> PASS
- `make verify-page-coverage ITER=ITER-14` -> PASS
- `make verify-traceability ITER=ITER-14` -> PASS
- `make verify-visual ITER=ITER-14 APPS="customer-app store-admin technician-app"` -> PASS (mock mode)
- `make backend-test` -> PASS
- `make frontend-build APPS="customer-app store-admin technician-app"` -> PASS
- Commit attempt from this Codex session -> BLOCKED (`.git/index.lock` write denied by sandbox policy)

### How to manually test (role-based)
1. `TECHNICIAN` success path
- Login token: `Bearer mock-jwt:tenant-a:tech01`.
- Call `GET /api/app/tech/profile` with `X-Permissions: app.tech.profile.read`.
- Expect `currentRoleMode=TECHNICIAN`, `tenantId=tenant-a`, `userId=tech01`.
- Call `POST /api/app/tech/role-mode-switch` with:
  - `X-Permissions: app.tech.role-mode.switch`
  - `Idempotency-Key: idem-iter14-role-switch-01`
  - body: `{ "targetRoleMode": "VALET" }`
- Expect `currentRoleMode=VALET`, response contains `requestId`, and audit action `APP_TECH_ROLE_MODE_SWITCH` written.

2. `TECHNICIAN` failure path (permission deny)
- Call `POST /api/app/tech/role-mode-switch` without `app.tech.role-mode.switch`.
- Expect HTTP `403`.

3. `VALET` role check
- Login token: `Bearer mock-jwt:tenant-a:valet01`.
- Call `GET /api/app/tech/profile` with `X-Permissions: app.tech.profile.read`.
- Expect tenant-bound profile response (`tenantId=tenant-a`, `userId=valet01`).

4. `AUDITOR/OPS` audit check
- Call `GET /api/audit/logs` with `X-Tenant-Id: tenant-a`, `X-Permissions: audit.log.read`.
- Confirm latest log contains `action=APP_TECH_ROLE_MODE_SWITCH` and includes `request_id`, actor, tenant, resource, result.

### Closed TODO for next chunk
- Closed on 2026-02-28: merged into consolidated completion pass.

