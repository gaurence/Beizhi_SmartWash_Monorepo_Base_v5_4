# ITER-08 Manual QA Runbook

## Chunk 01 (2026-02-28)
### What changed (files)
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/AppCustomerController.java`
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/AppCustomerService.java`
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/infrastructure/repository/InMemoryPlatformRepository.java`
- `backend/apps/api-server/src/test/java/com/beizhi/smartwash/PlatformGateIntegrationTest.java`
- `docs/qa/manual/ITER-08.md`

### What was verified (commands + result)
- `make verify-docs` -> PASS
- `make verify-openapi` -> PASS
- `make verify-asyncapi` -> PASS
- `make verify-page-coverage ITER=ITER-08` -> PASS
- `make verify-traceability ITER=ITER-08` -> PASS
- `make verify-visual ITER=ITER-08` -> PASS (mock mode)
- `make backend-test` -> PASS
- `make frontend-build APPS="platform-admin store-admin customer-app"` -> PASS

### How to manually test (role-based)
- `CUSTOMER`
1. Use Customer App token `Bearer mock-jwt:tenant-a:customer-0000` with `X-Permissions: app.order.create,app.order.read`.
2. Call `POST /app/orders` with `Idempotency-Key: idem-app-order-manual-01` and a payload including `servicePackageId`, `addonIds`, `amountSubtotal`, `amountDiscount`, `amountTotal`.
3. Repeat the same request with the same idempotency key; expected: same `orderId` returned.
4. Call `GET /app/orders`, `GET /app/orders/{orderId}`, and `GET /app/orders/{orderId}/timeline`; expected: tenant-a orders only, timeline contains `ORDER_CREATED`.
5. Switch token to `Bearer mock-jwt:tenant-b:customer-0001` and read tenant-a `orderId`; expected: `404`.
- `PLATFORM_ADMIN`
1. Call `GET /audit/logs` with `X-Tenant-Id: tenant-a` and `X-Permissions: audit.log.read`.
2. Expected: record with `action=APP_ORDER_CREATE` exists after successful create call; request metadata (`requestId`, actor, tenant) is present.

### Closed TODO (next chunk)
- Closed on 2026-02-28: merged into consolidated completion pass.

### Execution note
- Commit command failed in this sandbox:
  - `git add ... && git commit -m "ITER-08: add app order api chunk"`
  - Error: `fatal: Unable to create '/Users/michael/Beizhi_SmartWash_Monorepo_Base_v5_4/.git/index.lock': Operation not permitted`
