# ITER-02 Plan: 平台租户/组织/用户/角色权限（RBAC+ABAC）

## 1. 页面清单（本迭代范围）
- Platform_Admin_Portal:
  - `PA_03_Tenant_Management`
  - `PA_03_1_Tenant_Create`
  - `PA_03_2_Tenant_Detail_Overview`
  - `PA_03_3_Tenant_Detail_Stores`
  - `PA_03_4_Tenant_Detail_Policies`
  - `PA_03_5_Tenant_Edit`
  - `PA_03_6_Tenant_Audit`
  - `PA_03_7_Org_Tree`
  - `PA_04_User_Management`
  - `PA_04_1_User_Create`
  - `PA_04_2_User_Detail`
  - `PA_04_3_User_Edit`
  - `PA_04_4_User_Roles_Scopes`
  - `PA_05_RBAC_ABAC_Matrix`
  - `PA_05_1_Role_Management`
  - `PA_05_3_ABAC_Policy_Editor`

## 2. API/DTO 清单（ITER-02）
- Audit:
  - `GET /audit/logs`
  - `GET /audit/logs/{logId}`
  - `POST /audit/exports`
- Tenant & Org:
  - `GET /tenants`
  - `POST /tenants`
  - `GET /tenants/{tenantId}`
  - `PUT /tenants/{tenantId}`
  - `GET /tenants/{tenantId}/orgs`
  - `POST /tenants/{tenantId}/orgs`
  - `GET /orgs/tree`
- User & RBAC:
  - `GET /users`
  - `POST /users`
  - `GET /users/{userId}`
  - `PUT /users/{userId}`
  - `PUT /users/{userId}/roles`
  - `GET /roles`
  - `POST /roles`
  - `PUT /roles/{roleId}`
  - `PUT /roles/{roleId}/permissions`
  - `GET /permissions`
- ABAC:
  - `GET /abac/policies`
  - `POST /abac/policies`
  - `PUT /abac/policies/{policyId}`
  - `GET /access-grants`
  - `POST /access-grants`
- DTO（最小可运行）:
  - `TenantDto`, `OrgDto`
  - `UserDto`, `RoleDto`, `PermissionDto`
  - `AbacPolicyDto`, `AccessGrantDto`
  - `AuditExportRequest`, `AuditExportResponse`

## 3. 权限点清单（RBAC + ABAC）
- Tenant & Org:
  - `platform.tenant.read`
  - `platform.tenant.create`
  - `platform.tenant.update`
  - `platform.org.read`
  - `platform.org.manage`
- User & RBAC:
  - `iam.user.read`
  - `iam.user.create`
  - `iam.user.update`
  - `iam.user.role.bind`
  - `iam.role.read`
  - `iam.role.create`
  - `iam.role.update`
  - `iam.role.perm.bind`
  - `iam.perm.read`
- ABAC:
  - `iam.abac.read`
  - `iam.abac.policy.manage`
  - `iam.access-grant.issue`
- Audit:
  - `audit.log.read`
  - `audit.export`
- ABAC 约束（发放 access grant 必须绑定）:
  - `tenant_id`, `user_id`, `order_id`/`task_id`, `cabinet_id`/`slot_id`, `action`, `ttl`

## 4. 事件清单（AsyncAPI）
- 本迭代不新增 MQTT/事件主题；沿用现有 `docs/contracts/asyncapi.yaml`。

## 5. 数据与存储变更
- 继续采用内存仓储（迭代内可验收），新增租户隔离实体：
  - `tenant`, `org`
  - `iam_user`, `iam_role`, `iam_permission`
  - `abac_policy`, `access_grant`
  - `audit_export_job`
- 约束：
  - 所有业务对象按 `tenant_id` 分区访问。
  - 高风险动作（权限变更/ABAC策略/授权发放/导出）写 append-only 审计。
  - 幂等键作用域为 `tenant_id + action_scope + Idempotency-Key`。

## 6. 测试清单
- `tenant isolation`:
  - 跨租户读取租户/用户/角色资源返回 404/403（按接口语义）。
- `permission checks`:
  - IAM/Audit 新接口的 allow + deny。
- `idempotency`:
  - `POST /tenants` 与 `POST /audit/exports` 在相同 `Idempotency-Key` 下返回同一结果。
- `slot state transitions`:
  - 保持既有柜体格口状态机测试通过（防回归）。
- `audit write`:
  - 角色权限绑定、用户角色绑定、ABAC 策略更新、审计导出都会写入审计。
- `gates`:
  - `verify-docs`, `verify-openapi`, `verify-asyncapi`
  - `verify-page-coverage ITER=ITER-02`
  - `verify-traceability ITER=ITER-02`
  - `verify-visual ITER=ITER-02`
  - `backend-test`, `frontend-build APPS=platform-admin`
