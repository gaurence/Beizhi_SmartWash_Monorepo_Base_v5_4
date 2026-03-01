# ITER-16 Stage-1 Plan (Plan Only)
## 1) Scope (bounded context / modules / pages)
- Bounded contexts/modules: `Notification`, `Template & Channel Config`, `DLQ Retry/ProcessDue`, `In-App Message Center`, `Realtime Push (SSE)`, `RBAC+Tenant+Idempotency+Audit`.
- `Platform_Admin_Portal`: `PA_24_Notification_Templates`, `PA_24_1_NotificationTemplate_Edit`, `PA_24_2_Channel_Config`, `PA_25_Notifications_DLQ`, `PA_25_1_DLQ_Message_Detail`, `PA_25_2_DLQ_Retry_Confirm_Modal`, `PA_25_3_Process_Due_Run_Modal`.
- `Platform_Ops_Portal`: `PO_10_Notification_Templates`, `PO_10_1_Template_Edit`, `PO_11_Notifications_DLQ`, `PO_11_1_DLQ_Message_Detail`, `PO_11_2_DLQ_Retry_Confirm_Modal`.
- `Store_Admin_Portal`: `SA_17_Notification_Templates`, `SA_17_1_Template_Edit`.
- `Customer_App`: `CA_22_Messages`, `CA_22_1_Message_Detail`; `Technician_App`: `TA_17_1_Notifications_Prefs`.
## 2) Files to change (exact paths)
- Backend: `backend/apps/api-server/src/main/java/com/beizhi/smartwash/interfaces/rest/NotifyController.java` (new), `.../interfaces/rest/AppMessageController.java` (new), `.../interfaces/rest/AppTechController.java`, `.../application/service/NotifyService.java` (new), `.../application/service/AppMessageService.java` (new), `.../application/service/AppTechService.java`, `.../application/service/AuditService.java`, `.../application/service/IdempotencyService.java`, `.../infrastructure/repository/InMemoryPlatformRepository.java`, `backend/apps/api-server/src/test/java/com/beizhi/smartwash/PlatformGateIntegrationTest.java`.
- Frontend wiring: `tooling/codegen/generate-frontend.mjs`, `frontend/platform-admin/src/pages/PlatformAdminPage.vue`, `frontend/platform-ops/src/pages/GeneratedPage.vue`, `frontend/store-admin/src/pages/GeneratedPage.vue`, `frontend/customer-app/src/pages/GeneratedPage.vue`, `frontend/technician-app/src/pages/GeneratedPage.vue`.
- Frontend pages: `frontend/platform-admin/src/pages/iter16/{PA_24_Notification_Templates,PA_24_1_NotificationTemplate_Edit,PA_24_2_Channel_Config,PA_25_Notifications_DLQ,PA_25_1_DLQ_Message_Detail,PA_25_2_DLQ_Retry_Confirm_Modal,PA_25_3_Process_Due_Run_Modal}.vue` (new).
- Frontend pages: `frontend/platform-ops/src/pages/iter16/{PO_10_Notification_Templates,PO_10_1_Template_Edit,PO_11_Notifications_DLQ,PO_11_1_DLQ_Message_Detail,PO_11_2_DLQ_Retry_Confirm_Modal}.vue` (new), `frontend/store-admin/src/pages/iter16/{SA_17_Notification_Templates,SA_17_1_Template_Edit}.vue` (new).
- Frontend pages: `frontend/customer-app/src/pages/iter16/{CA_22_Messages,CA_22_1_Message_Detail}.vue` (new), `frontend/technician-app/src/pages/iter16/TA_17_1_Notifications_Prefs.vue` (new), plus `frontend/*/src/styles.css` for the five in-scope apps.
- Contracts/docs: `docs/contracts/openapi.yaml`, `docs/contracts/asyncapi.yaml`, `docs/traceability/page_to_api.md`, `docs/ui/page_inventory.csv`, `docs/qa/manual/ITER-16.md`.
## 3) APIs/events touched (operationId / topic)
- operationId (notify): `listNotifyTemplates`, `updateNotifyTemplate`, `sendNotifyManual`, `listNotifyChannels`, `updateNotifyChannel`, `listNotifyDlqMessages`, `getNotifyDlqMessage`, `retryNotifyDlqMessage`, `runNotifyProcessDue`.
- operationId (app): `listAppMessages`, `getAppMessageDetail`, `markAppMessageRead`, `getAppTechNotificationPrefs`, `updateAppTechNotificationPrefs`, `getNotificationStream`.
- Async topics/events: `smartwash.domain.events` (`Task.Assigned`,`Order.KeyReturned`,`Order.WashCompleted`,`Refund.Succeeded`), `smartwash.notification.commands`, `smartwash.notification.events` (new), `smartwash.notification.dlq` (new), `smartwash.realtime.push.user` (new).
## 4) Automated verification commands to run (make targets)
- `make verify-docs`
- `make verify-openapi`
- `make verify-asyncapi`
- `make verify-page-coverage ITER=ITER-16`
- `make verify-traceability ITER=ITER-16`
- `make verify-visual ITER=ITER-16`
- `make backend-test`
- `make frontend-build APPS="platform-admin platform-ops store-admin customer-app technician-app"`
## 5) Manual acceptance checklist by role (links/paths)
- `PLATFORM_ADMIN (pa_admin/Passw0rd!)`: verify template list/edit, channel config, DLQ list/detail/retry/process-due via `docs/ui/md/Platform_Admin_Portal/PA_24_Notification_Templates.md`, `PA_24_1_NotificationTemplate_Edit.md`, `PA_24_2_Channel_Config.md`, `PA_25_Notifications_DLQ.md`, `PA_25_1_DLQ_Message_Detail.md`, `PA_25_2_DLQ_Retry_Confirm_Modal.md`, `PA_25_3_Process_Due_Run_Modal.md`.
- `PLATFORM_OPS (ops_admin/Passw0rd!)`: verify notify template + DLQ flow via `docs/ui/md/Platform_Ops_Portal/PO_10_Notification_Templates.md`, `PO_10_1_Template_Edit.md`, `PO_11_Notifications_DLQ.md`, `PO_11_1_DLQ_Message_Detail.md`, `PO_11_2_DLQ_Retry_Confirm_Modal.md`.
- `STORE_ADMIN (store_admin/Passw0rd!)`: verify template scope + save/audit via `docs/ui/md/Store_Admin_Portal/SA_17_Notification_Templates.md`, `SA_17_1_Template_Edit.md`.
- `CUSTOMER (13800000000/123456)`: verify message list/detail/read-state/realtime push via `docs/ui/md/Customer_App/CA_22_Messages.md`, `CA_22_1_Message_Detail.md`.
- `TECHNICIAN (tech01/Passw0rd!)`: verify notification preference read/update via `docs/ui/md/Technician_App/TA_17_1_Notifications_Prefs.md`; `AUDITOR` cross-check append-only logs in `GET /audit/logs`.
## 6) Risks / assumptions / explicitly out of scope
- Risks: current traceability for these pages is `ui-static` with empty endpoints; moving to concrete APIs may break codegen/coverage/traceability gates until `openapi + traceability + generated pages` are updated together.
- Assumptions: tenant derives from JWT (`Authorization`) only, high-risk actions (`DLQ retry`,`process-due`,`template/channel update`,`sensitive message export`) require idempotency + audit, and PII stays masked in logs/responses.
- Out of scope: real SMS/push vendor delivery, production MQ broker ops tooling, and non-ITER-16 page redesigns outside listed page_key set.
