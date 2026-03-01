# ITER-13 Stage-1 Plan (Plan Only)
## 1) Scope (bounded context / modules / pages)
- Bounded contexts/modules: `Order & Key Custody`, `Cabinet Access/IoT`, `ABAC Access Grant`, `Payment & Invoice`, `Rating`, `Audit & Idempotency`, `Tenant Isolation + RBAC`.
- Subsystems: `Cabinet_Screen`, `Customer_App`.
- `Cabinet_Screen` page_key: `CK_06_Retrieve_Intro`, `CK_07_Retrieve_Open`, `CK_08_Retrieve_Success`.
- `Customer_App` retrieve/delegate page_key: `CA_17_Scan_Retrieve`, `CA_17_1_Retrieve_Instructions`, `CA_17_2_Delegate_Retrieve_Code`.
- `Customer_App` close/rate page_key: `CA_18_Retrieve_Success`, `CA_18_1_Rate_Order`, `CA_18_2_Rate_Success`.
- `Customer_App` payment/invoice page_key: `CA_19_Settlement_Pay`, `CA_19_1_Payment_Success`, `CA_19_2_Payment_Failed`, `CA_19_3_Invoice_Request`, `CA_19_4_Invoice_Headers`.
## 2) Files to change (exact paths)
- Backend: `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/AppCustomerController.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/AppCustomerService.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/infrastructure/repository/InMemoryPlatformRepository.java`, `backend/apps/api-server/src/test/java/com/beizhi/smartwash/PlatformGateIntegrationTest.java`.
- Frontend: `frontend/customer-app/src/pages/GeneratedPage.vue`, `frontend/customer-app/src/router/index.ts`, `frontend/customer-app/src/styles.css`, `frontend/cabinet-screen/src/pages/GeneratedPage.vue`, `frontend/cabinet-screen/src/router/index.ts`, `frontend/cabinet-screen/src/styles.css`.
- Contracts/docs: `docs/contracts/openapi.yaml`, `docs/contracts/asyncapi.yaml`, `docs/traceability/page_to_api.md`, `docs/ui/page_inventory.csv`, `docs/qa/manual/ITER-13.md`.
## 3) APIs/events touched (operationId / topic)
- Existing operationId: `postAppCabinetsScan`, `getAppOrders`, `postAppOrders`, `getAppOrdersByOrderId`, `postAppOrdersByOrderIdDepositKey`, `postAppOrdersByOrderIdRetrieveKey`, `getAppOrdersByOrderIdTimeline`.
- Planned operationId additions: `postAppOrdersByOrderIdDelegateRetrieveCode`, `postAppOrdersByOrderIdRating`, `postAppOrdersByOrderIdPay`, `postAppOrdersByOrderIdInvoiceRequest`, `getAppInvoiceHeaders`, `postAppInvoiceHeaders`.
- Topics/channels: `smartwash.domain.events` (`Order.KeyReturned`, `Order.KeyPickedUp`, `Order.Closed`, `Order.Rated`), `smartwash.finance.events` (`Payment.IntentCreated`, `Payment.Succeeded`), `smartwash.notification.commands`, `mqtt/cabinets/{cabinetId}/commands/open-slot`, `mqtt/cabinets/{cabinetId}/acks/open-slot`.
## 4) Automated verification commands to run (make targets)
- `make verify-docs`; `make verify-openapi`; `make verify-asyncapi`; `make verify-page-coverage ITER=ITER-13`; `make verify-traceability ITER=ITER-13`; `make verify-visual ITER=ITER-13`; `make backend-test`; `make frontend-build APPS="cabinet-screen customer-app"`.
## 5) Manual acceptance checklist by role (links/paths)
- `CUSTOMER` retrieve/delegate: `docs/ui/md/Customer_App/CA_17_Scan_Retrieve.md` -> `CA_17_1_Retrieve_Instructions.md` -> `CA_17_2_Delegate_Retrieve_Code.md`; check扫码开柜、代取码TTL、重复提交幂等、跨租户/无权限拒绝。
- `CUSTOMER` payment/invoice: `docs/ui/md/Customer_App/CA_19_Settlement_Pay.md` -> `CA_19_1_Payment_Success.md`/`CA_19_2_Payment_Failed.md` -> `CA_19_3_Invoice_Request.md` -> `CA_19_4_Invoice_Headers.md`; check支付成功/失败重试、发票抬头管理与申请状态。
- `CUSTOMER` close/rate: `docs/ui/md/Customer_App/CA_18_Retrieve_Success.md` -> `CA_18_1_Rate_Order.md` -> `CA_18_2_Rate_Success.md`; check评分提交、PII脱敏、审计记录。
- `CABINET_SCREEN`: `docs/ui/md/Cabinet_Screen/CK_06_Retrieve_Intro.md` -> `CK_07_Retrieve_Open.md` -> `CK_08_Retrieve_Success.md`; check开柜提示、关门确认、成功页跳转。
- `OPS/AUDITOR`: `docs/qa/manual/ITER-13.md` + `/audit/logs`; check高风险动作审计字段(`request_id`,`actor`,`tenant`,`resource`,`result`,`ip`,`user-agent`)完整。
## 6) Risks/assumptions and what is explicitly out of scope
- Risks: `openapi.yaml` currently缺少 app 侧支付/发票/评分/代取码接口；部分页面 MD 为通用模板，交互需以 SVG 文案和追踪矩阵补全。
- Assumptions: tenant 仅来自 JWT；代取/取回使用短时 ABAC token（绑定 `tenant_id/user_id/order_id/cabinet_id/slot_id/action/ttl`）；所有高风险动作写 append-only audit。
- Out of scope: 真实支付渠道联调、真实发票服务对接、BLE/硬件链路深度联调、非 ITER-13 页面改造。
