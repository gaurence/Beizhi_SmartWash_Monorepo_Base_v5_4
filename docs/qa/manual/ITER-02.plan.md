# ITER-02 Stage-1 Plan (Plan Only)

## 1) Scope (bounded context / modules / pages)
- Bounded Contexts/Modules: `tenant-org management`, `rbac`, `abac policy` (with mandatory `tenant isolation`, `idempotency`, `append-only audit`).
- Subsystem: `Platform_Admin_Portal`.
- page_key in scope: `PA_03_Tenant_Management`, `PA_03_1_Tenant_Create`, `PA_03_2_Tenant_Detail_Overview`, `PA_03_3_Tenant_Detail_Stores`, `PA_03_4_Tenant_Detail_Policies`, `PA_03_5_Tenant_Edit`, `PA_03_6_Tenant_Audit`, `PA_03_7_Org_Tree`, `PA_04_User_Management`, `PA_04_1_User_Create`, `PA_04_2_User_Detail`, `PA_04_3_User_Edit`, `PA_04_4_User_Roles_Scopes`, `PA_05_RBAC_ABAC_Matrix`, `PA_05_1_Role_Management`, `PA_05_3_ABAC_Policy_Editor`.

## 2) Files to change (exact paths)
- Backend API/app: `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/{TenantOrgController,RbacController,AbacController,AuditController}.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/{TenantOrgService,RbacService,AbacService,AuditService,IdempotencyService}.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/security/{RequestContextFilter,PermissionGuard}.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/infrastructure/repository/InMemoryPlatformRepository.java`.
- Backend tests: `backend/apps/api-server/src/test/java/com/beizhi/smartwash/PlatformGateIntegrationTest.java` (and add iter-specific IAM tests under same folder).
- Frontend (platform-admin): `frontend/platform-admin/src/pages/GeneratedPage.vue`, `frontend/platform-admin/src/pages/PlatformAdminPage.vue` (new), `frontend/platform-admin/src/router/index.ts`, `frontend/platform-admin/src/styles.css`, `frontend/platform-admin/src/generated/pages.ts` (codegen output).
- Contracts/traceability/page status/manual QA: `docs/contracts/openapi.yaml`, `docs/contracts/asyncapi.yaml` (only if event delta), `docs/traceability/page_to_api.md`, `docs/ui/page_inventory.csv`, `docs/qa/manual/ITER-02.md`.

## 3) APIs / events touched (operationId / topic)
- Tenant/Org: `getTenants`, `postTenants`, `getTenantsByTenantId`, `putTenantsByTenantId`, `getTenantsByTenantIdOrgs`, `postTenantsByTenantIdOrgs`, `getTenantsByTenantIdStores`, `getOrgsTree`.
- RBAC: `getUsers`, `postUsers`, `getUsersByUserId`, `putUsersByUserId`, `putUsersByUserIdRoles`, `getRoles`, `postRoles`, `putRolesByRoleId`, `putRolesByRoleIdPermissions`, `getPermissions`.
- ABAC: `getAbacPolicies`, `postAbacPolicies`, `putAbacPoliciesByPolicyId`, `getAccessGrants`, `postAccessGrants`.
- Audit: `getAuditLogs`, `getAuditLogsByLogId`, `postAuditExports`.
- Events/topics: no new topic planned for ITER-02; keep existing AsyncAPI topics unchanged (`smartwash.domain.events` only if implementation emits IAM/audit domain events).

## 4) Automated verification commands (make targets)
- `make verify-docs`
- `make verify-openapi`
- `make verify-asyncapi`
- `make verify-page-coverage ITER=ITER-02`
- `make verify-traceability ITER=ITER-02`
- `make verify-visual ITER=ITER-02`
- `make backend-test`
- `make frontend-build APPS=platform-admin`

## 5) Manual acceptance checklist by role (links/paths)
- `PLATFORM_ADMIN`: walk `PA_03* -> PA_04* -> PA_05*` via `/pages/{page_key}`; validate create/edit/detail/role-bind/abac-policy/audit-export success and audit append-only behavior.
- `PLATFORM_OPS` / `AGENT_ADMIN` / `STORE_ADMIN`: same route paths must return deny/invisible behavior for out-of-scope permissions (`403/404` as defined).
- Validate page skeleton and navigation against: `docs/ui/md/Platform_Admin_Portal/PA_03*.md`, `docs/ui/md/Platform_Admin_Portal/PA_04*.md`, `docs/ui/md/Platform_Admin_Portal/PA_05*.md` and corresponding `docs/ui/svg/Platform_Admin_Portal/*.svg`.
- Validate mapping consistency in `docs/traceability/page_to_api.md` and API contract in `docs/contracts/openapi.yaml`.

## 6) Risks / assumptions / explicit out-of-scope
- Risks: current `frontend/platform-admin/src/pages/GeneratedPage.vue` references missing `PlatformAdminPage.vue`; visual gate may fail until fixed. Contract base path (`/api` in OpenAPI vs `/api/v1` in design text) requires explicit alignment decision.
- Assumptions: JWT-derived tenant remains source-of-truth; no trust of request-body tenantId; Idempotency-Key required on write APIs; high-risk IAM/ABAC/audit-export actions always write audit.
- Out of scope: non-`PA_03/04/05` pages, non-IAM business domains (order/finance/iot logic changes), new AsyncAPI topic design beyond existing contract.
