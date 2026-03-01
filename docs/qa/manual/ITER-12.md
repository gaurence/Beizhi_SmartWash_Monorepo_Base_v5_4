# ITER-12 Manual Log

## Chunk 1 (2026-02-28)

### What changed (files)
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/OrderService.java` (new)
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/OrderController.java` (new)
- `backend/apps/api-server/src/test/java/com/beizhi/smartwash/PlatformGateIntegrationTest.java` (updated)

### What was verified
- `make verify-page-coverage ITER=ITER-12` -> PASS
- `make verify-traceability ITER=ITER-12` -> PASS
- `make verify-visual ITER=ITER-12` -> PASS (`mode=mock`)
- `make backend-test` -> PASS
- `make frontend-build APPS="customer-app store-admin technician-app"` -> PASS

### How to manually test (role-based)
1. `STORE_ADMIN` (tenant-a; permissions `order.read,order.manage,audit.log.read`)
- `GET /orders` returns tenant-a orders only (contains `ORD-TENANT-A-0001`, not tenant-b IDs).
- `POST /orders` with `Idempotency-Key` called twice returns same `orderId`.
- `POST /orders/{orderId}/dispatch` with `Idempotency-Key` called twice returns `status=DISPATCHED` and same `requestId`.
- `POST /orders/{orderId}/cancel` with `Idempotency-Key` called twice returns `status=CANCELLED` and same `requestId`.
- `GET /orders/{orderId}/timeline` contains `ORDER_DISPATCHED` and `ORDER_CANCELLED`.
- `GET /audit/logs` contains `ORDER_DISPATCH` and `ORDER_CANCEL` actions.

2. `STORE_ADMIN` deny path (tenant-a; missing permission)
- `GET /orders` without `order.read` returns `403`.

3. Cross-tenant isolation check (`tenant-b` with `order.read`)
- `GET /orders/{tenant-a-orderId}` returns `404`.

### Closed TODO for next chunk
- Closed on 2026-02-28: merged into consolidated completion pass.

## Chunk 2 (2026-02-28)

### What changed (files)
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/infrastructure/repository/InMemoryPlatformRepository.java` (added order evidence in-memory store + seed records for tenant orders)
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/OrderService.java` (added `getOrderEvidence` with tenant isolation + `order.read` check)
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/OrderController.java` (added `GET /orders/{orderId}/evidence`)
- `backend/apps/api-server/src/test/java/com/beizhi/smartwash/PlatformGateIntegrationTest.java` (added integration test for allow/deny/cross-tenant evidence read)

### What was verified
- `make verify-page-coverage ITER=ITER-12` -> PASS
- `make verify-traceability ITER=ITER-12` -> PASS
- `make verify-visual ITER=ITER-12` -> PASS (`mode=mock`)
- `make backend-test` -> PASS
- `make frontend-build APPS="customer-app store-admin technician-app"` -> PASS

### How to manually test (role-based)
1. `STORE_ADMIN` allow path (tenant-a; permission `order.read`)
- Call `GET /orders/ORD-TENANT-A-0001/evidence`.
- Expect `200` with evidence list containing immutable IDs like `EVD-ORD-TENANT-A-0001-001`.
- Verify each item includes `stage/mediaType/objectKey/sha256/capturedAt/requestId`.

2. `STORE_ADMIN` deny path (tenant-a; missing `order.read`)
- Call `GET /orders/ORD-TENANT-A-0001/evidence`.
- Expect `403`.

3. Cross-tenant isolation (`tenant-b` + `order.read`)
- Call `GET /orders/ORD-TENANT-A-0001/evidence`.
- Expect `404` (no cross-tenant visibility).

### Closed TODO for next chunk
- Closed on 2026-02-28: merged into consolidated completion pass.

