# ITER-00 Plan: 底座与三道硬门禁

## 1. 页面清单（本迭代范围）
- 页面来源：`docs/ui/page_inventory.csv`（排除 `_Common`）
- 覆盖口径：全量 `page_key` 必须在对应前端子应用可直达（`/pages/{page_key}`）
- 子系统映射：
  - `Agent_Admin_Portal` -> `frontend/agent-admin`
  - `Cabinet_Screen` -> `frontend/cabinet-screen`
  - `Customer_App` -> `frontend/customer-app`
  - `Device_Investor_Portal` -> `frontend/device-investor`
  - `Platform_Admin_Portal` -> `frontend/platform-admin`
  - `Platform_CS_Portal` -> `frontend/platform-cs`
  - `Platform_Finance_Portal` -> `frontend/platform-finance`
  - `Platform_IoTOps_Portal` -> `frontend/platform-iotops`
  - `Platform_Ops_Portal` -> `frontend/platform-ops`
  - `Property_Portal` -> `frontend/property-portal`
  - `Store_Admin_Portal` -> `frontend/store-admin`
  - `Technician_App` -> `frontend/technician-app`

## 2. API/DTO 清单（ITER-00 纵切）
- 重点 API（契约已在 `openapi.yaml`，本迭代做最小可运行实现）：
  - Identity: `POST /auth/login`, `POST /auth/mfa/verify`, `POST /auth/logout`
  - Cabinet/IoT: `GET /assets/cabinets`, `GET /assets/cabinets/{cabinetId}`, `GET /assets/cabinets/{cabinetId}/slots`, `POST /iot/cabinets/{cabinetId}/commands/open-slot`, `GET /iot/cabinets/{cabinetId}/events`
  - Asset Binding: `GET /asset/cabinets/{cabinetId}/store-bindings`, `POST /asset/cabinets/{cabinetId}/store-bindings`, `PATCH /asset/cabinet-store-bindings/{bindingId}`, `POST /asset/cabinet-store-bindings/{bindingId}/approve`
  - Audit: `GET /audit/logs`, `GET /audit/logs/{logId}`
- DTO（起步占位 + 可追踪）：
  - `ApiResponse`, `PageResponse`
  - `OpenSlotCommandRequest`, `OpenSlotCommandResponse`
  - `CabinetDto`, `SlotDto`, `CabinetStoreBindingDto`, `AuditLogDto`

## 3. 权限点清单（RBAC/ABAC）
- 页面权限：以 `docs/traceability/page_to_api.md` 的 `permission_key` 为主。
- 接口权限（后端最小集）：
  - `asset.cabinet.read`
  - `iot.open-slot.execute`
  - `asset.binding.manage`
  - `audit.log.read`
- ABAC（本迭代落最小模型）：开柜操作要求绑定 `tenant_id/user_id/cabinet_id/slot_id/action/ttl` 的 scoped token（模拟实现）。

## 4. 事件清单（AsyncAPI）
- `smartwash.domain.events`
- `smartwash.notification.commands`
- `smartwash.finance.events`
- `mqtt/cabinets/{cabinetId}/commands/open-slot`
- `mqtt/cabinets/{cabinetId}/acks/open-slot`
- `mqtt/cabinets/{cabinetId}/events`

## 5. 数据库与存储变更（ITER-00 起步）
- 本迭代优先实现工程门禁与最小可运行链路，后端先以内存仓储落地以下实体语义：
  - `cabinet`, `slot`（含 `tenant_id`）
  - `cabinet_store_binding`（含 `tenant_id`）
  - `idempotency_record`（含 `tenant_id`）
  - `audit_log`（append-only，含 `request_id/actor/tenant/resource/result/ip/ua`）
- 下一迭代可平滑迁移到 PostgreSQL 物理表。

## 6. 测试清单
- `tenant isolation`: 跨租户访问被拒绝（403/404）
- `permission`: allow + deny
- `idempotency`: 相同 `Idempotency-Key` 返回同一结果
- `slot state transition`: RESERVED/OCCUPIED/FAULT/LOCKED 合法转移与非法转移阻断
- `audit write`: 高风险动作写入 append-only 审计日志
- `gate tests`: page coverage / traceability / visual / openapi / asyncapi
