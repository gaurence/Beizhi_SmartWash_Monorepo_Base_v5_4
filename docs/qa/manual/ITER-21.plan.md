# ITER-21 Stage-1 Plan (Plan Only)
## 1) Scope (bounded context / modules / pages by page_key)
- Bounded contexts/modules: `reporting`, `analytics snapshots` (read models), plus export audit/idempotency hardening under v5.4 tenant/RBAC constraints.
- Pages in scope: `PA_02_Global_Dashboard`, `PF_16_Finance_Reports`, `SA_20_1_Report_Filter_Drawer`, `SA_20_Reports`, `AA_10_1_Report_Filter_Drawer`, `AA_10_Reports`, `DI_05_Reports`, `PP_08_Reports`.
## 2) Files to change (exact paths)
- Backend existing: `backend/apps/api-server/src/main/java/com/beizhi/smartwash/infrastructure/repository/InMemoryPlatformRepository.java`, `backend/apps/api-server/src/test/java/com/beizhi/smartwash/PlatformGateIntegrationTest.java`.
- Backend new: `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/ReportController.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/ReportService.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/domain/model/ReportSnapshot.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/domain/model/ReportExportJob.java`.
- Frontend generation/page logic: `tooling/codegen/generate-frontend.mjs`, `frontend/platform-admin/src/pages/PlatformAdminPage.vue`, `frontend/platform-finance/src/pages/GeneratedPage.vue`, `frontend/store-admin/src/pages/GeneratedPage.vue`, `frontend/agent-admin/src/pages/GeneratedPage.vue`, `frontend/device-investor/src/pages/GeneratedPage.vue`, `frontend/property-portal/src/pages/GeneratedPage.vue`.
- Frontend styles: `frontend/platform-admin/src/styles.css`, `frontend/platform-finance/src/styles.css`, `frontend/store-admin/src/styles.css`, `frontend/agent-admin/src/styles.css`, `frontend/device-investor/src/styles.css`, `frontend/property-portal/src/styles.css`.
- Contracts/traceability/docs: `docs/contracts/openapi.yaml`, `docs/contracts/asyncapi.yaml`, `docs/traceability/page_to_api.md`, `docs/ui/page_inventory.csv`, `docs/qa/manual/ITER-21.md`.
## 3) APIs/events touched (operationId / topic)
- REST operationId: `getReportsSummary` (`GET /reports/summary`), `postReportsExport` (`POST /reports/export`).
- Contract hardening in OpenAPI: report query/body schemas, tenant-scope filters, `Idempotency-Key` behavior for export, 403/404/error schema examples, permission mapping notes.
- Async topics: `smartwash.domain.events` (`Reporting.SnapshotRefreshed`, `Reporting.ExportRequested`, `Reporting.ExportReady`), `smartwash.notification.commands` (export-ready notice).
## 4) Automated verification commands to run (make targets)
- `make verify-docs`
- `make verify-openapi`
- `make verify-asyncapi`
- `make verify-page-coverage ITER=ITER-21`
- `make verify-traceability ITER=ITER-21`
- `make verify-visual ITER=ITER-21`
- `make backend-test`
- `make frontend-build APPS="platform-admin platform-finance store-admin agent-admin device-investor property-portal"`
## 5) Manual acceptance checklist by role (links/paths)
- `PLATFORM_ADMIN`: validate dashboard metrics/filter/down-drill on `docs/ui/md/Platform_Admin_Portal/PA_02_Global_Dashboard.md`.
- `PLATFORM_FINANCE`: validate report summary/export on `docs/ui/md/Platform_Finance_Portal/PF_16_Finance_Reports.md`.
- `AGENT_ADMIN`: validate report page + filter drawer on `docs/ui/md/Agent_Admin_Portal/AA_10_Reports.md`, `docs/ui/md/Agent_Admin_Portal/AA_10_1_Report_Filter_Drawer.md`.
- `STORE_ADMIN`: validate report page + filter drawer on `docs/ui/md/Store_Admin_Portal/SA_20_Reports.md`, `docs/ui/md/Store_Admin_Portal/SA_20_1_Report_Filter_Drawer.md`.
- `DEVICE_INVESTOR`: validate masked investor-scope reporting/export on `docs/ui/md/Device_Investor_Portal/DI_05_Reports.md`.
- `PROPERTY_FINANCE`: validate property reporting/export on `docs/ui/md/Property_Portal/PP_08_Reports.md`.
- Cross-role negatives: missing permission returns 403; cross-tenant summary/export is invisible/denied; export action is idempotent and audit-log appended.
- Manual runbook output: `docs/qa/manual/ITER-21.md` (startup, seeded accounts, happy/fail paths, UI-vs-SVG deltas).
## 6) Risks / assumptions / explicitly out of scope
- Risks: `PA_02_Global_Dashboard` is currently `ui-static` in traceability, and frontend codegen can overwrite manual page edits unless generator updates are included.
- Assumptions: tenant comes from JWT/request context only, report exports require idempotency + append-only audit, and investor/property views remain PII-masked by default.
- Explicitly out of scope: non-ITER-21 page redesigns, external BI warehouse integration, and real third-party notification delivery plumbing.
