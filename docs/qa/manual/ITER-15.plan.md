# ITER-15 Stage-1 Plan (Plan Only)
## 1) Scope (bounded context / modules / pages)
- Bounded contexts/modules: `Order & Fulfillment (Mode-C onsite)`, `Key Custody`, `Cabinet & IoT`, `Asset/Routing`, `Identity & Access (RBAC+ABAC)`, `Audit + Idempotency + Tenant isolation`.
- Subsystems: `Customer_App`, `Platform_Admin_Portal`.
- page_key (`Customer_App`): `CA_08_5_Onsite_Location_Input`, `CA_08_6_Onsite_KeyCabinet_Select`.
- page_key (`Platform_Admin_Portal`): `PA_42_CabinetRoutingPolicy`.
## 2) Files to change (exact paths)
- Backend: `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/AppCustomerController.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/AppCustomerService.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/CabinetController.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/CabinetService.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/infrastructure/repository/InMemoryPlatformRepository.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/AuditService.java`, `backend/apps/api-server/src/test/java/com/beizhi/smartwash/PlatformGateIntegrationTest.java`.
- Frontend: `tooling/codegen/generate-frontend.mjs`, `frontend/customer-app/src/pages/GeneratedPage.vue`, `frontend/customer-app/src/pages/iter15/CA_08_5_Onsite_Location_Input.vue`, `frontend/customer-app/src/pages/iter15/CA_08_6_Onsite_KeyCabinet_Select.vue`, `frontend/customer-app/src/styles.css`, `frontend/platform-admin/src/pages/PlatformAdminPage.vue`, `frontend/platform-admin/src/pages/iter15/PA_42_CabinetRoutingPolicy.vue`, `frontend/platform-admin/src/styles.css`.
- Contracts/trace/docs: `docs/contracts/openapi.yaml`, `docs/contracts/asyncapi.yaml`, `docs/traceability/page_to_api.md`, `docs/ui/page_inventory.csv` (status-only if needed), `docs/qa/manual/ITER-15.md`.
## 3) APIs/events touched (operationId / topic)
- REST operationId: `postAppCabinetsScan`, `postAppOrdersByOrderIdDepositKey`, `postAppOrdersByOrderIdRetrieveKey`, `getAssetsCabinets`, `getAssetsCabinetsByCabinetId`, `getAssetsCabinetsByCabinetIdSlots`, `postIotCabinetsByCabinetIdCommandsOpenSlot`, `getIotCabinetsByCabinetIdEvents`.
- OpenAPI补齐项（若缺）：Mode-C请求/响应schema、错误码`409/403`语义、幂等头说明与`request_id`返回字段。
- Async topics/event types: `smartwash.domain.events` (`Order.KeyDeposited`, `Order.KeyRetrieved`, `Order.RoutedToStore`), `mqtt/cabinets/{cabinetId}/commands/open-slot`, `mqtt/cabinets/{cabinetId}/acks/open-slot`, `mqtt/cabinets/{cabinetId}/events`, `smartwash.notification.commands`（状态通知）。
## 4) Automated verification commands (make targets)
- `make verify-docs`
- `make verify-openapi`
- `make verify-asyncapi`
- `make verify-page-coverage ITER=ITER-15`
- `make verify-traceability ITER=ITER-15`
- `make verify-visual ITER=ITER-15`
- `make backend-test`
- `make frontend-build APPS="customer-app platform-admin"`
## 5) Manual acceptance checklist by role (links/paths)
- `CUSTOMER`: `docs/ui/md/Customer_App/CA_08_Create_Order.md` -> `CA_08_5_Onsite_Location_Input.md` -> `CA_08_6_Onsite_KeyCabinet_Select.md`; 验证位置填写、附近柜体选择、扫码存钥匙/取钥匙成功+重复提交幂等。
- `PLATFORM_ADMIN`: `docs/ui/md/Platform_Admin_Portal/PA_42_CabinetRoutingPolicy.md`; 验证路由策略查看/保存、高风险操作审计留痕、权限允许场景。
- `PLATFORM_OPS/IOTOPS`: 使用 `GET /iot/cabinets/{cabinetId}/events` + `POST /iot/cabinets/{cabinetId}/commands/open-slot`; 验证在线/离线与异常分支。
- `AUDITOR`: `docs/qa/manual/ITER-15.md` + `/audit/logs`; 核对`request_id,actor,tenant,resource,result,ip,user_agent`与追加写入。
## 6) Risks / assumptions / out of scope
- Risks: 前端入口文件由`codegen-frontend`覆盖，必须先改`tooling/codegen/generate-frontend.mjs`再落地页面；现有MD为模板化描述，交互细节需以SVG+需求章节校准。
- Assumptions: 租户仅来自JWT/Authorization解析；ABAC令牌短时且绑定`tenant_id,user_id,order_id/task_id,cabinet_id/slot_id,action,ttl`；PII仅脱敏展示且不写日志。
- Out of scope: 真实地图/GPS导航、真实MQTT设备联调与BLE离线开锁、非ITER-15页面重构、支付/结算链路改造。
