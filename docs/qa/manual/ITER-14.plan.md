# ITER-14 Stage-1 Plan (Plan Only)
## 1) Scope (bounded context / modules / pages)
- Bounded contexts/modules: `Staff & Workforce (ValetDriver/external assignment)`, `Task & Execution (valet handoff)`, `App Order (Mode-B valet pickup details)`, `Identity & Access (RBAC+ABAC role-mode)`, `Audit + Idempotency + Tenant isolation`.
- Subsystems: `Customer_App`, `Store_Admin_Portal`, `Technician_App`.
- page_key (`Customer_App`): `CA_08_7_Valet_Pickup_Details`.
- page_key (`Store_Admin_Portal`): `SA_23_Valet_Drivers_List`, `SA_23_1_ValetDriver_Create`, `SA_24_ValetDriver_Detail`, `SA_24_1_ValetDriver_Edit`.
- page_key (`Technician_App`): `TA_02_2_Switch_Role_Mode`.
## 2) Files to change (exact paths)
- Backend: `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/AppCustomerController.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/AppTechController.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/StaffWorkforceController.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/AppCustomerService.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/AppTechService.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/StaffWorkforceService.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/infrastructure/repository/InMemoryPlatformRepository.java`, `backend/apps/api-server/src/test/java/com/beizhi/smartwash/PlatformGateIntegrationTest.java`.
- Frontend (new page implementations): `frontend/customer-app/src/pages/iter14/CA_08_7_Valet_Pickup_Details.vue`, `frontend/store-admin/src/pages/iter14/SA_23_Valet_Drivers_List.vue`, `frontend/store-admin/src/pages/iter14/SA_23_1_ValetDriver_Create.vue`, `frontend/store-admin/src/pages/iter14/SA_24_ValetDriver_Detail.vue`, `frontend/store-admin/src/pages/iter14/SA_24_1_ValetDriver_Edit.vue`, `frontend/technician-app/src/pages/iter14/TA_02_2_Switch_Role_Mode.vue`, `frontend/customer-app/src/styles.css`, `frontend/store-admin/src/styles.css`, `frontend/technician-app/src/styles.css`.
- Frontend generator/scaffold: `tooling/codegen/generate-frontend.mjs`, `frontend/customer-app/src/pages/GeneratedPage.vue`, `frontend/store-admin/src/pages/GeneratedPage.vue`, `frontend/technician-app/src/pages/GeneratedPage.vue`, `frontend/customer-app/src/generated/pages.ts`, `frontend/store-admin/src/generated/pages.ts`, `frontend/technician-app/src/generated/pages.ts`.
- Contracts/trace/docs: `docs/contracts/openapi.yaml`, `docs/contracts/asyncapi.yaml`, `docs/traceability/page_to_api.md`, `docs/ui/page_inventory.csv`, `docs/qa/manual/ITER-14.md`.
## 3) APIs/events touched (operationId / topic)
- Existing operationId reuse: `postAppOrders`, `getAppOrdersByOrderId`, `getAppOrdersByOrderIdTimeline`, `getAppTechTasks`, `postAppTechTasksByTaskIdAccept`, `postAppTechTasksByTaskIdComplete`.
- Planned OpenAPI additions: `getStaff`, `getStaffByStaffId`, `postStaff`, `putStaffByStaffId`, `getStaffByStaffIdPerformance`, `postAppOrdersByOrderIdValetPickupDetails`, `getAppOrdersByOrderIdValetPickupDetails`, `getAppTechProfile`, `postAppTechRoleModeSwitch`.
- Async topics/event types: `smartwash.domain.events` (`Order.ValetPickupDetailsUpdated`, `Task.Assigned`, `Task.Accepted`, `Task.Completed`, `Staff.Created`, `Staff.Updated`, `Staff.RoleModeSwitched`), `smartwash.notification.commands` (`STAFF_TASK_ASSIGNED`).
## 4) Automated verification commands to run (make targets)
- `make verify-docs`
- `make verify-openapi`
- `make verify-asyncapi`
- `make verify-page-coverage ITER=ITER-14`
- `make verify-traceability ITER=ITER-14`
- `make verify-visual ITER=ITER-14`
- `make backend-test`
- `make frontend-build APPS="customer-app store-admin technician-app"`
## 5) Manual acceptance checklist by role (links/paths)
- `CUSTOMER`: `docs/ui/md/Customer_App/CA_08_Create_Order.md` -> `docs/ui/md/Customer_App/CA_08_7_Valet_Pickup_Details.md`; check Mode-B pickup location/time save, idempotent resubmit, masked contact fields.
- `STORE_ADMIN`: `docs/ui/md/Store_Admin_Portal/SA_23_Valet_Drivers_List.md` -> `SA_23_1_ValetDriver_Create.md` -> `SA_24_ValetDriver_Detail.md` -> `SA_24_1_ValetDriver_Edit.md`; check create/edit/disable paths, tenant isolation, permission allow/deny.
- `TECHNICIAN/VALET`: `docs/ui/md/Technician_App/TA_02_Task_List.md` -> `docs/ui/md/Technician_App/TA_02_2_Switch_Role_Mode.md`; check role switch effects on visible task list and forbidden cross-role actions.
- `AUDITOR/OPS`: `docs/qa/manual/ITER-14.md` + `/audit/logs`; verify append-only audit fields (`request_id`,`actor`,`tenant`,`resource`,`result`,`ip`,`user_agent`) for valet create/edit/switch actions.
## 6) Risks/assumptions and explicit out of scope
- Risks: in-scope UI MD files are generic templates; concrete field/flow details must be inferred from SVG + requirements/design; OpenAPI currently lacks staff/valet endpoints and may require tag/path normalization.
- Assumptions: tenant derived from JWT claims/request context only; short-lived ABAC tokens bind `tenant_id,user_id,order_id/task_id,cabinet_id/slot_id,action,ttl`; high-risk valet/staff changes are audited and write operations are idempotent.
- Out of scope: real GPS/dispatch optimization, real BLE/MQTT device handshake, real MFA provider integration, payroll-grade performance analytics, and non-ITER-14 page redesign.
