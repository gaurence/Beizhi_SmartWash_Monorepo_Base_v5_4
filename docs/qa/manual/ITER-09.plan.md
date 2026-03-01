# ITER-09 Stage-1 Plan (Plan Only)
## 1) Scope (bounded context / modules / pages)
- Bounded contexts/modules: `Order & Fulfillment`（模式A下单/存钥匙）, `Queue & Reservation`（预占TTL/入队）, `Cabinet Access`（扫码开格口/事件回执）, `Idempotency + Audit + Tenant Isolation`.
- Subsystems: `Customer_App`, `Cabinet_Screen`, `Store_Admin_Portal`.
- Page keys: `CA_07_1_Site_Guide`, `CA_07_Site_Detail`, `CA_08_0_Fulfillment_Mode_Select`, `CA_08_Create_Order`, `CA_09_1_Slot_Reserve_Timeout`, `CA_09_Slot_Reserve`, `CA_10_Scan_Deposit`, `CA_11_Deposit_Instructions`, `CA_12_1_Queue_Detail`, `CA_12_Deposit_Success`, `CK_00_Select_Mode`, `CK_01_Welcome`, `CK_02_Scan_QR`, `CK_03_Open_Slot`, `CK_04_Deposit_Place_Key`, `CK_05_Deposit_Success`, `SA_03_1_Order_Board_Filter_Drawer`, `SA_03_Order_Board`.

## 2) Files to change (exact paths)
- Backend existing: `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/AppCustomerController.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/CabinetController.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/AppCustomerService.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/CabinetService.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/IdempotencyService.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/AuditService.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/domain/service/SlotStateMachine.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/infrastructure/repository/InMemoryPlatformRepository.java`, `backend/apps/api-server/src/test/java/com/beizhi/smartwash/PlatformGateIntegrationTest.java`.
- Backend new (planned): `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/OrderController.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/OrderService.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/domain/model/OrderStatus.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/domain/model/KeyCustodyStatus.java`.
- Frontend/codegen: `tooling/codegen/generate-frontend.mjs`, `frontend/customer-app/src/pages/GeneratedPage.vue`, `frontend/customer-app/src/pages/CustomerAppPage.vue`, `frontend/customer-app/src/styles.css`, `frontend/customer-app/src/router/index.ts`, `frontend/cabinet-screen/src/pages/GeneratedPage.vue`, `frontend/cabinet-screen/src/pages/CabinetScreenPage.vue`, `frontend/cabinet-screen/src/styles.css`, `frontend/store-admin/src/pages/GeneratedPage.vue`, `frontend/store-admin/src/pages/StoreAdminPage.vue`, `frontend/store-admin/src/styles.css`.
- Contracts/traceability/docs: `docs/contracts/openapi.yaml`, `docs/contracts/asyncapi.yaml`, `docs/traceability/page_to_api.md`, `docs/ui/page_inventory.csv`, `docs/qa/manual/ITER-09.md`.

## 3) APIs/events touched (operationId / topic)
- OperationIds: `postAppCabinetsScan`, `getAppOrders`, `postAppOrders`, `getAppOrdersByOrderId`, `postAppOrdersByOrderIdDepositKey`, `postAppOrdersByOrderIdRetrieveKey`, `getAppOrdersByOrderIdTimeline`, `getAssetsCabinets`, `getAssetsCabinetsByCabinetId`, `getAssetsCabinetsByCabinetIdSlots`, `postIotCabinetsByCabinetIdCommandsOpenSlot`, `getIotCabinetsByCabinetIdEvents`, `getOrders`, `postOrders`, `getOrdersByOrderId`, `postOrdersByOrderIdCancel`, `postOrdersByOrderIdDispatch`, `getOrdersByOrderIdTimeline`.
- Async topics: `smartwash.domain.events`（`Order.Created`, `Order.KeyDeposited`, `Order.Queued`, `Slot.Reserved`, `Slot.Opened`）, `mqtt/cabinets/{cabinetId}/commands/open-slot`, `mqtt/cabinets/{cabinetId}/acks/open-slot`, `mqtt/cabinets/{cabinetId}/events`, `smartwash.notification.commands`（存钥匙成功/排队更新通知）.

## 4) Automated verification commands to run (make targets)
- `make verify-docs`
- `make verify-openapi`
- `make verify-asyncapi`
- `make verify-page-coverage ITER=ITER-09`
- `make verify-traceability ITER=ITER-09`
- `make verify-visual ITER=ITER-09`
- `make backend-test`
- `make frontend-build APPS="customer-app cabinet-screen store-admin"`

## 5) Manual acceptance checklist by role (links/paths)
- `CUSTOMER`: `docs/ui/md/Customer_App/CA_07_Site_Detail.md` -> `CA_08_0_Fulfillment_Mode_Select.md` -> `CA_08_Create_Order.md` -> `CA_09_Slot_Reserve.md` -> `CA_10_Scan_Deposit.md` -> `CA_11_Deposit_Instructions.md` -> `CA_12_Deposit_Success.md`，覆盖成功链路、超时页(`CA_09_1_Slot_Reserve_Timeout.md`)、重复提交幂等。
- `STORE_ADMIN`: `docs/ui/md/Store_Admin_Portal/SA_03_Order_Board.md` + `SA_03_1_Order_Board_Filter_Drawer.md`，覆盖排队/在洗/待取筛选、派单/取消权限与拒绝场景。
- `PLATFORM_IOTOPS`/`PLATFORM_ADMIN`: `docs/ui/md/Cabinet_Screen/CK_00_Select_Mode.md` -> `CK_03_Open_Slot.md` -> `CK_05_Deposit_Success.md`，联查 `GET /iot/cabinets/{cabinetId}/events` 与 `GET /audit/logs` 验证 request_id、tenant、actor、result。

## 6) Risks / assumptions / explicit out of scope
- Risks: 当前多页 traceability 仍为 `ui-static`，若不补齐 `api_tag + permission_key + api_endpoints` 会直接卡 Gate B。
- Risks: `tooling/codegen/generate-frontend.mjs` 会覆盖 `GeneratedPage.vue`，需先固化三端自定义页面挂载再实现 UI。
- Assumptions: tenant 只取自 JWT/`RequestContextFilter`；高风险动作（开柜/存钥匙/取钥匙/派单/取消）统一 `Idempotency-Key` + append-only `audit_log`；不输出 PII 明文。
- Out of scope: 真实设备 MQTT 联网与硬件 ACK 联调、支付/退款/结算扩展链路、ITER-09 之外页面重做。
