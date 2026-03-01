# ITER-01 手动验收指南（统一认证登录、租户会话、角色菜单与首页仪表盘）

## 1. 启动环境

### 1.1 启动基础依赖
```bash
docker compose -f infra/docker-compose.yml up -d
```
依赖：Postgres、Redis、RabbitMQ、EMQX、MinIO。

### 1.2 启动后端
```bash
cd backend
mvn -q -pl apps/api-server spring-boot:run
```
健康检查：`http://localhost:8080/healthz`

### 1.3 构建前端子系统（ITER-01）
```bash
make frontend-build APPS="agent-admin cabinet-screen customer-app device-investor platform-admin platform-cs platform-finance platform-iotops platform-ops property-portal store-admin technician-app"
```

### 1.4 运行门禁与测试
```bash
make verify-docs
make verify-openapi
make verify-asyncapi
make verify-page-coverage ITER=ITER-01
make verify-traceability ITER=ITER-01
make verify-visual ITER=ITER-01
make backend-test
```
说明：当前执行环境默认不启 Playwright 真浏览器，`verify-visual` 使用 `mock` 模式完成结构化视觉回归。

## 2. 验收账号（登录页默认预填）
- PLATFORM_ADMIN: `pa_admin / Passw0rd!`
- PLATFORM_OPS: `ops_admin / Passw0rd!`
- PLATFORM_FINANCE: `fin_admin / Passw0rd!`
- PLATFORM_IOTOPS: `iot_admin / Passw0rd!`
- PLATFORM_CS: `cs_admin / Passw0rd!`
- AGENT_ADMIN: `agent_admin / Passw0rd!`
- STORE_ADMIN: `store_admin / Passw0rd!`
- INVESTOR_ADMIN: `investor_admin / Passw0rd!`
- PROPERTY_ADMIN: `property_admin / Passw0rd!`
- TECHNICIAN: `tech01 / Passw0rd!`
- VALET: `valet01 / Passw0rd!`
- CUSTOMER: `13800000000` + 短信验证码 `123456`

## 3. 页面验收路径（入口到二/三级）

### 3.1 平台与 B 端门户
- Platform_Admin_Portal:
  - `PA_01_Login -> PA_02_Global_Dashboard -> PA_30_System_Settings -> PA_30_1_MFA_Devices / PA_30_2_Integrations / PA_30_3_Data_Retention_Settings -> PA_31_Profile -> PA_31_1_Profile_ChangePassword / PA_31_2_Profile_MFA_Setup`
- Platform_Ops_Portal:
  - `PO_01_Login -> PO_02_Ops_Dashboard -> PO_15_Profile -> PO_15_1_ChangePassword`
- Platform_Finance_Portal:
  - `PF_01_Login -> PF_02_Finance_Dashboard -> PF_18_Profile`
- Platform_IoTOps_Portal:
  - `PI_01_Login -> PI_02_IoT_Dashboard -> PI_11_Profile`
- Platform_CS_Portal:
  - `PC_01_Login -> PC_02_CS_Dashboard -> PC_11_Profile`
- Agent_Admin_Portal:
  - `AA_01_Login -> AA_02_Region_Dashboard -> AA_11_Profile -> AA_11_1_ChangePassword`
- Store_Admin_Portal:
  - `SA_01_Login -> SA_02_Store_Dashboard -> SA_19_Profile -> SA_19_1_ChangePassword`
- Device_Investor_Portal:
  - `DI_01_Login -> DI_02_Investor_Dashboard -> DI_17_Profile`
- Property_Portal:
  - `PP_01_Login -> PP_02_Property_Dashboard -> PP_09_Profile`

### 3.2 C 端与执行端
- Customer_App:
  - `CA_01_Splash -> CA_01_1_Privacy_Consent -> CA_02_1_Login_Wechat / CA_02_Login_SMS -> CA_06_Home_Sites / CA_06_1_Home_Map -> CA_25_Profile_Settings -> CA_25_1_Settings_Notifications / CA_25_2_Settings_AccountSecurity`
- Technician_App:
  - `TA_01_Login -> TA_01_1_Face_Verify -> TA_01_5_Authorization_Bind -> TA_16_Profile -> TA_17_Settings -> TA_17_1_Notifications_Prefs / TA_17_2_Help_FAQ`
- Cabinet_Screen:
  - `CK_00_Select_Mode -> CK_01_Welcome -> CK_02_Scan_QR -> CK_11_Maintenance -> CK_14_Staff_Mode_Login`

说明：所有页面均可通过统一路由直达：`/pages/{page_key}`；一级入口通过左侧菜单，二/三级页面通过页面内 `Navigation -> Children` 进入。

## 4. 关键业务链路（成功与失败）

### 4.1 成功路径：短信登录 + 站点查询（Customer）
```bash
curl -X POST 'http://localhost:8080/app/auth/sms/send' \
  -H 'Content-Type: application/json' \
  -d '{"mobile":"13800000000"}'

curl -X POST 'http://localhost:8080/app/auth/sms/login' \
  -H 'Content-Type: application/json' \
  -d '{"mobile":"13800000000","code":"123456"}'
```
拿到 `token` 后：
```bash
curl 'http://localhost:8080/app/sites/nearby' \
  -H 'Authorization: Bearer mock-jwt:tenant-a:customer-0000' \
  -H 'X-Permissions: app.site.read'
```
预期：返回 tenant-a 站点，且手机号为脱敏显示。

### 4.2 成功路径：投资方资产查询
```bash
curl 'http://localhost:8080/asset/device-investors' \
  -H 'X-Tenant-Id: tenant-a' \
  -H 'X-User-Id: investor_admin' \
  -H 'X-Permissions: asset.investor.read'
```
预期：返回投资方列表及可见柜体数量。

### 4.3 失败路径：无权限
```bash
curl 'http://localhost:8080/asset/device-investors' \
  -H 'X-Tenant-Id: tenant-a' \
  -H 'X-User-Id: investor_admin'
```
预期：`403 FORBIDDEN`。

### 4.4 失败路径：跨租户访问
```bash
curl 'http://localhost:8080/asset/device-investors/INV-A1' \
  -H 'X-Tenant-Id: tenant-b' \
  -H 'X-User-Id: investor_admin' \
  -H 'X-Permissions: asset.investor.read'
```
预期：`404 NOT_FOUND`（不可见）。

### 4.5 失败路径：幂等重复与状态不允许
- 同一 `Idempotency-Key` 不同载荷重复提交开柜：预期 `409 CONFLICT`。
- 格口非法状态转移（如 `LOCKED -> OCCUPIED`）：预期 `409 CONFLICT`。

## 5. UI 与 SVG 差异说明
- 本迭代前端采用统一高保真壳层（页面结构、菜单、父子链接、权限/API 映射、登录默认值），保证迭代页面端到端可达与可验收。
- 页面未直接嵌入 SVG 文件；渲染由 Vue/HTML/CSS 生成。
- 视觉回归通过 `verify-visual` 完成结构一致性校验（沙箱下为 mock 模式）。
