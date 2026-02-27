# Beizhi SmartWash Monorepo Starter (v5.4)

本仓库为《Beizhi_SmartWash_Platform_Requirements_v5_4》与《Beizhi_SmartWash_Platform_DDD_Detailed_Design_v5_4》对应的 **SaaS 多租户（Java 21 + Spring Boot 3.x + DDD）** Monorepo 初始骨架。

目标：以 **“文档/契约/高保真页面”为唯一事实源**，支持 Codex/ChatGPT 端到端迭代开发（每次迭代一个可运行模块），并通过门禁确保实现不偏离 SVG 原型与需求。

## Single Source of Truth（实现必须引用）
- `docs/requirements/Beizhi_v5_4.md`（需求 Markdown，可 diff）
- `docs/design/Beizhi_DDD_Detailed_Design_v5_4.md`（详细设计 Markdown，可 diff）
- `docs/ui/page_inventory.csv`（页面清单：page_key/中文名/角色/模块/父页面/入口/subsystem/svg_path/md_path/impl_status）
- `docs/ui/svg/**`（SVG 原型）
- `docs/ui/md/**`（每页说明 MD）
- `docs/traceability/page_to_api.md`（页面 → API/DTO → 权限点追踪矩阵）
- `docs/contracts/openapi.yaml`（REST 契约）
- `docs/contracts/asyncapi.yaml`（事件/MQTT 契约）
- `AGENTS.md`（工程红线）
- `.codex/config.toml`（Codex 沙箱/审批策略）

## 目录概览
- `backend/`：Java 后端（Spring Boot 多模块骨架）
- `frontend/`：前端子系统（按角色/门户拆分）
- `infra/`：本地依赖（Postgres/Redis/MQ/MQTT/MinIO）
- `docs/`：需求/设计/契约/UI 原型/追踪矩阵

## 快速开始（示例）
> 具体命令以 Makefile/CONTRIBUTING 为准。

1. 启动依赖：`docker compose -f infra/docker-compose.yml up -d`
2. 启动后端：`cd backend && mvn -q -pl apps/api-server spring-boot:run`
3. 启动任一前端：`cd frontend/store-admin && npm i && npm run dev`

## 注意
- UI 实现必须对齐 `docs/ui/svg` + `docs/ui/md`，任何偏离必须在 PR 中说明原因。
- 合并前必须通过：契约校验、页面覆盖门禁、追踪矩阵门禁（后续可加视觉回归门禁）。
