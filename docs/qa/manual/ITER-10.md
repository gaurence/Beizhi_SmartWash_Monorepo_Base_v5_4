# ITER-10 Manual QA Runbook

## Chunk 01 (2026-02-28)
### What changed (files)
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/AppCustomerController.java`
- `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/AppCustomerService.java`
- `docs/contracts/openapi.yaml`
- `docs/traceability/page_to_api.md`
- `docs/qa/manual/ITER-10.md`

### What was verified (commands + result)
- `make verify-docs` -> PASS
- `make verify-openapi` -> PASS
- `make verify-asyncapi` -> PASS
- `make verify-page-coverage ITER=ITER-10` -> PASS
- `make verify-traceability ITER=ITER-10` -> PASS
- `make verify-visual ITER=ITER-10 APPS="platform-admin platform-ops store-admin customer-app"` -> PASS (mock mode)
- `make backend-test` -> PASS
- `make frontend-build APPS="platform-admin platform-ops store-admin customer-app"` -> PASS

### How to manually test (role-based steps)
- `CUSTOMER`（取消订单成功路径）
1. 使用 `Bearer mock-jwt:tenant-a:customer-0000`，`X-Permissions: app.order.read,app.order.create`。
2. 调用 `POST /app/orders` 创建订单，记录 `orderId`。
3. 调用 `POST /app/orders/{orderId}/cancel`（可带 `Idempotency-Key` + `{"reason":"PLAN_CHANGE"}`）。
4. 预期：返回 `status=CANCELLED`；`GET /app/orders/{orderId}/timeline` 包含 `ORDER_CANCELLED`；`GET /audit/logs` 可查 `APP_ORDER_CANCEL` 记录。
- `CUSTOMER`（失败/幂等路径）
1. 对同一 `orderId` 重复发送相同 `Idempotency-Key` 的取消请求。
2. 预期：返回同一业务结果，不产生重复状态流转。
3. 用 `tenant-b` token 取消 `tenant-a` 的订单。
4. 预期：`404`（租户隔离）。

### Closed TODO (next chunk)
- Closed on 2026-02-28: merged into consolidated completion pass.

### Execution note
- 提交命令受当前环境权限限制未能执行：
  - `git add backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/AppCustomerController.java backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/AppCustomerService.java docs/contracts/openapi.yaml docs/traceability/page_to_api.md docs/qa/manual/ITER-10.md && git commit -m "ITER-10: app order cancel endpoint"`
  - `fatal: Unable to create '/Users/michael/Beizhi_SmartWash_Monorepo_Base_v5_4/.git/index.lock': Operation not permitted`
