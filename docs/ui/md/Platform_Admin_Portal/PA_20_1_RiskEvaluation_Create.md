# 风控评估

- **Page Key**: `PA_20_1_RiskEvaluation_Create`
- **Subsystem**: `Platform_Admin_Portal`
- **Role View**: "font">平台管理员 `PLATFORM_ADMIN`
- **Page Type**: `Form`

## 1. 视觉风格（Style）
- 视觉基调：企业级 SaaS（深色侧栏 + 浅色内容区），信息以卡片/表格承载。
- 组件形态：矩形卡片（轻阴影/描边）、表格（灰色表头）、抽屉/弹窗（如页面包含）。
- 主色/强调色：`#FF7A00`（按钮、选中态、关键标记）；中性色：`#111827/#6B7280/#E5E7EB/#F3F4F6`。
- 字体：`PingFang SC` 优先，回退到系统 sans-serif。

## 2. 布局结构（Layout）
- 画布尺寸：1440×900
- 整体结构：左侧导航（约 260px）+ 顶部栏（约 64px）+ 主内容区（卡片/表格/表单）
- 字体：PingFang SC（中文）为主，辅以系统 sans-serif
- 色板：深色侧栏 #0B1220；主背景 #F6F7FB；主CTA #FF7A00；文本 #111827/#6B7280

## 3. 主要组件（Components）
- 顶部区：搜索框/快捷入口/用户信息（Web）；或返回/标题/更多（App）。
- 主内容区：指标卡片、筛选器、表格列表、详情卡片、Tab 区（按页面类型变化）。
- 交互控件：主按钮（CTA）、次按钮、状态 Badge、分页（列表页）。

## 4. 功能与交互（Functions & Interactions）
- 填写表单字段
- 校验必填/格式
- 提交保存
- 查看提交结果/进入详情

### 4.1 页面内链接/下一级页面（Navigation）
- 常见跳转：
  - 列表 → 详情：点击表格行/“查看详情”
  - 详情 → 编辑：点击“编辑/配置”
  - 表单 → 详情/列表：点击“保存/提交”后返回
- 具体跳转关系建议以 `docs/ui/page_inventory.csv` 与 `docs/traceability/page_to_api.md` 为准（保持可追溯）。

## 5. 相关接口（API Mapping）
> 说明：以下为该页面常用操作的接口建议；最终以仓库 `docs/contracts/openapi.yaml` 的 `operationId` 为准。

-（该页为静态/说明页或暂不对接接口）

## 6. 权限与审计（RBAC/ABAC & Audit）
- 访问控制：由 `role_code` + 页面权限点（permission_key）决定；高风险动作需二次校验（如应急开柜/绑定变更/退款审批）。
- 审计要求：对“开柜指令、绑定变更、分润规则/收款账户变更、导出明细、查看敏感明细”等动作写入 `audit_log`，并可追溯 request_id。
- 数据脱敏：对车主/车辆隐私字段（手机号、车牌、证件号等）按 v5.4 合规要求脱敏展示。

## 7. 对应需求点（Trace to Requirements）
- v5.4 第12章（按角色子系统需求）
