# ITER-10 Stage-1 Plan (Plan Only)
## 1) Scope (bounded context / modules / pages)
- Bounded contexts/modules: `Order & Fulfillment`（query/monitor/dispatch/cancel/force-close）, `Timeline`, `Evidence`, `Audit`, `Settlement Read Model`, `SSE/WebSocket Push`; hard constraints: tenant isolation + RBAC/ABAC + idempotency + append-only audit.
- Subsystems: `Platform_Admin_Portal`, `Platform_Ops_Portal`, `Store_Admin_Portal`, `Customer_App`.
- `Platform_Admin_Portal`: `PA_10_1_Orders_Advanced_Filter_Drawer`, `PA_10_Global_Orders_Monitor`, `PA_11_1_Order_Evidence`, `PA_11_2_Order_Settlement_Payment`, `PA_11_3_Order_Audit_Log`, `PA_11_4_Order_Assign_Modal`, `PA_11_5_Order_ForceClose_Modal`, `PA_11_6_Order_Close_Modal`, `PA_11_Order_Detail`.
- `Platform_Ops_Portal`: `PO_03_Orders_Monitor`, `PO_04_1_Order_Evidence`, `PO_04_2_Order_Audit_Log`, `PO_04_3_Order_ForceClose_Modal`, `PO_04_Order_Detail`.
- `Store_Admin_Portal`: `SA_04_1_Order_Evidence`, `SA_04_2_Order_Settlement`, `SA_04_3_Order_Audit`, `SA_04_Order_Detail`.
- `Customer_App`: `CA_12_1_Queue_Detail`, `CA_13_Order_List`, `CA_14_1_Order_Cancel_Confirm`, `CA_14_2_Order_Cancel_Result`, `CA_14_3_Onsite_Timeline`, `CA_14_Order_Detail_Timeline`.
## 2) Files to change (exact paths)
- Backend/API: `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/OrderController.java` (new), `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/EvidenceController.java` (new), `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/StreamController.java` (new), `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/FinanceController.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/AppCustomerController.java`.
- Backend/service/repo: `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/OrderService.java` (new), `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/EvidenceService.java` (new), `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/SettlementService.java` (new), `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/AppCustomerService.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/infrastructure/repository/InMemoryPlatformRepository.java`.
- Backend tests: `backend/apps/api-server/src/test/java/com/beizhi/smartwash/PlatformGateIntegrationTest.java`.
- Frontend: `frontend/platform-admin/src/pages/GeneratedPage.vue`, `frontend/platform-admin/src/styles.css`, `frontend/platform-admin/src/router/index.ts`, `frontend/platform-ops/src/pages/GeneratedPage.vue`, `frontend/platform-ops/src/styles.css`, `frontend/platform-ops/src/router/index.ts`, `frontend/store-admin/src/pages/GeneratedPage.vue`, `frontend/store-admin/src/styles.css`, `frontend/store-admin/src/router/index.ts`, `frontend/customer-app/src/pages/GeneratedPage.vue`, `frontend/customer-app/src/styles.css`, `frontend/customer-app/src/router/index.ts`, `tooling/codegen/generate-frontend.mjs`.
- Contracts/traceability/docs: `docs/contracts/openapi.yaml`, `docs/contracts/asyncapi.yaml`, `docs/traceability/page_to_api.md`, `docs/ui/page_inventory.csv`, `docs/qa/manual/ITER-10.md`.
## 3) APIs/events touched (operationId / topic)
- Order monitor/timeline ops: `getOrders`, `postOrders`, `getOrdersByOrderId`, `postOrdersByOrderIdCancel`, `postOrdersByOrderIdDispatch`, `getOrdersByOrderIdTimeline`, `getAppOrders`, `postAppOrders`, `getAppOrdersByOrderId`, `getAppOrdersByOrderIdTimeline`.
- Evidence/audit/settlement ops: `getOrdersByOrderIdEvidence`, `postEvidenceRecords`, `postEvidenceUploadUrl`, `getAuditLogs`, `getAuditLogsByLogId`, `postAuditExports`, `getFinanceSettlements`, `getFinanceSettlementsBySettlementId`, `getFinanceSettlementsBySettlementIdLines`, `postFinanceSettlementsBySettlementIdExport`.
- Contract补齐（planned new operationId）: `postAppOrdersByOrderIdCancel`, `getStreamOrdersByOrderId`, `getStreamSitesBySiteIdQueue`, `getWsTopics`.
- Async topics/channels: `smartwash.domain.events`（`Order.Created`,`Order.Cancelled`,`Task.Assigned`,`Task.Completed`,`Order.KeyReturned`,`Order.Closed`）, `smartwash.notification.commands`, `smartwash.finance.events`（settlement export notifications）.
## 4) Automated verification commands to run (make targets)
- `make verify-docs`
- `make verify-openapi`
- `make verify-asyncapi`
- `make verify-page-coverage ITER=ITER-10`
- `make verify-traceability ITER=ITER-10`
- `make verify-visual ITER=ITER-10`
- `make backend-test`
- `make frontend-build APPS="platform-admin platform-ops store-admin customer-app"`
## 5) Manual acceptance checklist by role (links/paths)
- `PLATFORM_ADMIN`: `docs/ui/md/Platform_Admin_Portal/PA_10_Global_Orders_Monitor.md` -> `PA_11_Order_Detail.md` -> `PA_11_1_Order_Evidence.md`/`PA_11_2_Order_Settlement_Payment.md`/`PA_11_3_Order_Audit_Log.md` + `PA_11_4/5/6` modals; verify monitor filter, dispatch/close flow, settlement read, audit export.
- `PLATFORM_OPS`: `docs/ui/md/Platform_Ops_Portal/PO_03_Orders_Monitor.md` -> `PO_04_Order_Detail.md` -> `PO_04_1_Order_Evidence.md`/`PO_04_2_Order_Audit_Log.md`/`PO_04_3_Order_ForceClose_Modal.md`; verify ops visibility and force-close authorization.
- `STORE_ADMIN`: `docs/ui/md/Store_Admin_Portal/SA_04_Order_Detail.md` -> `SA_04_1_Order_Evidence.md`/`SA_04_2_Order_Settlement.md`/`SA_04_3_Order_Audit.md`; verify tenant-scoped order list/detail, evidence upload, settlement line read.
- `CUSTOMER`: `docs/ui/md/Customer_App/CA_13_Order_List.md` -> `CA_14_Order_Detail_Timeline.md` -> `CA_14_1_Order_Cancel_Confirm.md` -> `CA_14_2_Order_Cancel_Result.md` + `CA_12_1_Queue_Detail.md`/`CA_14_3_Onsite_Timeline.md`; verify timeline updates + cancel path + deny cases.
## 6) Risks / assumptions / explicit out of scope
- Risks: current OpenAPI lacks app-cancel + stream endpoints; if不补齐会卡 Gate B（traceability/openapi mismatch）.
- Risks: current frontend pages are generic metadata renderers; SVG/MD high-fidelity replication for 24 pages is the main schedule risk.
- Assumptions: tenant only from JWT/request context; all state-changing APIs use `Idempotency-Key`; high-risk actions append `audit_log`; PII remains masked/no sensitive logs.
- Out of scope: real MQTT hardware联调、支付/退款完整链路、非 ITER-10 页面重构、跨子系统主题改版（仅补本迭代 contracts/traceability）。
