# ITER-08 Stage-1 Plan (Plan Only)
## 1) Scope (bounded context / modules / pages)
- Bounded contexts/modules: `Catalog & Pricing`（服务包/加项/动态定价/报价快照）, `Settlement Policy`（服务费/分润策略）, `Retention Policy`（滞留费规则/提醒日志）, `App Order`（下单链路价格固化）。
- Subsystems: `Customer_App`, `Store_Admin_Portal`, `Platform_Admin_Portal`.
- Page keys: `CA_08_1/2/3/4`, `SA_09_1/2 + SA_09`, `SA_10_1/2 + SA_10`, `SA_18_1/2 + SA_18`, `PA_12_1 + PA_12`, `PA_13_1 + PA_13`, `PA_14_1/2 + PA_14`.

## 2) Files to change (exact paths)
- Backend existing: `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/AppCustomerController.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/AppCustomerService.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/infrastructure/repository/InMemoryPlatformRepository.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/AuditService.java`, `backend/apps/api-server/src/test/java/com/beizhi/smartwash/PlatformGateIntegrationTest.java`.
- Backend new (planned): `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/CatalogPricingController.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/PolicyController.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/CatalogPricingService.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/PolicyService.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/domain/model/ServiceItem.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/domain/model/ServiceVariant.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/domain/model/ServiceAddon.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/domain/model/PricingRule.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/domain/model/RetentionPolicy.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/domain/model/PolicyAuditRecord.java`.
- Frontend/codegen: `tooling/codegen/generate-frontend.mjs`, `frontend/customer-app/src/pages/CustomerAppPage.vue` (new), `frontend/customer-app/src/pages/GeneratedPage.vue`, `frontend/customer-app/src/styles.css`, `frontend/store-admin/src/pages/StoreAdminPage.vue` (new), `frontend/store-admin/src/pages/GeneratedPage.vue`, `frontend/store-admin/src/styles.css`, `frontend/platform-admin/src/pages/PlatformAdminPage.vue`, `frontend/platform-admin/src/styles.css`.
- Contracts/traceability/docs: `docs/contracts/openapi.yaml`, `docs/contracts/asyncapi.yaml`, `docs/traceability/page_to_api.md`, `docs/ui/page_inventory.csv`, `docs/qa/manual/ITER-08.md` (new).

## 3) APIs/events touched (operationId / topic)
- Existing app order operationIds: `getAppOrders`, `postAppOrders`, `getAppOrdersByOrderId`, `getAppOrdersByOrderIdTimeline`.
- Planned operationIds: `getServices`, `postServices`, `putServicesByServiceId`, `getVariants`, `postVariants`, `putVariantsByVariantId`, `getAddons`, `postAddons`, `putAddonsByAddonId`, `getPricingRules`, `postPricingRules`, `putPricingRulesByRuleId`, `postQuotes`, `getPriceSnapshotsByOrderId`, `getFinanceServiceFeePolicies`, `putFinanceServiceFeePoliciesByPolicyId`, `getFinanceRevenueSharePolicies`, `putFinanceRevenueSharePoliciesByPolicyId`, `getRetentionPolicies`, `putRetentionPoliciesByPolicyId`, `getRetentionReminders`.
- Async topics: `smartwash.domain.events`（`Catalog.Updated`, `PricingRule.Updated`, `PriceSnapshot.Created`, `ServiceFeePolicy.Updated`, `RevenueSharePolicy.Updated`, `RetentionPolicy.Updated`, `RetentionReminder.Sent`）, `smartwash.notification.commands`（滞留提醒下发）。

## 4) Automated verification commands to run (make targets)
- `make verify-docs`
- `make verify-openapi`
- `make verify-asyncapi`
- `make verify-page-coverage ITER=ITER-08`
- `make verify-traceability ITER=ITER-08`
- `make verify-visual ITER=ITER-08`
- `make backend-test`
- `make frontend-build APPS="platform-admin store-admin customer-app"`

## 5) Manual acceptance checklist by role (links/paths)
- `CUSTOMER`: `docs/ui/md/Customer_App/CA_08_1_Select_Service_Package.md` → `CA_08_2_AddOns.md` → `CA_08_3_Coupon_Select.md` → `CA_08_4_Order_Confirm.md`；校验价格构成、优惠计算、重复提交幂等。
- `STORE_ADMIN`: `docs/ui/md/Store_Admin_Portal/SA_09_Service_Catalog.md`, `SA_09_1_Service_Create.md`, `SA_10_Dynamic_Pricing.md`, `SA_10_1_PricingRule_Create.md`, `SA_18_Retention_Policy.md`, `SA_18_2_Retention_Reminders.md`；校验新增/编辑/禁用、租户隔离、审计记录。
- `PLATFORM_ADMIN`: `docs/ui/md/Platform_Admin_Portal/PA_12_ServiceFee_Policy.md`, `PA_13_RevenueShare_Policy.md`, `PA_14_Retention_Policy.md`, `PA_14_2_Retention_Reminders_Log.md`；校验策略编辑写审计、越权拒绝、导出/敏感字段合规。

## 6) Risks / assumptions / explicit out of scope
- Risks: 当前 `openapi.yaml` 缺少 Catalog/Pricing/Retention/Policy 路径；若不先补齐契约与 traceability，Gate B 会失败。
- Risks: `generate-frontend.mjs` 会覆盖 `GeneratedPage.vue`，必须先改代码生成模板再做页面实现。
- Assumptions: tenant 仅来自 JWT/`RequestContext`；写操作统一 `Idempotency-Key`；服务费/分润/滞留策略变更均写 append-only `audit_log`。
- Out of scope: 第三方支付/券系统真实对接、运营报表扩展、非 ITER-08 页面重做。
