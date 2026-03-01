# ITER-06 Stage-1 Plan (Plan Only)
## 1) Scope (bounded context / modules / pages)
- Bounded contexts/modules: `asset-investment` (investor/ownership visibility), `cabinet-store-binding` (M:N + approval), `cabinet-routing-policy` (route mode/priority), with hard guards `tenant isolation`, `RBAC+ABAC`, `idempotency`, `append-only audit`, `PII masking`.
- `Platform_Admin_Portal`: `PA_39_DeviceInvestors_List`, `PA_39_1_DeviceInvestor_Detail`, `PA_39_2_DeviceInvestor_PayoutAccount_Review`, `PA_40_CabinetOwnership_History`, `PA_41_CabinetStoreBindings_List`, `PA_41_1_CabinetStoreBinding_Create`, `PA_41_2_CabinetStoreBinding_Edit`, `PA_41_3_CabinetStoreBinding_Approval`, `PA_42_CabinetRoutingPolicy`.
- `Agent_Admin_Portal`: `AA_12_CabinetStoreBindings`, `AA_12_1_Binding_Detail`, `AA_12_2_Binding_Request_Create`.
- `Store_Admin_Portal`: `SA_28_CabinetStoreBindings`, `SA_28_1_Binding_Request_Create`, `SA_28_2_Binding_History`, `SA_28_3_Binding_RoutingPolicy_View`.
- `Device_Investor_Portal`: `DI_03_Cabinets_List`, `DI_04_Cabinet_Detail_Overview`, `DI_13_Deployment_Binding_Requests`, `DI_14_Request_Create`, `DI_15_Request_Detail`.
## 2) Files to change (exact paths)
- Backend: `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/CabinetController.java`, `.../interfaces/rest/DeviceInvestorController.java`, `.../application/service/BindingService.java`, `.../application/service/DeviceInvestorService.java`, `.../application/service/CabinetService.java`, `.../infrastructure/repository/InMemoryPlatformRepository.java`, `backend/apps/api-server/src/test/java/com/beizhi/smartwash/PlatformGateIntegrationTest.java`.
- Backend (new): `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/FinanceController.java`, `.../application/service/PayoutAccountService.java`, `.../domain/model/PayoutAccount.java`.
- Frontend/common generation: `tooling/codegen/generate-frontend.mjs`, `frontend/platform-admin/src/pages/PlatformAdminPage.vue`, `frontend/agent-admin/src/pages/GeneratedPage.vue`, `frontend/store-admin/src/pages/GeneratedPage.vue`, `frontend/device-investor/src/pages/GeneratedPage.vue`.
- Frontend/generated metadata: `frontend/platform-admin/src/generated/pages.ts`, `frontend/agent-admin/src/generated/pages.ts`, `frontend/store-admin/src/generated/pages.ts`, `frontend/device-investor/src/generated/pages.ts`.
- Contracts/traceability/docs: `docs/contracts/openapi.yaml`, `docs/contracts/asyncapi.yaml`, `docs/traceability/page_to_api.md`, `docs/ui/page_inventory.csv`, `docs/qa/manual/ITER-06.md`.
## 3) APIs/events touched (operationId / topic)
- OpenAPI operationIds: `patchAssetCabinetStoreBindingsByBindingId`, `postAssetCabinetStoreBindingsByBindingIdApprove`, `getAssetCabinetsByCabinetIdStoreBindings`, `postAssetCabinetsByCabinetIdStoreBindings`, `getAssetDeviceInvestors`, `getAssetDeviceInvestorsByInvestorId`, `getAssetDeviceInvestorsByInvestorIdCabinets`, `getAssetsCabinets`, `getAssetsCabinetsByCabinetId`, `getAssetsCabinetsByCabinetIdSlots`, `getFinancePayoutAccounts`, `postFinancePayoutAccounts`, `patchFinancePayoutAccountsByAccountId`, `postFinancePayoutAccountsByAccountIdSubmitReview`, `postIotCabinetsByCabinetIdCommandsOpenSlot`, `getIotCabinetsByCabinetIdEvents`.
- AsyncAPI topics: `smartwash.domain.events` (`Cabinet.StoreBindingChanged`, `Cabinet.OwnershipChanged`, `Order.RoutedToStore` payload types), `smartwash.finance.events` (`PayoutAccount.Submitted`), `mqtt/cabinets/{cabinetId}/commands/open-slot`, `mqtt/cabinets/{cabinetId}/acks/open-slot`, `mqtt/cabinets/{cabinetId}/events`.
## 4) Automated verification commands to run (make targets)
- `make verify-docs`
- `make verify-openapi`
- `make verify-asyncapi`
- `make verify-page-coverage ITER=ITER-06`
- `make verify-traceability ITER=ITER-06`
- `make verify-visual ITER=ITER-06`
- `make backend-test`
- `make frontend-build APPS="platform-admin agent-admin store-admin device-investor"`
## 5) Manual acceptance checklist by role (links/paths)
- `PLATFORM_ADMIN`: cover investor list/detail/payout review + ownership history + binding create/edit/approval + routing policy; refs `docs/ui/md/Platform_Admin_Portal/PA_39*.md`, `PA_40*.md`, `PA_41*.md`, `PA_42*.md` and matching `docs/ui/svg/Platform_Admin_Portal/*.svg`.
- `AGENT_ADMIN`: cover shared binding dashboard/detail/request create; refs `docs/ui/md/Agent_Admin_Portal/AA_12*.md` + matching `docs/ui/svg/Agent_Admin_Portal/AA_12*.svg`.
- `STORE_ADMIN`: cover binding status/request/history/routing-view; refs `docs/ui/md/Store_Admin_Portal/SA_28*.md` + matching `docs/ui/svg/Store_Admin_Portal/SA_28*.svg`.
- `DEVICE_INVESTOR`: cover cabinet list/detail + deployment/binding requests + request create/detail; refs `docs/ui/md/Device_Investor_Portal/DI_03*.md`, `DI_04*.md`, `DI_13*.md`, `DI_14*.md`, `DI_15*.md` + matching SVG.
- Output manual runbook path for Stage-2: `docs/qa/manual/ITER-06.md`.
## 6) Risks / assumptions / explicit out of scope
- Risk: payout-account APIs are defined in OpenAPI but not implemented in backend controllers/services yet; gate and manual flow will fail until closed.
- Risk: routing policy is represented in requirements/design but contract fields/event schemas are still generic; DTO alignment may require OpenAPI/AsyncAPI schema expansion.
- Assumption: tenant is derived from JWT/request context only; all high-risk writes use `Idempotency-Key` and append audit with `request_id`, actor, tenant, ip, user-agent.
- Out of scope: non-ITER-06 page redesigns, settlement engine rule recalculation beyond required investor visibility, and hardware firmware behavior changes beyond documented command/event contract.
