# ITER-03 Stage-1 Plan (Plan Only)
## 1) Scope (bounded context / modules / pages)
- Bounded Contexts: `Tenant&Org` (store lifecycle), `Asset&Investment` (cabinet-store binding), `Cabinet&IoT` (open-slot/events), `Settlement` (commercial agreement link), plus cross-cutting `RBAC/ABAC`, `tenant isolation`, `idempotency`, `append-only audit`.
- Subsystem: `Agent_Admin_Portal`.
- page_key in scope: `AA_03_1_Store_Create`, `AA_03_Stores_List`, `AA_04_1_Store_Edit`, `AA_04_2_Store_Contracts`, `AA_04_Store_Detail`, `AA_05_Store_Onboarding`, `AA_05_1_Onboarding_Step1_Basic`, `AA_05_2_Onboarding_Step2_Contract`, `AA_05_3_Onboarding_Step3_DeviceBind`, `AA_05_4_Onboarding_Step4_GoLive`.
- Security/compliance baseline for this iter: JWT-derived `tenant_id` only, per-page/API `permission_key`, ABAC scoped token for open-slot/emergency actions, high-risk actions audited with `request_id`.

## 2) Files to change (exact paths)
- Backend: `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/TenantOrgController.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/CabinetController.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/TenantOrgService.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/CabinetService.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/BindingService.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/AuditService.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/IdempotencyService.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/infrastructure/repository/InMemoryPlatformRepository.java`.
- Frontend (Agent app): `frontend/agent-admin/src/pages/GeneratedPage.vue`, `frontend/agent-admin/src/pages/AgentAdminPage.vue` (new), `frontend/agent-admin/src/router/index.ts`, `frontend/agent-admin/src/styles.css`, `frontend/agent-admin/src/generated/pages.ts`.
- Frontend codegen support: `tooling/codegen/generate-frontend.mjs`.
- Contracts/traceability/docs: `docs/contracts/openapi.yaml`, `docs/contracts/asyncapi.yaml`, `docs/traceability/page_to_api.md`, `docs/ui/page_inventory.csv`, `docs/qa/manual/ITER-03.md`.
- Tests: `backend/apps/api-server/src/test/java/com/beizhi/smartwash/PlatformGateIntegrationTest.java`.

## 3) APIs/events touched (operationId / topic)
- Existing operationId: `getAssetsCabinets`, `getAssetsCabinetsByCabinetId`, `getAssetsCabinetsByCabinetIdSlots`, `postIotCabinetsByCabinetIdCommandsOpenSlot`, `getIotCabinetsByCabinetIdEvents`, `getAssetCabinetsByCabinetIdStoreBindings`, `postAssetCabinetsByCabinetIdStoreBindings`, `patchAssetCabinetStoreBindingsByBindingId`, `postAssetCabinetStoreBindingsByBindingIdApprove`, `getTenantsByTenantIdStores`.
- Planned contract additions for ITER-03 (per detailed design): `listStores`, `createStore`, `updateStore`, `listAgreements`, `createAgreement`.
- Async topics: `smartwash.domain.events` (event types include `Store.Created|Store.Updated|Agreement.Linked|Cabinet.StoreBindingChanged`), `mqtt/cabinets/{cabinetId}/commands/open-slot`, `mqtt/cabinets/{cabinetId}/acks/open-slot`, `mqtt/cabinets/{cabinetId}/events`.

## 4) Automated verification commands to run (make targets)
- `make verify-docs`
- `make verify-openapi`
- `make verify-asyncapi`
- `make verify-page-coverage ITER=ITER-03`
- `make verify-traceability ITER=ITER-03`
- `make verify-visual ITER=ITER-03`
- `make backend-test`
- `make frontend-build APPS=agent-admin`

## 5) Manual acceptance checklist by role (links/paths)
- `AGENT_ADMIN`: from `/pages/AA_03_Stores_List` complete create→detail→edit→contracts and onboarding step flow (`/pages/AA_05_Store_Onboarding` then Step1~Step4); verify route links and RBAC visibility.
- `PLATFORM_ADMIN`: verify cross-tenant denial/invisibility on store/cabinet/binding APIs and audit completeness for create/update/binding/open-slot actions.
- `PLATFORM_IOTOPS`: on Step3 DeviceBind verify open-slot idempotency and event trace (`request_id` correlation to audit/event).
- `STORE_ADMIN` and `INVESTOR_ADMIN`: verify no unauthorized access to Agent-only pages/actions and masked sensitive fields.
- UI/trace references: `docs/ui/md/Agent_Admin_Portal/AA_03_*.md`, `docs/ui/md/Agent_Admin_Portal/AA_04_*.md`, `docs/ui/md/Agent_Admin_Portal/AA_05_*.md`, `docs/ui/svg/Agent_Admin_Portal/AA_03_*.svg`, `docs/ui/svg/Agent_Admin_Portal/AA_04_*.svg`, `docs/ui/svg/Agent_Admin_Portal/AA_05_*.svg`, `docs/traceability/page_to_api.md`.

## 6) Risks / assumptions / explicit out of scope
- Risks: current OpenAPI lacks dedicated `/stores` and `/agreements` CRUD paths; operationId naming alignment between design (`/api/v1/*`) and current contract paths must be resolved in-contract before coding.
- Assumptions: keep existing make gates and generated route pattern `/pages/:pageKey`; preserve tenant isolation from JWT/context, no request-body tenant trust, high-risk actions append-only audited.
- Out of scope: non-`Agent_Admin_Portal` pages, unrelated order/finance/refund workflows, introducing new MQTT channels beyond existing AsyncAPI topics.
