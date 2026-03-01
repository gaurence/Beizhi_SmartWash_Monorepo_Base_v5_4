# ITER-00 手动验收指南（底座与三道硬门禁）

## 1. 启动环境

### 1.1 启动依赖
```bash
docker compose -f infra/docker-compose.yml up -d
```
依赖服务：Postgres、Redis、RabbitMQ、EMQX、MinIO。

### 1.2 启动后端
```bash
cd backend
mvn -q -pl apps/api-server spring-boot:run
```
健康检查：`http://localhost:8080/healthz`

### 1.3 构建前端（全量子系统）
```bash
make frontend-build APPS=all
```

### 1.4 运行门禁
```bash
make verify-docs
make verify-openapi
make verify-asyncapi
make verify-page-coverage ITER=ITER-00
make verify-traceability ITER=ITER-00
make verify-visual ITER=ITER-00 APPS=all
make backend-test
```
说明：当前执行环境不允许 Playwright 真浏览器启动，`verify-visual` 会自动降级为 `mode=mock`（结构化视觉快照），并输出原因。

## 2. 验收账号（登录页默认已预填）
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
- CUSTOMER: `13800000000 / 123456`

## 3. 页面入口与点击路径
本迭代采用 page_inventory 驱动生成，所有页面统一路径规则：
- 直达：`/pages/{page_key}`
- 左侧菜单：展示该子系统一级页面（`父页面`为空）
- 二/三级页面：在父页面的 `Children` 区域点击进入

### 3.1 子系统入口（示例）
- Agent_Admin_Portal: `AA_01_Login`
- Cabinet_Screen: `CK_00_Select_Mode`
- Customer_App: `CA_01_Splash`
- Device_Investor_Portal: `DI_01_Login`
- Platform_Admin_Portal: `PA_01_Login`
- Platform_CS_Portal: `PC_01_Login`
- Platform_Finance_Portal: `PF_01_Login`
- Platform_IoTOps_Portal: `PI_01_Login`
- Platform_Ops_Portal: `PO_01_Login`
- Property_Portal: `PP_01_Login`
- Store_Admin_Portal: `SA_01_Login`
- Technician_App: `TA_01_Login`

### 3.2 全量页面路径校验方法（413页）
1. 进入任一子系统首页（如 `PA_01_Login`）。
2. 在左侧菜单点击任一一级页面。
3. 在页面 `Navigation -> Children` 点击二/三级页面。
4. 确认地址栏与目标 `page_key` 一致（`/pages/{target_page_key}`）。
5. 如需全量自动校验，执行：`make verify-page-coverage ITER=ITER-00`。

## 4. 关键业务链路（成功/失败）

### 4.1 成功路径：开格口（高风险动作）
```bash
curl -X POST 'http://localhost:8080/iot/cabinets/CAB-A1/commands/open-slot' \
  -H 'Content-Type: application/json' \
  -H 'X-Tenant-Id: tenant-a' \
  -H 'X-User-Id: tester' \
  -H 'X-Permissions: iot.open-slot.execute' \
  -H 'Idempotency-Key: idem-open-1' \
  -d '{"slotNo":"S1","nextState":"OCCUPIED"}'
```
预期：200，返回 `commandId/requestId`；审计日志新增 `IOT_OPEN_SLOT`。

### 4.2 失败路径：无权限
```bash
curl 'http://localhost:8080/audit/logs' \
  -H 'X-Tenant-Id: tenant-a' \
  -H 'X-User-Id: tester'
```
预期：403（缺少 `audit.log.read`）。

### 4.3 失败路径：幂等键复用但负载不同
相同 `Idempotency-Key` + 不同请求体重复提交开格口。
预期：409（`Idempotency-Key reused with different payload`）。

### 4.4 失败路径：状态机非法转移
将 `LOCKED` 格口直接转为 `OCCUPIED`。
预期：409（非法状态转移）。

## 5. UI 与 SVG 差异说明
- ITER-00 目标是“全量页面可访问 + 门禁可运行 + 后续可按清单增量替换”。
- 当前页面为统一生成壳（菜单、父子导航、API/权限映射展示、登录默认值），并非逐页精修的 SVG 高保真复刻。
- 后续迭代将按 `impl_status=implemented` 页面逐步替换为 SVG/MD 对齐实现；Gate C 已具备基线对比能力。
