# ITER-09 Manual QA Runbook

## Chunk 01 (2026-02-28)
### What changed (files)
- `docs/qa/manual/ITER-09.md`

### What was verified (commands + result)
- `make verify-docs` -> PASS
- `make verify-openapi` -> PASS
- `make verify-asyncapi` -> PASS
- `make verify-page-coverage ITER=ITER-09` -> PASS
- `make verify-traceability ITER=ITER-09` -> PASS
- `make verify-visual ITER=ITER-09` -> PASS (mock mode)
- `make backend-test` -> PASS
- `make frontend-build APPS="customer-app cabinet-screen store-admin"` -> PASS

### How to manually test (role-based)
- `CUSTOMER`（下单与存钥匙模式A）
1. 使用 `Bearer mock-jwt:tenant-a:customer-0000`，`X-Permissions: app.cabinet.scan,app.order.create,app.order.read,app.order.deposit`.
2. 进入 `CA_07_Site_Detail -> CA_08_0_Fulfillment_Mode_Select -> CA_08_Create_Order`，提交订单并记录 `orderId`。
3. 进入 `CA_09_Slot_Reserve` 发起预占；预占成功后进入 `CA_10_Scan_Deposit`，扫码进入 `CA_11_Deposit_Instructions` 并完成存钥匙。
4. 预期：看到 `CA_12_Deposit_Success`，`GET /app/orders/{orderId}/timeline` 出现 `ORDER_CREATED -> SLOT_RESERVED -> KEY_DEPOSITED -> QUEUED`。
5. 失败路径：重复提交同一 `Idempotency-Key`，预期返回相同业务结果；切换为 `tenant-b` token 访问 `tenant-a` 的 `orderId`，预期 `404`。
- `STORE_ADMIN`（订单看板）
1. 使用 `Bearer mock-jwt:tenant-a:store-admin`，`X-Permissions: store.order.read,store.order.dispatch,store.order.cancel`。
2. 打开 `SA_03_Order_Board`，通过 `SA_03_1_Order_Board_Filter_Drawer` 过滤 `queue/in_wash/pending_pickup`。
3. 预期：仅可见本租户订单；执行派单/取消时状态流转与权限匹配。
- `PLATFORM_IOTOPS`（柜机链路）
1. 使用 `Bearer mock-jwt:tenant-a:iot-admin`，`X-Permissions: iot.cabinet.command.open,iot.cabinet.events.read,audit.log.read`。
2. 在 `CK_00_Select_Mode -> CK_02_Scan_QR -> CK_03_Open_Slot -> CK_04_Deposit_Place_Key -> CK_05_Deposit_Success` 完成一次链路。
3. 查询 `GET /iot/cabinets/{cabinetId}/events` 与 `GET /audit/logs`。
4. 预期：存在开格口/存钥匙事件，审计包含 `request_id`、actor、tenant、resource、result。

### Closed TODO (next chunk)
- Closed on 2026-02-28: merged into consolidated completion pass.

### Execution note
- 提交命令失败（沙箱权限限制）：
  - `git add docs/qa/manual/ITER-09.md && git commit -m "ITER-09: add chunk-01 manual runbook"`
  - `fatal: Unable to create '/Users/michael/Beizhi_SmartWash_Monorepo_Base_v5_4/.git/index.lock': Operation not permitted`
