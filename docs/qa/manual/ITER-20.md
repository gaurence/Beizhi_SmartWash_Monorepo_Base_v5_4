# ITER-20 Manual QA Log

## Chunk 1 - Baseline gate verification and acceptance guide scaffold

### What changed (files)
- `docs/qa/manual/ITER-20.md`

### What was verified (commands + result)
- `make verify-docs` -> PASS
- `make verify-openapi` -> PASS
- `make verify-asyncapi` -> PASS
- `make verify-page-coverage ITER=ITER-20` -> PASS
- `make verify-traceability ITER=ITER-20` -> PASS
- `make verify-visual ITER=ITER-20` -> PASS (`mode=mock`)
- `make backend-test` -> PASS
- `make frontend-build APPS="agent-admin platform-ops store-admin platform-iotops technician-app cabinet-screen platform-admin"` -> PASS
- `git add docs/qa/manual/ITER-20.md && git commit -m "ITER-20: add chunk1 qa log"` -> BLOCKED (`.git/index.lock: Operation not permitted` in current sandbox)

### How to manually test (role-based steps)
- `PLATFORM_OPS`:
  1. Open `Platform_Ops_Portal` `/pages/PO_08_Exception_Tickets`.
  2. Click create entry to `/pages/PO_08_1_Exception_Create`; submit one exception ticket.
  3. Open `/pages/PO_09_Exception_Ticket_Detail`; verify timeline/actions render and status transition controls are visible by permission.
- `STORE_ADMIN`:
  1. Open `Store_Admin_Portal` `/pages/SA_15_Exception_Tickets`.
  2. Navigate to `/pages/SA_15_1_Exception_Create`; create a ticket.
  3. Open `/pages/SA_16_Exception_Ticket_Detail`; trigger `Assign`, `Remark`, `Resolve` and confirm modal/drawer flows (`SA_16_1/2/3`) work.
- `AGENT_ADMIN`:
  1. Open `Agent_Admin_Portal` `/pages/AA_09_WorkOrders`.
  2. Navigate to `/pages/AA_09_1_WorkOrder_Create`; create work order.
  3. Open `/pages/AA_09_2_WorkOrder_Detail`; verify detail/actions and linked history.
- `PLATFORM_IOTOPS`:
  1. Open `Platform_IoTOps_Portal` `/pages/PI_09_Alerts_List`.
  2. Open one alert in `/pages/PI_10_Alert_Detail`; verify acknowledge/closure action visibility by permission.
- `TECHNICIAN`:
  1. Open `Technician_App` `/pages/TA_14_Exception_Report`; submit report.
  2. Verify `/pages/TA_14_1_Exception_List` and `/pages/TA_14_2_Exception_Detail` navigation.
  3. Open `/pages/TA_15_Offline_Mode`; verify offline-state guidance and fallback actions.
- `PLATFORM_ADMIN`:
  1. Open `Platform_Admin_Portal` `/pages/PA_37_Tickets_Center`.
  2. Open `/pages/PA_37_1_Ticket_Detail`; verify ticket actions and audit-related sections.
- `CABINET_SCREEN`:
  1. Open `/pages/CK_09_Error_Offline`, `/pages/CK_10_Error_SlotFault`, `/pages/CK_11_Maintenance`, `/pages/CK_12_DoorOpen_Timeout`.
  2. Verify each error state screen text/actions match corresponding MD prototype behavior.

### Closed TODO for next chunk
- Closed on 2026-02-28: merged into consolidated completion pass.

