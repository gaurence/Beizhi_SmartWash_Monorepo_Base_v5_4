# ITER-01 Plan: 统一认证登录、租户会话、角色菜单与首页仪表盘

## 1. 页面清单（本迭代范围）
- Platform_Admin_Portal:
  - `PA_01_Login`, `PA_02_Global_Dashboard`, `PA_30_System_Settings`, `PA_30_1_MFA_Devices`, `PA_30_2_Integrations`, `PA_30_3_Data_Retention_Settings`, `PA_31_Profile`, `PA_31_1_Profile_ChangePassword`, `PA_31_2_Profile_MFA_Setup`
- Platform_Ops_Portal:
  - `PO_01_Login`, `PO_02_Ops_Dashboard`, `PO_15_Profile`, `PO_15_1_ChangePassword`
- Platform_Finance_Portal:
  - `PF_01_Login`, `PF_02_Finance_Dashboard`, `PF_18_Profile`
- Platform_IoTOps_Portal:
  - `PI_01_Login`, `PI_02_IoT_Dashboard`, `PI_11_Profile`
- Platform_CS_Portal:
  - `PC_01_Login`, `PC_02_CS_Dashboard`, `PC_11_Profile`
- Agent_Admin_Portal:
  - `AA_01_Login`, `AA_02_Region_Dashboard`, `AA_11_Profile`, `AA_11_1_ChangePassword`
- Store_Admin_Portal:
  - `SA_01_Login`, `SA_02_Store_Dashboard`, `SA_19_Profile`, `SA_19_1_ChangePassword`
- Device_Investor_Portal:
  - `DI_01_Login`, `DI_02_Investor_Dashboard`, `DI_17_Profile`
- Property_Portal:
  - `PP_01_Login`, `PP_02_Property_Dashboard`, `PP_09_Profile`
- Customer_App:
  - `CA_01_Splash`, `CA_01_1_Privacy_Consent`, `CA_02_1_Login_Wechat`, `CA_02_Login_SMS`, `CA_06_Home_Sites`, `CA_06_1_Home_Map`, `CA_25_Profile_Settings`, `CA_25_1_Settings_Notifications`, `CA_25_2_Settings_AccountSecurity`
- Technician_App:
  - `TA_01_Login`, `TA_01_1_Face_Verify`, `TA_01_5_Authorization_Bind`, `TA_16_Profile`, `TA_17_Settings`, `TA_17_1_Notifications_Prefs`, `TA_17_2_Help_FAQ`
- Cabinet_Screen:
  - `CK_00_Select_Mode`, `CK_01_Welcome`, `CK_02_Scan_QR`, `CK_11_Maintenance`, `CK_14_Staff_Mode_Login`

## 2. API/DTO 清单（ITER-01）
- Identity / Tenant Session:
  - `POST /auth/login`
  - `POST /auth/mfa/verify`
  - `POST /auth/logout`
  - `POST /app/auth/sms/send`
  - `POST /app/auth/sms/login`
  - `POST /app/auth/logout`
- Customer App Site Discovery:
  - `GET /app/sites/nearby`
  - `GET /app/sites/{siteId}`
  - `GET /app/sites/{siteId}/queue`
- Asset / Investor:
  - `GET /asset/device-investors`
  - `GET /asset/device-investors/{investorId}`
  - `GET /asset/device-investors/{investorId}/cabinets`
  - `GET /assets/cabinets`
  - `GET /assets/cabinets/{cabinetId}`
  - `GET /assets/cabinets/{cabinetId}/slots`
- IoT:
  - `POST /iot/cabinets/{cabinetId}/commands/open-slot`
  - `GET /iot/cabinets/{cabinetId}/events`
- DTO（最小可运行）：
  - `ApiResponse`, `PageResponse`
  - `AuthLoginResponse`, `AppSmsLoginResponse`
  - `SiteDto`, `SiteQueueItemDto`
  - `DeviceInvestorDto`, `CabinetDto`, `SlotDto`

## 3. 权限点清单（RBAC + ABAC）
- 接口权限（后端）：
  - `asset.cabinet.read`
  - `iot.open-slot.execute`
  - `asset.binding.manage`
  - `asset.investor.read`
  - `app.site.read`
  - `audit.log.read`
- 页面权限：以 `docs/traceability/page_to_api.md` 中 `permission_key` 为准（本迭代页面必须全量映射）。
- ABAC（ITER-01 最小实现）：高风险开柜链路继续使用短期上下文（tenant/user/slot/action/request_id）并保留幂等与审计写入。

## 4. 事件清单（AsyncAPI）
- `mqtt/cabinets/{cabinetId}/commands/open-slot`
- `mqtt/cabinets/{cabinetId}/acks/open-slot`
- `mqtt/cabinets/{cabinetId}/events`
- `smartwash.domain.events`（审计/状态变更占位）

## 5. 数据与存储变更
- 继续采用内存仓储实现（用于本迭代端到端验收），新增租户隔离实体：
  - `site`（站点基础信息）
  - `site_queue`（站点排队视图）
  - `device_investor`（投资方主数据）
  - `device_investor_cabinet`（投资方可见柜体关系）
- 既有模型继续保留：
  - `cabinet`, `slot`, `cabinet_store_binding`, `idempotency_record`, `audit_log`
- 约束：
  - 所有业务对象按 `tenant_id` 分区访问
  - 审计 append-only
  - 幂等键作用域包含 `tenant_id + action + Idempotency-Key`

## 6. 测试清单
- `tenant isolation`：
  - 跨租户柜体访问返回 404
  - 基于 Bearer token 的租户解析可隔离 App 站点数据
  - 设备投资方接口跨租户访问返回 404
- `permission checks`：
  - `audit.log.read`, `asset.investor.read`, `app.site.read` 的 allow/deny
- `idempotency`：
  - `open-slot` 重复调用返回同一 commandId/requestId
- `slot state transitions`：
  - 非法转移返回 409
- `audit write`：
  - 高风险操作（开柜、共享绑定）写入审计日志
- `gates`：
  - `verify-docs`, `verify-openapi`, `verify-asyncapi`
  - `verify-page-coverage ITER=ITER-01`
  - `verify-traceability ITER=ITER-01`
  - `verify-visual ITER=ITER-01`（沙箱中默认 mock 模式）
