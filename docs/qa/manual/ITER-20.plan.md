# ITER-20 Stage-1 Plan (Plan Only)
## 1) Scope (bounded context / modules / pages)
- Bounded contexts/modules: `ops-monitoring`, `alerts`, `workorders/exceptions`, `offline-cabinet-error`, `iot-alert-closure`; enforce JWT-derived tenant isolation, RBAC+ABAC, idempotency, append-only audit, PII masking.
- `Platform_Ops_Portal`: `PO_08_1_Exception_Create`, `PO_08_Exception_Tickets`, `PO_09_Exception_Ticket_Detail`.
- `Store_Admin_Portal`: `SA_15_1_Exception_Create`, `SA_15_Exception_Tickets`, `SA_16_1_Assign_Modal`, `SA_16_2_Remark_Drawer`, `SA_16_3_Resolve_Modal`, `SA_16_Exception_Ticket_Detail`.
- `Agent_Admin_Portal` + `Platform_Admin_Portal`: `AA_09_1_WorkOrder_Create`, `AA_09_2_WorkOrder_Detail`, `AA_09_WorkOrders`, `PA_37_1_Ticket_Detail`, `PA_37_Tickets_Center`.
- `Platform_IoTOps_Portal` + `Technician_App`: `PI_09_Alerts_List`, `PI_10_Alert_Detail`, `TA_14_1_Exception_List`, `TA_14_2_Exception_Detail`, `TA_14_Exception_Report`, `TA_15_Offline_Mode`.
- `Cabinet_Screen`: `CK_09_Error_Offline`, `CK_10_Error_SlotFault`, `CK_11_Maintenance`, `CK_12_DoorOpen_Timeout`.
## 2) Files to change (exact paths)
- Backend APIs/services/tests: `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/CabinetController.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/FinanceController.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/OrderController.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/CabinetService.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/OrderService.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/SettlementService.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/OpsTicketService.java` (new), `backend/apps/api-server/src/test/java/com/beizhi/smartwash/PlatformGateIntegrationTest.java`.
- Backend support data/guardrails: `backend/apps/api-server/src/main/java/com/beizhi/smartwash/infrastructure/repository/InMemoryPlatformRepository.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/security/PermissionGuard.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/AuditService.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/IdempotencyService.java`.
- Frontend pages/routes/styles: `frontend/agent-admin/src/pages/GeneratedPage.vue`, `frontend/agent-admin/src/router/index.ts`, `frontend/agent-admin/src/styles.css`, `frontend/platform-ops/src/pages/GeneratedPage.vue`, `frontend/platform-ops/src/router/index.ts`, `frontend/platform-ops/src/styles.css`, `frontend/store-admin/src/pages/GeneratedPage.vue`, `frontend/store-admin/src/router/index.ts`, `frontend/store-admin/src/styles.css`, `frontend/platform-iotops/src/pages/GeneratedPage.vue`, `frontend/platform-iotops/src/router/index.ts`, `frontend/platform-iotops/src/styles.css`, `frontend/technician-app/src/pages/GeneratedPage.vue`, `frontend/technician-app/src/router/index.ts`, `frontend/technician-app/src/styles.css`, `frontend/cabinet-screen/src/pages/GeneratedPage.vue`, `frontend/cabinet-screen/src/router/index.ts`, `frontend/cabinet-screen/src/styles.css`, `frontend/platform-admin/src/pages/GeneratedPage.vue`, `frontend/platform-admin/src/pages/PlatformAdminPage.vue`, `frontend/platform-admin/src/router/index.ts`, `frontend/platform-admin/src/styles.css`.
- Contracts/traceability/docs/tooling: `docs/contracts/openapi.yaml`, `docs/contracts/asyncapi.yaml`, `docs/traceability/page_to_api.md`, `docs/ui/page_inventory.csv`, `docs/qa/manual/ITER-20.md`, `tooling/codegen/generate-frontend.mjs`.
## 3) APIs/events touched (operationId / topic)
- Existing OpenAPI operationIds: `getAssetsCabinets`, `getAssetsCabinetsByCabinetId`, `getAssetsCabinetsByCabinetIdSlots`, `postIotCabinetsByCabinetIdCommandsOpenSlot`, `getIotCabinetsByCabinetIdEvents`, `getFinanceReconciliationTickets`, `postFinanceReconciliationTickets`, `getFinanceReconciliationTicketsByTicketId`, `getOrders`, `postOrders`, `getOrdersByOrderId`, `postOrdersByOrderIdCancel`, `postOrdersByOrderIdDispatch`, `getOrdersByOrderIdTimeline`.
- Iter-20 contract additions (operationIds to add): `getIotAlerts`, `getIotAlertsByAlertId`, `postIotAlertsByAlertIdAcknowledge`, `getOpsExceptionTickets`, `postOpsExceptionTickets`, `getOpsExceptionTicketsByTicketId`, `postOpsExceptionTicketsByTicketIdAssign`, `postOpsExceptionTicketsByTicketIdResolve`, `postOpsExceptionTicketsByTicketIdRemark`.
- AsyncAPI topics: `mqtt/cabinets/{cabinetId}/commands/open-slot`, `mqtt/cabinets/{cabinetId}/acks/open-slot`, `mqtt/cabinets/{cabinetId}/events`, `smartwash.domain.events`, `smartwash.notification.commands`.
## 4) Automated verification commands to run (make targets)
- `make verify-docs`
- `make verify-openapi`
- `make verify-asyncapi`
- `make verify-page-coverage ITER=ITER-20`
- `make verify-traceability ITER=ITER-20`
- `make verify-visual ITER=ITER-20`
- `make backend-test`
- `make frontend-build APPS="agent-admin platform-ops store-admin platform-iotops technician-app cabinet-screen platform-admin"`
## 5) Manual acceptance checklist by role (links/paths)
- `PLATFORM_OPS`: `/pages/PO_08_Exception_Tickets -> /pages/PO_08_1_Exception_Create -> /pages/PO_09_Exception_Ticket_Detail`; refs `docs/ui/md/Platform_Ops_Portal/PO_08_Exception_Tickets.md`, `docs/ui/md/Platform_Ops_Portal/PO_08_1_Exception_Create.md`, `docs/ui/md/Platform_Ops_Portal/PO_09_Exception_Ticket_Detail.md`.
- `STORE_ADMIN`: `/pages/SA_15_Exception_Tickets -> /pages/SA_15_1_Exception_Create -> /pages/SA_16_Exception_Ticket_Detail` + `SA_16_1/2/3`; refs `docs/ui/md/Store_Admin_Portal/SA_15_Exception_Tickets.md`, `docs/ui/md/Store_Admin_Portal/SA_15_1_Exception_Create.md`, `docs/ui/md/Store_Admin_Portal/SA_16_*.md`.
- `AGENT_ADMIN`: `/pages/AA_09_WorkOrders -> /pages/AA_09_1_WorkOrder_Create -> /pages/AA_09_2_WorkOrder_Detail`; refs `docs/ui/md/Agent_Admin_Portal/AA_09_*.md`.
- `PLATFORM_IOT_OPS`: `/pages/PI_09_Alerts_List -> /pages/PI_10_Alert_Detail`; refs `docs/ui/md/Platform_IoTOps_Portal/PI_09_Alerts_List.md`, `docs/ui/md/Platform_IoTOps_Portal/PI_10_Alert_Detail.md`.
- `TECHNICIAN`: `/pages/TA_14_Exception_Report -> /pages/TA_14_1_Exception_List -> /pages/TA_14_2_Exception_Detail` and `/pages/TA_15_Offline_Mode`; refs `docs/ui/md/Technician_App/TA_14_*.md`, `docs/ui/md/Technician_App/TA_15_Offline_Mode.md`.
- `PLATFORM_ADMIN` + `CABINET_SCREEN`: `/pages/PA_37_Tickets_Center -> /pages/PA_37_1_Ticket_Detail`; `/pages/CK_09_Error_Offline`, `/pages/CK_10_Error_SlotFault`, `/pages/CK_11_Maintenance`, `/pages/CK_12_DoorOpen_Timeout`; refs `docs/ui/md/Platform_Admin_Portal/PA_37_*.md`, `docs/ui/md/Cabinet_Screen/CK_09_*.md`..`docs/ui/md/Cabinet_Screen/CK_12_*.md`.
## 6) Risks / assumptions / explicit out of scope
- Risk: several ITER-20 pages are currently `api_tag=ui-static` in `docs/traceability/page_to_api.md`; Gate B fails unless API/permission mappings are completed.
- Risk: OpenAPI has finance-reconciliation/order/cabinet endpoints but no dedicated alert/exception/workorder resources; new operationIds/schema ownership must be finalized early.
- Assumptions: tenant is derived only from JWT/request context; high-risk writes require `Idempotency-Key` and append-only `audit_log`; no PII appears in logs.
- Explicit out of scope: non-ITER-20 page redesigns, firmware/BLE cryptography implementation, production MQTT infra rollout changes.
