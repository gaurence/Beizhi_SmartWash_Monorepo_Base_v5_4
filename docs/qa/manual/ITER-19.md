# ITER-19 Manual QA Log

## Chunk 1 - Risk policy backend API slice

### What changed (files)
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/RiskService.java`
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/RiskController.java`
- `backend/apps/api-server/src/test/java/com/beizhi/smartwash/PlatformGateIntegrationTest.java`
- `docs/qa/manual/ITER-19.md`

### What was verified (commands + result)
- `make verify-docs` -> PASS
- `make verify-openapi` -> PASS
- `make verify-asyncapi` -> PASS
- `make verify-page-coverage ITER=ITER-19` -> PASS
- `make verify-traceability ITER=ITER-19` -> PASS
- `make verify-visual ITER=ITER-19` -> PASS (mock mode)
- `make backend-test` -> PASS
- `make frontend-build APPS="platform-admin platform-ops platform-finance platform-cs device-investor"` -> PASS
- `git add ... && git commit -m "ITER-19: risk policy api slice"` -> BLOCKED (`.git/index.lock: Operation not permitted` in current sandbox)

### How to manually test (role-based steps)
- `PLATFORM_ADMIN`:
  1. Call `GET /risk/policies` with `X-Tenant-Id: tenant-a`, `X-Permissions: risk.policy.read`; expect policy list.
  2. Repeat `GET /risk/policies` without `risk.policy.read`; expect `403`.
  3. Pick returned `policyId`, call `PUT /risk/policies/{policyId}` with:
     - `X-Tenant-Id: tenant-a`
     - `X-Permissions: risk.policy.manage`
     - `Idempotency-Key: idem-risk-policy-update-iter19`
     - body `{ "status": "DISABLED", "triggerScore": 88, "reviewWindowMinutes": 45 }`
     - expect updated policy values in response.
  4. Repeat step 3 with same `Idempotency-Key`; expect same `requestId` (idempotent replay).
  5. Cross-tenant check: run step 3 under `X-Tenant-Id: tenant-b` for tenant-a `policyId`; expect `404`.
  6. Audit check: call `GET /audit/logs` with `audit.log.read`; expect action `RISK_POLICY_UPDATE`.

### Closed TODO for next chunk
- Closed on 2026-02-28: merged into consolidated completion pass.

