# Stage 2 — IMPLEMENT (anti-disconnect, incremental)

**HARD RULES (must follow):**
- This stage must be incremental and resilient to disconnects.
- Work in **time-boxed chunks**: ≤ 8 minutes OR ≤ 5 files changed per run.
- After each chunk: run required automated checks, then **git commit** with message `ITER-10: <short>`.
- Write all explanations/notes to `docs/qa/manual/ITER-10.md` (append/update). **Do not** stream long explanations in chat/terminal.
- Terminal/chat output must be minimal: changed files list + commands run + pass/fail summary + next TODO.

**You MUST read and follow the plan from:** `docs/qa/manual/ITER-10.plan.md`

**DoD (must pass before marking chunk done):**
- Gate A: `make verify-page-coverage ITER=ITER-10`
- Gate B: `make verify-traceability ITER=ITER-10`
- Gate C: `make verify-visual ITER=ITER-10` (run minimal set if configured)
- Plus module tests/builds as specified in the plan

If any command fails, fix and re-run until green. If network is required, clearly state what you need me to do (pre-warm deps, etc.) and stop.

Now implement **only the next smallest chunk** from the plan, commit it, update `docs/qa/manual/ITER-10.md` with:
- What changed (files)
- What was verified (commands + result)
- How to manually test (role-based steps)
- Remaining TODO for next chunk

Then stop.

---

## Context (from original ITER prompt)

# ITER-10 订单监控与时间线：平台/运维/门店/车主端统一订单视图

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
- order query & monitor
- timeline
- SSE/WebSocket push

### 2.2 涉及的前端子系统（Sub-Apps）
- Customer_App
- Platform_Admin_Portal
- Platform_Ops_Portal
- Store_Admin_Portal

### 2.3 页面范围（必须完成 UI/交互/链接，按 MD）
### Platform_Admin_Portal
- `PA_10_1_Orders_Advanced_Filter_Drawer` 订单监控  
  - SVG: `docs/ui/svg/Platform_Admin_Portal/PA_10_1_Orders_Advanced_Filter_Drawer.svg`  
  - MD: `docs/ui/md/Platform_Admin_Portal/PA_10_1_Orders_Advanced_Filter_Drawer.md`
- `PA_10_Global_Orders_Monitor` 全局订单监控  
  - SVG: `docs/ui/svg/Platform_Admin_Portal/PA_10_Global_Orders_Monitor.svg`  
  - MD: `docs/ui/md/Platform_Admin_Portal/PA_10_Global_Orders_Monitor.md`
- `PA_11_1_Order_Evidence` 订单详情  
  - SVG: `docs/ui/svg/Platform_Admin_Portal/PA_11_1_Order_Evidence.svg`  
  - MD: `docs/ui/md/Platform_Admin_Portal/PA_11_1_Order_Evidence.md`
- `PA_11_2_Order_Settlement_Payment` 订单详情  
  - SVG: `docs/ui/svg/Platform_Admin_Portal/PA_11_2_Order_Settlement_Payment.svg`  
  - MD: `docs/ui/md/Platform_Admin_Portal/PA_11_2_Order_Settlement_Payment.md`
- `PA_11_3_Order_Audit_Log` 订单详情  
  - SVG: `docs/ui/svg/Platform_Admin_Portal/PA_11_3_Order_Audit_Log.svg`  
  - MD: `docs/ui/md/Platform_Admin_Portal/PA_11_3_Order_Audit_Log.md`
- `PA_11_4_Order_Assign_Modal` 订单详情  
  - SVG: `docs/ui/svg/Platform_Admin_Portal/PA_11_4_Order_Assign_Modal.svg`  
  - MD: `docs/ui/md/Platform_Admin_Portal/PA_11_4_Order_Assign_Modal.md`
- `PA_11_5_Order_ForceClose_Modal` 订单详情  
  - SVG: `docs/ui/svg/Platform_Admin_Portal/PA_11_5_Order_ForceClose_Modal.svg`  
  - MD: `docs/ui/md/Platform_Admin_Portal/PA_11_5_Order_ForceClose_Modal.md`
- `PA_11_6_Order_Close_Modal` 订单详情  
  - SVG: `docs/ui/svg/Platform_Admin_Portal/PA_11_6_Order_Close_Modal.svg`  
  - MD: `docs/ui/md/Platform_Admin_Portal/PA_11_6_Order_Close_Modal.md`
- `PA_11_Order_Detail` 订单详情  
  - SVG: `docs/ui/svg/Platform_Admin_Portal/PA_11_Order_Detail.svg`  
  - MD: `docs/ui/md/Platform_Admin_Portal/PA_11_Order_Detail.md`

### Platform_Ops_Portal
- `PO_03_Orders_Monitor` 订单监控  
  - SVG: `docs/ui/svg/Platform_Ops_Portal/PO_03_Orders_Monitor.svg`  
  - MD: `docs/ui/md/Platform_Ops_Portal/PO_03_Orders_Monitor.md`
- `PO_04_1_Order_Evidence` 订单详情  
  - SVG: `docs/ui/svg/Platform_Ops_Portal/PO_04_1_Order_Evidence.svg`  
  - MD: `docs/ui/md/Platform_Ops_Portal/PO_04_1_Order_Evidence.md`
- `PO_04_2_Order_Audit_Log` 订单详情  
  - SVG: `docs/ui/svg/Platform_Ops_Portal/PO_04_2_Order_Audit_Log.svg`  
  - MD: `docs/ui/md/Platform_Ops_Portal/PO_04_2_Order_Audit_Log.md`
- `PO_04_3_Order_ForceClose_Modal` 订单详情  
  - SVG: `docs/ui/svg/Platform_Ops_Portal/PO_04_3_Order_ForceClose_Modal.svg`  
  - MD: `docs/ui/md/Platform_Ops_Portal/PO_04_3_Order_ForceClose_Modal.md`
- `PO_04_Order_Detail` 订单详情  
  - SVG: `docs/ui/svg/Platform_Ops_Portal/PO_04_Order_Detail.svg`  
  - MD: `docs/ui/md/Platform_Ops_Portal/PO_04_Order_Detail.md`

### Store_Admin_Portal
- `SA_04_1_Order_Evidence` 订单详情  
  - SVG: `docs/ui/svg/Store_Admin_Portal/SA_04_1_Order_Evidence.svg`  
  - MD: `docs/ui/md/Store_Admin_Portal/SA_04_1_Order_Evidence.md`
- `SA_04_2_Order_Settlement` 订单详情  
  - SVG: `docs/ui/svg/Store_Admin_Portal/SA_04_2_Order_Settlement.svg`  
  - MD: `docs/ui/md/Store_Admin_Portal/SA_04_2_Order_Settlement.md`
- `SA_04_3_Order_Audit` 订单详情  
  - SVG: `docs/ui/svg/Store_Admin_Portal/SA_04_3_Order_Audit.svg`  
  - MD: `docs/ui/md/Store_Admin_Portal/SA_04_3_Order_Audit.md`
- `SA_04_Order_Detail` 订单详情（派单/状态/证据）  
  - SVG: `docs/ui/svg/Store_Admin_Portal/SA_04_Order_Detail.svg`  
  - MD: `docs/ui/md/Store_Admin_Portal/SA_04_Order_Detail.md`

### Customer_App
- `CA_12_1_Queue_Detail` 排队详情  
  - SVG: `docs/ui/svg/Customer_App/CA_12_1_Queue_Detail.svg`  
  - MD: `docs/ui/md/Customer_App/CA_12_1_Queue_Detail.md`
- `CA_13_Order_List` 我的订单  
  - SVG: `docs/ui/svg/Customer_App/CA_13_Order_List.svg`  
  - MD: `docs/ui/md/Customer_App/CA_13_Order_List.md`
- `CA_14_1_Order_Cancel_Confirm` 取消订单  
  - SVG: `docs/ui/svg/Customer_App/CA_14_1_Order_Cancel_Confirm.svg`  
  - MD: `docs/ui/md/Customer_App/CA_14_1_Order_Cancel_Confirm.md`
- `CA_14_2_Order_Cancel_Result` 订单已取消  
  - SVG: `docs/ui/svg/Customer_App/CA_14_2_Order_Cancel_Result.svg`  
  - MD: `docs/ui/md/Customer_App/CA_14_2_Order_Cancel_Result.md`
- `CA_14_3_Onsite_Timeline` 订单详情  
  - SVG: `docs/ui/svg/Customer_App/CA_14_3_Onsite_Timeline.svg`  
  - MD: `docs/ui/md/Customer_App/CA_14_3_Onsite_Timeline.md`
- `CA_14_Order_Detail_Timeline` 订单详情  
  - SVG: `docs/ui/svg/Customer_App/CA_14_Order_Detail_Timeline.svg`  
  - MD: `docs/ui/md/Customer_App/CA_14_Order_Detail_Timeline.md`


### 2.4 本迭代相关 API（从页面 MD 自动提取 + 需要你补齐）
- `GET /app/orders`
- `POST /app/orders`
- `GET /app/orders/{orderId}`
- `GET /app/orders/{orderId}/timeline`
- `POST /audit/exports`
- `GET /audit/logs`
- `GET /audit/logs/{logId}`
- `POST /evidence/records`
- `POST /evidence/upload-url`
- `GET /finance/settlements`
- `GET /finance/settlements/{settlementId}`
- `POST /finance/settlements/{settlementId}/export`
- `GET /finance/settlements/{settlementId}/lines`
- `GET /orders`
- `POST /orders`
- `GET /orders/{orderId}`
- `POST /orders/{orderId}/cancel`
- `POST /orders/{orderId}/dispatch`
- `GET /orders/{orderId}/evidence`
- `GET /orders/{orderId}/timeline`

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
