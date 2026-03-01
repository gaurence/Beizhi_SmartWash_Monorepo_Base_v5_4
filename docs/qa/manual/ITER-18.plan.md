# ITER-18 Stage-1 Plan (Plan Only)
## 1) Scope (bounded context / modules / pages by page_key)
- Bounded contexts/modules: `Payment & Refund`, `Settlement & Reconcile`, `Ticketing`, `Property Remittance Tasks`, `Tenant/RBAC/Idempotency/Audit`.
- `Platform_Admin_Portal`: `PA_17_Refunds, PA_18_1_Refund_Approve_Modal, PA_18_2_Refund_Reject_Modal, PA_18_3_Refund_Callback_Log, PA_18_Refund_Detail, PA_26_1_Reconciliation_Run_Config, PA_26_2_Reconciliation_Import_Drawer, PA_26_Reconciliation_Run_Import, PA_27_Reconciliation_Tickets, PA_28_Reconciliation_Ticket_Detail`.
- `Platform_Finance_Portal`: `PF_08_Refunds_List, PF_09_Refund_Detail, PF_10_Reconciliation_Run, PF_11_Reconciliation_Tickets, PF_12_Reconciliation_Ticket_Detail`.
- `Platform_Ops_Portal`: `PO_12_1_Run_Config, PO_12_2_Import_Drawer, PO_12_Reconciliation_Run_Import, PO_13_1_Ticket_Detail, PO_13_Reconciliation_Tickets`.
- `Store_Admin_Portal`: `SA_13_1_Refund_Create, SA_13_Refunds_List, SA_14_Refund_Detail`.
- `Agent_Admin_Portal`: `AA_08_1_Ticket_Detail, AA_08_Reconciliation_Tickets`.
- `Device_Investor_Portal`: `DI_08_Reconciliation_Tickets, DI_09_Ticket_Detail`.
- `Property_Portal`: `PP_05_Remittance_Tasks, PP_06_Reconciliation_Tickets, PP_07_Ticket_Detail`.
- `Customer_App`: `CA_20_Refund_Request, CA_21_Retention_Fee`.
## 2) Files to change (exact paths)
- Backend existing: `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/FinanceController.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/infrastructure/repository/InMemoryPlatformRepository.java`, `backend/apps/api-server/src/test/java/com/beizhi/smartwash/PlatformGateIntegrationTest.java`.
- Backend new: `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/TaskController.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/RefundService.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/ReconciliationService.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/TaskService.java`.
- Backend new domain: `backend/apps/api-server/src/main/java/com/beizhi/smartwash/domain/model/RefundRequestRecord.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/domain/model/ReconciliationTicketRecord.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/domain/model/RemittanceTaskRecord.java`.
- Frontend shells: `frontend/platform-admin/src/pages/PlatformAdminPage.vue`, `frontend/platform-finance/src/pages/GeneratedPage.vue`, `frontend/platform-ops/src/pages/GeneratedPage.vue`, `frontend/store-admin/src/pages/GeneratedPage.vue`, `frontend/agent-admin/src/pages/GeneratedPage.vue`, `frontend/device-investor/src/pages/GeneratedPage.vue`, `frontend/property-portal/src/pages/GeneratedPage.vue`, `frontend/customer-app/src/pages/GeneratedPage.vue`.
- Frontend style/router metadata: `frontend/platform-admin/src/styles.css`, `frontend/platform-finance/src/styles.css`, `frontend/platform-ops/src/styles.css`, `frontend/store-admin/src/styles.css`, `frontend/agent-admin/src/styles.css`, `frontend/device-investor/src/styles.css`, `frontend/property-portal/src/styles.css`, `frontend/customer-app/src/styles.css`, `frontend/platform-admin/src/generated/pages.ts`, `frontend/platform-finance/src/generated/pages.ts`, `frontend/platform-ops/src/generated/pages.ts`, `frontend/store-admin/src/generated/pages.ts`, `frontend/agent-admin/src/generated/pages.ts`, `frontend/device-investor/src/generated/pages.ts`, `frontend/property-portal/src/generated/pages.ts`, `frontend/customer-app/src/generated/pages.ts`.
- Contracts/traceability/qa: `docs/contracts/openapi.yaml`, `docs/contracts/asyncapi.yaml`, `docs/traceability/page_to_api.md`, `docs/ui/page_inventory.csv`, `docs/qa/manual/ITER-18.md`.
## 3) APIs/events touched (operationId / topic)
- Existing operationId: `getFinanceReconciliationTickets`, `postFinanceReconciliationTickets`, `getFinanceReconciliationTicketsByTicketId`, `getFinanceRefunds`, `postFinanceRefunds`, `postFinanceRefundsByRefundIdApprove`, `getTasks`, `postTasksByTaskIdAccept`, `postTasksByTaskIdAssign`, `postTasksByTaskIdComplete`.
- Contract补齐目标（run/import/reject gaps）: add operationIds for refund reject + reconciliation import/run and map to `PO_12_1`, `PO_12_2`, `CA_20`, `CA_21` pages.
- Async topics: `smartwash.finance.events`, `smartwash.domain.events`, `smartwash.notification.commands`.
## 4) Automated verification commands to run (make targets)
- `make verify-docs`; `make verify-openapi`; `make verify-asyncapi`; `make verify-page-coverage ITER=ITER-18`; `make verify-traceability ITER=ITER-18`; `make verify-visual ITER=ITER-18`.
- `make backend-test`; `make frontend-build APPS="agent-admin customer-app device-investor platform-admin platform-finance platform-ops property-portal store-admin"`.
## 5) Manual acceptance checklist by role (links/paths)
- `PLATFORM_ADMIN`: validate refund + reconciliation run/import + ticket detail via `docs/ui/md/Platform_Admin_Portal/{PA_17_Refunds,PA_18_*,PA_26_*,PA_27_Reconciliation_Tickets,PA_28_Reconciliation_Ticket_Detail}.md`.
- `PLATFORM_FINANCE` + `PLATFORM_OPS`: validate `PF_08~PF_12` and `PO_12~PO_13` flows via `docs/ui/md/Platform_Finance_Portal/*.md` and `docs/ui/md/Platform_Ops_Portal/*.md`.
- `STORE_ADMIN` + `CUSTOMER`: validate refund create/detail and customer refund/retention pages via `docs/ui/md/Store_Admin_Portal/{SA_13_1_Refund_Create,SA_13_Refunds_List,SA_14_Refund_Detail}.md` and `docs/ui/md/Customer_App/{CA_20_Refund_Request,CA_21_Retention_Fee}.md`.
- `AGENT_ADMIN` + `DEVICE_INVESTOR` + `PROPERTY_FINANCE`: validate reconciliation tickets/remittance via `docs/ui/md/Agent_Admin_Portal/AA_08_*.md`, `docs/ui/md/Device_Investor_Portal/DI_0{8,9}_*.md`, `docs/ui/md/Property_Portal/{PP_05_Remittance_Tasks,PP_06_Reconciliation_Tickets,PP_07_Ticket_Detail}.md`.
- `Audit/Compliance`: verify high-risk actions (refund approve/reject, remittance task transitions, reconciliation ticket create/update/export) write append-only audit with `request_id`, actor, tenant.
## 6) Risks / assumptions / explicitly out of scope
- Risks: current MD/traceability mark `PO_12_1`, `PO_12_2`, `CA_20`, `CA_21` as static; converting to real APIs can break traceability/openapi/visual gates if not updated atomically.
- Assumptions: tenant from JWT/request context only, no request-body tenant trust; all write APIs use `Idempotency-Key`; no PII in logs/responses beyond masked fields.
- Out of scope: real payment-channel/物业银行打款 integration, production MQ workflow orchestration, and non-ITER-18 page redesign outside listed page_keys.
