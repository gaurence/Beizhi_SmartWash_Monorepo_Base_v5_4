# ITER-22 Stage-1 Plan (Plan Only)
## 1) Scope (bounded context / modules / pages by page_key)
- Bounded contexts/modules: `Support(Ticketing/Complaint)`, `CS Tooling(Order Search/Detail)`, `Finance Refund Assist`, `Evidence Export Assist` (evidence tab/export action in ticket/order detail).
- `Customer_App`: `CA_23_Support_Tickets`, `CA_23_1_Ticket_Detail`, `CA_24_Create_Ticket`.
- `Platform_CS_Portal`: `PC_03_Tickets_List`, `PC_04_Ticket_Detail`, `PC_05_Order_Search`, `PC_06_Order_Detail`, `PC_10_Refund_Assist`.
- Hard constraints inherited from AGENTS.md: JWT-derived tenant isolation, RBAC permission_key coverage, idempotent writes, append-only audit, evidence append-only + masked PII.
## 2) Files to change (exact paths)
- Backend existing: `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/FinanceController.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/OrderController.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/OrderService.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/RefundService.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/AuditService.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/infrastructure/repository/InMemoryPlatformRepository.java`, `backend/apps/api-server/src/test/java/com/beizhi/smartwash/PlatformGateIntegrationTest.java`.
- Backend new: `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/TicketService.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/domain/model/TicketRecord.java`.
- Frontend generator/shell: `tooling/codegen/generate-frontend.mjs`, `frontend/customer-app/src/pages/GeneratedPage.vue`, `frontend/customer-app/src/router/index.ts`, `frontend/customer-app/src/styles.css`, `frontend/platform-cs/src/pages/GeneratedPage.vue`, `frontend/platform-cs/src/router/index.ts`, `frontend/platform-cs/src/styles.css`.
- Frontend new pages: `frontend/customer-app/src/pages/iter22/CA_23_Support_Tickets.vue`, `frontend/customer-app/src/pages/iter22/CA_23_1_Ticket_Detail.vue`, `frontend/customer-app/src/pages/iter22/CA_24_Create_Ticket.vue`, `frontend/platform-cs/src/pages/iter22/PC_03_Tickets_List.vue`, `frontend/platform-cs/src/pages/iter22/PC_04_Ticket_Detail.vue`, `frontend/platform-cs/src/pages/iter22/PC_05_Order_Search.vue`, `frontend/platform-cs/src/pages/iter22/PC_06_Order_Detail.vue`, `frontend/platform-cs/src/pages/iter22/PC_10_Refund_Assist.vue`.
- Contracts/traceability/docs: `docs/contracts/openapi.yaml`, `docs/contracts/asyncapi.yaml`, `docs/traceability/page_to_api.md`, `docs/ui/page_inventory.csv`, `docs/qa/manual/ITER-22.md`.
## 3) APIs/events touched (operationId / topic)
- REST operationIds (existing contracts): `getFinanceReconciliationTickets`, `postFinanceReconciliationTickets`, `getFinanceReconciliationTicketsByTicketId`, `getFinanceRefunds`, `postFinanceRefunds`, `postFinanceRefundsByRefundIdApprove`, `getOrders`, `postOrders`, `getOrdersByOrderId`, `postOrdersByOrderIdCancel`, `postOrdersByOrderIdDispatch`, `getOrdersByOrderIdTimeline`, `getOrdersByOrderIdEvidence`, `postEvidenceUploadUrl`, `postEvidenceRecords`, `postReportsExport`.
- Async topics: `smartwash.domain.events` (ticket/order updates), `smartwash.finance.events` (refund/reconcile updates), `smartwash.notification.commands` (CS/customer notifications).
## 4) Automated verification commands to run (make targets)
- `make verify-docs`
- `make verify-openapi`
- `make verify-asyncapi`
- `make verify-page-coverage ITER=ITER-22`
- `make verify-traceability ITER=ITER-22`
- `make verify-visual ITER=ITER-22`
- `make backend-test`
- `make frontend-build APPS="customer-app platform-cs"`
## 5) Manual acceptance checklist by role (links/paths)
- `CUSTOMER` (`13800000000`/`123456`): `/pages/CA_23_Support_Tickets -> /pages/CA_23_1_Ticket_Detail -> /pages/CA_24_Create_Ticket`; refs `docs/ui/md/Customer_App/CA_23_Support_Tickets.md`, `docs/ui/md/Customer_App/CA_23_1_Ticket_Detail.md`, `docs/ui/md/Customer_App/CA_24_Create_Ticket.md`.
- `PLATFORM_CS` (`cs_admin`/`Passw0rd!`): `/pages/PC_03_Tickets_List -> /pages/PC_04_Ticket_Detail`; `/pages/PC_05_Order_Search -> /pages/PC_06_Order_Detail`; `/pages/PC_10_Refund_Assist`; refs `docs/ui/md/Platform_CS_Portal/PC_03_Tickets_List.md`, `docs/ui/md/Platform_CS_Portal/PC_04_Ticket_Detail.md`, `docs/ui/md/Platform_CS_Portal/PC_05_Order_Search.md`, `docs/ui/md/Platform_CS_Portal/PC_06_Order_Detail.md`, `docs/ui/md/Platform_CS_Portal/PC_10_Refund_Assist.md`.
- `PLATFORM_ADMIN` (`pa_admin`/`Passw0rd!`): audit spot-check for ticket/refund/export actions via `GET /audit/logs` against `docs/requirements/Beizhi_v5_4.md` section `16.5`.
## 6) Risks / assumptions / explicitly out of scope
- Risks: OpenAPI already defines `/finance/reconciliation/tickets*` but backend controller/service is missing; traceability/customer pages still `ui-static` and must stay consistent with Gate B.
- Assumptions: tenant comes only from JWT/RequestContext (`Authorization`/claims), all write APIs carry `Idempotency-Key`, and audit records include `request_id`, actor, tenant, resource, result, ip/user-agent.
- Explicit out of scope: dedicated `PC_07_Evidence_Export` page implementation, external payment/refund provider integration, MQTT/slot workflow changes unrelated to ITER-22 pages.
