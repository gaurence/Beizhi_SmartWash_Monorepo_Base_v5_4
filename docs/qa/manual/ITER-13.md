# ITER-13 Manual Log

## Chunk 1 (2026-02-28)

### What changed (files)
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/AppCustomerController.java` (added `POST /app/orders/{orderId}/delegate-retrieve-code` endpoint)
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/AppCustomerService.java` (added delegate retrieve code issue flow with tenant isolation, permission check, idempotency, short TTL token binding, timeline append, and audit write)
- `backend/apps/api-server/src/test/java/com/beizhi/smartwash/PlatformGateIntegrationTest.java` (added integration test for allow/deny/cross-tenant/idempotency/audit on delegate retrieve code API)

### What was verified
- `make verify-docs` -> PASS
- `make verify-openapi` -> PASS
- `make verify-asyncapi` -> PASS
- `make verify-page-coverage ITER=ITER-13` -> PASS
- `make verify-traceability ITER=ITER-13` -> PASS
- `make verify-visual ITER=ITER-13 APPS="cabinet-screen customer-app"` -> PASS (`mode=mock`)
- `make backend-test` -> PASS
- `make frontend-build APPS="cabinet-screen customer-app"` -> PASS

### How to manually test (role-based)
1. `CUSTOMER` allow path (tenant-a; permission `app.order.delegate`)
- Call `POST /app/orders/ORD-TENANT-A-0001/delegate-retrieve-code` with headers:
  - `Authorization: Bearer mock-jwt:tenant-a:customer-0000`
  - `X-Permissions: app.order.delegate`
  - `Idempotency-Key: idem-app-delegate-code-manual-1`
- Request body example:
  - `{"cabinetId":"CAB-A1","slotId":"S1","delegateMobile":"13800138000","ttlSeconds":180}`
- Expect `200`, response includes:
  - `delegateRetrieveCode` (6-digit)
  - `delegateRetrieveToken` (scoped token)
  - `action=RETRIEVE_KEY`
  - `cabinetId=CAB-A1`, `slotId=S1`
  - `delegateMobileMasked=138****8000`
  - `expiresAt` within short TTL window.

2. `CUSTOMER` idempotency path (same tenant/user)
- Repeat the same request with the same `Idempotency-Key` and unchanged body.
- Expect same `delegateRetrieveCode`, same `delegateRetrieveToken`, same `requestId`.

3. `CUSTOMER` deny path (missing permission)
- Call same endpoint without `app.order.delegate` in `X-Permissions`.
- Expect `403`.

4. Cross-tenant isolation
- Call `POST /app/orders/ORD-TENANT-A-0001/delegate-retrieve-code` with:
  - `Authorization: Bearer mock-jwt:tenant-b:customer-0001`
  - `X-Permissions: app.order.delegate`
- Expect `404` (tenant-b cannot access tenant-a order).

5. `OPS/AUDITOR` audit verification (tenant-a)
- Call `GET /audit/logs` with `X-Tenant-Id: tenant-a`, `X-Permissions: audit.log.read`.
- Expect action `APP_ORDER_DELEGATE_RETRIEVE_CODE_ISSUE` exists and includes request-scoped metadata.
- Call `GET /app/orders/ORD-TENANT-A-0001/timeline` as tenant-a customer with `app.order.read`.
- Expect timeline includes `ORDER_DELEGATE_RETRIEVE_CODE_ISSUED`.

### Closed TODO for next chunk
- Closed on 2026-02-28: merged into consolidated completion pass.

