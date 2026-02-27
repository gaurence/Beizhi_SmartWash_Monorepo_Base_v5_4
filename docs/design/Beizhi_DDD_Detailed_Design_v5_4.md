# 备至智洗平台（SaaS）详细设计与开发说明书（Java 21 + Spring Boot 3.x + DDD）

基于需求文档：Beizhi_SmartWash_Platform_Requirements_v5_4.docx

输出日期：2026-02-27

# 0. 文档控制

本文档为研发/测试/运维使用的“实现级”详细设计说明书，目标是让团队能够在不额外补充设计的情况下，直接开展后端（Java）与前端（多子系统）端到端开发、联调与上线。

所有设计点均标注其在 v5.4 需求中的来源章节，便于追溯与验收。

|版本|日期|基线需求版本|负责人|变更说明|
|---|---|---|---|---|
|v1.0|2026-02-27|v5.4|研发架构/产品联名|首次输出：按 v5.4 完整覆盖（DDD + 数据模型 + API/DTO + 事件 + 风控合规 + 运维）|

## 0.1 阅读指引

推荐阅读顺序：

- 第 2 章：总体架构与工程分层（理解整体蓝图与技术选型）
- 第 3 章：限界上下文与上下文映射（确定模块边界与依赖）
- 第 4~5 章：领域模型与数据模型（实现关键一致性与并发控制）
- 第 6~7 章：API/DTO 与事件（前后端联调、设备联调的契约）
- 第 8 章：IoT/MQTT 协议（柜体接入、开柜安全）
- 第 9 章：风控、审计与合规（上线前必须落实）
- 第 10~11 章：非功能与运维（容量、告警、应急预案）
# 1. 目标、范围与关键约束

目标：在 Java 栈（Java 21 + Spring Boot 3.x）下，以 DDD（领域驱动设计）方式构建可运维、可扩展的多租户 SaaS 平台与多端 App，实现智能钥匙柜代客洗车的端到端闭环。

范围：覆盖 v5.4 需求文档中的全部业务（含设备投资方、柜体-门店多对多绑定、模式A/B/C、多人协作、结算对账、风控审计合规等）。

关键约束（必须）：

- 多租户强隔离：除平台超管外，默认禁止跨租户访问；所有业务表必须具备 tenant_id（需求来源：v5.4 5.1、13.3、16.1）。
- 钥匙托管与开柜安全：按单授权（ABAC）+ TTL + 设备 ACK + 全量审计（需求来源：v5.4 7、10、16.2、15）。
- 订单/任务/格口状态机：所有状态转移必须幂等、可重放；禁止非法转移（需求来源：v5.4 10、13.4、15.1）。
- 证据链不可抵赖：证据只增不改（append-only），支持抽检与导出，满足纠纷与理赔（需求来源：v5.4 16.3、12.1.4、12.2.4）。
- 商业模式可配置：定价权主体、收款方主体、收益分配与设备投资分成可配置，且下单时固化快照（需求来源：v5.4 4.5、5.2~5.5、14）。
# 2. 总体架构与技术栈

本章给出推荐技术栈与逻辑/部署架构。技术栈与需求强相关点：强一致交易、弱网/离线、IoT 可靠指令链路、可观测性与审计合规。

## 2.1 推荐技术栈（后端）

|层面|推荐栈|设计原因|需求来源（v5.4）|
|---|---|---|---|
|运行时|Java 21 + Spring Boot 3.x|LTS、生态成熟、便于运维与容器化|需求 17/18 非功能与运维|
|安全|Spring Security + JWT/OAuth2|RBAC + ABAC（按单授权）|需求 16.1/16.2|
|数据库|PostgreSQL|强一致事务、复杂查询与审计|需求 13/14/16.6|
|缓存/队列|Redis|排队/限流/临时 token/幂等记录|需求 10.5/17.2|
|消息|Kafka 或 RabbitMQ（择一）+ Outbox|事件驱动、解耦通知与结算；Outbox 保证一致性|需求 15/14|
|对象存储|S3/OSS/MinIO|证据链图片/视频存储与签名上传|需求 16.3/12.2.4|
|实时|SSE/WebSocket|排队与订单状态推送|需求 15.3/12.1.4|
|IoT|MQTT（EMQX/Mosquitto）|开柜指令/ACK/心跳/事件|需求 12.6/14.3|

## 2.2 逻辑架构（建议）

建议采用“模块化单体（Modular Monolith）+ 明确限界上下文”的方式起步：

- 研发效率高：单仓库、单发布单元、接口调用为本地方法，避免早期分布式复杂度。
- 边界清晰：以 DDD 限界上下文划分模块，可逐步演进为微服务（拆分点来自上下文映射与依赖方向）。
- 运维成本低：便于统一监控、统一审计与统一配置（需求来源：v5.4 18 运营运维）。
逻辑组件（建议）：API Gateway（可选）/ BFF（多端） / 核心业务模块（订单、柜体、任务、结算…） / 设备接入网关（MQTT） / 通知服务 / 报表与审计。

## 2.3 部署架构（建议）

推荐容器化部署（Docker/Kubernetes）。最小可用环境：

- smartwash-api（Spring Boot）
- postgres（主库）+ 可选只读副本
- redis
- mq（kafka 或 rabbitmq）
- mqtt broker（emqx 或 mosquitto）
- object storage（minio）
- observability（Prometheus/Grafana/Loki/Tempo 或云厂商替代）
生产建议：多副本 + 负载均衡；数据库主从与定期备份；对象存储版本化与生命周期；MQTT broker 集群与 ACL。

## 2.4 工程分层与 DDD 实现规范（必须）

后端采用典型 DDD 分层：

- Interface Layer（接口层）：REST Controller / WebSocket/SSE Controller；仅做协议适配、鉴权、参数校验与错误映射。
- Application Layer（应用层）：UseCase/CommandHandler；编排聚合与领域服务、事务边界；发布领域事件（Outbox）。
- Domain Layer（领域层）：聚合根、实体、值对象、领域服务、领域事件；包含业务不变量与状态机。
- Infrastructure Layer（基础设施层）：Repository 实现、MQTT/MQ/对象存储/支付/短信等外部适配器。
命名约定（建议）：`…application.*UseCase`、`…domain.model.*`、`…domain.service.*`、`…infrastructure.persistence.*`。

## 2.5 多租户实现策略（必须）

推荐采用单库共享（shared database, shared schema）+ tenant_id 行级隔离：

- 所有业务表必须包含 tenant_id（UUID），并建立组合索引（tenant_id, id）或（tenant_id, natural_key）。
- TenantContext 从 JWT claim 提取；Repository 默认强制带 tenant_id 过滤。
- 平台超管跨租户查询：必须显式传入 tenant_id 或通过平台查询接口，且全量审计。
- 可选增强：PostgreSQL Row Level Security（RLS）作为第二道防线（需评估运维复杂度）。
需求来源：v5.4 5.1、13.3、16.1。

## 2.6 幂等与并发控制策略（必须）

对所有会改变状态的接口（创建/支付/开柜/存取钥匙/派单/结算等）强制幂等：

- 客户端必须携带 `Idempotency-Key`（推荐 UUID），服务端维护 `idempotency_record`（tenant_id, key, request_hash, response_body, status, expires_at）。
- 对设备指令与事件：使用 `request_id`（全局唯一）+ `event_id` 去重；Outbox 消费端同样幂等。
- 对聚合并发：使用 `row_version`（乐观锁）与数据库唯一约束（例如同一格口同一时刻只能有一个有效 token）。
需求来源：v5.4 10.5、15.1、13.4。

# 3. 限界上下文（Bounded Context）与上下文映射

本章用于确定模块边界与依赖方向。模块边界一旦确定，应尽量避免跨上下文直接访问对方数据表；跨上下文交互通过 API 或领域事件完成。

需求来源：v5.4 13.1、11.1。

|限界上下文|职责范围（摘要）|依赖/调用|需求来源（v5.4）|
|---|---|---|---|
|Identity & Access|用户、登录、MFA、RBAC/ABAC、会话与按单授权 AccessGrant|所有上下文|v5.4 9/16.1/14(Auth&RBAC)|
|Tenant & Org|租户/组织树、代理/门店/站点、物业集团与合同主体|Identity、Settlement|v5.4 5.1/12.5/12.4|
|Staff & Workforce|人员、洗车组、班次、外部协作人员授权（代泊可来自物业）|Tenant&Org, Order, Task|v5.4 4.1/4.2/8/12.3.3|
|Catalog & Pricing|服务目录、规格、加项、定价规则、报价与价格快照|Order, Settlement|v5.4 4.4/6/12.3.4|
|Cabinet & IoT|柜体/格口、设备身份、心跳、指令、事件、固件/OTA|Order, KeyCustody|v5.4 12.6/14.3/16.2|
|Asset & Investment|设备投资认领/转让、投放部署、柜体-门店绑定（多对多）、投资方分成|Cabinet&IoT, Settlement, Order|v5.4 5.4/5.5/9.5/12.7|
|Order & Fulfillment|订单、排队、履约模式 A/B/C、归因（origin_cabinet_id）、状态机|Pricing, Cabinet, Task, Payment|v5.4 7/10/12.1.3/5.5.3|
|Task & Execution|任务拆分、多人协作(2+)、代泊/洗车/质检任务、进度|Order, Staff|v5.4 4.1/8/12.2/12.3.2|
|Key Custody|钥匙托管状态、取/还钥匙、短期 token、开柜授权|Cabinet, Order, Identity|v5.4 10.3/16.2/7|
|Evidence|证据链（验车/完工/停车位）、对象存储、只增不改|Order, Task, Risk, CS|v5.4 16.3/12.2.4|
|Payment & Refund|支付意图、回调、退款申请/审批|Order, Settlement, Risk|v5.4 12.1.5/14(Payment/Refund)/16.6|
|Settlement & Reconcile|商业协议、分润规则、结算批次、对账、差异工单、物业代收|Order, Asset, Payment|v5.4 4.5/5.2~5.5/16.6|
|Notification|模板、站内信/推送/短信、通知触发点|All (via events)|v5.4 15.3/20.2|
|Risk & Audit|风控策略、命中记录、审计日志规范、合规脱敏|All|v5.4 16|
|Ticketing (CS)|客服工单、评论、附件与导出|Order, Evidence, Risk|v5.4 12.5/14(Tickets)|
|Reporting|运营报表、设备在线率、柜体引流绩效|All (read models)|v5.4 18/12.4/12.5/12.7|

## 3.1 上下文映射（Context Map）与依赖方向（建议）

依赖方向（建议从上到下）：

- Identity & Access → 所有上下文（提供鉴权、按单授权、审计 actor 信息）。
- Tenant & Org → Staff/Order/Cabinet/Settlement（提供组织树与归属关系）。
- Catalog & Pricing → Order（报价与价格快照）→ Settlement（分润结算基于快照）。
- Asset & Investment → Settlement（设备投资分成）→ Reporting（投资绩效）。
- Cabinet & IoT ↔ Key Custody ↔ Order（柜体状态与订单状态需要一致性，采用状态机+事件最终一致）。
- Order → Task（生成任务）→ Evidence（证据）→ Notification（状态通知）。
- Payment → Settlement/Reconcile（对账与结算）。
- Risk & Audit 横切所有上下文。
## 3.2 跨上下文集成原则（必须）

- 写模型与读模型分离：运营报表与监控建议采用读模型/物化视图，避免污染核心聚合。
- 跨上下文查询禁止直接 join 对方写库表；采用查询 API 或事件投影（Projection）。
- 跨上下文关键字段必须在下单/派单等时刻固化快照（AgreementSnapshot/BindingSnapshot/PriceSnapshot）。
- 外部系统对接（支付、短信、保险）采用适配器 + 反腐层（ACL），内部使用统一领域模型。
需求来源：v5.4 13.4、14、15。

# 4. 领域模型与数据模型（实现级）

本章给出可直接落库的关系模型设计（PostgreSQL），并按限界上下文组织。所有表默认带 tenant_id、审计字段与乐观锁字段。

需求来源：v5.4 13.2~13.5、16.5。

## 4.0 通用字段规范（所有业务表默认包含）

|字段|类型/说明|适用范围|备注|
|---|---|---|---|
|id (uuid)|主键，使用 UUIDv7 或 UUIDv4；建议业务层封装为 TypedId|所有表|便于分库分表与日志追踪|
|tenant_id (uuid)|多租户隔离键；平台级字典可 tenant_id = 0 或独立表|所有业务表|禁止跨租户查询（默认）|
|created_at (timestamptz)|创建时间|所有业务表||
|created_by (uuid)|操作者 user_id/staff_id（系统任务可为空或 SYSTEM）|所有业务表|与审计日志互补|
|updated_at (timestamptz)|更新时间|所有业务表||
|updated_by (uuid)|更新操作者|所有业务表||
|row_version (bigint)|乐观锁版本号；update 时 +1|强一致聚合表|并发写保护|
|is_deleted (boolean)|软删除标记（或 deleted_at）|非审计/证据表|审计表禁止软删|
|ext (jsonb)|扩展字段（可选），用于低频自定义属性|可扩展对象|避免频繁迁移|

### 4.0.1 命名与索引规范（必须）

- 表名：snake_case；索引名：idx_<table>_<cols>；唯一索引：uk_<table>_<cols>。
- 所有高频查询必须包含 tenant_id；索引建议以 (tenant_id, xxx) 作为前缀。
- 时间范围查询（例如绑定有效期、结算周期）使用 (tenant_id, start_at, end_at) 组合索引。
- JSONB 字段必须给出明确 schema（文档/DTO），避免随意塞字段。
### 4.0.2 PII 与敏感数据分级（必须）

为满足合规与最小可见原则，建议对字段进行分级：

|级别|说明|示例|
|---|---|---|
|P0|非敏感|站点名称、服务价格、设备在线状态|
|P1|业务敏感（可公开但需控制）|订单号、车位号（脱敏）、评分|
|P2|个人信息|手机号、车牌号、姓名、VIN、地址|
|P3|高度敏感|身份证号、驾照号、行驶证图片、人脸照片/特征值（如有）|

- P2/P3 字段：日志禁止明文输出；对外 API 返回必须脱敏（需求来源：v5.4 16.4）。
- P3 字段：建议字段级加密（AES-GCM）或单独加密表；访问必须强审计（需求来源：v5.4 16.4、16.5）。
## 4.1 Identity & Access（身份与访问控制）

需求来源：v5.4 9（角色）、16.1（RBAC+ABAC）、14（Auth/User&RBAC）。

核心聚合根：User、Role、Permission、AccessGrant。

设计要点：

- RBAC：角色→权限点；用户可绑定多个角色；权限点用于接口/菜单控制。
- ABAC：按单授权（AccessGrant）用于开柜/取钥匙等高风险动作，带 TTL、scope 与绑定资源（order_id/slot_id）。
- 审计：权限变更、按单授权发放/撤销必须记录 audit_log。
### 4.1.1 数据表：user_account

|字段（不含通用字段）|约束/索引|PII级别|需求来源|说明|
|---|---|---|---|---|
|username (varchar(64))|uk(tenant_id, username)|P1|v5.4 12/16.1|登录名（平台/代理/门店人员）；客户侧可为空|
|phone (varchar(20))|idx(tenant_id, phone)|P2|v5.4 12.1.1|手机号（车主/技师可用手机号登录）|
|email (varchar(128))|idx(tenant_id, email)|P2|可选|用于找回/通知|
|password_hash (varchar(255))|not null|P3|16.1|BCrypt/Argon2 hash；严禁返回|
|status (varchar(32))|idx(tenant_id, status)|P0|16.1|ACTIVE/SUSPENDED/DELETED|
|user_type (varchar(32))||P0|9|PLATFORM/AGENT/STORE/TECHNICIAN/CUSTOMER/PROPERTY/INVESTOR|
|mfa_enabled (boolean)||P0|16.1|是否启用 MFA|
|last_login_at (timestamptz)|idx(tenant_id, last_login_at)|P1|16.5||

### 4.1.2 数据表：role / permission / role_permission / user_role

|表|关键字段（不含通用字段）|需求来源|说明|
|---|---|---|---|
|role|role_code, role_name, scope, is_system|v5.4 9/16.1|角色表|
|permission|perm_key, perm_name, resource_type|v5.4 16.1|权限点字典|
|role_permission|role_id, permission_id|v5.4 16.1|多对多|
|user_role|user_id, role_id, data_scope_json|v5.4 16.1|可扩展数据范围，如可管理的 store_id 列表|

### 4.1.3 数据表：access_grant（按单授权）

|字段（不含通用字段）|约束/索引|PII级别|需求来源|说明|
|---|---|---|---|---|
|grant_no (varchar(64))|uk(tenant_id, grant_no)|P0|16.1/16.2|授权编号（可用于展示/追踪）|
|grant_type (varchar(32))|idx(tenant_id, grant_type)|P0|16.2|OPEN_SLOT / PICKUP_KEY / RETURN_KEY / EMERGENCY_OPEN|
|subject_type (varchar(32))||P0||USER/STAFF/DEVICE|
|subject_id (uuid)|idx(tenant_id, subject_id)|P1|16.2|被授权主体（技师/车主）|
|order_id (uuid)|idx(tenant_id, order_id)|P1|7/16.2|绑定订单|
|cabinet_id (uuid)||P0|12.6/16.2||
|slot_id (uuid)||P0|12.6/16.2||
|scope (varchar(256))||P0|16.2|细粒度 scope：open_slot:slot_id 等|
|token_hash (varchar(255))|uk(tenant_id, token_hash)|P3|16.2|短期 token hash；明文仅返回一次|
|issued_at (timestamptz)||P0|||
|expires_at (timestamptz)|idx(tenant_id, expires_at)|P0|16.2|TTL 到期失效|
|status (varchar(32))|idx(tenant_id, status)|P0|16.2|ISSUED/USED/EXPIRED/REVOKED|
|device_ack_event_id (uuid)||P0|15|关联设备 ACK 事件（可选）|

### 4.1.4 数据表：idempotency_record（幂等记录）

|字段（不含通用字段）|约束/索引|PII级别|需求来源|说明|
|---|---|---|---|---|
|idempotency_key (varchar(64))|uk(tenant_id, idempotency_key)|P0|10.5/14.0||
|request_hash (varchar(64))||P0||用于检测同 key 不同请求体|
|response_code (int)||P0|||
|response_body (jsonb)||P1||可选存储；或仅存引用|
|status (varchar(16))||P0||IN_PROGRESS/SUCCEEDED/FAILED|
|expires_at (timestamptz)|idx(tenant_id, expires_at)|P0||过期后可清理|

## 4.2 Tenant & Org（租户与组织树）

需求来源：v5.4 5.1（组织层级）、12.4/12.5（代理/平台）、5.2（商业协议主体）。

核心聚合根：Tenant、Organization（含 Agent/Franchise/PropertyGroup/Investor）、Store、Site。

### 4.2.1 数据表：tenant

|字段（不含通用字段）|约束/索引|PII级别|需求来源|说明|
|---|---|---|---|---|
|tenant_code (varchar(64))|uk(tenant_code)|P0|5.1|租户编码|
|tenant_name (varchar(128))||P0|5.1|租户名称|
|status (varchar(16))||P0||ACTIVE/SUSPENDED|
|timezone (varchar(64))||P0||默认时区|

### 4.2.2 数据表：organization（合同/经营主体）

|字段（不含通用字段）|约束/索引|PII级别|需求来源|说明|
|---|---|---|---|---|
|org_code (varchar(64))|uk(tenant_id, org_code)|P0|5.1/5.2|组织编码|
|org_name (varchar(256))|idx(tenant_id, org_name)|P0|5.1|组织名称|
|org_type (varchar(32))|idx(tenant_id, org_type)|P0|5.1|PLATFORM/AGENT/FRANCHISE/STORE/PROPERTY_GROUP/INVESTOR/OTHER|
|parent_org_id (uuid)|idx(tenant_id, parent_org_id)|P0|5.1|组织树父节点（代理下挂门店等）|
|legal_entity_name (varchar(256))||P1|5.2|合同主体抬头|
|tax_id (varchar(64))||P2|12.1.5/16.4|税号（如需要开票）|
|contact_name (varchar(64))||P2|||
|contact_phone (varchar(20))||P2|||
|status (varchar(16))||P0||ACTIVE/INACTIVE|

### 4.2.3 数据表：store / site（门店与站点）

|表|关键字段（不含通用字段）|需求来源|说明|
|---|---|---|---|
|store|org_id, store_code, store_name, store_type, status, default_site_id|v5.4 5.1/12.3|门店/加盟商维度（可含连锁门店）|
|site|store_id, site_code, site_name, address_json, geo_lat, geo_lng, open_hours_json, parking_rules_json, status|v5.4 12.1.2/12.3.1|实际服务站点（小区/园区/洗车点）|

### 4.2.4 数据对象：payout_account（收款账户）

收款账户可属于平台/代理/门店/物业集团/设备投资方等任一组织主体（需求来源：v5.4 4.5、5.2）。

|字段（不含通用字段）|约束/索引|PII级别|需求来源|说明|
|---|---|---|---|---|
|owner_org_id (uuid)|idx(tenant_id, owner_org_id)|P1|5.2|账户所属组织主体|
|account_type (varchar(32))||P0|5.2|BANK/ALIPAY/WECHAT/OTHER|
|account_no_masked (varchar(64))||P2|16.4|脱敏账号展示（如尾号）|
|account_no_enc (text)||P3|16.4|加密后的账号明文（仅财务可解密）|
|holder_name (varchar(128))||P2||开户名/收款方名称|
|channel_merchant_id (varchar(64))||P1||支付渠道商户号（如有）|
|status (varchar(16))||P0||ACTIVE/INACTIVE/VERIFYING|

## 4.3 Staff & Workforce（人员、班组与外部协作）

需求来源：v5.4 4.1（2+技师协作）、4.2（代泊来源多样）、8（人员/岗位/班组）、12.3.3/12.2。

### 4.3.1 数据表：staff（门店/物业/投资方人员档案）

|字段（不含通用字段）|约束/索引|PII级别|需求来源|说明|
|---|---|---|---|---|
|org_id (uuid)|idx(tenant_id, org_id)|P0|8/9|所属组织（门店/物业/投资方）|
|store_id (uuid)|idx(tenant_id, store_id)|P0|12.3.3|默认门店（可为空）|
|staff_no (varchar(64))|uk(tenant_id, staff_no)|P1||工号|
|name (varchar(64))||P2|12.2.1|姓名|
|phone (varchar(20))|idx(tenant_id, phone)|P2|12.2.1|手机号|
|staff_type (varchar(32))|idx(tenant_id, staff_type)|P0|8.1|TECHNICIAN/VALET_DRIVER/QA/CS/DEVICE_OPS/FINANCE|
|skills_json (jsonb)||P0|4.1|技能标签（精洗/打蜡等）|
|certs_json (jsonb)||P2|12.2.1/16.4|证件到期/类型（可选）|
|status (varchar(16))||P0||ACTIVE/INACTIVE|

### 4.3.2 数据表：wash_team / wash_team_member

|表|关键字段（不含通用字段）|需求来源|说明|
|---|---|---|---|
|wash_team|store_id, team_code, team_name, min_size, max_size, status|v5.4 4.1/8.2|洗车协作组；min_size 默认 2|
|wash_team_member|team_id, staff_id, role_in_team, is_leader, active|v5.4 4.1/8.2|组员关系；role_in_team 可为 WASH/VALET/QC|

### 4.3.3 数据表：external_staff_assignment（外部协作授权）

用于“代客泊车人员可能是物业人员”的场景：门店管理员可对外部人员授予特定站点/时间窗/岗位权限（可撤销）。

|字段（不含通用字段）|约束/索引|PII级别|需求来源|说明|
|---|---|---|---|---|
|staff_id (uuid)|idx(tenant_id, staff_id)|P1|4.2/8.1|外部人员 staff_id（所属 org_type=PROPERTY_GROUP 等）|
|granted_store_id (uuid)|idx(tenant_id, granted_store_id)|P0|4.2|授权给哪个门店|
|granted_site_id (uuid)|idx(tenant_id, granted_site_id)|P0|4.2|授权站点|
|allowed_staff_types (varchar(128))||P0|4.2|允许岗位，如 VALET_DRIVER|
|start_at/end_at (timestamptz)|idx(tenant_id, start_at, end_at)|P0|4.2|有效期（必须）|
|status (varchar(16))||P0||ACTIVE/REVOKED/EXPIRED|
|approved_by (uuid)||P1|16.5|审批人（如需）|

### 4.3.4 数据表：shift（排班/班次，可选）

|表|关键字段（不含通用字段）|需求来源|说明|
|---|---|---|---|
|shift|site_id, staff_id, shift_date, start_time, end_time, status|v5.4 12.3.3|可用于产能与队列预测（可选）|

## 4.4 Catalog & Pricing（服务目录与定价）

需求来源：v5.4 4.4（服务可扩展）、6（商品模型与定价优先级）、12.3.4、12.1.2。

### 4.4.1 数据表：service_catalog / service_item / service_variant / service_addon

|表|关键字段（不含通用字段）|需求来源|说明|
|---|---|---|---|
|service_catalog|store_id, catalog_name, status|v5.4 6.1|门店服务目录（可多套）|
|service_item|catalog_id, service_code, service_name, supported_modes[], default_required_techs, base_duration_min|v5.4 4.4/6.1/4.1|服务项（普通洗/精洗/打蜡等）|
|service_variant|service_item_id, variant_code, variant_name, list_price, duration_min, required_techs|v5.4 4.4/6|规格/档位（不同定价）|
|service_addon|service_item_id, addon_code, addon_name, addon_price, duration_min|v5.4 4.4/6|增值项|

### 4.4.2 数据表：price_plan（可选）与 price_snapshot（必须）

price_snapshot 为下单时固化的不可变快照（建议单独表 + jsonb），是对账/结算/纠纷的依据。

|字段（不含通用字段）|约束/索引|PII级别|需求来源|说明|
|---|---|---|---|---|
|order_id (uuid)|uk(tenant_id, order_id)|P1|4.5/6.2/12.1.3|绑定订单|
|currency (varchar(8))||P0||默认 CNY|
|items_json (jsonb)||P1|6.1/6.2|服务项/加项明细（含数量、单价、折扣）|
|total_amount (numeric(12,2))||P0|12.1.5|总金额|
|discount_amount (numeric(12,2))||P0||优惠金额|
|platform_fee_amount (numeric(12,2))||P0|4.5|平台服务费（可选）|
|tax_amount (numeric(12,2))||P0||税费（可选）|
|pricing_source (varchar(32))||P0|6.2|AGREEMENT/STORE/PLATFORM_GUIDE|
|pricing_trace_json (jsonb)||P1|6.2|用于解释“为什么是这个价”|

### 4.4.3 数据表：service_checklist_rule（必拍/质检规则）

用于无AI情况下的固定角度拍摄/必拍清单、验车/还车证据要求（需求来源：v5.4 16.3、12.2.4）。

|字段（不含通用字段）|约束/索引|PII级别|需求来源|说明|
|---|---|---|---|---|
|variant_id (uuid)|idx(tenant_id, variant_id)|P0|16.3/12.2.4|绑定规格档位|
|stage (varchar(32))|idx(tenant_id, stage)|P0|16.3|PRE_CHECK/POST_CHECK/PARKING/KEY_RETURN|
|required_photos (int)||P0||必拍数量|
|required_angles_json (jsonb)||P0||角度/说明列表（前/后/左/右/里程表等）|
|require_video (boolean)||P0||是否要求视频|
|watermark_policy (varchar(32))||P0|16.3|水印策略：order_no+time+actor|

## 4.5 Cabinet & IoT（柜体、格口与设备接入）

需求来源：v5.4 12.6（设备能力/弱网安全）、14.3（MQTT）、16.2（开柜安全控制点）、5.5（柜体归因/绑定）。

### 4.5.1 数据表：cabinet

|字段（不含通用字段）|约束/索引|PII级别|需求来源|说明|
|---|---|---|---|---|
|cabinet_code (varchar(64))|uk(tenant_id, cabinet_code)|P0|12.6|柜体编码（业务）|
|hardware_sn (varchar(64))|uk(hardware_sn)|P0|12.6|硬件序列号（全局唯一）|
|site_id (uuid)|idx(tenant_id, site_id)|P0|12.3.1/12.6|部署站点（物理位置）|
|cabinet_model (varchar(64))||P0||型号|
|slot_count (int)||P0||格口数量|
|firmware_version (varchar(32))||P0|12.6||
|device_status (varchar(16))|idx(tenant_id, device_status)|P0|12.6|ONLINE/OFFLINE/MAINTENANCE|
|last_heartbeat_at (timestamptz)|idx(tenant_id, last_heartbeat_at)|P0|12.6||
|mqtt_client_id (varchar(128))|uk(mqtt_client_id)|P0|14.3|设备 MQTT clientId|
|secret_key_id (uuid)||P0|16.2|设备密钥引用（轮换）|

### 4.5.2 数据表：slot（格口）

|字段（不含通用字段）|约束/索引|PII级别|需求来源|说明|
|---|---|---|---|---|
|cabinet_id (uuid)|idx(tenant_id, cabinet_id)|P0|12.6||
|slot_no (int)|uk(cabinet_id, slot_no)|P0|12.6|格口编号（1..N）|
|status (varchar(16))|idx(cabinet_id, status)|P0|10.5/20.1|EMPTY/RESERVED/OCCUPIED/FAULT/LOCKED|
|reserved_until (timestamptz)|idx(cabinet_id, reserved_until)|P0|5.5.1/17.2|预占过期时间|
|current_order_id (uuid)|idx(cabinet_id, current_order_id)|P1|7.2/7.4|当前占用订单（可空）|
|door_state (varchar(16))||P0|12.6.1|OPEN/CLOSED/UNKNOWN（来自设备事件）|
|last_open_request_id (varchar(64))||P0|14.3/15.1|最后一次开门指令 request_id|
|fault_code (varchar(32))||P0||故障码（可选）|

### 4.5.3 数据表：device_event（设备事件/ACK）

|字段（不含通用字段）|约束/索引|PII级别|需求来源|说明|
|---|---|---|---|---|
|cabinet_id (uuid)|idx(tenant_id, cabinet_id)|P0|14.3/15||
|event_id (varchar(64))|uk(tenant_id, event_id)|P0|15.1|事件幂等去重 key|
|event_type (varchar(32))|idx(tenant_id, event_type)|P0|14.3|HEARTBEAT/SLOT_STATE/CMD_ACK/ALARM/LOG|
|request_id (varchar(64))|idx(tenant_id, request_id)|P0|15.1|指令相关时必填|
|payload (jsonb)||P1|14.3|原始 payload（脱敏）|
|received_at (timestamptz)|idx(tenant_id, received_at)|P0|||

### 4.5.4 数据表：device_identity / device_secret（设备身份与密钥轮换）

|表|关键字段（不含通用字段）|需求来源|说明|
|---|---|---|---|
|device_identity|cabinet_id, mqtt_client_id, cert_fingerprint?, status, last_seen_at|v5.4 12.6.2/16.2|设备身份；可用证书/密钥方式|
|device_secret|secret_key_id, cabinet_id, secret_enc, active_from, active_to, status|v5.4 16.2|设备密钥轮换；secret 必须加密存储|

## 4.6 Asset & Investment（设备资产、投资认领与柜体-门店绑定）

需求来源：v5.4 5.4（设备投资认领与分成）、5.5（柜体-门店多对多绑定与归因）、4.6（柜体引流支持多门店）、9.5/12.7（设备投资方角色与控制台）。

### 4.6.1 业务定义与关键概念

- 设备投资方（Investor）：投资购买/部署智能钥匙柜的主体，可为平台、区域代理、门店或第三方组织；对其投资认领的柜体拥有“资产视角”数据可见与收益分成权。
- 柜体投放部署（Deployment）：柜体物理部署到某站点（site），可能迁移/撤场；投放与运营门店可解耦。
- 柜体-门店绑定（CabinetStoreBinding）：柜体可被配置为支持多个门店接单（多对多）；绑定可为 EXCLUSIVE（独占）或 SHARED（共享），可按时段/权重路由。
- 订单归因（Attribution）：订单必须固化 origin_cabinet_id（引流柜体）与 fulfillment_store_id（履约门店），并固化当时有效的 binding_snapshot 供结算对账。
### 4.6.2 数据表：cabinet_ownership_claim（投资认领/所有权）

|字段（不含通用字段）|约束/索引|PII级别|需求来源|说明|
|---|---|---|---|---|
|cabinet_id (uuid)|uk(tenant_id, cabinet_id, active_from)|P0|5.4|柜体|
|investor_org_id (uuid)|idx(tenant_id, investor_org_id)|P0|5.4/9.5|投资方组织主体|
|ownership_type (varchar(32))||P0|5.4|OWN/LEASE/REVENUE_SHARE|
|share_mode (varchar(32))||P0|5.4|PERCENT/FIXED_PER_ORDER/FIXED_PER_MONTH|
|share_value (numeric(12,4))||P0|5.4|比例（0-1）或固定金额|
|active_from/active_to (timestamptz)|idx(tenant_id, active_from, active_to)|P0|5.4|有效期（支持转让/变更）|
|status (varchar(16))||P0||ACTIVE/ENDED/PENDING_APPROVAL|
|approved_by (uuid)||P1|16.5|审批人（平台或代理）|

### 4.6.3 数据表：cabinet_deployment（投放部署/迁移/撤场）

|字段（不含通用字段）|约束/索引|PII级别|需求来源|说明|
|---|---|---|---|---|
|cabinet_id (uuid)|idx(tenant_id, cabinet_id)|P0|5.4||
|site_id (uuid)|idx(tenant_id, site_id)|P0|5.4/12.6|物理部署站点|
|deployed_by_org_id (uuid)||P0|5.4|投放执行主体（可代理/平台）|
|start_at/end_at (timestamptz)|idx(tenant_id, start_at, end_at)|P0|5.4|部署有效期|
|status (varchar(16))||P0||ACTIVE/MOVED/REMOVED|
|reason (varchar(256))||P1||迁移/撤场原因|

### 4.6.4 数据表：cabinet_store_binding（柜体-门店绑定，多对多）

|字段（不含通用字段）|约束/索引|PII级别|需求来源|说明|
|---|---|---|---|---|
|cabinet_id (uuid)|idx(tenant_id, cabinet_id)|P0|5.5||
|store_id (uuid)|idx(tenant_id, store_id)|P0|5.5|可接单门店|
|binding_type (varchar(16))|idx(tenant_id, cabinet_id, binding_type)|P0|4.6/5.5|EXCLUSIVE/SHARED|
|priority (int)||P0|5.5.1|路由优先级（越小越优先）|
|weight (int)||P0|5.5.1|共享场景权重（如 1-100）|
|time_windows_json (jsonb)||P0|5.5.1|按时段生效（可选）|
|route_policy (varchar(32))||P0|5.5|STATIC/WEIGHTED/QUEUE_AWARE（实现上可先做 STATIC/WEIGHTED）|
|active_from/active_to (timestamptz)|idx(tenant_id, active_from, active_to)|P0|5.5|绑定有效期|
|status (varchar(16))||P0|5.5.4|DRAFT/APPROVING/ACTIVE/SUSPENDED/ENDED|
|change_request_id (uuid)||P0|5.5.4|变更单/审批流程引用|

### 4.6.5 订单归因快照：binding_snapshot（建议）

为了结算可追溯，建议在订单创建或存钥匙确认时固化：

- origin_cabinet_id：引流柜体
- binding_id：生效的 cabinet_store_binding.id
- binding_type/priority/weight/route_policy：生效策略快照
- fulfillment_store_id：最终履约门店（可能与下单选择不同；以策略路由结果为准）
需求来源：v5.4 5.5.3（订单归因与结算口径）。

## 4.7 Order & Fulfillment（订单、排队与履约）

需求来源：v5.4 7（模式A/B/C）、10（状态机）、12.1（车主端）、5.5（柜体归因/路由）。

### 4.7.1 数据表：customer / vehicle / vehicle_document

车主首次使用需手机号注册与车辆信息登记（身份证/驾照/行驶证），属于 P2/P3 信息，需严格脱敏与加密（需求来源：v5.4 12.1.1、16.4）。

|表|关键字段（不含通用字段）|PII级别|需求来源|说明|
|---|---|---|---|---|
|customer|user_id, name, phone, status|P2|12.1.1|车主档案；user_id 关联 user_account|
|vehicle|customer_id, plate_no_masked, plate_no_enc, vin_masked, vin_enc, brand, model, color|P2/P3|12.1.1|车辆信息；plate/vin 建议加密存储|
|vehicle_document|vehicle_id, doc_type(ID_CARD/DRIVER_LICENSE/VEHICLE_LICENSE), file_object_key, verified_status|P3|12.1.1/16.4|证件影像与审核状态|

### 4.7.2 数据表：order（订单聚合）

|字段（不含通用字段）|约束/索引|PII级别|需求来源|说明|
|---|---|---|---|---|
|order_no (varchar(32))|uk(tenant_id, order_no)|P1|12.1.3|展示用订单号|
|customer_id (uuid)|idx(tenant_id, customer_id)|P1|12.1||
|vehicle_id (uuid)|idx(tenant_id, vehicle_id)|P1|12.1.1||
|fulfillment_mode (varchar(32))|idx(tenant_id, fulfillment_mode)|P0|7/20.1|A_CABINET_DROP / B_VALET_TRANSFER / C_MOBILE_ONSITE（均基于柜体托管）|
|status (varchar(16))|idx(tenant_id, status)|P0|10.1|订单状态机|
|payment_status (varchar(16))|idx(tenant_id, payment_status)|P0|12.1.5|UNPAID/PAID/REFUNDED...|
|site_id (uuid)|idx(tenant_id, site_id)|P0|12.1.2|下单站点（展示/引流）|
|origin_cabinet_id (uuid)|idx(tenant_id, origin_cabinet_id)|P0|5.5.3|引流柜体（模式A/C必填，B可选）|
|origin_binding_id (uuid)|idx(tenant_id, origin_binding_id)|P0|5.5.3|生效绑定 id（快照引用）|
|fulfillment_store_id (uuid)|idx(tenant_id, fulfillment_store_id)|P0|5.5.3|最终履约门店（固化）|
|cabinet_slot_id (uuid)|idx(tenant_id, cabinet_slot_id)|P0|7|存放钥匙格口（可为空/待确定）|
|key_custody_status (varchar(16))||P0|10.3|IN_SLOT/OUT_FOR_SERVICE/RETURNED...|
|vehicle_custody_status (varchar(16))||P0|10.4|车辆托管状态（可选）|
|price_snapshot_id (uuid)|uk(tenant_id, price_snapshot_id)|P0|6/4.5|价格快照引用|
|agreement_snapshot_id (uuid)||P0|5.2/4.5|商业协议快照引用|
|binding_snapshot_json (jsonb)||P1|5.5.3|绑定策略快照（防变更）|
|contact_phone (varchar(20))||P2|12.1.3|联系手机号（可与注册一致）|
|notes (varchar(512))||P1|12.1.3|备注（车位/门禁/上门地址说明等）|
|preferred_time_window (jsonb)||P1|12.1.3/7.3|期望时间窗（模式B/C）|

### 4.7.3 数据表：order_item

|字段（不含通用字段）|约束/索引|PII级别|需求来源|说明|
|---|---|---|---|---|
|order_id (uuid)|idx(tenant_id, order_id)|P0|6/12.1.3||
|item_type (varchar(16))||P0|6|SERVICE/ADDON|
|ref_id (uuid)||P0|6|引用 variant/addon id（快照中也应固化名称与价格）|
|name_snapshot (varchar(128))||P0|6|名称快照|
|unit_price (numeric(12,2))||P0|6||
|quantity (int)||P0|||
|amount (numeric(12,2))||P0|||

### 4.7.4 数据表：order_queue（排队/队列）

排队既要实时性（Redis）也要可追溯（DB）。建议：DB 保存队列记录，Redis 做实时队列镜像。

|字段（不含通用字段）|约束/索引|PII级别|需求来源|说明|
|---|---|---|---|---|
|site_id (uuid)|idx(tenant_id, site_id)|P0|12.3.2|站点队列|
|order_id (uuid)|uk(tenant_id, site_id, order_id)|P0|7||
|queue_no (int)|idx(tenant_id, site_id, queue_no)|P0||队列序号（可重算）|
|status (varchar(16))||P0||WAITING/CALLED/SKIPPED/CANCELLED|
|enqueued_at (timestamptz)||P0|||
|dequeued_at (timestamptz)||P0|||
|eta_minutes (int)||P0|12.1.2|预计等待时间（可选）|

### 4.7.5 数据表：order_location（车辆位置/停车位/上门信息）

用于记录车辆当前位置、车位照片、门禁信息等，支持模式A/B/C；模式C 仍通过附近柜体托管钥匙，再根据位置找到车辆现场施工。

|字段（不含通用字段）|约束/索引|PII级别|需求来源|说明|
|---|---|---|---|---|
|order_id (uuid)|uk(tenant_id, order_id)|P0|7.2/7.3/7.4||
|parking_lot_name (varchar(128))||P1|7|停车场/楼栋|
|parking_spot_no (varchar(64))||P1|7|车位号（建议脱敏显示）|
|geo_lat/geo_lng (numeric(10,7))||P1|7.4|车辆坐标（可选）|
|access_note (varchar(512))||P1|7.3/7.4|门禁/取车说明|
|photo_evidence_id (uuid)||P0|7|停车位照片证据引用（见 Evidence）|

## 4.8 Task & Execution（任务拆分与多人协作）

需求来源：v5.4 4.1（2+技师）、8.3（任务模型）、12.2/12.3.2（任务系统）。

### 4.8.1 数据表：task

|字段（不含通用字段）|约束/索引|PII级别|需求来源|说明|
|---|---|---|---|---|
|order_id (uuid)|idx(tenant_id, order_id)|P0|8.3/12.3.2||
|task_type (varchar(16))|idx(tenant_id, task_type)|P0|4.2/7.3/8.3|WASH/VALET_PICKUP/VALET_DROPOFF/QC|
|status (varchar(16))|idx(tenant_id, status)|P0|10.2|PENDING/ASSIGNED/IN_PROGRESS/DONE/CANCELLED|
|required_assignees (int)||P0|4.1|默认 2（洗车）；代泊可为 1|
|assigned_team_id (uuid)||P0|8.2|派给哪个洗车组（可选）|
|start_at/end_at (timestamptz)||P0|||
|notes (varchar(512))||P1||任务备注|

### 4.8.2 数据表：task_assignee（多人协作）

|字段（不含通用字段）|约束/索引|PII级别|需求来源|说明|
|---|---|---|---|---|
|task_id (uuid)|idx(tenant_id, task_id)|P0|4.1/8.3||
|staff_id (uuid)|idx(tenant_id, staff_id)|P1|4.1/12.2.2|技师/代泊人员|
|role_in_task (varchar(16))||P0|4.1|LEADER/MEMBER/VALET|
|accept_at (timestamptz)||P0|||
|complete_at (timestamptz)||P0|||
|status (varchar(16))||P0||ASSIGNED/ACCEPTED/DONE/REMOVED|

### 4.8.3 数据表：task_activity_log（过程记录，可选）

|表|关键字段（不含通用字段）|需求来源|说明|
|---|---|---|---|
|task_activity_log|task_id, actor_staff_id, action, from_status, to_status, payload_json, occurred_at|v5.4 15/16.5|用于回放与争议处理（也可用审计/事件替代）|

## 4.9 Key Custody（钥匙托管与取还钥匙闭环）

需求来源：v5.4 7（流程）、10.3（钥匙托管状态）、12.2.3（取钥匙/开柜高风险）、16.2（控制点）。

### 4.9.1 数据表：key_custody_log（钥匙流转日志）

|字段（不含通用字段）|约束/索引|PII级别|需求来源|说明|
|---|---|---|---|---|
|order_id (uuid)|idx(tenant_id, order_id)|P0|10.3/16.2||
|cabinet_id (uuid)||P0|||
|slot_id (uuid)||P0|||
|action (varchar(32))|idx(tenant_id, action)|P0|16.2|DEPOSIT/TECH_PICKUP/RETURN/CUST_RETRIEVE/EMERGENCY_OPEN|
|actor_type (varchar(16))||P0|16.2|CUSTOMER/STAFF/SYSTEM|
|actor_id (uuid)||P1|16.2/16.5|操作者|
|access_grant_id (uuid)||P0|16.2|关联按单授权（如有）|
|request_id (varchar(64))||P0|15.1|设备指令 request_id（如有）|
|result (varchar(16))||P0||SUCCESS/FAIL|
|occurred_at (timestamptz)|idx(tenant_id, occurred_at)|P0|||

### 4.9.2 数据一致性规则（必须）

- slot.status 与 order.key_custody_status 必须保持一致（最终一致）：写入顺序建议为：设备 ACK → slot 更新 → order 更新 → 发布事件（Outbox）。
- 任何取/还钥匙动作必须有可追溯证据：access_grant（如适用）+ device_event + key_custody_log + audit_log。
- 弱网/离线：设备事件可延迟上报；服务端需允许“迟到 ACK”并做幂等处理（需求来源：v5.4 12.6.2）。
## 4.10 Evidence（证据链）

需求来源：v5.4 16.3（不可抵赖）、12.2.4（预检/完工/车位拍照）、12.1.4（车主查看）。

### 4.10.1 数据表：evidence_record

|字段（不含通用字段）|约束/索引|PII级别|需求来源|说明|
|---|---|---|---|---|
|order_id (uuid)|idx(tenant_id, order_id)|P0|16.3||
|task_id (uuid)|idx(tenant_id, task_id)|P0|12.2.4|关联任务（可选）|
|stage (varchar(32))|idx(tenant_id, stage)|P0|16.3|PRE_CHECK/POST_CHECK/PARKING/KEY_RETURN/OTHER|
|uploader_type (varchar(16))||P0||STAFF/CUSTOMER|
|uploader_id (uuid)||P1|||
|file_count (int)||P0|||
|status (varchar(16))||P0||ACTIVE/REDACTED（仅平台合规可做遮盖处理，不删除原件）|
|hash_chain_prev (varchar(64))||P0|16.3|可选：hash chain 上一节点|
|hash_chain_curr (varchar(64))||P0|16.3|可选：当前节点 hash|

### 4.10.2 数据表：evidence_file

|字段（不含通用字段）|约束/索引|PII级别|需求来源|说明|
|---|---|---|---|---|
|evidence_id (uuid)|idx(tenant_id, evidence_id)|P0|16.3||
|object_key (varchar(512))|uk(tenant_id, object_key)|P0|16.3|对象存储 key|
|file_type (varchar(16))||P0||IMAGE/VIDEO|
|mime_type (varchar(64))||P0|||
|size_bytes (bigint)||P0|||
|sha256 (varchar(64))||P0|16.3|完整性校验|
|watermark_applied (boolean)||P0|16.3|是否打水印|
|metadata_json (jsonb)||P1||拍摄设备/时间/位置（需脱敏）|

### 4.10.3 对象存储路径规范（建议）

建议 object_key 结构：

tenant/<tenant_id>/orders/<order_id>/evidence/<evidence_id>/<file_id>.<ext>

- 对象存储开启版本化与生命周期（如 2 年后归档/冷存储），满足合规保留期（需求来源：v5.4 16.4、18）。
- 上传采用 Pre-signed URL；服务端落库时校验 sha256；必要时添加水印（order_no/time/actor）。
## 4.11 Payment & Refund（支付与退款）

需求来源：v5.4 12.1.5（支付/发票/评价/投诉）、16.6（资金风控与对账）、14（Payment/Refund）。

### 4.11.1 数据表：payment_intent / payment_txn

|表|关键字段（不含通用字段）|需求来源|说明|
|---|---|---|---|
|payment_intent|order_id, payer_customer_id, amount, currency, channel, status, idempotency_key, expires_at|v5.4 12.1.5/16.6|支付意图；支持多次发起/过期|
|payment_txn|intent_id, provider_txn_id, paid_amount, paid_at, raw_notify_json, status|v5.4 16.6|支付渠道流水；notify 原文需脱敏存储|

### 4.11.2 数据表：refund_request（退款申请/审批）

|字段（不含通用字段）|约束/索引|PII级别|需求来源|说明|
|---|---|---|---|---|
|order_id (uuid)|idx(tenant_id, order_id)|P0|14/16.6||
|requested_by (uuid)||P1||申请人（门店/平台/客服）|
|reason_code (varchar(32))||P0||质量/超时/纠纷等|
|reason_desc (varchar(512))||P1|||
|amount (numeric(12,2))||P0|||
|status (varchar(16))|idx(tenant_id, status)|P0|14|REQUESTED/APPROVING/APPROVED/REJECTED/REFUNDED|
|approved_by (uuid)||P1|16.5|审批人|
|provider_refund_id (varchar(64))||P0||渠道退款单号|

## 4.12 Settlement & Reconcile（商业协议、结算分润与对账）

需求来源：v5.4 4.5（商业模式拆分）、5.2~5.5（商业协议/定价权/投资分成/绑定归因）、16.6（资金风控对账）、18.3。

### 4.12.1 数据表：commercial_agreement（商业协议）

|字段（不含通用字段）|约束/索引|PII级别|需求来源|说明|
|---|---|---|---|---|
|agreement_code (varchar(64))|uk(tenant_id, agreement_code)|P0|5.2||
|agreement_name (varchar(256))||P0|5.2||
|party_a_org_id (uuid)||P0|5.2|平台/运营合伙方等|
|party_b_org_id (uuid)||P0|5.2|代理/门店/物业/投资方等|
|pricing_owner_org_id (uuid)||P0|4.5/5.3|定价权主体（谁可改价/定价）|
|collector_org_id (uuid)||P0|4.5/5.2|收款方主体（钱进谁账户）|
|effective_from/effective_to (timestamptz)|idx(tenant_id, effective_from, effective_to)|P0|5.2||
|status (varchar(16))||P0||DRAFT/ACTIVE/EXPIRED/SUSPENDED|
|settlement_policy_json (jsonb)||P0|5.3/5.4|分润规则（平台/代理/门店/物业/设备投资/人员提成等）|
|approval_flow_json (jsonb)||P0|5.2|审批流配置（可选）|

### 4.12.2 数据表：agreement_snapshot（订单级快照，必须）

|字段（不含通用字段）|约束/索引|PII级别|需求来源|说明|
|---|---|---|---|---|
|order_id (uuid)|uk(tenant_id, order_id)|P0|4.5/5.2||
|agreement_id (uuid)|idx(tenant_id, agreement_id)|P0|5.2||
|pricing_owner_org_id (uuid)||P0|5.3||
|collector_org_id (uuid)||P0|4.5||
|settlement_policy_snapshot (jsonb)||P0|5.3/5.4/5.5.3|分润规则快照（含设备投资方分成）|
|created_reason (varchar(64))||P0||AT_ORDER_CREATE/AT_KEY_DEPOSIT 等|

### 4.12.3 数据表：settlement_batch / settlement_line

|表|关键字段（不含通用字段）|需求来源|说明|
|---|---|---|---|
|settlement_batch|batch_no, period_start, period_end, collector_org_id, status, generated_at|v5.4 18.3/14(Settlement)|结算批次（按收款方/周期）|
|settlement_line|batch_id, order_id, line_type(PLATFORM/AGENT/STORE/PROPERTY/INVESTOR/STAFF), payee_org_id, amount, policy_snapshot|v5.4 5.3/5.4/5.5.3|分润明细（以订单快照计算）|

### 4.12.4 对账：reconcile_import / reconcile_diff / reconcile_ticket

|表|关键字段（不含通用字段）|需求来源|说明|
|---|---|---|---|
|reconcile_import|collector_org_id, period, source_file_key, status, imported_at|v5.4 16.6/14(Reconcile)|导入渠道/物业对账单|
|reconcile_diff|import_id, order_id, diff_type(AMOUNT/STATUS/MISSING), expected_json, actual_json, status|v5.4 16.6|差异明细|
|reconcile_ticket|diff_id, owner_user_id, status, resolution, closed_at|v5.4 14(Tickets)/16.6|差异处理工单|

## 4.13 Notification（通知与消息中心）

需求来源：v5.4 15.3（通知触发点与渠道）、20.2（模板占位）、12.1.4/12.2（状态提醒）。

### 4.13.1 数据表：notify_template

|字段（不含通用字段）|约束/索引|PII级别|需求来源|说明|
|---|---|---|---|---|
|template_code (varchar(64))|uk(tenant_id, template_code)|P0|20.2|模板编码（如 CUST_KEY_DEPOSITED）|
|channel (varchar(16))||P0|15.3|INAPP/PUSH/SMS/EMAIL|
|locale (varchar(16))||P0||zh-CN|
|title_tpl (varchar(256))||P0|20.2|标题模板|
|content_tpl (text)||P0|20.2|内容模板（含变量占位）|
|variables_json (jsonb)||P0|20.2|变量清单（可校验）|
|status (varchar(16))||P0||ACTIVE/INACTIVE|

### 4.13.2 数据表：notify_message / notify_delivery_attempt

|表|关键字段（不含通用字段）|需求来源|说明|
|---|---|---|---|
|notify_message|template_code, recipient_type, recipient_id, title, content, status, related_order_id, created_at|v5.4 15.3/20.2|站内信/推送消息记录|
|notify_delivery_attempt|message_id, channel, provider, attempt_no, status, error_code, sent_at|v5.4 15.3|投递记录与重试|

## 4.14 Risk & Audit（风控与审计）

需求来源：v5.4 16（风控/审计/合规），尤其 16.2（开柜安全）、16.5（审计落库规范）。

### 4.14.1 数据表：audit_log（不可变追加）

|字段（不含通用字段；注意：audit_log 不允许软删）|约束/索引|PII级别|需求来源|说明|
|---|---|---|---|---|
|action (varchar(64))|idx(tenant_id, action)|P0|16.5|动作编码：CABINET_OPEN/REFUND_APPROVE/ROLE_CHANGE 等|
|actor_id (uuid)|idx(tenant_id, actor_id)|P1|16.5|操作者（user/staff）|
|actor_type (varchar(16))||P0|16.5|USER/STAFF/SYSTEM|
|resource_type (varchar(32))||P0|16.5|ORDER/CABINET/SLOT/AGREEMENT/...|
|resource_id (uuid)||P0|16.5||
|request_id (varchar(64))|idx(tenant_id, request_id)|P0|16.5|贯穿链路的 Request-Id|
|ip (inet)||P1|16.5||
|user_agent (varchar(256))||P1|16.5||
|result (varchar(16))||P0|16.5|SUCCESS/FAIL|
|detail_json (jsonb)||P1|16.5|脱敏详情（禁止 P3 明文）|
|occurred_at (timestamptz)|idx(tenant_id, occurred_at)|P0|16.5||

### 4.14.2 数据表：risk_policy / risk_event

|表|关键字段（不含通用字段）|需求来源|说明|
|---|---|---|---|
|risk_policy|policy_code, name, enabled, threshold_json, action_json, scope_json|v5.4 16|风控策略配置（如连续开柜失败锁定）|
|risk_event|policy_id, actor_id, resource_type, resource_id, hit_reason, decision, occurred_at|v5.4 16|策略命中记录与处置结果|

## 4.15 Ticketing（客服工单/投诉）

需求来源：v5.4 12.1.5（投诉）、14（Tickets）、12.5（平台客服）。

### 4.15.1 数据表：ticket / ticket_comment / ticket_attachment

|表|关键字段（不含通用字段）|需求来源|说明|
|---|---|---|---|
|ticket|ticket_no, type(COMPLAINT/DIFF/DEVICE), order_id?, site_id?, status, priority, owner_user_id|v5.4 14(Tickets)|工单主表|
|ticket_comment|ticket_id, commenter_id, content, created_at|v5.4 14|评论沟通|
|ticket_attachment|ticket_id, object_key, mime_type, sha256|v5.4 14/16.3|附件（复用对象存储）|

## 4.16 Reporting（报表与指标读模型，建议）

需求来源：v5.4 12.5（平台监管）、12.7（投资方可见柜体运营数据）、18（运营运维）。

建议采用事件投影生成读模型（避免直接扫描交易表），典型读模型：

- site_daily_metrics：站点日订单数/收入/排队峰值/平均时长
- cabinet_daily_metrics：柜体日开柜次数、引流订单数、在线率、故障率
- investor_payout_summary：投资方按周期分润汇总
读模型可用物化视图或独立表；更新来源为 Outbox 事件流。

# 5. 关键业务约束、状态机与一致性设计

本章将 v5.4 的状态枚举与关键约束落实为可编码的规则（Domain Invariants），并给出并发控制与一致性策略。

需求来源：v5.4 10、13.4、5.5.4、16.2。

## 5.1 订单状态机（OrderStatus）

订单状态机建议由领域层统一维护（例如使用枚举 + 转移表），任何写操作必须通过 `OrderAggregate.transitionTo(next, command)` 校验合法性。

|当前状态|允许下一状态|触发命令/条件（摘要）|需求来源|
|---|---|---|---|
|CREATED|WAITING_KEY_DEPOSIT|创建订单后等待车主存钥匙（模式A/C）或等待代泊取车（模式B）|7.2/7.4/7.3|
|WAITING_KEY_DEPOSIT|IN_QUEUE|存钥匙确认成功/格口占用后入队|7.2/10.3|
|IN_QUEUE|CALLED|门店叫号/派单|12.3.2|
|CALLED|IN_SERVICE|技师取钥匙成功（按单授权）并开始施工|12.2.3/16.2|
|IN_SERVICE|WAITING_KEY_RETURN|完工并准备还钥匙/归位|12.2.4|
|WAITING_KEY_RETURN|KEY_RETURNED|还钥匙入柜确认成功|7/16.2|
|KEY_RETURNED|CLOSED|车主取回钥匙/确认完成|7.2/12.1.4|
|ANY|CANCELLED|取消（按策略：未取钥匙/未开工可取消）|10.1|

## 5.2 任务状态机（TaskStatus）与多人协作规则

洗车任务（WASH）默认 required_assignees=2；只有当完成确认人数达到 required_assignees，任务才可进入 DONE，并推动订单状态推进。

- Task.status: PENDING → ASSIGNED → IN_PROGRESS → DONE（取消/改派需要审计）
- TaskAssignee.status: ASSIGNED → ACCEPTED → DONE（可被移除/替换）
- 订单推进规则：所有必须任务 DONE + 钥匙状态满足 → 才可推进下一订单状态。
## 5.3 格口状态机（SlotStatus）与预占 TTL

|当前状态|允许下一状态|触发命令/条件（摘要）|需求来源|
|---|---|---|---|
|EMPTY|RESERVED|预占成功（下单/扫码前）|17.2/5.5.2|
|RESERVED|EMPTY|TTL 到期/取消订单|17.2/10.5|
|RESERVED|OCCUPIED|存钥匙确认成功|7.2/16.2|
|OCCUPIED|EMPTY|车主取回钥匙后释放|7.2|
|OCCUPIED|RESERVED|异常释放再预占（谨慎）|运维场景|
|ANY|FAULT|门店/平台标记故障或设备上报故障|12.3.1/12.6|
|FAULT|EMPTY|修复完成解除故障|18.2|

## 5.4 柜体-门店绑定约束（多对多）

- 同一 cabinet 在同一时间窗内，最多只能存在 1 条 ACTIVE 的 EXCLUSIVE 绑定；若存在 EXCLUSIVE，则不允许并存 SHARED（需求来源：v5.4 5.5.1）。
- SHARED 绑定允许多条并存，但必须有明确 route_policy（STATIC/WEIGHTED）与权重/优先级（需求来源：v5.4 4.6、5.5.1）。
- 绑定变更必须走变更单/审批（可选简化为平台/代理直接审批），并全量审计（需求来源：v5.4 5.5.4、16.5）。
## 5.5 设备投资认领与分成约束

- 同一 cabinet 在同一时刻只能有一个 ACTIVE 的 ownership_claim（除非设计为多方共投，此处建议先不支持，避免结算复杂度）。
- 订单结算时，设备投资分成必须以订单创建时固化的 agreement_snapshot + binding_snapshot + ownership_claim 生效期为准。
- 投资方数据可见：仅能查看其投资柜体的资产/运营数据；不得查看非授权门店的 PII/证据细节（需求来源：v5.4 9.5/12.7/16.4）。
## 5.6 一致性策略：事务边界 + Outbox（必须）

强一致场景（同一聚合内）：单事务提交；跨聚合/跨上下文：使用 Outbox 发布事件实现最终一致。

- 订单创建：Order + OrderItems + PriceSnapshot + AgreementSnapshot 同事务。
- 存钥匙确认：Slot 状态更新 + KeyCustodyLog + Order 状态推进 同事务；事务成功后写 Outbox: Order.KeyDeposited。
- 开柜：发放 AccessGrant 同事务；设备 ACK 到达后更新 Slot/Order，并写 Outbox: Slot.Opened/AccessGrant.Used。
- 结算：SettlementBatch/Lines 生成同事务；发布 Settlement.BatchGenerated 事件供导出/通知。
需求来源：v5.4 15.1、10.5。

# 6. REST API 设计（实现级）

本章给出后端 REST API 的实现级规范与接口清单。建议以 OpenAPI 作为单一事实源（source of truth），并通过代码生成前端 client。

需求来源：v5.4 14（接口与 DTO 总体约定）。

## 6.1 API 总体约定（必须）

- Base URL：/api/v1
- 认证：Authorization: Bearer <JWT>
- 链路追踪：X-Request-Id（若无则服务端生成并回传）
- 幂等：对写接口要求 Idempotency-Key（POST/PUT/command）
- 多租户：tenant_id 从 JWT claim 提取；平台跨租户接口可带 query tenant_id（强审计）
- 分页：page,size,sort；返回 totalElements/totalPages
- 错误模型：{code, message, requestId, details[]}；禁止返回堆栈
## 6.2 权限与数据范围（RBAC+ABAC）

权限校验分两层：

- RBAC：接口级权限点（permission_key）——决定能不能调用该接口。
- 数据范围：基于角色 scope 与 user_role.data_scope_json（例如可管理的 store_id 列表）。
- ABAC：对高风险动作（开柜/取钥匙/应急开柜）使用 AccessGrant（按单授权，短期 token）。
## 6.3 接口分组与清单（建议覆盖范围）

下表为实现级接口清单。每个接口均标注：权限点、是否幂等、关联 DTO 与需求来源章节。

### 6.3.1 Auth

|operationId|方法|路径|permission_key|幂等|请求DTO|响应DTO|需求来源（v5.4）|
|---|---|---|---|---|---|---|---|
|authLogin|POST|/api/v1/auth/login|public.auth.login|N/A|AuthLoginRequest|AuthTokenResponse|v5.4 14(Auth)/12.1.1/16.1|
|authRefresh|POST|/api/v1/auth/refresh|public.auth.refresh|N/A|AuthRefreshRequest|AuthTokenResponse|v5.4 14(Auth)|
|authLogout|POST|/api/v1/auth/logout|auth.logout|Y|AuthLogoutRequest|VoidResponse|v5.4 14(Auth)|
|sendSmsCode|POST|/api/v1/auth/sms/send|public.auth.sms.send|Y|SendSmsCodeRequest|SendSmsCodeResponse|v5.4 12.1.1|
|verifySmsCode|POST|/api/v1/auth/sms/verify|public.auth.sms.verify|Y|VerifySmsCodeRequest|AuthTokenResponse|v5.4 12.1.1|

### 6.3.2 User & RBAC

|operationId|方法|路径|permission_key|幂等|请求DTO|响应DTO|需求来源（v5.4）|
|---|---|---|---|---|---|---|---|
|listUsers|GET|/api/v1/users|iam.user.read|N/A|PageQuery|Page<UserDTO>|v5.4 16.1/14(User&RBAC)|
|createUser|POST|/api/v1/users|iam.user.create|Y|UserCreateRequest|UserDTO|v5.4 16.1|
|updateUser|PUT|/api/v1/users/{userId}|iam.user.update|Y|UserUpdateRequest|UserDTO|v5.4 16.1|
|setUserRoles|PUT|/api/v1/users/{userId}/roles|iam.user.role.bind|Y|UserRoleBindRequest|UserDTO|v5.4 16.1|
|listRoles|GET|/api/v1/roles|iam.role.read|N/A|PageQuery|Page<RoleDTO>|v5.4 16.1|
|createRole|POST|/api/v1/roles|iam.role.create|Y|RoleCreateRequest|RoleDTO|v5.4 16.1|
|updateRole|PUT|/api/v1/roles/{roleId}|iam.role.update|Y|RoleUpdateRequest|RoleDTO|v5.4 16.1|
|setRolePermissions|PUT|/api/v1/roles/{roleId}/permissions|iam.role.perm.bind|Y|RolePermissionBindRequest|RoleDTO|v5.4 16.1|
|listPermissions|GET|/api/v1/permissions|iam.perm.read|N/A|PageQuery|Page<PermissionDTO>|v5.4 16.1|
|issueAccessGrant|POST|/api/v1/access-grants|cabinet.access.grant.issue|Y|AccessGrantIssueRequest|AccessGrantDTO|v5.4 16.1/16.2|
|revokeAccessGrant|POST|/api/v1/access-grants/{grantId}/revoke|cabinet.access.grant.revoke|Y|AccessGrantRevokeRequest|AccessGrantDTO|v5.4 16.2|

### 6.3.3 Org/Tenant

|operationId|方法|路径|permission_key|幂等|请求DTO|响应DTO|需求来源（v5.4）|
|---|---|---|---|---|---|---|---|
|listTenants|GET|/api/v1/tenants|platform.tenant.read|N/A|PageQuery|Page<TenantDTO>|v5.4 5.1/12.5|
|createTenant|POST|/api/v1/tenants|platform.tenant.create|Y|TenantCreateRequest|TenantDTO|v5.4 5.1|
|listOrgs|GET|/api/v1/orgs|org.read|N/A|PageQuery|Page<OrgDTO>|v5.4 5.1|
|createOrg|POST|/api/v1/orgs|org.create|Y|OrgCreateRequest|OrgDTO|v5.4 5.1/5.2|
|updateOrg|PUT|/api/v1/orgs/{orgId}|org.update|Y|OrgUpdateRequest|OrgDTO|v5.4 5.1|
|listStores|GET|/api/v1/stores|store.read|N/A|PageQuery|Page<StoreDTO>|v5.4 12.3/12.4|
|createStore|POST|/api/v1/stores|store.create|Y|StoreCreateRequest|StoreDTO|v5.4 12.3|
|updateStore|PUT|/api/v1/stores/{storeId}|store.update|Y|StoreUpdateRequest|StoreDTO|v5.4 12.3|
|listSites|GET|/api/v1/sites|site.read|N/A|PageQuery|Page<SiteDTO>|v5.4 12.1.2/12.3.1|
|createSite|POST|/api/v1/sites|site.create|Y|SiteCreateRequest|SiteDTO|v5.4 12.3.1|
|updateSite|PUT|/api/v1/sites/{siteId}|site.update|Y|SiteUpdateRequest|SiteDTO|v5.4 12.3.1|

### 6.3.4 Staff

|operationId|方法|路径|permission_key|幂等|请求DTO|响应DTO|需求来源（v5.4）|
|---|---|---|---|---|---|---|---|
|listStaff|GET|/api/v1/staff|staff.read|N/A|PageQuery|Page<StaffDTO>|v5.4 8/12.3.3|
|createStaff|POST|/api/v1/staff|staff.create|Y|StaffCreateRequest|StaffDTO|v5.4 4.2/8|
|updateStaff|PUT|/api/v1/staff/{staffId}|staff.update|Y|StaffUpdateRequest|StaffDTO|v5.4 8|
|listWashTeams|GET|/api/v1/wash-teams|washTeam.read|N/A|PageQuery|Page<WashTeamDTO>|v5.4 4.1/8.2|
|createWashTeam|POST|/api/v1/wash-teams|washTeam.create|Y|WashTeamCreateRequest|WashTeamDTO|v5.4 4.1/8.2|
|updateWashTeam|PUT|/api/v1/wash-teams/{teamId}|washTeam.update|Y|WashTeamUpdateRequest|WashTeamDTO|v5.4 8.2|
|addWashTeamMember|POST|/api/v1/wash-teams/{teamId}/members|washTeam.member.add|Y|WashTeamMemberAddRequest|WashTeamDTO|v5.4 4.1|
|removeWashTeamMember|DELETE|/api/v1/wash-teams/{teamId}/members/{staffId}|washTeam.member.remove|Y|Void|VoidResponse|v5.4 4.1|
|createExternalAssignment|POST|/api/v1/external-assignments|staff.external.assign|Y|ExternalAssignmentCreateRequest|ExternalAssignmentDTO|v5.4 4.2|
|revokeExternalAssignment|POST|/api/v1/external-assignments/{id}/revoke|staff.external.revoke|Y|Void|ExternalAssignmentDTO|v5.4 4.2|

### 6.3.5 Catalog

|operationId|方法|路径|permission_key|幂等|请求DTO|响应DTO|需求来源（v5.4）|
|---|---|---|---|---|---|---|---|
|listCatalogs|GET|/api/v1/catalogs|catalog.read|N/A|PageQuery|Page<CatalogDTO>|v5.4 6/12.3.4|
|createCatalog|POST|/api/v1/catalogs|catalog.create|Y|CatalogCreateRequest|CatalogDTO|v5.4 6|
|listServices|GET|/api/v1/services|service.read|N/A|PageQuery|Page<ServiceItemDTO>|v5.4 6|
|createService|POST|/api/v1/services|service.create|Y|ServiceItemCreateRequest|ServiceItemDTO|v5.4 6|
|listVariants|GET|/api/v1/variants|service.variant.read|N/A|PageQuery|Page<ServiceVariantDTO>|v5.4 6|
|createVariant|POST|/api/v1/variants|service.variant.create|Y|ServiceVariantCreateRequest|ServiceVariantDTO|v5.4 6|
|listAddons|GET|/api/v1/addons|service.addon.read|N/A|PageQuery|Page<ServiceAddonDTO>|v5.4 6|
|createAddon|POST|/api/v1/addons|service.addon.create|Y|ServiceAddonCreateRequest|ServiceAddonDTO|v5.4 6|
|setChecklistRules|PUT|/api/v1/variants/{variantId}/checklist-rules|service.checklist.set|Y|ChecklistRuleSetRequest|ChecklistRuleDTO|v5.4 16.3/12.2.4|

### 6.3.6 Pricing

|operationId|方法|路径|permission_key|幂等|请求DTO|响应DTO|需求来源（v5.4）|
|---|---|---|---|---|---|---|---|
|createQuote|POST|/api/v1/quotes|pricing.quote.create|Y|PriceQuoteRequest|PriceQuoteResponse|v5.4 6.2/12.1.3|
|getPriceSnapshot|GET|/api/v1/price-snapshots/{orderId}|pricing.snapshot.read|N/A|Void|PriceSnapshotDTO|v5.4 6|

### 6.3.7 Order

|operationId|方法|路径|permission_key|幂等|请求DTO|响应DTO|需求来源（v5.4）|
|---|---|---|---|---|---|---|---|
|createOrder|POST|/api/v1/orders|order.create|Y|OrderCreateRequest|OrderDTO|v5.4 12.1.3/7|
|getOrder|GET|/api/v1/orders/{orderId}|order.read|N/A|Void|OrderDTO|v5.4 12.1.4|
|listOrders|GET|/api/v1/orders|order.read|N/A|PageQuery|Page<OrderDTO>|v5.4 12.1.4/12.3.2|
|cancelOrder|POST|/api/v1/orders/{orderId}/cancel|order.cancel|Y|OrderCancelRequest|OrderDTO|v5.4 10.1|
|depositKeyConfirm|POST|/api/v1/orders/{orderId}/key/deposit|order.key.deposit|Y|KeyDepositConfirmRequest|OrderDTO|v5.4 7.2/16.2|
|technicianPickupKey|POST|/api/v1/orders/{orderId}/key/pickup|order.key.pickup|Y|TechPickupKeyRequest|OrderDTO|v5.4 12.2.3/16.2|
|returnKeyConfirm|POST|/api/v1/orders/{orderId}/key/return|order.key.return|Y|KeyReturnConfirmRequest|OrderDTO|v5.4 7/16.2|
|customerRetrieveKey|POST|/api/v1/orders/{orderId}/key/retrieve|order.key.retrieve|Y|CustomerRetrieveKeyRequest|OrderDTO|v5.4 7.2|
|rateOrder|POST|/api/v1/orders/{orderId}/rating|order.rate|Y|OrderRatingRequest|OrderRatingDTO|v5.4 12.1.5|

### 6.3.8 Queue

|operationId|方法|路径|permission_key|幂等|请求DTO|响应DTO|需求来源（v5.4）|
|---|---|---|---|---|---|---|---|
|getSiteQueue|GET|/api/v1/sites/{siteId}/queue|queue.read|N/A|PageQuery|QueueStatusDTO|v5.4 12.1.2/12.3.2|
|enqueueOrder|POST|/api/v1/orders/{orderId}/queue/enqueue|queue.enqueue|Y|Void|QueueEntryDTO|v5.4 7.1|
|callNext|POST|/api/v1/sites/{siteId}/queue/call-next|queue.call|Y|CallNextRequest|QueueEntryDTO|v5.4 12.3.2|
|skipQueueEntry|POST|/api/v1/orders/{orderId}/queue/skip|queue.skip|Y|SkipQueueRequest|QueueEntryDTO|v5.4 12.3.2|

### 6.3.9 Cabinet

|operationId|方法|路径|permission_key|幂等|请求DTO|响应DTO|需求来源（v5.4）|
|---|---|---|---|---|---|---|---|
|listCabinets|GET|/api/v1/cabinets|cabinet.read|N/A|PageQuery|Page<CabinetDTO>|v5.4 12.3.1/12.6|
|createCabinet|POST|/api/v1/cabinets|cabinet.create|Y|CabinetCreateRequest|CabinetDTO|v5.4 12.6|
|listSlots|GET|/api/v1/cabinets/{cabinetId}/slots|cabinet.slot.read|N/A|PageQuery|Page<SlotDTO>|v5.4 12.6|
|markSlotFault|POST|/api/v1/slots/{slotId}/fault|cabinet.slot.fault|Y|SlotFaultRequest|SlotDTO|v5.4 12.3.1/18.2|

### 6.3.10 Cabinet Access

|operationId|方法|路径|permission_key|幂等|请求DTO|响应DTO|需求来源（v5.4）|
|---|---|---|---|---|---|---|---|
|reserveSlot|POST|/api/v1/cabinets/{cabinetId}/slots/reserve|cabinet.slot.reserve|Y|SlotReserveRequest|SlotReserveResponse|v5.4 17.2/5.5.2|
|openSlot|POST|/api/v1/slots/{slotId}/open|cabinet.slot.open|Y|SlotOpenRequest|SlotOpenResponse|v5.4 16.2/14.3|
|emergencyOpenSlot|POST|/api/v1/slots/{slotId}/open-emergency|cabinet.slot.open.emergency|Y|EmergencyOpenRequest|SlotOpenResponse|v5.4 16.2/18.4|

### 6.3.11 Tasks

|operationId|方法|路径|permission_key|幂等|请求DTO|响应DTO|需求来源（v5.4）|
|---|---|---|---|---|---|---|---|
|listOrderTasks|GET|/api/v1/orders/{orderId}/tasks|task.read|N/A|PageQuery|Page<TaskDTO>|v5.4 12.2.2/12.3.2|
|createOrderTask|POST|/api/v1/orders/{orderId}/tasks|task.create|Y|TaskCreateRequest|TaskDTO|v5.4 8.3|
|assignTask|POST|/api/v1/tasks/{taskId}/assign|task.assign|Y|TaskAssignRequest|TaskDTO|v5.4 12.3.2|
|completeTask|POST|/api/v1/tasks/{taskId}/complete|task.complete|Y|TaskCompleteRequest|TaskDTO|v5.4 4.1/12.2.2|

### 6.3.12 Evidence

|operationId|方法|路径|permission_key|幂等|请求DTO|响应DTO|需求来源（v5.4）|
|---|---|---|---|---|---|---|---|
|createEvidence|POST|/api/v1/orders/{orderId}/evidence|evidence.create|Y|EvidenceCreateRequest|EvidenceDTO|v5.4 16.3/12.2.4|
|listOrderEvidence|GET|/api/v1/orders/{orderId}/evidence|evidence.read|N/A|PageQuery|Page<EvidenceDTO>|v5.4 12.1.4|
|presignEvidenceUpload|POST|/api/v1/evidence/{evidenceId}/files/presign|evidence.upload|Y|EvidencePresignRequest|EvidencePresignResponse|v5.4 16.3|
|commitEvidenceFile|POST|/api/v1/evidence/{evidenceId}/files/commit|evidence.upload|Y|EvidenceCommitRequest|EvidenceDTO|v5.4 16.3|

### 6.3.13 Payment

|operationId|方法|路径|permission_key|幂等|请求DTO|响应DTO|需求来源（v5.4）|
|---|---|---|---|---|---|---|---|
|createPayment|POST|/api/v1/pay/create|pay.create|Y|PayCreateRequest|PayCreateResponse|v5.4 12.1.5|
|paymentNotify|POST|/api/v1/pay/notify|public.pay.notify|N/A|PayNotifyRequest|VoidResponse|v5.4 16.6|

### 6.3.14 Refund

|operationId|方法|路径|permission_key|幂等|请求DTO|响应DTO|需求来源（v5.4）|
|---|---|---|---|---|---|---|---|
|createRefund|POST|/api/v1/refunds|refund.create|Y|RefundCreateRequest|RefundDTO|v5.4 14(Refund)|
|approveRefund|POST|/api/v1/refunds/{refundId}/approve|refund.approve|Y|RefundApproveRequest|RefundDTO|v5.4 14(Refund)/16.6|
|rejectRefund|POST|/api/v1/refunds/{refundId}/reject|refund.reject|Y|RefundRejectRequest|RefundDTO|v5.4 14(Refund)/16.6|

### 6.3.15 Settlement

|operationId|方法|路径|permission_key|幂等|请求DTO|响应DTO|需求来源（v5.4）|
|---|---|---|---|---|---|---|---|
|listAgreements|GET|/api/v1/agreements|agreement.read|N/A|PageQuery|Page<AgreementDTO>|v5.4 5.2/14(Settlement)|
|createAgreement|POST|/api/v1/agreements|agreement.create|Y|AgreementCreateRequest|AgreementDTO|v5.4 5.2|
|generateSettlementBatch|POST|/api/v1/settlement/batches/generate|settlement.batch.generate|Y|SettlementGenerateRequest|SettlementBatchDTO|v5.4 18.3|
|listSettlementBatches|GET|/api/v1/settlement/batches|settlement.batch.read|N/A|PageQuery|Page<SettlementBatchDTO>|v5.4 18.3|
|listSettlementLines|GET|/api/v1/settlement/lines|settlement.line.read|N/A|PageQuery|Page<SettlementLineDTO>|v5.4 5.3/5.4|

### 6.3.16 Reconcile

|operationId|方法|路径|permission_key|幂等|请求DTO|响应DTO|需求来源（v5.4）|
|---|---|---|---|---|---|---|---|
|importReconcileFile|POST|/api/v1/reconcile/import|reconcile.import|Y|ReconcileImportRequest|ReconcileImportDTO|v5.4 16.6|
|listReconcileDiffs|GET|/api/v1/reconcile/diffs|reconcile.diff.read|N/A|PageQuery|Page<ReconcileDiffDTO>|v5.4 16.6|
|createReconcileTicket|POST|/api/v1/reconcile/tickets|reconcile.ticket.create|Y|ReconcileTicketCreateRequest|TicketDTO|v5.4 16.6|

### 6.3.17 Tickets

|operationId|方法|路径|permission_key|幂等|请求DTO|响应DTO|需求来源（v5.4）|
|---|---|---|---|---|---|---|---|
|createTicket|POST|/api/v1/tickets|ticket.create|Y|TicketCreateRequest|TicketDTO|v5.4 14(Tickets)|
|listTickets|GET|/api/v1/tickets|ticket.read|N/A|PageQuery|Page<TicketDTO>|v5.4 14(Tickets)|
|addTicketComment|POST|/api/v1/tickets/{ticketId}/comments|ticket.comment.add|Y|TicketCommentCreateRequest|TicketCommentDTO|v5.4 14(Tickets)|

### 6.3.18 Notify

|operationId|方法|路径|permission_key|幂等|请求DTO|响应DTO|需求来源（v5.4）|
|---|---|---|---|---|---|---|---|
|listNotifyTemplates|GET|/api/v1/notify/templates|notify.template.read|N/A|PageQuery|Page<NotifyTemplateDTO>|v5.4 20.2|
|updateNotifyTemplate|PUT|/api/v1/notify/templates/{templateCode}|notify.template.update|Y|NotifyTemplateUpdateRequest|NotifyTemplateDTO|v5.4 20.2|
|sendNotifyManual|POST|/api/v1/notify/send|notify.send|Y|NotifySendRequest|NotifySendResponse|v5.4 15.3|

### 6.3.19 Asset & Investment

|operationId|方法|路径|permission_key|幂等|请求DTO|响应DTO|需求来源（v5.4）|
|---|---|---|---|---|---|---|---|
|claimCabinetOwnership|POST|/api/v1/asset/cabinets/{cabinetId}/ownership/claim|asset.cabinet.ownership.claim|Y|CabinetOwnershipClaimRequest|CabinetOwnershipDTO|v5.4 5.4|
|transferCabinetOwnership|POST|/api/v1/asset/cabinets/{cabinetId}/ownership/transfer|asset.cabinet.ownership.transfer|Y|CabinetOwnershipTransferRequest|CabinetOwnershipDTO|v5.4 5.4|
|deployCabinet|POST|/api/v1/asset/cabinets/{cabinetId}/deployment|asset.cabinet.deploy|Y|CabinetDeployRequest|CabinetDeploymentDTO|v5.4 5.4|
|bindCabinetToStore|POST|/api/v1/asset/cabinets/{cabinetId}/store-bindings|asset.cabinet.storeBinding.create|Y|CabinetStoreBindingCreateRequest|CabinetStoreBindingDTO|v5.4 5.5|
|updateCabinetStoreBinding|PUT|/api/v1/asset/cabinets/{cabinetId}/store-bindings/{bindingId}|asset.cabinet.storeBinding.update|Y|CabinetStoreBindingUpdateRequest|CabinetStoreBindingDTO|v5.4 5.5.4|
|listCabinetStoreBindings|GET|/api/v1/asset/cabinets/{cabinetId}/store-bindings|asset.cabinet.storeBinding.read|N/A|PageQuery|Page<CabinetStoreBindingDTO>|v5.4 5.5.1|
|investorListCabinets|GET|/api/v1/investor/cabinets|investor.cabinet.read|N/A|PageQuery|Page<InvestorCabinetDTO>|v5.4 12.7/9.5|
|investorCabinetMetrics|GET|/api/v1/investor/cabinets/{cabinetId}/metrics|investor.cabinet.metrics|N/A|MetricsQuery|InvestorCabinetMetricsDTO|v5.4 12.7|
|investorListSettlementBatches|GET|/api/v1/investor/settlement/batches|investor.settlement.read|N/A|PageQuery|Page<SettlementBatchDTO>|v5.4 5.4|
|investorListSettlementLines|GET|/api/v1/investor/settlement/lines|investor.settlement.read|N/A|PageQuery|Page<SettlementLineDTO>|v5.4 5.4|
|investorUpsertPayoutAccount|POST|/api/v1/investor/payout-accounts|investor.payout.update|Y|PayoutAccountUpsertRequest|PayoutAccountDTO|v5.4 5.4/16.4|

### 6.3.20 Cabinet Routing

|operationId|方法|路径|permission_key|幂等|请求DTO|响应DTO|需求来源（v5.4）|
|---|---|---|---|---|---|---|---|
|listCabinetProviders|GET|/api/v1/cabinets/{cabinetId}/providers|cabinet.routing.read|N/A|Void|CabinetProviderListDTO|v5.4 5.5.2/5.5.3|

## 6.4 统一错误码与异常映射（建议）

|错误码|说明|HTTP|客户端处理建议|
|---|---|---|---|
|AUTH_001|未登录或 token 过期|HTTP 401|提示重新登录|
|AUTH_002|无权限（RBAC）|HTTP 403|提示联系管理员|
|TENANT_001|tenant_id 不匹配/越权|HTTP 403|强审计告警|
|ORDER_001|订单状态不允许该操作|HTTP 409|返回当前状态与允许操作|
|CABINET_001|设备离线/无法下发指令|HTTP 503|提示稍后重试/联系运维|
|CABINET_002|格口被占用/预占过期|HTTP 409|引导重新扫码/重新预占|
|IDEMP_001|幂等冲突：同 key 不同请求|HTTP 409|要求更换 Idempotency-Key|
|PAY_001|支付回调验签失败|HTTP 400|记录风控事件|

## 6.5 关键接口的事务与事件（开发要点）

以下接口为全链路关键路径，必须在 PR 验收中覆盖：

- createOrder → 生成订单/价格快照/协议快照；发布 Order.Created
- reserveSlot / depositKeyConfirm → 预占格口/确认存钥匙；发布 Slot.Reserved/Order.KeyDeposited
- issueAccessGrant + openSlot + 设备ACK → 发布 AccessGrant.Issued/Slot.Opened
- technicianPickupKey → 钥匙托管状态变更；发布 Order.KeyPickedUp
- completeTask + returnKeyConfirm → 完工/还钥匙；发布 Task.Completed/Order.KeyReturned/Order.Completed
- pay.create + pay.notify → 支付成功；发布 Payment.Succeeded
- settlement batch generate → 结算批次生成；发布 Settlement.BatchGenerated
# 7. DTO（数据对象）详细定义（字段级）

本章定义所有对外 API 使用的 DTO 字段（含类型、必填、约束、需求来源）。建议与 OpenAPI schema 保持一一对应，后续以代码生成/契约测试确保不漂移。

需求来源：v5.4 14.2（DTO 字段级定义）。

## 7.0 DTO 通用约定（必须）

- 命名：JSON 字段使用 camelCase；枚举使用大写字符串。
- 时间：统一使用 ISO-8601（UTC）字符串；后端落库 timestamptz。
- 金额：使用 decimal（numeric），避免 float；单位为元（CNY）。
- 脱敏：对 P2/P3 字段返回 masked 版本（如 phoneMasked、plateNoMasked），如需明文必须走特权接口且全审计。
- 扩展：低频扩展字段使用 ext（object），并必须在文档中登记 schema。
## 7.1 通用 DTO

### ErrorResponse

统一错误返回结构（所有非 2xx）。

需求来源：v5.4 14.0

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|code|string|Y|错误码（见 6.4）|14.0|
|message|string|Y|面向用户/开发者的错误信息|14.0|
|requestId|string|Y|X-Request-Id，用于排障|14.0|
|details|array<object>|N|可选明细，如字段校验错误列表|14.0|

### VoidResponse

无业务数据的统一响应（可选）。HTTP 204 也可。

需求来源：v5.4 14.0

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|success|boolean|Y|true/false|14.0|
|requestId|string|Y|链路追踪|14.0|

### PageQuery

分页查询参数模型（也可直接用 querystring）。

需求来源：v5.4 14.0

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|page|int|N|页码，从 0 开始；默认 0|14.0|
|size|int|N|每页数量；默认 20；最大 200|14.0|
|sort|string|N|排序：field,asc|desc|14.0|

### PageResponse

分页响应通用结构（content 为任意 DTO 列表）。

需求来源：v5.4 14.0

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|content|array<object>|Y|数据列表|14.0|
|page|int|Y|页码|14.0|
|size|int|Y|每页数量|14.0|
|totalElements|long|Y|总条数|14.0|
|totalPages|int|Y|总页数|14.0|
|sort|string|N|排序|14.0|

## 7.2 认证与会话 DTO

### AuthLoginRequest

登录请求。支持账号密码、手机号验证码等多种方式（按租户策略启用）。

需求来源：v5.4 14(Auth)、12.1.1、16.1

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|loginType|string|Y|PASSWORD/SMS_CODE|12.1.1|
|username|string|N|loginType=PASSWORD 时必填|14(Auth)|
|phone|string|N|loginType=SMS_CODE 时必填|12.1.1|
|password|string|N|loginType=PASSWORD 时必填|16.1|
|smsCode|string|N|loginType=SMS_CODE 时必填|12.1.1|
|tenantCode|string|N|平台多租户入口可选；通常从域名/配置确定|5.1|
|deviceInfo|object|N|设备信息：os,appVersion,deviceId（用于风控）|16|

### AuthRefreshRequest

刷新 token。

需求来源：v5.4 14(Auth)

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|refreshToken|string|Y|刷新令牌|14(Auth)|

### AuthLogoutRequest

注销（可选：拉黑 refreshToken）。

需求来源：v5.4 14(Auth)

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|refreshToken|string|N|如需要服务端撤销|14(Auth)|

### AuthTokenResponse

登录/刷新后返回 token 与会话信息。

需求来源：v5.4 14(Auth)、16.1

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|accessToken|string|Y|JWT access token|16.1|
|refreshToken|string|Y|refresh token|16.1|
|expiresIn|int|Y|accessToken 有效期（秒）|16.1|
|tokenType|string|Y|Bearer|16.1|
|user|UserDTO|Y|当前登录用户信息（脱敏）|16.1|
|permissions|array<string>|Y|权限点列表（perm_key）|16.1|
|tenantId|string|Y|租户 ID|5.1|

### SendSmsCodeRequest

发送短信验证码。

需求来源：v5.4 12.1.1

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|phone|string|Y|手机号|12.1.1|
|scene|string|Y|LOGIN/REGISTER/RESET_PASSWORD|12.1.1|

### SendSmsCodeResponse

发送验证码响应（不返回验证码）。

需求来源：v5.4 12.1.1

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|requestId|string|Y|请求标识|14.0|
|cooldownSeconds|int|Y|下次可发送倒计时|16|

### VerifySmsCodeRequest

验证短信验证码（可用于注册/登录）。

需求来源：v5.4 12.1.1

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|phone|string|Y|手机号|12.1.1|
|smsCode|string|Y|验证码|12.1.1|
|scene|string|Y|LOGIN/REGISTER|12.1.1|

## 7.3 用户、角色与按单授权 DTO

### UserDTO

用户信息（脱敏）。用于平台/代理/门店后台与 APP。

需求来源：v5.4 16.1、9

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|userId|string|Y|用户ID（uuid）|16.1|
|username|string|N|登录名（后台人员）|16.1|
|phoneMasked|string|N|手机号脱敏显示|16.4|
|emailMasked|string|N|邮箱脱敏显示|16.4|
|userType|string|Y|PLATFORM/AGENT/STORE/TECHNICIAN/CUSTOMER/PROPERTY/INVESTOR|9|
|status|string|Y|ACTIVE/SUSPENDED|16.1|
|mfaEnabled|boolean|Y|是否启用 MFA|16.1|
|roles|array<RoleDTO>|N|角色列表|16.1|
|createdAt|string|N|创建时间（ISO）|16.5|

### UserCreateRequest

创建用户（后台）。

需求来源：v5.4 16.1

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|username|string|N|后台账号可用 username|16.1|
|phone|string|N|手机号（会脱敏返回）|12.2.1|
|email|string|N|邮箱|16.1|
|password|string|N|可选：初始密码；或走重置流程|16.1|
|userType|string|Y|用户类型|9|
|status|string|N|默认 ACTIVE|16.1|
|roleIds|array<string>|N|初始角色|16.1|
|dataScope|object|N|数据范围（storeIds/siteIds 等）|16.1|

### UserUpdateRequest

更新用户。

需求来源：v5.4 16.1

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|phone|string|N|手机号|16.1|
|email|string|N|邮箱|16.1|
|status|string|N|ACTIVE/SUSPENDED|16.1|
|mfaEnabled|boolean|N|是否启用 MFA|16.1|
|dataScope|object|N|数据范围更新|16.1|

### UserRoleBindRequest

绑定用户角色。

需求来源：v5.4 16.1

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|roleIds|array<string>|Y|角色ID列表|16.1|

### RoleDTO

角色。

需求来源：v5.4 9/16.1

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|roleId|string|Y|uuid|16.1|
|roleCode|string|Y|角色编码（唯一）|9|
|roleName|string|Y|显示名|9|
|scope|string|Y|PLATFORM/TENANT/ORG/STORE/SITE/ORDER|16.1|
|permissionKeys|array<string>|N|权限点列表|16.1|
|isSystem|boolean|Y|系统内置|16.1|

### RoleCreateRequest

创建角色。

需求来源：v5.4 16.1

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|roleCode|string|Y|唯一编码|16.1|
|roleName|string|Y|显示名|16.1|
|scope|string|Y|范围|16.1|
|permissionKeys|array<string>|N|权限点（可后续绑定）|16.1|

### RoleUpdateRequest

更新角色。

需求来源：v5.4 16.1

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|roleName|string|N|显示名|16.1|
|permissionKeys|array<string>|N|权限点列表|16.1|

### RolePermissionBindRequest

绑定角色权限点。

需求来源：v5.4 16.1

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|permissionKeys|array<string>|Y|权限点 key 列表|16.1|

### PermissionDTO

权限点。

需求来源：v5.4 16.1

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|permKey|string|Y|唯一 key，如 order.read|16.1|
|permName|string|Y|显示名|16.1|
|resourceType|string|N|ORDER/CABINET/SETTLEMENT...|16.1|
|description|string|N|说明|16.1|

### AccessGrantIssueRequest

签发按单授权（用于开柜/取钥匙等）。服务端也可内部触发自动签发。

需求来源：v5.4 16.2

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|grantType|string|Y|OPEN_SLOT/PICKUP_KEY/RETURN_KEY/EMERGENCY_OPEN|16.2|
|orderId|string|Y|订单ID|7/16.2|
|cabinetId|string|Y|柜体ID|12.6|
|slotId|string|Y|格口ID|12.6|
|subjectType|string|Y|USER/STAFF|16.2|
|subjectId|string|Y|被授权主体|16.2|
|ttlSeconds|int|Y|有效期秒数（建议 60~300）|16.2|
|reason|string|N|签发原因（应急开柜必填）|16.2/18.4|

### AccessGrantRevokeRequest

撤销按单授权。

需求来源：v5.4 16.2

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|reason|string|Y|撤销原因|16.2|

### AccessGrantDTO

按单授权信息（不返回 token 明文；明文 token 仅在签发时返回一次）。

需求来源：v5.4 16.2/15.1

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|grantId|string|Y|uuid|16.2|
|grantNo|string|Y|展示用编号|16.2|
|grantType|string|Y|授权类型|16.2|
|orderId|string|Y|订单ID|7|
|cabinetId|string|Y|柜体ID|12.6|
|slotId|string|Y|格口ID|12.6|
|subjectType|string|Y|USER/STAFF|16.2|
|subjectId|string|Y|主体ID|16.2|
|status|string|Y|ISSUED/USED/EXPIRED/REVOKED|20.1|
|issuedAt|string|Y|签发时间|16.2|
|expiresAt|string|Y|过期时间|16.2|
|token|string|N|仅在签发响应中返回一次（短期 token）|16.2|

## 7.4 租户与组织 DTO

### TenantDTO

租户信息（平台级）。

需求来源：v5.4 5.1

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|tenantId|string|Y|uuid|5.1|
|tenantCode|string|Y|唯一编码|5.1|
|tenantName|string|Y|名称|5.1|
|status|string|Y|ACTIVE/SUSPENDED|5.1|
|timezone|string|N|默认时区|5.1|
|createdAt|string|N|创建时间|16.5|

### TenantCreateRequest

创建租户（平台超管）。

需求来源：v5.4 5.1

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|tenantCode|string|Y|唯一编码|5.1|
|tenantName|string|Y|名称|5.1|
|timezone|string|N|默认时区|5.1|

### OrgDTO

组织/合同主体。

需求来源：v5.4 5.1/5.2

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|orgId|string|Y|uuid|5.1|
|orgCode|string|N|组织编码|5.1|
|orgName|string|Y|名称|5.1|
|orgType|string|Y|PLATFORM/AGENT/FRANCHISE/STORE/PROPERTY_GROUP/INVESTOR|5.1|
|parentOrgId|string|N|父组织|5.1|
|legalEntityName|string|N|合同主体抬头|5.2|
|taxIdMasked|string|N|税号脱敏|16.4|
|contactName|string|N|联系人|5.2|
|contactPhoneMasked|string|N|联系人电话脱敏|16.4|
|status|string|Y|ACTIVE/INACTIVE|5.1|

### OrgCreateRequest

创建组织。

需求来源：v5.4 5.1/5.2

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|orgName|string|Y|名称|5.1|
|orgType|string|Y|类型|5.1|
|parentOrgId|string|N|父组织（代理-门店关系）|5.1|
|legalEntityName|string|N|合同主体抬头|5.2|
|taxId|string|N|税号（P2，需加密）|16.4|
|contactName|string|N|联系人|5.2|
|contactPhone|string|N|联系人电话|5.2|

### OrgUpdateRequest

更新组织。

需求来源：v5.4 5.1/5.2

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|orgName|string|N|名称|5.1|
|legalEntityName|string|N|合同抬头|5.2|
|taxId|string|N|税号（更新需审计）|16.4|
|contactName|string|N|联系人|5.2|
|contactPhone|string|N|电话|5.2|
|status|string|N|ACTIVE/INACTIVE|5.1|

### StoreDTO

门店/加盟商。

需求来源：v5.4 12.3/5.1

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|storeId|string|Y|uuid|12.3|
|orgId|string|Y|所属组织|5.1|
|storeCode|string|N|编码|12.3|
|storeName|string|Y|名称|12.3|
|storeType|string|N|SINGLE/CHAIN|5.1|
|status|string|Y|ACTIVE/INACTIVE|12.3|
|defaultSiteId|string|N|默认站点|12.3|

### StoreCreateRequest

创建门店。

需求来源：v5.4 12.3

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|orgId|string|Y|所属组织（store org）|5.1|
|storeName|string|Y|名称|12.3|
|storeCode|string|N|编码|12.3|
|storeType|string|N|SINGLE/CHAIN|5.1|
|status|string|N|默认 ACTIVE|12.3|

### StoreUpdateRequest

更新门店。

需求来源：v5.4 12.3

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|storeName|string|N|名称|12.3|
|status|string|N|ACTIVE/INACTIVE|12.3|
|defaultSiteId|string|N|默认站点|12.3|

### SiteDTO

站点（小区/园区/洗车点）。

需求来源：v5.4 12.1.2/12.3.1

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|siteId|string|Y|uuid|12.3.1|
|storeId|string|Y|所属门店|12.3.1|
|siteCode|string|N|编码|12.3.1|
|siteName|string|Y|名称|12.1.2|
|address|object|N|地址结构：province/city/district/detail|12.1.2|
|geoLat|number|N|纬度|12.1.2|
|geoLng|number|N|经度|12.1.2|
|openHours|object|N|营业时间|12.3.1|
|parkingRules|object|N|停车规则/说明|12.3.1|
|status|string|Y|ACTIVE/INACTIVE|12.3.1|

### SiteCreateRequest

创建站点。

需求来源：v5.4 12.3.1

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|storeId|string|Y|所属门店|12.3.1|
|siteName|string|Y|名称|12.3.1|
|siteCode|string|N|编码|12.3.1|
|address|object|N|地址|12.3.1|
|geoLat|number|N|纬度|12.3.1|
|geoLng|number|N|经度|12.3.1|
|openHours|object|N|营业时间|12.3.1|
|parkingRules|object|N|停车规则|12.3.1|

### SiteUpdateRequest

更新站点。

需求来源：v5.4 12.3.1

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|siteName|string|N|名称|12.3.1|
|address|object|N|地址|12.3.1|
|geoLat|number|N|纬度|12.3.1|
|geoLng|number|N|经度|12.3.1|
|openHours|object|N|营业时间|12.3.1|
|parkingRules|object|N|停车规则|12.3.1|
|status|string|N|ACTIVE/INACTIVE|12.3.1|

### PayoutAccountDTO

收款账户（脱敏展示）。

需求来源：v5.4 4.5/5.2/16.4

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|accountId|string|Y|uuid|5.2|
|ownerOrgId|string|Y|所属组织|5.2|
|accountType|string|Y|BANK/ALIPAY/WECHAT/OTHER|5.2|
|accountNoMasked|string|Y|脱敏展示|16.4|
|holderName|string|N|开户名（可脱敏）|16.4|
|status|string|Y|ACTIVE/INACTIVE/VERIFYING|5.2|

### PayoutAccountUpsertRequest

新增/更新收款账户（投资方/组织财务）。

需求来源：v5.4 5.4/16.4

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|ownerOrgId|string|Y|所属组织（投资方 org）|5.4|
|accountType|string|Y|类型|5.2|
|accountNo|string|Y|账号明文（P3，加密存储）|16.4|
|holderName|string|Y|开户名|5.2|
|channelMerchantId|string|N|渠道商户号|16.6|

## 7.5 人员与班组 DTO

### StaffDTO

人员档案（脱敏）。

需求来源：v5.4 8/12.3.3/4.2

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|staffId|string|Y|uuid|8|
|orgId|string|Y|所属组织|8|
|storeId|string|N|默认门店|12.3.3|
|staffNo|string|N|工号|8|
|name|string|Y|姓名|12.2.1|
|phoneMasked|string|Y|手机号脱敏|16.4|
|staffType|string|Y|TECHNICIAN/VALET_DRIVER/QA/...|8.1|
|skills|array<string>|N|技能标签|4.4|
|status|string|Y|ACTIVE/INACTIVE|8|

### StaffCreateRequest

创建人员。

需求来源：v5.4 12.3.3/4.2

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|orgId|string|Y|所属组织|8|
|storeId|string|N|默认门店|12.3.3|
|name|string|Y|姓名|12.2.1|
|phone|string|Y|手机号|12.2.1|
|staffType|string|Y|岗位类型|8.1|
|skills|array<string>|N|技能标签|4.4|

### StaffUpdateRequest

更新人员。

需求来源：v5.4 12.3.3

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|storeId|string|N|默认门店|12.3.3|
|name|string|N|姓名|12.2.1|
|phone|string|N|手机号|12.2.1|
|staffType|string|N|岗位类型|8.1|
|skills|array<string>|N|技能|4.4|
|status|string|N|ACTIVE/INACTIVE|8|

### WashTeamDTO

洗车组（多人协作）。

需求来源：v5.4 4.1/8.2

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|teamId|string|Y|uuid|8.2|
|storeId|string|Y|所属门店|8.2|
|teamCode|string|N|编码|8.2|
|teamName|string|Y|名称|8.2|
|minSize|int|Y|默认 2|4.1|
|maxSize|int|N|可选|4.1|
|status|string|Y|ACTIVE/INACTIVE|8.2|
|members|array<WashTeamMemberDTO>|N|成员列表|8.2|

### WashTeamMemberDTO

洗车组成员。

需求来源：v5.4 4.1/8.2

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|staffId|string|Y|uuid|8.2|
|name|string|N|姓名|8.2|
|roleInTeam|string|Y|WASH/VALET/QC|4.1|
|isLeader|boolean|N|是否组长|4.1|

### WashTeamCreateRequest

创建洗车组。

需求来源：v5.4 8.2

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|storeId|string|Y|门店|8.2|
|teamName|string|Y|名称|8.2|
|minSize|int|N|默认 2|4.1|
|maxSize|int|N|可选|4.1|

### WashTeamUpdateRequest

更新洗车组。

需求来源：v5.4 8.2

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|teamName|string|N|名称|8.2|
|minSize|int|N|最小人数|4.1|
|maxSize|int|N|最大人数|4.1|
|status|string|N|ACTIVE/INACTIVE|8.2|

### WashTeamMemberAddRequest

新增组员。

需求来源：v5.4 4.1

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|staffId|string|Y|人员ID|4.1|
|roleInTeam|string|Y|WASH/VALET/QC|4.1|
|isLeader|boolean|N|是否组长|4.1|

### ExternalAssignmentDTO

外部协作授权（例如物业代泊人员）。

需求来源：v5.4 4.2

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|assignmentId|string|Y|uuid|4.2|
|staffId|string|Y|外部人员|4.2|
|grantedStoreId|string|Y|授权门店|4.2|
|grantedSiteId|string|N|授权站点|4.2|
|allowedStaffTypes|array<string>|Y|允许岗位列表|4.2|
|startAt|string|Y|生效时间|4.2|
|endAt|string|Y|失效时间|4.2|
|status|string|Y|ACTIVE/REVOKED/EXPIRED|4.2|

### ExternalAssignmentCreateRequest

创建外部协作授权。

需求来源：v5.4 4.2

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|staffId|string|Y|外部人员|4.2|
|grantedStoreId|string|Y|授权门店|4.2|
|grantedSiteId|string|N|授权站点|4.2|
|allowedStaffTypes|array<string>|Y|允许岗位|4.2|
|startAt|string|Y|开始时间|4.2|
|endAt|string|Y|结束时间|4.2|

## 7.6 服务目录与定价 DTO

### CatalogDTO

服务目录。

需求来源：v5.4 6.1

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|catalogId|string|Y|uuid|6.1|
|storeId|string|Y|门店|6.1|
|catalogName|string|Y|名称|6.1|
|status|string|Y|ACTIVE/INACTIVE|6.1|

### CatalogCreateRequest

创建服务目录。

需求来源：v5.4 6.1

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|storeId|string|Y|门店|6.1|
|catalogName|string|Y|名称|6.1|

### ServiceItemDTO

服务项（如普通洗、精洗、打蜡等）。

需求来源：v5.4 4.4/6.1

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|serviceItemId|string|Y|uuid|6.1|
|catalogId|string|Y|目录|6.1|
|serviceCode|string|N|编码|6.1|
|serviceName|string|Y|名称|4.4|
|supportedModes|array<string>|Y|A/B/C 支持列表|7|
|defaultRequiredTechs|int|N|默认需要技师数量，默认 2|4.1|
|baseDurationMin|int|N|基准工时|6.1|
|status|string|Y|ACTIVE/INACTIVE|6.1|

### ServiceItemCreateRequest

创建服务项。

需求来源：v5.4 6.1

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|catalogId|string|Y|目录|6.1|
|serviceName|string|Y|名称|4.4|
|serviceCode|string|N|编码|6.1|
|supportedModes|array<string>|Y|支持模式列表|7|
|defaultRequiredTechs|int|N|默认 2|4.1|
|baseDurationMin|int|N|工时|6.1|

### ServiceVariantDTO

服务规格/档位（不同价格、工时）。

需求来源：v5.4 6.1

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|variantId|string|Y|uuid|6.1|
|serviceItemId|string|Y|服务项|6.1|
|variantCode|string|N|编码|6.1|
|variantName|string|Y|名称|6.1|
|listPrice|number|Y|标价|6.2|
|durationMin|int|N|工时|6.1|
|requiredTechs|int|N|需要技师数量（可覆盖默认）|4.1|
|status|string|Y|ACTIVE/INACTIVE|6.1|

### ServiceVariantCreateRequest

创建服务规格。

需求来源：v5.4 6.1

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|serviceItemId|string|Y|服务项|6.1|
|variantName|string|Y|名称|6.1|
|variantCode|string|N|编码|6.1|
|listPrice|number|Y|标价|6.2|
|durationMin|int|N|工时|6.1|
|requiredTechs|int|N|技师数|4.1|

### ServiceAddonDTO

加项/增值服务。

需求来源：v5.4 6.1

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|addonId|string|Y|uuid|6.1|
|serviceItemId|string|Y|服务项|6.1|
|addonCode|string|N|编码|6.1|
|addonName|string|Y|名称|6.1|
|addonPrice|number|Y|价格|6.2|
|durationMin|int|N|工时|6.1|
|status|string|Y|ACTIVE/INACTIVE|6.1|

### ServiceAddonCreateRequest

创建加项。

需求来源：v5.4 6.1

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|serviceItemId|string|Y|服务项|6.1|
|addonName|string|Y|名称|6.1|
|addonPrice|number|Y|价格|6.2|
|durationMin|int|N|工时|6.1|

### ChecklistRuleDTO

证据必拍/质检规则。

需求来源：v5.4 16.3/12.2.4

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|variantId|string|Y|规格ID|16.3|
|stage|string|Y|PRE_CHECK/POST_CHECK/PARKING/KEY_RETURN|16.3|
|requiredPhotos|int|Y|必拍数量|16.3|
|requiredAngles|array<string>|N|角度列表|16.3|
|requireVideo|boolean|N|是否要求视频|16.3|
|watermarkPolicy|string|N|水印策略|16.3|

### ChecklistRuleSetRequest

设置某规格的证据规则（覆盖）。

需求来源：v5.4 16.3

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|rules|array<ChecklistRuleDTO>|Y|规则列表|16.3|

### PriceQuoteRequest

报价请求。

需求来源：v5.4 6.2/12.1.3

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|storeId|string|Y|门店|6.2|
|siteId|string|N|站点|12.1.2|
|fulfillmentMode|string|Y|A/B/C|7|
|items|array<OrderItemRequest>|Y|服务项/加项列表|6.1|
|couponCode|string|N|优惠券（可选）|6.2|

### PriceQuoteResponse

报价响应（不落库或短期缓存）。下单时会固化 priceSnapshot。

需求来源：v5.4 6.2

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|quoteId|string|Y|报价ID（短期）|6.2|
|totalAmount|number|Y|总金额|6.2|
|discountAmount|number|N|优惠金额|6.2|
|payableAmount|number|Y|应付金额|6.2|
|pricingSource|string|Y|AGREEMENT/STORE/PLATFORM_GUIDE|6.2|
|breakdown|object|Y|明细：items/fees/taxes|6.2|
|expiresAt|string|Y|报价过期时间|6.2|

### PriceSnapshotDTO

价格快照（订单级，不可变）。

需求来源：v5.4 6/4.5

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|orderId|string|Y|订单ID|6|
|currency|string|Y|CNY|6|
|items|array<PriceSnapshotItemDTO>|Y|明细（含折扣）|6|
|totalAmount|number|Y|总金额|6|
|discountAmount|number|N|优惠|6|
|platformFeeAmount|number|N|平台服务费|4.5|
|taxAmount|number|N|税费|6|
|pricingSource|string|Y|来源|6.2|
|pricingTrace|object|N|解释信息|6.2|

### PriceSnapshotItemDTO

价格快照条目。

需求来源：v5.4 6

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|itemType|string|Y|SERVICE/ADDON|6|
|refId|string|N|引用ID|6|
|name|string|Y|名称快照|6|
|unitPrice|number|Y|单价|6|
|quantity|int|Y|数量|6|
|amount|number|Y|金额|6|
|discount|number|N|折扣|6.2|

## 7.7 车主与车辆 DTO

### CustomerDTO

车主档案（脱敏）。

需求来源：v5.4 12.1.1/16.4

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|customerId|string|Y|uuid|12.1.1|
|userId|string|Y|关联 user_account|12.1.1|
|name|string|N|姓名（可脱敏）|16.4|
|phoneMasked|string|Y|手机号脱敏|16.4|
|status|string|Y|ACTIVE/INACTIVE|12.1.1|

### VehicleDTO

车辆信息（脱敏）。

需求来源：v5.4 12.1.1

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|vehicleId|string|Y|uuid|12.1.1|
|customerId|string|Y|车主|12.1.1|
|plateNoMasked|string|Y|车牌脱敏|16.4|
|vinMasked|string|N|VIN 脱敏|16.4|
|brand|string|N|品牌|12.1.1|
|model|string|N|车型|12.1.1|
|color|string|N|颜色|12.1.1|

### VehicleDocumentDTO

车辆证件信息。

需求来源：v5.4 12.1.1/16.4

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|documentId|string|Y|uuid|12.1.1|
|vehicleId|string|Y|车辆|12.1.1|
|docType|string|Y|ID_CARD/DRIVER_LICENSE/VEHICLE_LICENSE|12.1.1|
|objectKey|string|Y|对象存储 key（仅特权可查看）|16.4|
|verifiedStatus|string|Y|PENDING/VERIFIED/REJECTED|12.1.1|
|verifiedAt|string|N|审核时间|12.1.1|

## 7.8 订单、钥匙托管与排队 DTO

### OrderItemRequest

下单明细项（请求）。

需求来源：v5.4 6/12.1.3

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|itemType|string|Y|SERVICE/ADDON|6|
|refId|string|Y|规格/加项ID|6|
|quantity|int|N|默认 1|6|

### OrderCreateRequest

创建订单请求（车主端/门店端）。共享柜体场景需校验 originCabinetId 与 storeId 存在有效绑定。

需求来源：v5.4 12.1.3/5.5.2/7

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|storeId|string|N|门店（若由柜体路由，可为空或仅为意向门店）|5.5.2|
|siteId|string|N|站点（可选）|12.1.2|
|originCabinetId|string|N|引流柜体（模式A/C必填）|5.5.3|
|vehicleId|string|Y|车辆|12.1.1|
|fulfillmentMode|string|Y|A_CABINET_DROP/B_VALET_TRANSFER/C_MOBILE_ONSITE|7/20.1|
|items|array<OrderItemRequest>|Y|服务项/加项|6|
|contactPhone|string|Y|联系手机号|12.1.3|
|notes|string|N|备注|12.1.3|
|preferredTimeWindow|object|N|期望时间窗（模式B/C）|7.3/7.4|
|parkingInfo|object|N|车辆位置/车位/门禁信息（模式B/C必填）|7.3/7.4|

### OrderCancelRequest

取消订单。

需求来源：v5.4 10.1

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|reason|string|Y|取消原因|10.1|

### OrderDTO

订单聚合视图（含归因与快照引用）。

需求来源：v5.4 7/10/5.5.3/12.1.4

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|orderId|string|Y|uuid|12.1.4|
|orderNo|string|Y|订单号|12.1.4|
|status|string|Y|订单状态|10.1|
|paymentStatus|string|Y|支付状态|12.1.5|
|fulfillmentMode|string|Y|履约模式|7|
|siteId|string|N|站点|12.1.2|
|originCabinetId|string|N|引流柜体|5.5.3|
|fulfillmentStoreId|string|N|履约门店|5.5.3|
|cabinetSlotId|string|N|存放格口|7.2|
|keyCustodyStatus|string|N|钥匙托管状态|10.3|
|totalAmount|number|Y|总金额（来自价格快照）|6|
|payableAmount|number|Y|应付金额|6|
|priceSnapshot|PriceSnapshotDTO|N|可选内嵌快照|6|
|agreementSnapshot|AgreementSnapshotDTO|N|可选内嵌协议快照|5.2|
|items|array<OrderItemDTO>|N|订单明细（可内嵌）|6|
|tasks|array<TaskDTO>|N|任务列表（可内嵌）|12.2.2|
|createdAt|string|Y|创建时间|12.1.4|
|updatedAt|string|Y|更新时间|12.1.4|

### OrderItemDTO

订单明细（响应）。

需求来源：v5.4 6

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|itemId|string|Y|uuid|6|
|itemType|string|Y|SERVICE/ADDON|6|
|name|string|Y|名称快照|6|
|unitPrice|number|Y|单价|6|
|quantity|int|Y|数量|6|
|amount|number|Y|金额|6|

### KeyDepositConfirmRequest

存钥匙确认（车主扫码开格口后放入钥匙并关门）。

需求来源：v5.4 7.2/16.2

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|cabinetId|string|Y|柜体|7.2|
|slotId|string|Y|格口|7.2|
|doorClosed|boolean|Y|车主确认已关门|7.2|
|parkingSpotNo|string|N|车位号（用于技师找车）|7.2|

### TechPickupKeyRequest

技师取钥匙（可能先签发 AccessGrant，再调用开柜）。

需求来源：v5.4 12.2.3/16.2

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|accessGrantToken|string|Y|按单授权 token 明文（短期）|16.2|
|cabinetId|string|Y|柜体|12.6|
|slotId|string|Y|格口|12.6|

### KeyReturnConfirmRequest

还钥匙确认（服务人员归还钥匙入柜）。

需求来源：v5.4 7/16.2

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|cabinetId|string|Y|柜体|7|
|slotId|string|Y|格口|7|
|parkingEvidenceId|string|N|停车位证据引用（完工后）|12.2.4|

### CustomerRetrieveKeyRequest

车主取回钥匙确认（车主扫码/授权开柜取回）。

需求来源：v5.4 7.2

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|accessGrantToken|string|N|可选：若车主端也走按单授权|16.2|
|cabinetId|string|Y|柜体|7.2|
|slotId|string|Y|格口|7.2|

### OrderRatingRequest

订单评价。

需求来源：v5.4 12.1.5

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|score|int|Y|1-5|12.1.5|
|comment|string|N|评价内容|12.1.5|
|tags|array<string>|N|标签|12.1.5|

### OrderRatingDTO

评价信息。

需求来源：v5.4 12.1.5

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|orderId|string|Y|订单|12.1.5|
|score|int|Y|1-5|12.1.5|
|comment|string|N|内容|12.1.5|
|tags|array<string>|N|标签|12.1.5|
|createdAt|string|Y|时间|12.1.5|

### QueueStatusDTO

站点排队状态（车主端查看附近门店排队情况）。

需求来源：v5.4 12.1.2/12.3.2

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|siteId|string|Y|站点|12.1.2|
|queueLength|int|Y|当前排队数|12.1.2|
|currentServingOrderNo|string|N|当前服务订单号（可脱敏）|12.3.2|
|estimatedWaitMinutes|int|N|预估等待时间|12.1.2|
|priceRange|object|N|示例价格区间|12.1.2|

### QueueEntryDTO

队列条目。

需求来源：v5.4 12.3.2

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|siteId|string|Y|站点|12.3.2|
|orderId|string|Y|订单|12.3.2|
|queueNo|int|Y|序号|12.3.2|
|status|string|Y|WAITING/CALLED/SKIPPED/CANCELLED|12.3.2|
|enqueuedAt|string|Y|入队时间|12.3.2|
|etaMinutes|int|N|预计等待|12.1.2|

### CallNextRequest

叫号下一个。

需求来源：v5.4 12.3.2

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|strategy|string|N|FIFO/PRIORITY（默认 FIFO）|12.3.2|

### SkipQueueRequest

跳过某订单（异常/车主不在等）。

需求来源：v5.4 12.3.2

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|reason|string|Y|原因|12.3.2|

## 7.9 柜体、格口与路由 DTO

### CabinetDTO

柜体信息。

需求来源：v5.4 12.6/12.3.1

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|cabinetId|string|Y|uuid|12.6|
|cabinetCode|string|Y|业务编码|12.6|
|hardwareSn|string|Y|硬件SN|12.6|
|siteId|string|Y|部署站点|12.6|
|cabinetModel|string|N|型号|12.6|
|slotCount|int|Y|格口数量|12.6|
|firmwareVersion|string|N|固件版本|12.6|
|deviceStatus|string|Y|ONLINE/OFFLINE/MAINTENANCE|12.6|
|lastHeartbeatAt|string|N|最后心跳|12.6|

### CabinetCreateRequest

创建/登记柜体（平台/代理运维）。

需求来源：v5.4 12.6

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|cabinetCode|string|Y|编码|12.6|
|hardwareSn|string|Y|硬件SN|12.6|
|siteId|string|Y|部署站点|12.6|
|cabinetModel|string|N|型号|12.6|
|slotCount|int|Y|格口数量|12.6|

### SlotDTO

格口信息。

需求来源：v5.4 12.6/10.5

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|slotId|string|Y|uuid|12.6|
|cabinetId|string|Y|柜体|12.6|
|slotNo|int|Y|格口号|12.6|
|status|string|Y|EMPTY/RESERVED/OCCUPIED/FAULT/LOCKED|10.5|
|currentOrderId|string|N|占用订单|7.2|
|reservedUntil|string|N|预占过期|17.2|
|doorState|string|N|OPEN/CLOSED/UNKNOWN|12.6|

### SlotFaultRequest

标记/解除格口故障。

需求来源：v5.4 18.2

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|fault|boolean|Y|true=标记故障，false=解除|18.2|
|faultCode|string|N|故障码|12.6|
|reason|string|N|原因|18.2|

### SlotReserveRequest

预占格口（给车主开门放钥匙前的占位）。

需求来源：v5.4 17.2/5.5.2

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|orderId|string|N|如已创建订单可带|7|
|ttlSeconds|int|Y|预占 TTL，默认 300|17.2|

### SlotReserveResponse

预占结果。

需求来源：v5.4 17.2

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|slotId|string|Y|预占格口|17.2|
|slotNo|int|Y|格口号|17.2|
|reservedUntil|string|Y|过期时间|17.2|

### SlotOpenRequest

开柜开格口请求（必须携带按单授权 token）。

需求来源：v5.4 16.2/14.3

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|accessGrantToken|string|Y|短期 token|16.2|
|requestId|string|N|客户端可传，服务端也可生成|15.1|

### EmergencyOpenRequest

应急开柜（高风险，需双人复核/强审计）。

需求来源：v5.4 16.2/18.4

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|reason|string|Y|应急原因|18.4|
|approverUserId|string|N|可选：二次审批人|18.4|

### SlotOpenResponse

开格口结果（指令已下发/ACK）。

需求来源：v5.4 14.3/15.1

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|requestId|string|Y|指令ID|15.1|
|status|string|Y|SENT/ACKED/FAILED|15.1|
|deviceAckAt|string|N|ACK 时间|15.1|
|errorCode|string|N|失败原因|12.6|

### CabinetProviderListDTO

某柜体可接单门店列表（按绑定策略计算）。

需求来源：v5.4 5.5.2

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|cabinetId|string|Y|柜体|5.5.2|
|providers|array<CabinetProviderDTO>|Y|门店列表|5.5.2|

### CabinetProviderDTO

可接单门店信息。

需求来源：v5.4 5.5.2

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|storeId|string|Y|门店|5.5.2|
|storeName|string|Y|名称|5.5.2|
|bindingType|string|Y|EXCLUSIVE/SHARED|5.5.1|
|priority|int|N|优先级|5.5.1|
|weight|int|N|权重|5.5.1|
|routePolicy|string|N|STATIC/WEIGHTED|5.5.1|
|isActive|boolean|Y|是否当前可接单|5.5.2|

## 7.10 任务与证据 DTO

### TaskDTO

任务视图。

需求来源：v5.4 8.3/12.2.2

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|taskId|string|Y|uuid|8.3|
|orderId|string|Y|订单|8.3|
|taskType|string|Y|WASH/VALET_PICKUP/VALET_DROPOFF/QC|8.3|
|status|string|Y|PENDING/ASSIGNED/IN_PROGRESS/DONE/CANCELLED|10.2|
|requiredAssignees|int|Y|需要人数（洗车默认2）|4.1|
|assignedTeamId|string|N|洗车组|8.2|
|assignees|array<TaskAssigneeDTO>|N|执行人列表|4.1|
|startAt|string|N|开始时间|8.3|
|endAt|string|N|结束时间|8.3|

### TaskAssigneeDTO

任务执行人。

需求来源：v5.4 4.1

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|staffId|string|Y|执行人|12.2.2|
|name|string|N|姓名|12.2.2|
|roleInTask|string|Y|LEADER/MEMBER/VALET|4.1|
|status|string|Y|ASSIGNED/ACCEPTED/DONE/REMOVED|10.2|
|acceptAt|string|N|接受时间|8.3|
|completeAt|string|N|完成时间|8.3|

### TaskCreateRequest

创建任务（通常由订单自动生成；也允许补充任务）。

需求来源：v5.4 8.3

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|taskType|string|Y|任务类型|8.3|
|requiredAssignees|int|N|默认按服务项/门店配置|4.1|
|notes|string|N|备注|8.3|

### TaskAssignRequest

派单/分配执行人或洗车组。

需求来源：v5.4 12.3.2

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|teamId|string|N|派给洗车组|8.2|
|staffIds|array<string>|N|直接指定人员（可多名）|4.1|

### TaskCompleteRequest

完成任务。

需求来源：v5.4 4.1/12.2.2

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|staffId|string|Y|提交完成的人员|4.1|
|resultNote|string|N|完成说明|12.2.2|
|evidenceIds|array<string>|N|相关证据引用|12.2.4|

### EvidenceCreateRequest

创建证据记录（返回 evidenceId，随后 presign 上传并 commit）。

需求来源：v5.4 16.3/12.2.4

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|stage|string|Y|PRE_CHECK/POST_CHECK/PARKING/KEY_RETURN|16.3|
|taskId|string|N|关联任务|12.2.4|
|note|string|N|说明|12.2.4|

### EvidenceDTO

证据记录（包含文件列表的只读视图）。

需求来源：v5.4 16.3

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|evidenceId|string|Y|uuid|16.3|
|orderId|string|Y|订单|16.3|
|taskId|string|N|任务|12.2.4|
|stage|string|Y|阶段|16.3|
|uploaderType|string|Y|STAFF/CUSTOMER|16.3|
|uploaderId|string|Y|上传者|16.3|
|files|array<EvidenceFileDTO>|N|文件列表|16.3|
|status|string|Y|ACTIVE/REDACTED|16.3|
|createdAt|string|Y|创建时间|16.3|

### EvidenceFileDTO

证据文件（脱敏元数据）。

需求来源：v5.4 16.3

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|fileId|string|Y|uuid|16.3|
|objectKey|string|Y|对象存储 key（需权限）|16.4|
|fileType|string|Y|IMAGE/VIDEO|16.3|
|mimeType|string|N|mime|16.3|
|sizeBytes|int|N|大小|16.3|
|sha256|string|N|hash|16.3|
|watermarkApplied|boolean|N|是否打水印|16.3|

### EvidencePresignRequest

获取预签名上传 URL。

需求来源：v5.4 16.3

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|fileType|string|Y|IMAGE/VIDEO|16.3|
|mimeType|string|Y|image/jpeg 等|16.3|
|sizeBytes|int|Y|文件大小|16.3|
|sha256|string|Y|客户端计算 hash|16.3|
|fileName|string|N|原始文件名|16.3|

### EvidencePresignResponse

预签名上传响应。

需求来源：v5.4 16.3

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|uploadUrl|string|Y|PUT/POST 上传 URL|16.3|
|objectKey|string|Y|对象 key|16.3|
|headers|object|N|需要携带的 header|16.3|
|expiresAt|string|Y|过期时间|16.3|

### EvidenceCommitRequest

提交上传完成（服务端校验并落库 EvidenceFile）。

需求来源：v5.4 16.3

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|objectKey|string|Y|对象 key|16.3|
|sha256|string|Y|hash|16.3|
|sizeBytes|int|Y|大小|16.3|
|metadata|object|N|拍摄信息（需脱敏）|16.3|

## 7.11 支付、退款、结算与对账 DTO

### PayCreateRequest

创建支付单（返回支付参数）。

需求来源：v5.4 12.1.5/16.6

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|orderId|string|Y|订单|12.1.5|
|channel|string|Y|WECHAT/ALIPAY/...|16.6|
|amount|number|Y|应付金额（后端校验与 priceSnapshot 一致）|16.6|
|payerCustomerId|string|Y|付款人|12.1.5|

### PayCreateResponse

支付参数响应。

需求来源：v5.4 12.1.5

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|intentId|string|Y|payment_intent id|16.6|
|provider|string|Y|渠道|16.6|
|payParams|object|Y|前端拉起支付所需参数|12.1.5|
|expiresAt|string|Y|过期时间|16.6|

### PayNotifyRequest

支付回调通知（第三方支付平台调用）。需要验签与幂等。

需求来源：v5.4 16.6

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|provider|string|Y|渠道|16.6|
|rawBody|string|Y|原始请求体（用于验签）|16.6|
|signature|string|N|签名（按渠道）|16.6|
|headers|object|N|必要 header（如 timestamp, nonce）|16.6|

### RefundCreateRequest

发起退款申请。

需求来源：v5.4 14(Refund)/16.6

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|orderId|string|Y|订单|14|
|amount|number|Y|退款金额|14|
|reasonCode|string|Y|原因码|14|
|reasonDesc|string|N|说明|14|

### RefundApproveRequest

审批通过退款。

需求来源：v5.4 16.6

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|comment|string|N|审批意见|16.6|

### RefundRejectRequest

拒绝退款。

需求来源：v5.4 16.6

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|comment|string|Y|拒绝原因|16.6|

### RefundDTO

退款单。

需求来源：v5.4 14(Refund)

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|refundId|string|Y|uuid|14|
|orderId|string|Y|订单|14|
|amount|number|Y|金额|14|
|reasonCode|string|Y|原因码|14|
|reasonDesc|string|N|说明|14|
|status|string|Y|REQUESTED/APPROVING/APPROVED/REJECTED/REFUNDED|14|
|requestedBy|string|Y|申请人|16.5|
|approvedBy|string|N|审批人|16.5|
|providerRefundId|string|N|渠道退款单号|16.6|
|createdAt|string|Y|创建时间|16.5|

### AgreementDTO

商业协议。

需求来源：v5.4 5.2/14(Settlement)

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|agreementId|string|Y|uuid|5.2|
|agreementCode|string|Y|编码|5.2|
|agreementName|string|Y|名称|5.2|
|partyAOrgId|string|Y|甲方|5.2|
|partyBOrgId|string|Y|乙方|5.2|
|pricingOwnerOrgId|string|Y|定价权主体|5.3|
|collectorOrgId|string|Y|收款主体|4.5|
|effectiveFrom|string|Y|生效时间|5.2|
|effectiveTo|string|N|失效时间|5.2|
|status|string|Y|DRAFT/ACTIVE/EXPIRED/SUSPENDED|5.2|
|settlementPolicy|object|Y|分润规则（json）|5.3/5.4|

### AgreementCreateRequest

创建商业协议。

需求来源：v5.4 5.2

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|agreementCode|string|Y|编码|5.2|
|agreementName|string|Y|名称|5.2|
|partyAOrgId|string|Y|甲方|5.2|
|partyBOrgId|string|Y|乙方|5.2|
|pricingOwnerOrgId|string|Y|定价权主体|5.3|
|collectorOrgId|string|Y|收款主体|4.5|
|effectiveFrom|string|Y|生效|5.2|
|effectiveTo|string|N|失效|5.2|
|settlementPolicy|object|Y|分润规则|5.3/5.4|

### AgreementSnapshotDTO

订单级商业协议快照（只读）。

需求来源：v5.4 4.5/5.2

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|orderId|string|Y|订单|5.2|
|agreementId|string|Y|协议|5.2|
|pricingOwnerOrgId|string|Y|定价权|5.3|
|collectorOrgId|string|Y|收款主体|4.5|
|settlementPolicySnapshot|object|Y|分润快照|5.3/5.4|
|createdAt|string|Y|创建时间|16.5|

### SettlementGenerateRequest

生成结算批次。

需求来源：v5.4 18.3

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|collectorOrgId|string|Y|收款主体（按主体分批）|4.5|
|periodStart|string|Y|周期开始|18.3|
|periodEnd|string|Y|周期结束|18.3|
|dryRun|boolean|N|仅计算不落库（可选）|18.3|

### SettlementBatchDTO

结算批次。

需求来源：v5.4 18.3

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|batchId|string|Y|uuid|18.3|
|batchNo|string|Y|批次号|18.3|
|collectorOrgId|string|Y|收款主体|4.5|
|periodStart|string|Y|开始|18.3|
|periodEnd|string|Y|结束|18.3|
|status|string|Y|GENERATED/CONFIRMED/PAID/VOID|18.3|
|totalAmount|number|Y|批次总额|18.3|
|generatedAt|string|Y|生成时间|18.3|

### SettlementLineDTO

结算明细（按订单、按受益方）。

需求来源：v5.4 5.3/5.4/5.5.3

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|lineId|string|Y|uuid|5.3|
|batchId|string|Y|批次|18.3|
|orderId|string|Y|订单|5.3|
|lineType|string|Y|PLATFORM/AGENT/STORE/PROPERTY/INVESTOR/STAFF|5.3/5.4|
|payeeOrgId|string|Y|受益方组织|5.3|
|amount|number|Y|金额|5.3|
|policySnapshot|object|N|计算依据快照|5.3/5.4|

### ReconcileImportRequest

导入对账文件（物业/渠道）。

需求来源：v5.4 16.6

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|collectorOrgId|string|Y|收款主体|16.6|
|periodStart|string|Y|周期开始|16.6|
|periodEnd|string|Y|周期结束|16.6|
|sourceFileObjectKey|string|Y|对象存储 key（先上传）|16.6|
|sourceType|string|Y|PROPERTY/BANK/PAY_PROVIDER|16.6|

### ReconcileImportDTO

对账导入记录。

需求来源：v5.4 16.6

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|importId|string|Y|uuid|16.6|
|collectorOrgId|string|Y|主体|16.6|
|periodStart|string|Y|开始|16.6|
|periodEnd|string|Y|结束|16.6|
|status|string|Y|IMPORTED/PROCESSING/DONE/FAILED|16.6|
|importedAt|string|Y|导入时间|16.6|

### ReconcileDiffDTO

对账差异。

需求来源：v5.4 16.6

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|diffId|string|Y|uuid|16.6|
|importId|string|Y|导入记录|16.6|
|orderId|string|N|关联订单|16.6|
|diffType|string|Y|AMOUNT/STATUS/MISSING|16.6|
|expected|object|Y|系统应有|16.6|
|actual|object|Y|对账单实际|16.6|
|status|string|Y|OPEN/RESOLVED/IGNORED|16.6|

### ReconcileTicketCreateRequest

基于差异创建处理工单。

需求来源：v5.4 16.6/14(Tickets)

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|diffId|string|Y|差异ID|16.6|
|priority|string|N|LOW/MEDIUM/HIGH|14|
|note|string|N|说明|14|

## 7.12 工单与通知 DTO

### TicketCreateRequest

创建工单（投诉/差异/设备）。

需求来源：v5.4 14(Tickets)/12.1.5

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|type|string|Y|COMPLAINT/DIFF/DEVICE/OTHER|14|
|orderId|string|N|关联订单|12.1.5|
|siteId|string|N|站点|14|
|title|string|Y|标题|14|
|content|string|Y|描述|14|
|priority|string|N|LOW/MEDIUM/HIGH|14|
|attachments|array<string>|N|对象存储 key 列表|14|

### TicketDTO

工单。

需求来源：v5.4 14(Tickets)

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|ticketId|string|Y|uuid|14|
|ticketNo|string|Y|工单号|14|
|type|string|Y|类型|14|
|orderId|string|N|订单|14|
|siteId|string|N|站点|14|
|status|string|Y|OPEN/IN_PROGRESS/RESOLVED/CLOSED|14|
|priority|string|N|优先级|14|
|title|string|Y|标题|14|
|content|string|Y|内容|14|
|ownerUserId|string|N|负责人|14|
|createdAt|string|Y|创建时间|16.5|

### TicketCommentCreateRequest

新增工单评论。

需求来源：v5.4 14(Tickets)

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|content|string|Y|评论内容|14|
|attachments|array<string>|N|附件对象 key|14|

### TicketCommentDTO

工单评论。

需求来源：v5.4 14(Tickets)

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|commentId|string|Y|uuid|14|
|ticketId|string|Y|工单|14|
|commenterId|string|Y|评论人|14|
|content|string|Y|内容|14|
|createdAt|string|Y|时间|16.5|

### NotifyTemplateDTO

通知模板。

需求来源：v5.4 20.2/15.3

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|templateCode|string|Y|模板编码|20.2|
|channel|string|Y|INAPP/PUSH/SMS/EMAIL|15.3|
|locale|string|N|zh-CN|20.2|
|titleTpl|string|N|标题模板|20.2|
|contentTpl|string|Y|内容模板|20.2|
|variables|array<string>|N|变量清单|20.2|
|status|string|Y|ACTIVE/INACTIVE|15.3|

### NotifyTemplateUpdateRequest

更新通知模板（运营配置）。

需求来源：v5.4 20.2

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|titleTpl|string|N|标题模板|20.2|
|contentTpl|string|Y|内容模板|20.2|
|status|string|N|ACTIVE/INACTIVE|20.2|

### NotifySendRequest

手动发送通知（平台/客服）。

需求来源：v5.4 15.3

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|templateCode|string|Y|模板|20.2|
|recipientType|string|Y|USER/STAFF/ORG|15.3|
|recipientId|string|Y|接收者|15.3|
|variables|object|N|变量值|20.2|
|channels|array<string>|N|指定渠道，默认模板渠道|15.3|

### NotifySendResponse

发送结果。

需求来源：v5.4 15.3

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|messageId|string|Y|消息ID|15.3|
|status|string|Y|QUEUED/SENT/FAILED|15.3|

## 7.13 设备资产与投资方 DTO

### CabinetOwnershipClaimRequest

投资方认领柜体（或平台登记）。

需求来源：v5.4 5.4

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|investorOrgId|string|Y|投资方组织|5.4|
|ownershipType|string|Y|OWN/LEASE/REVENUE_SHARE|5.4|
|shareMode|string|Y|PERCENT/FIXED_PER_ORDER/FIXED_PER_MONTH|5.4|
|shareValue|number|Y|比例/金额|5.4|
|activeFrom|string|Y|生效时间|5.4|
|activeTo|string|N|失效时间|5.4|

### CabinetOwnershipTransferRequest

投资认领转让/变更。

需求来源：v5.4 5.4

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|toInvestorOrgId|string|Y|新投资方组织|5.4|
|effectiveAt|string|Y|生效时间|5.4|
|reason|string|N|原因|5.4|

### CabinetOwnershipDTO

柜体投资认领信息。

需求来源：v5.4 5.4/12.7

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|cabinetId|string|Y|柜体|5.4|
|investorOrgId|string|Y|投资方组织|5.4|
|ownershipType|string|Y|OWN/LEASE/REVENUE_SHARE|5.4|
|shareMode|string|Y|PERCENT/FIXED_PER_ORDER/FIXED_PER_MONTH|5.4|
|shareValue|number|Y|比例/金额|5.4|
|activeFrom|string|Y|生效|5.4|
|activeTo|string|N|失效|5.4|
|status|string|Y|ACTIVE/ENDED/PENDING_APPROVAL|5.4|

### CabinetDeployRequest

投放/迁移柜体到站点。

需求来源：v5.4 5.4

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|siteId|string|Y|部署站点|12.6|
|startAt|string|Y|开始时间|5.4|
|endAt|string|N|结束时间|5.4|
|reason|string|N|原因（迁移/撤场）|5.4|

### CabinetDeploymentDTO

柜体投放部署记录。

需求来源：v5.4 5.4

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|deploymentId|string|Y|uuid|5.4|
|cabinetId|string|Y|柜体|5.4|
|siteId|string|Y|站点|12.6|
|startAt|string|Y|开始|5.4|
|endAt|string|N|结束|5.4|
|status|string|Y|ACTIVE/MOVED/REMOVED|5.4|

### CabinetStoreBindingCreateRequest

创建柜体-门店绑定（多对多）。

需求来源：v5.4 5.5

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|storeId|string|Y|门店|5.5|
|bindingType|string|Y|EXCLUSIVE/SHARED|5.5.1|
|priority|int|N|优先级|5.5.1|
|weight|int|N|权重|5.5.1|
|routePolicy|string|N|STATIC/WEIGHTED|5.5.1|
|timeWindows|object|N|时段|5.5.1|
|activeFrom|string|Y|生效时间|5.5|
|activeTo|string|N|失效时间|5.5|

### CabinetStoreBindingUpdateRequest

更新绑定（可能需要变更单/审批）。

需求来源：v5.4 5.5.4

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|bindingType|string|N|EXCLUSIVE/SHARED|5.5.1|
|priority|int|N|优先级|5.5.1|
|weight|int|N|权重|5.5.1|
|routePolicy|string|N|策略|5.5.1|
|timeWindows|object|N|时段|5.5.1|
|activeFrom|string|N|生效|5.5|
|activeTo|string|N|失效|5.5|
|status|string|N|ACTIVE/SUSPENDED/ENDED|5.5.4|

### CabinetStoreBindingDTO

柜体-门店绑定信息。

需求来源：v5.4 5.5

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|bindingId|string|Y|uuid|5.5|
|cabinetId|string|Y|柜体|5.5|
|storeId|string|Y|门店|5.5|
|bindingType|string|Y|EXCLUSIVE/SHARED|5.5.1|
|priority|int|N|优先级|5.5.1|
|weight|int|N|权重|5.5.1|
|routePolicy|string|N|STATIC/WEIGHTED|5.5.1|
|timeWindows|object|N|时段|5.5.1|
|activeFrom|string|Y|生效|5.5|
|activeTo|string|N|失效|5.5|
|status|string|Y|DRAFT/APPROVING/ACTIVE/SUSPENDED/ENDED|5.5.4|

### InvestorCabinetDTO

投资方视角的柜体列表项（不含门店敏感信息）。

需求来源：v5.4 12.7/16.4

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|cabinetId|string|Y|柜体|12.7|
|cabinetCode|string|Y|编码|12.7|
|siteId|string|Y|站点|12.7|
|deviceStatus|string|Y|在线状态|12.7|
|todayOrders|int|N|今日引流订单数（读模型）|12.7|
|todayRevenue|number|N|今日引流收入（读模型）|12.7|

### MetricsQuery

指标查询参数（时间范围等）。

需求来源：v5.4 12.7

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|start|string|Y|开始日期/时间|12.7|
|end|string|Y|结束日期/时间|12.7|
|granularity|string|N|DAY/HOUR|12.7|

### InvestorCabinetMetricsDTO

柜体指标（投资方）。

需求来源：v5.4 12.7

|字段|类型|必填|约束/说明|需求来源（v5.4）|
|---|---|---|---|---|
|cabinetId|string|Y|柜体|12.7|
|periodStart|string|Y|开始|12.7|
|periodEnd|string|Y|结束|12.7|
|orders|int|Y|引流订单数|12.7|
|gmv|number|Y|引流订单 GMV|12.7|
|investorShare|number|Y|投资方应得分成（按快照规则计算）|5.4|
|onlineRate|number|N|在线率（0-1）|12.7|
|faultCount|int|N|故障次数|12.7|
|series|array<object>|N|时间序列（granularity）|12.7|

# 8. 事件、消息与实时推送设计

本章定义 Outbox 领域事件、MQ（Kafka/RabbitMQ）主题、以及 SSE/WebSocket 推送通道。目标：在强一致交易基础上，实现跨模块解耦与最终一致。

需求来源：v5.4 15（事件与通知）、10.5（幂等）、12.6（设备事件）、18（运维告警）。

## 8.1 Outbox 表结构（必须）

|字段|约束/索引|说明|
|---|---|---|
|event_id (uuid)|pk|全局唯一|
|tenant_id (uuid)|idx|租户隔离|
|event_type (varchar(64))|idx|如 Order.Created|
|aggregate_type (varchar(64))||Order/Slot/Payment...|
|aggregate_id (uuid)|idx|聚合主键|
|payload (jsonb)||事件内容（脱敏）|
|occurred_at (timestamptz)|idx||
|published_at (timestamptz)|idx|发布成功时间|
|publish_status (varchar(16))|idx|NEW/PUBLISHED/FAILED|
|retry_count (int)|||
|trace_json (jsonb)||traceId/spanId/requestId|

## 8.2 事件封装规范（Event Envelope）

{
  "eventId": "uuid",
  "eventType": "Order.KeyDeposited",
  "tenantId": "uuid",
  "occurredAt": "2026-02-27T12:00:00Z",
  "aggregate": {"type": "Order", "id": "uuid"},
  "version": 1,
  "payload": {...},
  "trace": {"requestId":"...", "traceId":"...", "spanId":"..."}
}

注意：payload 必须遵守 PII 脱敏策略；P3 明文禁止进入事件流。

## 8.3 领域事件清单（必须）

|事件类型|说明|典型触发点|需求来源（v5.4）|
|---|---|---|---|
|Order.Created|订单创建成功|createOrder|7/12.1.3|
|Order.Cancelled|订单取消|cancelOrder|10.1|
|Slot.Reserved|格口预占|reserveSlot|17.2|
|Order.KeyDeposited|存钥匙完成|depositKeyConfirm|7.2/10.3|
|AccessGrant.Issued|签发按单授权|issueAccessGrant|16.2|
|Slot.OpenCommandSent|开柜指令已下发|openSlot|14.3/15.1|
|Slot.Opened|设备 ACK：格口已打开|device cmd_ack|14.3/15.1|
|Order.KeyPickedUp|技师取钥匙完成|technicianPickupKey|12.2.3|
|Task.Created|任务创建|auto/manual|8.3|
|Task.Assigned|任务派单|assignTask|12.3.2|
|Task.Completed|任务完成|completeTask|12.2.2|
|Order.KeyReturned|还钥匙完成|returnKeyConfirm|7|
|Order.Closed|订单完结|系统推进|10.1|
|Payment.IntentCreated|支付意图创建|pay.create|12.1.5|
|Payment.Succeeded|支付成功|pay.notify|16.6|
|Refund.Requested|退款申请|createRefund|14|
|Refund.Approved|退款通过|approveRefund|16.6|
|Settlement.BatchGenerated|结算批次生成|generateSettlementBatch|18.3|
|Reconcile.Imported|对账导入完成|importReconcileFile|16.6|
|Cabinet.HeartbeatMissed|设备心跳超时|监控规则|12.6/18|
|Cabinet.AlarmRaised|设备告警|device alarm|12.6|

## 8.4 通知触发点映射（建议）

通知（站内信/推送/短信）建议由 Notification 模块订阅事件并生成消息。示例映射：

|事件|接收方|模板/内容摘要|需求来源|
|---|---|---|---|
|Order.KeyDeposited|车主、门店|提示已存钥匙/排队中|v5.4 15.3/12.1.4|
|Task.Assigned|技师|新任务提醒|v5.4 12.2.2|
|Order.KeyReturned|车主|提示可取钥匙|v5.4 12.1.4|
|Payment.Succeeded|车主、门店财务|支付成功|v5.4 12.1.5|
|Cabinet.HeartbeatMissed|运维|设备离线告警|v5.4 18.2|

## 8.5 SSE/WebSocket 推送通道（建议）

- SSE：/api/v1/stream/orders/{orderId} —— 推送订单状态/任务进度（车主/门店端）
- SSE：/api/v1/stream/sites/{siteId}/queue —— 推送排队变更（门店端/大屏）
- WebSocket：/ws —— 更适合多主题订阅（门店后台）
- 推送消息体建议复用领域事件 envelope（脱敏后的 payload）
# 9. IoT（MQTT）接入与设备指令链路（实现级）

本章定义柜体与平台之间的 MQTT Topic、消息体、幂等与安全策略。目标：确保开柜指令可靠、可审计、可追溯，兼容弱网/离线补报。

需求来源：v5.4 12.6（弱网与安全）、14.3（MQTT）、16.2（开柜安全控制点）。

## 9.1 Topic 规划（建议）

|方向|Topic|说明|
|---|---|---|
|云→设备|beizhi/{tenantId}/{hardwareSn}/cmd/open_slot|下发开格口指令|
|云→设备|beizhi/{tenantId}/{hardwareSn}/cmd/ping|连通性探测|
|设备→云|beizhi/{tenantId}/{hardwareSn}/evt/heartbeat|心跳上报|
|设备→云|beizhi/{tenantId}/{hardwareSn}/evt/slot_state|格口状态变更上报|
|设备→云|beizhi/{tenantId}/{hardwareSn}/evt/cmd_ack|指令 ACK（含开门结果）|
|设备→云|beizhi/{tenantId}/{hardwareSn}/evt/alarm|告警（断电/撬门/故障）|

## 9.2 消息体规范（必须）

所有消息必须携带 requestId/eventId 以实现幂等；建议统一 envelope：

{
  "id": "uuid-or-string",          // requestId 或 eventId
  "ts": 1700000000000,            // 毫秒时间戳
  "tenantId": "uuid",
  "hardwareSn": "SN123",
  "type": "OPEN_SLOT|HEARTBEAT|CMD_ACK|SLOT_STATE|ALARM",
  "payload": {...},
  "sig": "HMAC-SHA256(base64)",   // 可选：消息签名
  "keyId": "uuid"                 // 可选：密钥版本
}

## 9.3 开格口指令（OPEN_SLOT）

// Topic: beizhi/{tenantId}/{hardwareSn}/cmd/open_slot
{
  "id": "req-uuid",
  "ts": 1700000000000,
  "type": "OPEN_SLOT",
  "payload": {
    "slotNo": 12,
    "grantNo": "G2026...",
    "expiresAt": "2026-02-27T12:05:00Z"
  },
  "sig": "..."
}

设备侧校验：1) ts 在容忍窗口内；2) sig 验签；3) grantNo 未使用/未过期；4) slotNo 合法；5) 执行开门并上报 CMD_ACK。

## 9.4 指令 ACK（CMD_ACK）

// Topic: .../evt/cmd_ack
{
  "id": "evt-uuid",
  "ts": 1700000001000,
  "type": "CMD_ACK",
  "payload": {
    "requestId": "req-uuid",
    "cmd": "OPEN_SLOT",
    "slotNo": 12,
    "result": "SUCCESS|FAIL",
    "errorCode": "DOOR_STUCK|TIMEOUT|UNAUTHORIZED",
    "doorState": "OPEN|CLOSED"
  }
}

服务端处理：以 requestId 幂等去重；写 device_event；更新 slot.door_state/status；若 SUCCESS 则将 access_grant 标记 USED，并写 key_custody_log/audit_log。

## 9.5 安全与 ACL（必须）

- 传输层：MQTT over TLS；禁用明文 1883（生产）。
- 鉴权：设备使用 clientId=hardwareSn；username=hardwareSn；password=动态密钥（轮换）或使用证书。
- ACL：每个设备只能订阅/发布自身 topic（beizhi/{tenantId}/{hardwareSn}/#）。
- 签名：建议 HMAC-SHA256，key 来自 device_secret；支持 keyId 轮换。
- 重放防护：ts + nonce；服务端/设备侧维护最近 N 个 id 去重缓存。
## 9.6 弱网/离线策略（必须）

- 设备离线：平台 openSlot 返回 status=SENT/FAILED；若离线则直接 FAILED 并提示运维。
- ACK 迟到：服务端按 requestId 幂等处理，并记录“迟到 ACK”审计。
- 设备事件补报：slot_state/heartbeat 允许批量补报；payload 中携带原始发生时间。
- 缓存队列：设备侧可短期缓存未上报事件（上限 N 条），避免断网丢失。
# 10. 安全、合规与运维可观测性（必须）

本章给出上线所需的安全与运维落地项清单（可直接转化为 DevOps 任务）。

需求来源：v5.4 16（安全合规）、17（性能与容量）、18（运营运维）。

## 10.1 安全基线（后端）

- 最小权限：所有接口必须配置 permission_key；默认 deny-all。
- 输入校验：使用 Bean Validation；对枚举/金额/时间窗做强校验。
- 输出脱敏：统一 JsonSerializer 对 phone/plate/vin 等字段脱敏；日志拦截器禁止输出 P3。
- 密钥管理：数据库加密字段使用 KMS（云）或 Vault；密钥轮换与审计。
- 依赖安全：启用 SCA（OWASP Dependency-Check/Snyk），按 CVE 阈值阻断发布。
- 防刷与限流：基于 Redis + 滑动窗口；对登录/发短信/开柜接口重点限流（需求来源：v5.4 16）。
## 10.2 审计与合规落地检查表

|项目|要求|需求来源|
|---|---|---|
|权限变更|role/user_role 变更必须写 audit_log|16.5|
|开柜/取还钥匙|access_grant + device_event + key_custody_log + audit_log 全链路齐全|16.2|
|证据链|不可删除；导出需权限；对象存储版本化|16.3|
|PII|P2/P3 字段加密/脱敏；访问留痕|16.4|
|资金|支付回调验签；对账差异闭环；关键操作双人复核（可选）|16.6|

## 10.3 可观测性（Metrics/Logs/Tracing）

- 指标（Prometheus）：HTTP QPS/RT/错误率；MQ 发布/消费延迟；MQTT 在线设备数；开柜成功率；排队长度；订单转化漏斗。
- 日志（Loki/ELK）：结构化 JSON；强制包含 tenantId、requestId、userId、orderId、cabinetId。
- 链路追踪（OpenTelemetry）：从 API → DB → MQ → MQTT gateway 全链路 trace；便于定位开柜失败。
## 10.4 运维告警（建议）

|告警|条件|建议等级/动作|
|---|---|---|
|Cabinet 离线|last_heartbeat_at 超过 2min（可配置）|P1 运维处理|
|开柜失败率升高|SlotOpenResponse FAILED / total > 阈值|P0 立即处理|
|排队异常积压|queueLength > 阈值 且持续 10min|P1 通知门店增派人员|
|支付回调失败|PAY_001 连续出现|P0 财务/风控|
|Outbox 堆积|NEW/FAILED 数量持续上升|P1 检查 MQ/消费者|

## 10.5 数据库迁移与发布策略（必须）

- 使用 Flyway/Liquibase 管理迁移脚本；禁止手工改库。
- 发布策略：蓝绿/滚动发布；数据库向前兼容（先加字段后改代码，再删字段）。
- 每次发布自动回归：关键路径 API + 开柜模拟 + 结算批次生成。
## 10.6 备份与灾备（建议）

- PostgreSQL：每日全量 + 每小时增量（WAL）备份；定期演练恢复。
- 对象存储：开启版本化；跨区域复制（可选）。
- Redis：作为缓存可重建；如用于幂等/限流，可启用 AOF 持久化（评估）。
# 11. 工程结构、开发规范与测试策略（建议）

本章给出可直接落地的代码工程结构与测试/CI 规范，确保可持续迭代与可运维。

## 11.1 代码工程结构（建议，Modular Monolith）

smartwash/
  smartwash-bootstrap/          # Spring Boot 启动模块
  smartwash-interface/          # Controller / DTO / WebSocket/SSE
  smartwash-application/        # UseCase/CommandHandler/QueryService
  smartwash-domain/             # 聚合根/实体/值对象/领域服务/事件
  smartwash-infrastructure/     # Repository 实现/外部适配器（MQTT/MQ/S3/支付）
  smartwash-migration/          # Flyway/Liquibase 脚本
  smartwash-test/               # 集成测试/契约测试

## 11.2 关键包与类命名规范（建议）

- 聚合根：OrderAggregate、CabinetAggregate、AgreementAggregate 等；聚合根持有不变量与状态机。
- 应用服务：CreateOrderUseCase、OpenSlotUseCase、GenerateSettlementBatchUseCase。
- 仓储接口：OrderRepository（domain 层），实现：JpaOrderRepository（infrastructure）。
- 领域事件：OrderCreatedEvent（domain），OutboxPublisher（infrastructure）。
- 反腐层：PaymentGateway、SmsGateway、ObjectStorageClient。
## 11.3 测试策略（必须）

- 单元测试：领域层状态机与不变量（Order/Slot/AccessGrant）必须覆盖。
- 集成测试：PostgreSQL+Redis+MQTT（Testcontainers）联测关键链路：下单→预占→开柜→ACK→取钥匙→完工→还钥匙→支付→结算。
- 契约测试：对外 API 使用 OpenAPI contract test；设备 MQTT 协议使用 JSON schema + 回放测试。
- 压测：对排队/开柜/支付回调进行并发压测；验证幂等与锁冲突率。
## 11.4 CI/CD（建议）

- CI：代码规范检查（Spotless/Checkstyle）、单元测试、集成测试、依赖安全扫描。
- CD：构建镜像、推送镜像仓库、灰度发布、自动回滚（健康检查失败）。
- 配置：使用 Spring Config + K8s Secret；敏感配置禁入 Git。
# 12. 需求追溯矩阵（v5.4 → 设计实现）

用于验收与范围控制：每个关键需求点在设计中的落点（模块/表/API/事件）。

|需求点|落地上下文/模块|主要表/对象|关键接口|关键事件|
|---|---|---|---|---|
|4.1 多人协作（2+）|Staff/Task|wash_team, task, task_assignee|assignTask/completeTask|Task.Assigned/Completed|
|4.2 代客泊车来源多样|Staff & Workforce|external_staff_assignment|createExternalAssignment|AuditLog|
|4.4 服务可扩展|Catalog & Pricing|service_item/variant/addon|createService/createVariant|CatalogChanged(可选)|
|4.5 商业模式拆分|Settlement|commercial_agreement/agreement_snapshot/settlement_line|createAgreement/generateSettlementBatch|Settlement.BatchGenerated|
|5.4 设备投资认领分成|Asset & Investment/Settlement|cabinet_ownership_claim/settlement_line|claimCabinetOwnership|Settlement.BatchGenerated|
|5.5 柜体-门店多对多绑定|Asset & Investment/Order|cabinet_store_binding + binding_snapshot|bindCabinetToStore/listCabinetProviders|Order.Created|
|7 模式 A/B/C|Order & KeyCustody|order/order_location/key_custody_log|createOrder/depositKeyConfirm|Order.KeyDeposited|
|10 状态机与幂等|Order/Slot/Idempotency|order/slot/idempotency_record|所有写接口|Outbox events|
|12.1 车主端|Order/Queue/Payment/Evidence|customer/vehicle/order/evidence|getOrder/listOrderEvidence/pay.create|Order/Payment events|
|12.2 技师端|Task/KeyCustody/Evidence|task/key_custody_log/evidence|listOrderTasks/technicianPickupKey|Task/Key events|
|12.3 门店端|Store/Queue/Task|store/site/order_queue|getSiteQueue/callNext|Queue events|
|12.4 代理端|Org/Reporting|organization/store/site|listStores/listSites|Reporting projections|
|12.5 平台端|All + Risk/Audit|audit_log/risk_event|listTenants/notify templates|Audit events|
|12.6 柜体与弱网|Cabinet & IoT|cabinet/slot/device_event|openSlot/reserveSlot|Slot.Opened/Cabinet.HeartbeatMissed|
|12.7 投资方端|Asset/Reporting|cabinet_ownership_claim/read models|investorListCabinets/metrics|Reporting projections|
|14 接口与 DTO|API Layer|N/A|第6/7章|N/A|
|15 事件与通知|Outbox/Notification|outbox/notify_message|notify.send|第8章事件清单|
|16 安全合规|Security/Risk/Audit|audit_log/risk_policy/access_grant|open-emergency/refund approve|Risk events|
|17 性能容量|Infra/Redis/MQ|redis keys/outbox|reserveSlot/queue|N/A|
|18 运维|Observability|metrics/logs|health endpoints(建议)|Cabinet alarms|

## 附录 A：推荐健康检查与运维接口（建议）

建议开放仅内网可访问的健康检查与运维接口：

- GET /actuator/health（Spring Boot Actuator）
- GET /actuator/metrics
- GET /api/v1/ops/cabinets/{cabinetId}/events/recent（查看设备最近事件）
- POST /api/v1/ops/orders/{orderId}/replay-events（谨慎：仅用于补偿/回放）
## 附录 B：Redis Key 设计（建议）

# 排队实时队列（按站点）
queue:site:{tenantId}:{siteId} = ZSET(orderId -> score=enqueueTime)

# 格口预占
slot:reserve:{tenantId}:{cabinetId}:{slotId} = orderId (TTL)

# 幂等
idempo:{tenantId}:{key} = response (TTL)

# 限流
rate:{tenantId}:{scope}:{subjectId} = counter (TTL)

## 附录 C：数据保留与清理策略（建议）

- outbox：成功发布后保留 30~90 天，便于重放；超过期限归档/清理。
- device_event：保留 90~180 天；关键 ACK 事件可更长。
- audit_log/key_custody_log：建议保留 2~5 年（按合规要求）。
- evidence_file：按纠纷/保险需要设置保留期；对象存储生命周期管理。
