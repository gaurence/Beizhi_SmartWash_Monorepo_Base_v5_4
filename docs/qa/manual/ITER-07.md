# ITER-07 Manual QA Runbook

## Chunk 01 (2026-02-28)
### What changed (files)
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/domain/model/CustomerProfile.java`
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/domain/model/Vehicle.java`
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/domain/model/VehicleDocument.java`

### What was verified (commands + result)
- `make verify-docs` -> PASS
- `make verify-openapi` -> PASS
- `make verify-asyncapi` -> PASS
- `make verify-page-coverage ITER=ITER-07` -> PASS
- `make verify-traceability ITER=ITER-07` -> PASS
- `make verify-visual ITER=ITER-07` -> PASS (mock mode)
- `make backend-test` -> PASS
- `make frontend-build APPS="customer-app"` -> PASS

### How to manually test (role-based)
- `CUSTOMER`
1. Login by SMS in Customer App (`13800000000` / `123456`).
2. Open KYC pages (`CA_03_*`) and Vehicle pages (`CA_04_*`, `CA_05_*`) from their configured entries.
3. Confirm pages still render and navigation is intact after backend domain-model addition (no API regression expected in this chunk).
- `PLATFORM_ADMIN`
1. Call `GET /audit/logs` with `audit.log.read` permission.
2. Confirm audit list endpoint is still available (regression smoke for backend startup/tests).

### Closed TODO (next chunk)
- Closed on 2026-02-28: merged into consolidated completion pass.

### Execution note
- `git add ... && git commit -m "ITER-07: add customer-vehicle domain models"` failed in this environment: `fatal: Unable to create '.git/index.lock': Operation not permitted`.
- Cause: sandbox cannot write inside `.git` directory, so commit must be completed outside this sandbox.
