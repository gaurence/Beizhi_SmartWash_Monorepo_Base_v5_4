# ITER-18 Manual QA Log

## Chunk 1 - Refund workflow backend skeleton

### What changed (files)
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/domain/model/RefundRequestRecord.java`
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/RefundService.java`
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/FinanceController.java`
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/infrastructure/repository/InMemoryPlatformRepository.java`
- `docs/qa/manual/ITER-18.md`

### What was verified (commands + result)
- `make verify-docs` -> PASS
- `make verify-openapi` -> PASS
- `make verify-asyncapi` -> PASS
- `make verify-page-coverage ITER=ITER-18` -> PASS
- `make verify-traceability ITER=ITER-18` -> PASS
- `make verify-visual ITER=ITER-18` -> PASS (mock mode)
- `make backend-test` -> PASS
- `make frontend-build APPS="agent-admin customer-app device-investor platform-admin platform-finance platform-ops property-portal store-admin"` -> PASS
- `git add ... && git commit -m "ITER-18: ..."` -> BLOCKED (`.git/index.lock: Operation not permitted` in current sandbox)

### How to manually test (role-based steps)
- `PLATFORM_ADMIN`:
  1. `GET /finance/refunds` with `X-Tenant-Id: tenant-a`, `X-Permissions: finance.refund.read`, expect only tenant-a refund rows.
  2. `POST /finance/refunds` with `X-Permissions: finance.refund.manage` and body:
     - `{ "orderId": "ORD-TENANT-A-0001", "amount": 22.5, "reason": "SERVICE_DELAY", "currency": "CNY" }`
     - Expect status `PENDING_APPROVAL`, no PII in payload.
  3. Repeat step 2 with same `Idempotency-Key`, expect same `refundId`.
  4. `POST /finance/refunds/{refundId}/approve` with same tenant + `finance.refund.manage`, expect status `APPROVED`.
  5. Cross-tenant check: call step 4 under `tenant-b`, expect `404`.
  6. Audit check: `GET /audit/logs` with `audit.log.read`, expect `REFUND_REQUEST_CREATE` and `REFUND_APPROVE`.

### Closed TODO for next chunk
- Closed on 2026-02-28: merged into consolidated completion pass.

