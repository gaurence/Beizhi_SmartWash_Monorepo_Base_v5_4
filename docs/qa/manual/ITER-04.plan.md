# ITER-04 Stage-1 Plan (Plan Only)
## 1) Scope (bounded context / modules / pages)
- Bounded contexts/modules: `Staff & Workforce` (technician/valet roster), `Wash Team`, `Shift Scheduling`, `App-Tech Profile/Shift`, plus cross-cutting `tenant isolation`, `RBAC+ABAC`, `idempotency`, `append-only audit`, `PII masking`.
- Subsystems: `Store_Admin_Portal`, `Technician_App`.
- Store page_key: `SA_06_1_Technician_Create`, `SA_06_Technicians_List`, `SA_07_1_Technician_Edit`, `SA_07_2_Technician_Shifts`, `SA_07_3_Technician_Credentials`, `SA_07_Technician_Detail`.
- Store page_key: `SA_21_1_WashTeam_Create`, `SA_21_WashTeams_List`, `SA_22_1_WashTeam_Edit`, `SA_22_2_WashTeam_Members_Assign`, `SA_22_WashTeam_Detail`.
- Store page_key: `SA_23_1_ValetDriver_Create`, `SA_23_Valet_Drivers_List`, `SA_24_1_ValetDriver_Edit`, `SA_24_ValetDriver_Detail`, `SA_27_1_Shift_Create`, `SA_27_Shift_Schedule`.
- Tech page_key: `TA_01_2_Shift_PunchIn`, `TA_01_3_Shift_PunchOut`, `TA_16_2_Performance`, `TA_16_3_Credentials`.

## 2) Files to change (exact paths)
- Backend: `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/StaffWorkforceController.java` (new), `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/AppTechController.java` (new), `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/StaffWorkforceService.java` (new), `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/AppTechService.java` (new), `backend/apps/api-server/src/main/java/com/beizhi/smartwash/infrastructure/repository/InMemoryPlatformRepository.java`, `backend/apps/api-server/src/test/java/com/beizhi/smartwash/PlatformGateIntegrationTest.java`.
- Frontend: `frontend/store-admin/src/pages/GeneratedPage.vue`, `frontend/store-admin/src/router/index.ts`, `frontend/store-admin/src/styles.css`, `frontend/technician-app/src/pages/GeneratedPage.vue`, `frontend/technician-app/src/router/index.ts`, `frontend/technician-app/src/styles.css`, `tooling/codegen/generate-frontend.mjs`.
- Contracts/traceability/docs: `docs/contracts/openapi.yaml`, `docs/contracts/asyncapi.yaml`, `docs/traceability/page_to_api.md`, `docs/ui/page_inventory.csv`, `docs/qa/manual/ITER-04.md`.

## 3) APIs/events touched (operationId / topic)
- Staff/workforce (from design, add/align in OpenAPI): `listStaff`, `createStaff`, `updateStaff`, `listWashTeams`, `createWashTeam`, `updateWashTeam`, `addWashTeamMember`, `removeWashTeamMember`, `createExternalAssignment`, `revokeExternalAssignment`.
- Shift/performance/credentials (ITER-04 supplement to design matrix): `listShifts`, `createShift`, `updateShift`, `punchInShift`, `punchOutShift`, `getStaffPerformance`, `listStaffCredentials`, `upsertStaffCredential`.
- Dispatch/app-tech reuse: `getTasks`, `postTasksByTaskIdAssign`, `getAppTechTasks`, `postAppTechTasksByTaskIdAccept`, `postAppTechTasksByTaskIdComplete`.
- Async topics: `smartwash.domain.events` (event types `StaffEnrolled|StaffRoleAssigned|StaffClockedIn|StaffClockedOut|WashTeamUpdated`), `smartwash.notification.commands`; no new MQTT topic expected.

## 4) Automated verification commands to run (make targets)
- `make verify-docs`
- `make verify-openapi`
- `make verify-asyncapi`
- `make verify-page-coverage ITER=ITER-04`
- `make verify-traceability ITER=ITER-04`
- `make verify-visual ITER=ITER-04`
- `make backend-test`
- `make frontend-build APPS="store-admin technician-app"`

## 5) Manual acceptance checklist by role (links/paths)
- `STORE_ADMIN`: validate full create/list/edit/detail flows for technician/wash-team/valet/shift pages via `/pages/{page_key}`; references `docs/ui/md/Store_Admin_Portal/SA_06*.md`, `SA_07*.md`, `SA_21*.md`, `SA_22*.md`, `SA_23*.md`, `SA_24*.md`, `SA_27*.md` and matching `docs/ui/svg/Store_Admin_Portal/*.svg`.
- `TECHNICIAN`: validate punch in/out + performance + credentials pages and action results; references `docs/ui/md/Technician_App/TA_01_2_Shift_PunchIn.md`, `TA_01_3_Shift_PunchOut.md`, `TA_16_2_Performance.md`, `TA_16_3_Credentials.md` and matching SVG.
- `PLATFORM_ADMIN`: verify tenant isolation (cross-tenant deny/invisible), permission allow+deny, idempotency on write APIs, and audit append-only records for high-risk workforce actions.
- `PROPERTY_ADMIN`/`INVESTOR_ADMIN`: verify out-of-scope access denied and PII masked in any shared read model/export.

## 6) Risks / assumptions / explicit out of scope
- Risks: current `docs/contracts/openapi.yaml` lacks `/staff|/wash-teams|/shifts` paths; operation naming/path alignment (`/api` contract vs design `/api/v1`) must be resolved in-contract first.
- Assumptions: `tenant_id` is JWT-derived only, all write APIs use `Idempotency-Key`, traceability rows for ITER-04 move from `ui-static` to concrete API mappings with permission keys.
- Out of scope: non-ITER-04 pages, new business domains outside workforce (finance/order/cabinet routing), and introducing new MQTT channels beyond current `docs/contracts/asyncapi.yaml`.
