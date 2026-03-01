# ITER-11 Stage-1 Plan (Plan Only)
## 1) Scope (bounded context / modules / pages)
- Bounded contexts/modules: `Task & Execution` (dispatch, multi-assignee), `Key Custody` (pickup/return), `Evidence`, `Cabinet Access/IoT`, `Identity & Access` (RBAC+ABAC token), `Risk & Audit`; enforce tenant isolation + idempotency + append-only audit.
- Subsystems: `Store_Admin_Portal`, `Technician_App`, `Cabinet_Screen`, `Platform_Admin_Portal`.
- `Store_Admin_Portal` page_keys: `SA_05_1_Assign_Drawer`, `SA_05_Dispatch_Assign`, `SA_25_Tasks_List`, `SA_26_1_Task_Collaboration`, `SA_26_Task_Detail`.
- `Technician_App` page_keys: `TA_02_1_Task_Filter`, `TA_02_2_Switch_Role_Mode`, `TA_02_Task_List`, `TA_03_1_Task_Evidence_Detail`, `TA_03_2_Collaboration_Board`, `TA_03_3_Collaboration_Invite`, `TA_03_4_Collaboration_Member_Status`, `TA_03_Task_Detail`, `TA_04_1_PickUpKey_BLE_Handshake`, `TA_04_2_PickUpKey_BLE_Success`, `TA_04_PickUpKey_Scan`, `TA_05_PickUpKey_Token`, `TA_06_PreCheck_Checklist`, `TA_07_PreCheck_Upload`, `TA_08_Start_Service`.
- `Cabinet_Screen` page_keys: `CK_14_Staff_Mode_Login`, `CK_15_Staff_Mode_OpenSlot`.
- `Platform_Admin_Portal` page_keys: `PA_36_1_Task_Detail`, `PA_36_Global_Tasks_Monitor`.
## 2) Files to change (exact paths)
- Backend existing: `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/AppCustomerController.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/CabinetController.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/AuthController.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/AppCustomerService.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/CabinetService.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/AuditService.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/IdempotencyService.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/security/PermissionGuard.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/infrastructure/repository/InMemoryPlatformRepository.java`, `backend/apps/api-server/src/test/java/com/beizhi/smartwash/PlatformGateIntegrationTest.java`.
- Backend new (planned): `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/AppTechController.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/TaskController.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/AppTechService.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/TaskService.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/EvidenceService.java`.
- Frontend/codegen: `tooling/codegen/generate-frontend.mjs`, `frontend/store-admin/src/pages/GeneratedPage.vue`, `frontend/store-admin/src/router/index.ts`, `frontend/store-admin/src/styles.css`, `frontend/technician-app/src/pages/GeneratedPage.vue`, `frontend/technician-app/src/router/index.ts`, `frontend/technician-app/src/styles.css`, `frontend/cabinet-screen/src/pages/GeneratedPage.vue`, `frontend/cabinet-screen/src/router/index.ts`, `frontend/cabinet-screen/src/styles.css`, `frontend/platform-admin/src/pages/PlatformAdminPage.vue`, `frontend/platform-admin/src/router/index.ts`, `frontend/platform-admin/src/styles.css`.
- Contracts/traceability/docs: `docs/contracts/openapi.yaml`, `docs/contracts/asyncapi.yaml`, `docs/traceability/page_to_api.md`, `docs/ui/page_inventory.csv`, `docs/qa/manual/ITER-11.md`.
## 3) APIs/events touched (operationId / topic)
- OperationId: `postAppCabinetsScan`, `postAppOrdersByOrderIdDepositKey`, `postAppOrdersByOrderIdRetrieveKey`, `postAppTechEvidenceRecords`, `postAppTechEvidenceUploadUrl`, `postAppTechOrdersByOrderIdPickupKey`, `postAppTechOrdersByOrderIdReturnKey`, `getAppTechTasks`, `postAppTechTasksByTaskIdAccept`, `postAppTechTasksByTaskIdComplete`.
- OperationId: `getAssetsCabinets`, `getAssetsCabinetsByCabinetId`, `getAssetsCabinetsByCabinetIdSlots`, `postAuthLogin`, `postAuthLogout`, `postAuthMfaVerify`, `postIotCabinetsByCabinetIdCommandsOpenSlot`, `getIotCabinetsByCabinetIdEvents`, `getTasks`, `postTasksByTaskIdAccept`, `postTasksByTaskIdAssign`, `postTasksByTaskIdComplete`.
- Topics/channels: `smartwash.domain.events` (`Task.Assigned`, `Task.Completed`, `Order.KeyPickedUp`, `Order.KeyReturned`), `mqtt/cabinets/{cabinetId}/commands/open-slot`, `mqtt/cabinets/{cabinetId}/acks/open-slot`, `mqtt/cabinets/{cabinetId}/events`, `smartwash.notification.commands`.
## 4) Automated verification commands to run (make targets)
- `make verify-docs`
- `make verify-openapi`
- `make verify-asyncapi`
- `make verify-page-coverage ITER=ITER-11`
- `make verify-traceability ITER=ITER-11`
- `make verify-visual ITER=ITER-11`
- `make backend-test`
- `make frontend-build APPS="platform-admin store-admin technician-app cabinet-screen"`
## 5) Manual acceptance checklist by role (links/paths)
- `STORE_ADMIN`: `docs/ui/md/Store_Admin_Portal/SA_05_Dispatch_Assign.md` -> `SA_05_1_Assign_Drawer.md` -> `SA_25_Tasks_List.md` -> `SA_26_Task_Detail.md` -> `SA_26_1_Task_Collaboration.md`; check assign/reassign, multi-tech progress, reject-by-permission, repeated submit idempotency.
- `TECHNICIAN`: `docs/ui/md/Technician_App/TA_02_Task_List.md` -> `TA_02_1_Task_Filter.md` -> `TA_03_Task_Detail.md` -> `TA_03_1_Task_Evidence_Detail.md` -> `TA_04_PickUpKey_Scan.md` -> `TA_04_1_PickUpKey_BLE_Handshake.md` -> `TA_05_PickUpKey_Token.md` -> `TA_06_PreCheck_Checklist.md` -> `TA_08_Start_Service.md`; check ABAC token TTL/scope, evidence append-only, task state transitions.
- `PLATFORM_ADMIN`: `docs/ui/md/Platform_Admin_Portal/PA_36_Global_Tasks_Monitor.md` -> `PA_36_1_Task_Detail.md`; check global visibility, assign/accept/complete audit trail and cross-tenant deny.
- `CABINET_STAFF`/`PLATFORM_IOTOPS`: `docs/ui/md/Cabinet_Screen/CK_14_Staff_Mode_Login.md` -> `CK_15_Staff_Mode_OpenSlot.md`; check open-slot command/ACK chain, slot state consistency, emergency/high-risk audit fields.
## 6) Risks / assumptions / explicit out of scope
- Risks: `/tasks` and `/app/tech/*` runtime endpoints are not yet implemented in current backend; many scoped pages are still metadata-style UI and may fail visual parity gate if not replaced with MD/SVG-faithful layouts.
- Assumptions: tenant resolved from token/request context only (never body), all state-changing calls require `Idempotency-Key`, ABAC grant binds `tenant_id/user_id/order_id|task_id/cabinet_id|slot_id/action/ttl`, and high-risk actions always append audit with `request_id`.
- Out of scope: real BLE hardware handshake cryptography, live MQTT device fleet integration, and non-ITER-11 page redesigns or finance/refund workflow changes.
