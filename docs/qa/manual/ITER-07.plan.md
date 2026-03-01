# ITER-07 Stage-1 Plan (Plan Only)
## 1) Scope (bounded context / modules / pages)
- Bounded contexts/modules: `Customer/Vehicle` (实名/KYC、车辆档案), `Identity & Access` (API permission_key), `Risk & Audit` (append-only audit), `PII masking` (phone/plate/vin/id/license).
- Subsystem: `Customer_App`.
- Page keys in scope: `CA_03_KYC_RealName`, `CA_03_1_KYC_ID_Capture`, `CA_03_2_KYC_DriverLicense`, `CA_03_3_KYC_Review_Pending`, `CA_04_Vehicle_List`, `CA_04_1_Vehicle_Detail`, `CA_05_Vehicle_Add`, `CA_05_1_Vehicle_Edit`.
## 2) Files to change (exact paths)
- Backend: `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/AppCustomerController.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/AppCustomerService.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/infrastructure/repository/InMemoryPlatformRepository.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/application/service/AuditService.java`, `backend/apps/api-server/src/test/java/com/beizhi/smartwash/PlatformGateIntegrationTest.java`.
- Backend (new): `backend/apps/api-server/src/main/java/com/beizhi/smartwash/domain/model/CustomerProfile.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/domain/model/Vehicle.java`, `backend/apps/api-server/src/main/java/com/beizhi/smartwash/domain/model/VehicleDocument.java`.
- Frontend: `tooling/codegen/generate-frontend.mjs`, `frontend/customer-app/src/pages/CustomerAppPage.vue` (new), `frontend/customer-app/src/pages/GeneratedPage.vue`, `frontend/customer-app/src/generated/pages.ts`.
- Contracts/traceability/docs: `docs/contracts/openapi.yaml`, `docs/contracts/asyncapi.yaml`, `docs/traceability/page_to_api.md`, `docs/qa/manual/ITER-07.md` (new).
## 3) APIs/events touched (operationId / topic)
- OpenAPI operationIds (new/updated): `getAppCustomersMe`, `getAppCustomersMeKyc`, `postAppCustomersMeKycUploadUrl`, `postAppCustomersMeKycSubmit`, `getAppVehicles`, `postAppVehicles`, `getAppVehiclesByVehicleId`, `putAppVehiclesByVehicleId`, `deleteAppVehiclesByVehicleId`, `postAppVehiclesByVehicleIdDocumentsUploadUrl`.
- Permission keys for above APIs: `app.customer.profile.read`, `app.customer.kyc.submit`, `app.vehicle.read`, `app.vehicle.manage` (allow+deny tests).
- AsyncAPI topics: `smartwash.domain.events` (eventType additions `Customer.KycSubmitted`, `Customer.KycStatusChanged`, `Vehicle.Added`, `Vehicle.Updated`, `Vehicle.Deleted`), optional notify command on `smartwash.notification.commands` for KYC result/pending updates.
## 4) Automated verification commands to run (make targets)
- `make verify-docs`
- `make verify-openapi`
- `make verify-asyncapi`
- `make verify-page-coverage ITER=ITER-07`
- `make verify-traceability ITER=ITER-07`
- `make verify-visual ITER=ITER-07`
- `make backend-test`
- `make frontend-build APPS="customer-app"`
## 5) Manual acceptance checklist by role (links/paths)
- `CUSTOMER`: login(`CA_02_Login_SMS`) -> 实名流程(`docs/ui/md/Customer_App/CA_03*.md`, `docs/ui/svg/Customer_App/CA_03*.svg`) -> 车辆新增/编辑/详情(`docs/ui/md/Customer_App/CA_04*.md`, `CA_05*.md`)；校验脱敏显示、提交后“审核中”、车辆列表/详情可读写。
- `PLATFORM_ADMIN`/`PLATFORM_CS`: 用审计与接口验证合规（`GET /audit/logs` + KYC/Vehicle API），确认 `request_id`、actor、tenant、resource、result 完整，且无PII明文日志。
- Manual runbook output path (Stage-2): `docs/qa/manual/ITER-07.md`.
## 6) Risks / assumptions / explicit out of scope
- Risks: frontend codegen会覆盖 `GeneratedPage.vue`；需先在 `tooling/codegen/generate-frontend.mjs` 固化 `Customer_App` 自定义页面挂载逻辑。
- Risks: 当前契约未定义KYC/Vehicle schema；openapi/traceability必须同步补齐，否则 Gate B 失败。
- Assumptions: tenant仅来自JWT/RequestContext；状态变更接口统一 `Idempotency-Key`；KYC与车辆敏感字段默认脱敏返回并写审计。
- Out of scope: 平台侧KYC人工审核后台、第三方OCR/实名服务真实对接、非ITER-07页面重做。
