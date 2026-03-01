# ITER-05 Stage-1 Plan (Plan Only)
## 1) Scope (bounded context / modules / pages)
- Bounded contexts/modules: `cabinet-asset`, `slot-state-machine`, `iot-gateway(mqtt)`, `device-logs/ota/remote-command`, with guardrails `tenant isolation`, `RBAC+ABAC`, `idempotency`, `append-only audit`, `PII masking`.
- `Platform_Admin_Portal`: `PA_06_1_Cabinet_Import_Batch`, `PA_06_Cabinet_Asset_Registry`, `PA_07_Cabinet_Register`, `PA_08_Cabinet_Scan_BindStore`, `PA_09_Cabinet_Detail`, `PA_09_1_Cabinet_Slots`, `PA_09_2_Cabinet_Logs_Events`, `PA_09_3_Cabinet_OTA_RemoteCommand`.
- `Store_Admin_Portal`: `SA_08_Cabinets_Slots`, `SA_08_1_Slot_Detail`, `SA_08_2_Manual_OpenSlot_Modal`, `SA_08_3_Device_Logs`, `SA_08_4_OTA_RemoteCommand`.
- `Agent_Admin_Portal`: `AA_06_Devices_Health`, `AA_06_1_Device_Detail`.
- `Platform_IoTOps_Portal`: `PI_03_Device_Fleet`, `PI_04_Device_Detail`, `PI_05_OTA_RemoteCommand`, `PI_06_IoT_Events`, `PI_07_IoT_Event_Detail`, `PI_08_IoT_Replay`.
- `Platform_Ops_Portal`: `PO_05_Device_Operations`, `PO_05_1_Device_Detail`, `PO_05_2_OTA_RemoteCommand`, `PO_06_IoT_Events`, `PO_06_1_IoT_Event_Detail`, `PO_06_2_IoT_Publish_Drawer`, `PO_07_IoT_Replay`, `PO_07_1_IoT_Replay_Confirm_Modal`.
- `Cabinet_Screen`: `CK_09_Error_Offline`, `CK_10_Error_SlotFault`, `CK_11_Maintenance`, `CK_12_DoorOpen_Timeout`, `CK_13_UVC_Running`, `CK_15_Staff_Mode_OpenSlot`.
## 2) Files to change (exact paths)
- Backend: `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/CabinetController.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/CabinetService.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/domain/service/SlotStateMachine.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/infrastructure/repository/InMemoryPlatformRepository.java`, `backend/apps/api-server/src/test/java/com/beizhi/smartwash/PlatformGateIntegrationTest.java`.
- Frontend codegen/app shells: `tooling/codegen/generate-frontend.mjs`, `frontend/platform-admin/src/pages/PlatformAdminPage.vue`, `frontend/platform-admin/src/router/index.ts`, `frontend/platform-admin/src/generated/pages.ts`, `frontend/agent-admin/src/pages/GeneratedPage.vue`, `frontend/agent-admin/src/router/index.ts`, `frontend/agent-admin/src/generated/pages.ts`, `frontend/store-admin/src/pages/GeneratedPage.vue`, `frontend/store-admin/src/router/index.ts`, `frontend/store-admin/src/generated/pages.ts`, `frontend/platform-iotops/src/pages/GeneratedPage.vue`, `frontend/platform-iotops/src/router/index.ts`, `frontend/platform-iotops/src/generated/pages.ts`, `frontend/platform-ops/src/pages/GeneratedPage.vue`, `frontend/platform-ops/src/router/index.ts`, `frontend/platform-ops/src/generated/pages.ts`, `frontend/cabinet-screen/src/pages/GeneratedPage.vue`, `frontend/cabinet-screen/src/router/index.ts`, `frontend/cabinet-screen/src/generated/pages.ts`.
- Contracts/traceability/docs: `docs/contracts/openapi.yaml`, `docs/contracts/asyncapi.yaml`, `docs/traceability/page_to_api.md`, `docs/ui/page_inventory.csv`, `docs/qa/manual/ITER-05.md`.
## 3) APIs/events touched (operationId / topic)
- Existing OpenAPI operationIds: `getAssetsCabinets`, `getAssetsCabinetsByCabinetId`, `getAssetsCabinetsByCabinetIdSlots`, `postIotCabinetsByCabinetIdCommandsOpenSlot`, `getIotCabinetsByCabinetIdEvents`, `getAssetCabinetsByCabinetIdStoreBindings`, `postAssetCabinetsByCabinetIdStoreBindings`, `patchAssetCabinetStoreBindingsByBindingId`, `postAssetCabinetStoreBindingsByBindingIdApprove`.
- AsyncAPI topics: `mqtt/cabinets/{cabinetId}/commands/open-slot`, `mqtt/cabinets/{cabinetId}/acks/open-slot`, `mqtt/cabinets/{cabinetId}/events`, plus `smartwash.domain.events` for replay/audit projections.
- Contract additions in this ITER (to define in OpenAPI/AsyncAPI with matching permission_key): OTA command, generic remote command, IoT replay publish/replay-result endpoints/topics.
## 4) Automated verification commands to run (make targets)
- `make verify-docs`
- `make verify-openapi`
- `make verify-asyncapi`
- `make verify-page-coverage ITER=ITER-05`
- `make verify-traceability ITER=ITER-05`
- `make verify-visual ITER=ITER-05`
- `make backend-test`
- `make frontend-build APPS="platform-admin store-admin agent-admin platform-iotops platform-ops cabinet-screen"`
## 5) Manual acceptance checklist by role (links/paths)
- `PLATFORM_ADMIN`: walk full cabinet asset/import/register/bind/detail tabs; refs `docs/ui/md/Platform_Admin_Portal/PA_06_*.md`, `PA_07_*.md`, `PA_08_*.md`, `PA_09*.md` + matching `docs/ui/svg/Platform_Admin_Portal/*.svg`.
- `STORE_ADMIN`: walk cabinet/slot list, slot detail, manual open, logs, OTA; refs `docs/ui/md/Store_Admin_Portal/SA_08*.md` + `docs/ui/svg/Store_Admin_Portal/SA_08*.svg`.
- `AGENT_ADMIN`: device health list/detail flows; refs `docs/ui/md/Agent_Admin_Portal/AA_06*.md` + matching SVG.
- `PLATFORM_IOTOPS`: fleet/detail/OTA/events/detail/replay flows; refs `docs/ui/md/Platform_IoTOps_Portal/PI_03*.md`..`PI_08*.md` + matching SVG.
- `PLATFORM_OPS`: device ops + IoT events/publish/replay flows; refs `docs/ui/md/Platform_Ops_Portal/PO_05*.md`..`PO_07*.md` + matching SVG.
- `CABINET_SCREEN` staff/maintenance/error states: refs `docs/ui/md/Cabinet_Screen/CK_09*.md`..`CK_15*.md` + matching SVG.
## 6) Risks / assumptions / explicit out of scope
- Risks: current traceability marks several OTA/IoT pages as `ui-static`; this ITER must replace with concrete API+permission mappings or gate B fails.
- Risks: OpenAPI/AsyncAPI currently lacks explicit OTA/replay operations/channels; naming and DTO shape must be finalized before implementation.
- Assumptions: tenant context comes from JWT/request context only, high-risk actions (`open-slot`, binding changes, replay publish) require `Idempotency-Key` + append-only audit.
- Explicit out of scope: non-ITER-05 page redesigns, unrelated finance/order feature expansion, hardware firmware changes beyond contract-level command/event simulation.
