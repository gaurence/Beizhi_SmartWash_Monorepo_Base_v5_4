# ITER-04 Manual QA Log

## Chunk 01 (2026-02-28)

### What changed (files)
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/StaffWorkforceController.java`
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/StaffWorkforceService.java`
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/infrastructure/repository/InMemoryPlatformRepository.java`
- `backend/apps/api-server/src/test/java/com/beizhi/smartwash/PlatformGateIntegrationTest.java`

### What was verified (commands + result)
- `make verify-docs` -> PASS
- `make verify-openapi` -> PASS
- `make verify-asyncapi` -> PASS
- `make verify-page-coverage ITER=ITER-04` -> PASS
- `make verify-traceability ITER=ITER-04` -> PASS
- `make verify-visual ITER=ITER-04` -> PASS (mock mode)
- `make backend-test` -> PASS
- `make frontend-build APPS="store-admin technician-app"` -> PASS

### How to manually test (role-based)
- `STORE_ADMIN` (permission includes `workforce.staff.read`, `workforce.staff.create`):
  1. `POST /staff` with header `Idempotency-Key: idem-staff-create-iter04` and body:
     ```json
     {
       "staffType": "TECHNICIAN",
       "employeeCode": "TECH-ITER04-001",
       "displayName": "迭代技师",
       "mobile": "13812345678"
     }
     ```
  2. Repeat the same request with same idempotency key; expected same `staffId`.
  3. `GET /staff?staffType=TECHNICIAN`; expected newly created staff present.
  4. `GET /staff/{staffId}`; expected `mobileMasked=138****5678` (no raw mobile exposed).
- `STORE_ADMIN` without workforce permissions:
  1. `GET /staff` without `workforce.staff.read`; expected `403 FORBIDDEN`.
- Cross-tenant isolation check (`PLATFORM_ADMIN` validation step):
  1. Create staff in `tenant-a`.
  2. Query same `staffId` with `tenant-b` + `workforce.staff.read`; expected `404 NOT_FOUND` (tenant invisible).
- Audit check (`PLATFORM_ADMIN` or `PLATFORM_OPS` with `audit.log.read`):
  1. `GET /audit/logs` after staff creation.
  2. Expected action contains `WORKFORCE_STAFF_CREATE` and request_id is present.

### Closed TODO for next chunk
- Closed on 2026-02-28: merged into consolidated completion pass.

