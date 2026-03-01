# Stage 1 — PLAN ONLY (anti-disconnect)

**HARD RULES (must follow):**
- Do **NOT** modify any code or files in this stage. Planning only.
- Keep the plan short and actionable (aim ≤ 40 lines).
- Output must be written to: `docs/qa/manual/ITER-01.plan.md`
- Terminal/chat output must be minimal: only confirm the plan file path.

**Inputs (single source of truth — you MUST read these files):**
- `docs/requirements/Beizhi_v5_4.md`
- `docs/design/Beizhi_DDD_Detailed_Design_v5_4.md`
- `docs/ui/page_inventory.csv`
- `docs/ui/svg/...` and `docs/ui/md/...` for pages in scope
- `docs/traceability/page_to_api.md`
- `docs/contracts/openapi.yaml`
- `docs/contracts/asyncapi.yaml`
- `AGENTS.md`
- `.codex/config.toml`

**Plan must include:**
1) Scope (bounded context / modules / pages by page_key)
2) Files to change (exact paths)
3) APIs/events touched (operationId / topic)
4) Automated verification commands to run (make targets)
5) Manual acceptance checklist by role (links/paths)
6) Risks/assumptions and what is explicitly out of scope

Now produce the plan and write it to `docs/qa/manual/ITER-01.plan.md`, then stop.

---

## Context (from original ITER prompt)

# ITER-01 统一认证登录、租户会话、角色菜单与首页仪表盘

> **执行方式（VS Code Codex Extension）**：把本文件全文作为一次任务提示词交给 Codex 执行。

## 0. 迭代目标（必须达成）
- 本迭代完成后，相关子系统/页面可以 **端到端运行**（前端可访问、后端可调用、数据可读写/或模拟），并可按指定角色完成手动验收。
- 本迭代涉及页面的 UI 实现必须以对应 SVG/MD 为基准：**不直接嵌入 SVG**，而是用 Vue/HTML/CSS 复刻布局与组件，交互与链接以 MD 为准。
- 通过三道硬门禁（Page Coverage / Traceability / Visual）。

## 1. 必读输入（不得跳过）
- 需求：`docs/requirements/Beizhi_v5_4.md`
- 详细设计：`docs/design/Beizhi_DDD_Detailed_Design_v5_4.md`
- 页面清单：`docs/ui/page_inventory.csv`
- 追踪矩阵：`docs/traceability/page_to_api.md`
- REST 契约：`docs/contracts/openapi.yaml`
- 事件契约：`docs/contracts/asyncapi.yaml`
- 工程红线：`AGENTS.md`

## 2. 本迭代范围
### 2.1 后端范围（Bounded Context / Module）
- identity-access (JWT/OAuth2)
- tenant-context (tenant resolution)
- profile-security (MFA/ChangePwd)

### 2.2 涉及的前端子系统（Sub-Apps）
- Agent_Admin_Portal
- Cabinet_Screen
- Customer_App
- Device_Investor_Portal
- Platform_Admin_Portal
- Platform_CS_Portal
- Platform_Finance_Portal
- Platform_IoTOps_Portal
- Platform_Ops_Portal
- Property_Portal
- Store_Admin_Portal
- Technician_App

### 2.3 页面范围（必须完成 UI/交互/链接，按 MD）
### Platform_Admin_Portal
- `PA_01_Login` 备至·智洗平台  
  - SVG: `docs/ui/svg/Platform_Admin_Portal/PA_01_Login.svg`  
  - MD: `docs/ui/md/Platform_Admin_Portal/PA_01_Login.md`
- `PA_02_Global_Dashboard` 运营大盘  
  - SVG: `docs/ui/svg/Platform_Admin_Portal/PA_02_Global_Dashboard.svg`  
  - MD: `docs/ui/md/Platform_Admin_Portal/PA_02_Global_Dashboard.md`
- `PA_30_1_MFA_Devices` MFA 设备管理  
  - SVG: `docs/ui/svg/Platform_Admin_Portal/PA_30_1_MFA_Devices.svg`  
  - MD: `docs/ui/md/Platform_Admin_Portal/PA_30_1_MFA_Devices.md`
- `PA_30_2_Integrations` 集成配置  
  - SVG: `docs/ui/svg/Platform_Admin_Portal/PA_30_2_Integrations.svg`  
  - MD: `docs/ui/md/Platform_Admin_Portal/PA_30_2_Integrations.md`
- `PA_30_3_Data_Retention_Settings` 数据保留与脱敏  
  - SVG: `docs/ui/svg/Platform_Admin_Portal/PA_30_3_Data_Retention_Settings.svg`  
  - MD: `docs/ui/md/Platform_Admin_Portal/PA_30_3_Data_Retention_Settings.md`
- `PA_30_System_Settings` 系统设置（安全基线/MFA）  
  - SVG: `docs/ui/svg/Platform_Admin_Portal/PA_30_System_Settings.svg`  
  - MD: `docs/ui/md/Platform_Admin_Portal/PA_30_System_Settings.md`
- `PA_31_1_Profile_ChangePassword` 修改密码  
  - SVG: `docs/ui/svg/Platform_Admin_Portal/PA_31_1_Profile_ChangePassword.svg`  
  - MD: `docs/ui/md/Platform_Admin_Portal/PA_31_1_Profile_ChangePassword.md`
- `PA_31_2_Profile_MFA_Setup` MFA 设置  
  - SVG: `docs/ui/svg/Platform_Admin_Portal/PA_31_2_Profile_MFA_Setup.svg`  
  - MD: `docs/ui/md/Platform_Admin_Portal/PA_31_2_Profile_MFA_Setup.md`
- `PA_31_Profile` 我的账号  
  - SVG: `docs/ui/svg/Platform_Admin_Portal/PA_31_Profile.svg`  
  - MD: `docs/ui/md/Platform_Admin_Portal/PA_31_Profile.md`

### Platform_Ops_Portal
- `PO_01_Login` 备至·智洗平台  
  - SVG: `docs/ui/svg/Platform_Ops_Portal/PO_01_Login.svg`  
  - MD: `docs/ui/md/Platform_Ops_Portal/PO_01_Login.md`
- `PO_02_Ops_Dashboard` 运营大盘  
  - SVG: `docs/ui/svg/Platform_Ops_Portal/PO_02_Ops_Dashboard.svg`  
  - MD: `docs/ui/md/Platform_Ops_Portal/PO_02_Ops_Dashboard.md`
- `PO_15_1_ChangePassword` 修改密码  
  - SVG: `docs/ui/svg/Platform_Ops_Portal/PO_15_1_ChangePassword.svg`  
  - MD: `docs/ui/md/Platform_Ops_Portal/PO_15_1_ChangePassword.md`
- `PO_15_Profile` 我的账号  
  - SVG: `docs/ui/svg/Platform_Ops_Portal/PO_15_Profile.svg`  
  - MD: `docs/ui/md/Platform_Ops_Portal/PO_15_Profile.md`

### Platform_Finance_Portal
- `PF_01_Login` 备至·智洗平台  
  - SVG: `docs/ui/svg/Platform_Finance_Portal/PF_01_Login.svg`  
  - MD: `docs/ui/md/Platform_Finance_Portal/PF_01_Login.md`
- `PF_02_Finance_Dashboard` 运营大盘  
  - SVG: `docs/ui/svg/Platform_Finance_Portal/PF_02_Finance_Dashboard.svg`  
  - MD: `docs/ui/md/Platform_Finance_Portal/PF_02_Finance_Dashboard.md`
- `PF_18_Profile` 我的账号  
  - SVG: `docs/ui/svg/Platform_Finance_Portal/PF_18_Profile.svg`  
  - MD: `docs/ui/md/Platform_Finance_Portal/PF_18_Profile.md`

### Platform_IoTOps_Portal
- `PI_01_Login` 备至·智洗平台  
  - SVG: `docs/ui/svg/Platform_IoTOps_Portal/PI_01_Login.svg`  
  - MD: `docs/ui/md/Platform_IoTOps_Portal/PI_01_Login.md`
- `PI_02_IoT_Dashboard` 运营大盘  
  - SVG: `docs/ui/svg/Platform_IoTOps_Portal/PI_02_IoT_Dashboard.svg`  
  - MD: `docs/ui/md/Platform_IoTOps_Portal/PI_02_IoT_Dashboard.md`
- `PI_11_Profile` 我的账号  
  - SVG: `docs/ui/svg/Platform_IoTOps_Portal/PI_11_Profile.svg`  
  - MD: `docs/ui/md/Platform_IoTOps_Portal/PI_11_Profile.md`

### Platform_CS_Portal
- `PC_01_Login` 备至·智洗平台  
  - SVG: `docs/ui/svg/Platform_CS_Portal/PC_01_Login.svg`  
  - MD: `docs/ui/md/Platform_CS_Portal/PC_01_Login.md`
- `PC_02_CS_Dashboard` 运营大盘  
  - SVG: `docs/ui/svg/Platform_CS_Portal/PC_02_CS_Dashboard.svg`  
  - MD: `docs/ui/md/Platform_CS_Portal/PC_02_CS_Dashboard.md`
- `PC_11_Profile` 我的账号  
  - SVG: `docs/ui/svg/Platform_CS_Portal/PC_11_Profile.svg`  
  - MD: `docs/ui/md/Platform_CS_Portal/PC_11_Profile.md`

### Agent_Admin_Portal
- `AA_01_Login` 备至·智洗平台  
  - SVG: `docs/ui/svg/Agent_Admin_Portal/AA_01_Login.svg`  
  - MD: `docs/ui/md/Agent_Admin_Portal/AA_01_Login.md`
- `AA_02_Region_Dashboard` 区域运营大盘  
  - SVG: `docs/ui/svg/Agent_Admin_Portal/AA_02_Region_Dashboard.svg`  
  - MD: `docs/ui/md/Agent_Admin_Portal/AA_02_Region_Dashboard.md`
- `AA_11_1_ChangePassword` 修改密码  
  - SVG: `docs/ui/svg/Agent_Admin_Portal/AA_11_1_ChangePassword.svg`  
  - MD: `docs/ui/md/Agent_Admin_Portal/AA_11_1_ChangePassword.md`
- `AA_11_Profile` 我的账号  
  - SVG: `docs/ui/svg/Agent_Admin_Portal/AA_11_Profile.svg`  
  - MD: `docs/ui/md/Agent_Admin_Portal/AA_11_Profile.md`

### Store_Admin_Portal
- `SA_01_Login` 备至·智洗平台  
  - SVG: `docs/ui/svg/Store_Admin_Portal/SA_01_Login.svg`  
  - MD: `docs/ui/md/Store_Admin_Portal/SA_01_Login.md`
- `SA_02_Store_Dashboard` 门店看板  
  - SVG: `docs/ui/svg/Store_Admin_Portal/SA_02_Store_Dashboard.svg`  
  - MD: `docs/ui/md/Store_Admin_Portal/SA_02_Store_Dashboard.md`
- `SA_19_1_ChangePassword` 修改密码  
  - SVG: `docs/ui/svg/Store_Admin_Portal/SA_19_1_ChangePassword.svg`  
  - MD: `docs/ui/md/Store_Admin_Portal/SA_19_1_ChangePassword.md`
- `SA_19_Profile` 我的账号  
  - SVG: `docs/ui/svg/Store_Admin_Portal/SA_19_Profile.svg`  
  - MD: `docs/ui/md/Store_Admin_Portal/SA_19_Profile.md`

### Device_Investor_Portal
- `DI_01_Login` 备至·智洗平台  
  - SVG: `docs/ui/svg/Device_Investor_Portal/DI_01_Login.svg`  
  - MD: `docs/ui/md/Device_Investor_Portal/DI_01_Login.md`
- `DI_02_Investor_Dashboard` 资产总览  
  - SVG: `docs/ui/svg/Device_Investor_Portal/DI_02_Investor_Dashboard.svg`  
  - MD: `docs/ui/md/Device_Investor_Portal/DI_02_Investor_Dashboard.md`
- `DI_17_Profile` 我的账号  
  - SVG: `docs/ui/svg/Device_Investor_Portal/DI_17_Profile.svg`  
  - MD: `docs/ui/md/Device_Investor_Portal/DI_17_Profile.md`

### Property_Portal
- `PP_01_Login` 备至·智洗平台  
  - SVG: `docs/ui/svg/Property_Portal/PP_01_Login.svg`  
  - MD: `docs/ui/md/Property_Portal/PP_01_Login.md`
- `PP_02_Property_Dashboard` 运营大盘  
  - SVG: `docs/ui/svg/Property_Portal/PP_02_Property_Dashboard.svg`  
  - MD: `docs/ui/md/Property_Portal/PP_02_Property_Dashboard.md`
- `PP_09_Profile` 我的账号  
  - SVG: `docs/ui/svg/Property_Portal/PP_09_Profile.svg`  
  - MD: `docs/ui/md/Property_Portal/PP_09_Profile.md`

### Customer_App
- `CA_01_1_Privacy_Consent` 隐私与授权  
  - SVG: `docs/ui/svg/Customer_App/CA_01_1_Privacy_Consent.svg`  
  - MD: `docs/ui/md/Customer_App/CA_01_1_Privacy_Consent.md`
- `CA_01_Splash` 备至代客洗车  
  - SVG: `docs/ui/svg/Customer_App/CA_01_Splash.svg`  
  - MD: `docs/ui/md/Customer_App/CA_01_Splash.md`
- `CA_02_1_Login_Wechat` 登录方式  
  - SVG: `docs/ui/svg/Customer_App/CA_02_1_Login_Wechat.svg`  
  - MD: `docs/ui/md/Customer_App/CA_02_1_Login_Wechat.md`
- `CA_02_Login_SMS` 手机号登录  
  - SVG: `docs/ui/svg/Customer_App/CA_02_Login_SMS.svg`  
  - MD: `docs/ui/md/Customer_App/CA_02_Login_SMS.md`
- `CA_06_1_Home_Map` 附近站点地图  
  - SVG: `docs/ui/svg/Customer_App/CA_06_1_Home_Map.svg`  
  - MD: `docs/ui/md/Customer_App/CA_06_1_Home_Map.md`
- `CA_06_Home_Sites` 附近站点  
  - SVG: `docs/ui/svg/Customer_App/CA_06_Home_Sites.svg`  
  - MD: `docs/ui/md/Customer_App/CA_06_Home_Sites.md`
- `CA_25_1_Settings_Notifications` 通知设置  
  - SVG: `docs/ui/svg/Customer_App/CA_25_1_Settings_Notifications.svg`  
  - MD: `docs/ui/md/Customer_App/CA_25_1_Settings_Notifications.md`
- `CA_25_2_Settings_AccountSecurity` 账号与安全  
  - SVG: `docs/ui/svg/Customer_App/CA_25_2_Settings_AccountSecurity.svg`  
  - MD: `docs/ui/md/Customer_App/CA_25_2_Settings_AccountSecurity.md`
- `CA_25_Profile_Settings` 我的  
  - SVG: `docs/ui/svg/Customer_App/CA_25_Profile_Settings.svg`  
  - MD: `docs/ui/md/Customer_App/CA_25_Profile_Settings.md`

### Technician_App
- `TA_01_1_Face_Verify` 人脸验证  
  - SVG: `docs/ui/svg/Technician_App/TA_01_1_Face_Verify.svg`  
  - MD: `docs/ui/md/Technician_App/TA_01_1_Face_Verify.md`
- `TA_01_5_Authorization_Bind` 人脸验证  
  - SVG: `docs/ui/svg/Technician_App/TA_01_5_Authorization_Bind.svg`  
  - MD: `docs/ui/md/Technician_App/TA_01_5_Authorization_Bind.md`
- `TA_01_Login` TA_01_Login  
  - SVG: `docs/ui/svg/Technician_App/TA_01_Login.svg`  
  - MD: `docs/ui/md/Technician_App/TA_01_Login.md`
- `TA_16_Profile` TECH-03 张三  
  - SVG: `docs/ui/svg/Technician_App/TA_16_Profile.svg`  
  - MD: `docs/ui/md/Technician_App/TA_16_Profile.md`
- `TA_17_1_Notifications_Prefs` 通知设置  
  - SVG: `docs/ui/svg/Technician_App/TA_17_1_Notifications_Prefs.svg`  
  - MD: `docs/ui/md/Technician_App/TA_17_1_Notifications_Prefs.md`
- `TA_17_2_Help_FAQ` 帮助与FAQ  
  - SVG: `docs/ui/svg/Technician_App/TA_17_2_Help_FAQ.svg`  
  - MD: `docs/ui/md/Technician_App/TA_17_2_Help_FAQ.md`
- `TA_17_Settings` 设置  
  - SVG: `docs/ui/svg/Technician_App/TA_17_Settings.svg`  
  - MD: `docs/ui/md/Technician_App/TA_17_Settings.md`

### Cabinet_Screen
- `CK_00_Select_Mode` CK_00_Select_Mode  
  - SVG: `docs/ui/svg/Cabinet_Screen/CK_00_Select_Mode.svg`  
  - MD: `docs/ui/md/Cabinet_Screen/CK_00_Select_Mode.md`
- `CK_01_Welcome` CK_01_Welcome  
  - SVG: `docs/ui/svg/Cabinet_Screen/CK_01_Welcome.svg`  
  - MD: `docs/ui/md/Cabinet_Screen/CK_01_Welcome.md`
- `CK_02_Scan_QR` CK_02_Scan_QR  
  - SVG: `docs/ui/svg/Cabinet_Screen/CK_02_Scan_QR.svg`  
  - MD: `docs/ui/md/Cabinet_Screen/CK_02_Scan_QR.md`
- `CK_11_Maintenance` CK_11_Maintenance  
  - SVG: `docs/ui/svg/Cabinet_Screen/CK_11_Maintenance.svg`  
  - MD: `docs/ui/md/Cabinet_Screen/CK_11_Maintenance.md`
- `CK_14_Staff_Mode_Login` 工号/口令  
  - SVG: `docs/ui/svg/Cabinet_Screen/CK_14_Staff_Mode_Login.svg`  
  - MD: `docs/ui/md/Cabinet_Screen/CK_14_Staff_Mode_Login.md`


### 2.4 本迭代相关 API（从页面 MD 自动提取 + 需要你补齐）
- `POST /app/auth/logout`
- `POST /app/auth/sms/login`
- `POST /app/auth/sms/send`
- `GET /app/sites/nearby`
- `GET /app/sites/{siteId}`
- `GET /app/sites/{siteId}/queue`
- `GET /asset/device-investors`
- `GET /asset/device-investors/{investorId}`
- `GET /asset/device-investors/{investorId}/cabinets`
- `GET /assets/cabinets`
- `GET /assets/cabinets/{cabinetId}`
- `GET /assets/cabinets/{cabinetId}/slots`
- `POST /auth/login`
- `POST /auth/logout`
- `POST /auth/mfa/verify`
- `POST /iot/cabinets/{cabinetId}/commands/open-slot`
- `GET /iot/cabinets/{cabinetId}/events`

## 3. 输出物（必须提交到 PR）
1) **后端**：对应领域模型/应用服务/接口层/基础设施层代码 + 单元测试/集成测试
2) **前端**：对应页面与路由/菜单/状态管理 + Mock 数据（若后端尚未全部完成）
3) **契约与追踪**：
   - 更新 `docs/contracts/openapi.yaml`（补齐本迭代涉及接口与 schema）
   - 如涉及事件/MQTT：更新 `docs/contracts/asyncapi.yaml`
   - 更新 `docs/traceability/page_to_api.md`（本迭代页面 → API/DTO → permission_key）
4) **页面清单状态**：更新 `docs/ui/page_inventory.csv`：本迭代页面 `impl_status=implemented`（至少达到可验收的高保真）；其余保持原样
5) **人工验收指南**：新增 `docs/qa/manual/{ITER}.md`（包含：环境启动、账号、操作步骤、预期结果、失败排查）

## 4. 三道硬门禁（Definition of Done）
### Gate A — Page Coverage
- 本迭代页面必须：
  - 在对应前端子系统可访问（路由可直达 + 左侧菜单/入口可达）
  - 入口与父子层级符合 `page_inventory.csv`（父页面/入口字段）
  - 页面骨架与交互符合对应页面 MD

### Gate B — Traceability
- `docs/traceability/page_to_api.md` 中，本迭代页面必须补齐：
  - `api_tag`（对应 openapi tag）
  - `permission_key`（RBAC 权限点；如需 ABAC 也要写明）
  - `api_endpoints`（方法+路径）+ 对应 DTO（可先占位，但必须可追踪）
- `docs/contracts/openapi.yaml` 必须包含本迭代接口（至少：路径、方法、参数、请求体/响应体 schema 占位、错误码）。

### Gate C — Visual Regression
- 对本迭代 `impl_status=implemented` 的页面：
  - Playwright 截图用例必须覆盖（mock 模式或真实后端均可）
  - 与 SVG 基线对比：结构与布局一致（允许极少像素差异；若差异大必须解释并更新基线）

## 5. 实施步骤（Codex 必须按顺序执行）
1) **先产出方案**：在 PR 描述或 `docs/qa/plan/{ITER}.md` 输出：页面清单、API/DTO 清单、权限点清单、事件清单、数据库表变更、测试清单。
2) **实现后端**：严格按 DDD 分层（domain/application/interfaces/infrastructure），确保 tenant 隔离、幂等、审计。
3) **实现前端**：逐页对照 SVG/MD；按钮/链接必须跳转到正确下级页面（或弹窗）；数据先用 mock，再逐步换成真实 API。
4) **补齐契约**：openapi/asyncapi + traceability + page_inventory 状态。
5) **自动化验证**：尽可能在本地自动跑完（见下一节命令）。
6) **生成手动验收指南**：角色/权限清晰，含预期。

## 6. Codex 必须执行的自动化命令（失败必须修复）
> 若仓库尚未提供这些 make/npm/mvn 命令，请在本迭代内补齐（ITER-00 必须建立）。

建议命令（按实际仓库调整）：
- `make verify-docs`（markdown/yaml 校验）
- `make verify-openapi` / `make verify-asyncapi`
- `make verify-page-coverage ITER={ITER}`
- `make verify-traceability ITER={ITER}`
- `make verify-visual ITER={ITER}`
- `make backend-test`（含 tenant/permission/idempotency/audit）
- `make frontend-build APPS=...`（仅构建本迭代涉及的前端子系统）


### 建议本地种子账号（用于手动验收）
> 具体以 `backend/.../db/seed` 为准；但本迭代如涉及登录/权限，请确保至少包含以下账号（并在登录表单中**默认预填**）。

- 平台管理员（PLATFORM_ADMIN）：`pa_admin` / `Passw0rd!`
- 平台运维（PLATFORM_OPS）：`ops_admin` / `Passw0rd!`
- 平台财务（PLATFORM_FINANCE）：`fin_admin` / `Passw0rd!`
- 平台 IoT 运维（PLATFORM_IOTOPS）：`iot_admin` / `Passw0rd!`
- 平台客服（PLATFORM_CS）：`cs_admin` / `Passw0rd!`
- 区域代理管理员（AGENT_ADMIN）：`agent_admin` / `Passw0rd!`
- 门店管理员（STORE_ADMIN）：`store_admin` / `Passw0rd!`
- 设备投资方管理员（INVESTOR_ADMIN）：`investor_admin` / `Passw0rd!`
- 物业管理员（PROPERTY_ADMIN）：`property_admin` / `Passw0rd!`
- 技师（TECHNICIAN）：`tech01` / `Passw0rd!`
- 代泊（VALET）：`valet01` / `Passw0rd!`
- 车主/用户（CUSTOMER）：手机号 `13800000000`（验证码 mock：`123456`）

## 7. 手动验收（你要写进 docs/qa/manual/{ITER}.md）
请至少包含：
- 如何启动依赖（Postgres/Redis/MQ/MQTT/S3）与后端、前端
- 使用哪些角色账号登录（及权限差异）
- 本迭代每个页面从入口到二/三级页面的点击路径
- 关键业务链路：成功路径 + 失败路径（无权限/幂等重复/状态不允许）
- 预期 UI 与 SVG 差异说明（如有）

## 8. 额外约束（必须遵守）
- 不允许把 SVG 当图片嵌进去当 UI；SVG 只是原型参考。
- 不允许在日志中输出 PII。
- 高风险动作必须写审计与幂等。
- 所有接口必须 tenant 隔离。
