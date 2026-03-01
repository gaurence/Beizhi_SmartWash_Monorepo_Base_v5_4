# ITER-15 Manual QA Notes

## Chunk 1 (Backend onsite key-cabinet APIs)

### What changed (files)
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/AppCustomerController.java`
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/AppCustomerService.java`
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/infrastructure/repository/InMemoryPlatformRepository.java`
- `backend/apps/api-server/src/test/java/com/beizhi/smartwash/PlatformGateIntegrationTest.java`
- `docs/qa/manual/ITER-15.md`

### Implementation summary
- Added missing ITER-15 customer endpoints:
  - `POST /app/cabinets/scan`
  - `POST /app/orders/{orderId}/deposit-key`
  - `POST /app/orders/{orderId}/retrieve-key`
- Enforced tenant isolation via `RequestContext` tenant usage for all reads/writes.
- Added RBAC checks:
  - `app.cabinet.scan`
  - `app.order.deposit-key`
  - `app.order.retrieve-key`
- Added idempotency for deposit/retrieve transitions using `Idempotency-Key`.
- Added slot-lock guarded state transitions:
  - `RESERVED -> OCCUPIED` on deposit
  - `OCCUPIED -> RESERVED` on retrieve
- Added append-only audit records for high-risk key operations:
  - `APP_ORDER_KEY_DEPOSIT`
  - `APP_ORDER_KEY_RETRIEVE`
- Added cabinet event append entries and order timeline entries for both actions.
- Added permission seeds for the 3 new app permissions.
- Added integration test coverage for:
  - allow/deny
  - tenant isolation
  - idempotency
  - audit existence

### Verified (commands + result)
- `make verify-docs` -> PASS
- `make verify-openapi` -> PASS
- `make verify-asyncapi` -> PASS
- `make verify-page-coverage ITER=ITER-15` -> PASS
- `make verify-traceability ITER=ITER-15` -> PASS
- `make verify-visual ITER=ITER-15 APPS="customer-app platform-admin"` -> PASS (mock mode)
- `make backend-test` -> first run FAIL (lambda capture compile issue in `retrieveKey`), fixed in `AppCustomerService`, re-run PASS
- `make frontend-build APPS="customer-app platform-admin"` -> PASS

## Manual test steps (role-based)

### Role: CUSTOMER (tenant-a)
1. Call `POST /app/cabinets/scan` with:
   - `Authorization: Bearer mock-jwt:tenant-a:customer-0000`
   - `X-Permissions: app.cabinet.scan`
   - body: `{ "keyword": "CAB-A" }`
2. Expect only tenant-a cabinets (e.g. `CAB-A1`) in response.
3. Create an order via `POST /app/orders` with:
   - `X-Permissions: app.order.create`
   - `Idempotency-Key: <key>`
4. Deposit key via `POST /app/orders/{orderId}/deposit-key` with:
   - `X-Permissions: app.order.deposit-key`
   - `Idempotency-Key: <key>`
   - body: `{ "cabinetId": "CAB-A1", "slotNo": "S1" }`
5. Re-send the same request with same idempotency key; expect same `requestId` and stable response.
6. Retrieve key via `POST /app/orders/{orderId}/retrieve-key` with:
   - `X-Permissions: app.order.retrieve-key`
   - `Idempotency-Key: <key>`
   - body: `{ "cabinetId": "CAB-A1", "slotNo": "S1" }`
7. Re-send retrieve request with same key; expect idempotent stable response.
8. Verify timeline `GET /app/orders/{orderId}/timeline` contains:
   - `ORDER_KEY_DEPOSITED`
   - `ORDER_KEY_RETRIEVED`

### Role: CUSTOMER (cross-tenant negative)
1. Using `Authorization: Bearer mock-jwt:tenant-b:customer-0001`, call:
   - `POST /app/orders/{tenant-a-orderId}/deposit-key`
   - `POST /app/orders/{tenant-a-orderId}/retrieve-key`
2. Expect `404` (cross-tenant invisible/not found behavior).

### Role: AUDITOR / PLATFORM_ADMIN
1. Call `GET /audit/logs` with:
   - `X-Tenant-Id: tenant-a`
   - `X-Permissions: audit.log.read`
2. Expect append-only entries containing actions:
   - `APP_ORDER_KEY_DEPOSIT`
   - `APP_ORDER_KEY_RETRIEVE`
3. Spot-check each contains request context fields (`requestId`, tenant, actor, resource, result).

## Closed TODO for next chunk
- Closed on 2026-02-28: merged into consolidated completion pass.

