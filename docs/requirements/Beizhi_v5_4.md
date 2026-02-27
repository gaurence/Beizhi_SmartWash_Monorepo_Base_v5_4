备至代客洗车智洗平台
最终版需求说明书 v5.4

（融合 v2/v3 需求并吸收 v4 运营增强，并补齐多履约模式、多方收款与分润、多人协作、DDD 模型、接口与数据对象）

版本: v5.4
日期: 2026-02-27
所属: 备至科技（智洗平台产品与研发）

# 0. 文档控制（Document Control）

本文档用于指导“备至代客洗车智洗平台”产品、研发、测试、交付与运维。适用于平台总部、区域代理、加盟门店、物业方及车主/服务人员等多角色的统一需求口径。

## 0.1 版本记录（Change Log）

|版本|日期|作者/负责人|范围|说明|
|---|---|---|---|---|
|v1.0|2026-02-25|备至产品|基础版|四步闭环、角色与子系统、核心流程与异常兜底|
|v2.0|2026-02-25|备至产品|强化版|状态机增强、容量控制/预约锁定、风控与证据链、弱网与离线兜底、分润与对账细化|
|v3.0|2026-02-25|备至产品|详细设计版|需求拆分与页面深化、接口与DTO矩阵（API/DTO Matrix / DTO Matrix）|
|v4.0|2026-02-25|备至产品|增强版|融入真实运营事实：多人协作(2+)、代泊人员多来源、多履约模式、服务可扩展、定价权与收款方解耦、物业代收二次分配|
|v5.1|2026-02-25|备至产品|终版（优化）|明确模式C上门洗车仍基于钥匙柜托管闭环（存入-取出-归还-取回状态同步）；保持不纳入AI验车与AR导航|
|v5.2|2026-02-25|备至产品|终版（可研发）|对章节13-18与20进行研发级细化：DDD模型/关系、API/DTO字段级建议、事件与通知体系、风控审计合规、NFR、运维SOP、状态枚举与通知模板|
|v5.3|2026-02-27|备至产品|终版（补充）|新增设备投资方（钥匙柜投资认领）模型与角色：支持平台/代理/门店/第三方投资；柜体所有权可见性与分成权益；同步更新商业协议、DDD、API/DTO、事件通知、风控审计。|
|v5.4|2026-02-27|备至产品|终版（补充）|补充“柜体可共享多门店”商业与运营需求：钥匙柜作为引流入口在区域代理配置下支持绑定多个门店（多对多/一对一）；新增柜体-门店绑定与路由策略、订单归因与快照固化；同步更新DDD模型关系、API/DTO、事件通知、风控审计、状态枚举与通知模板。|

## 0.2 阅读指引

- 业务/运营侧：重点阅读第 4-9 章（运营事实、商业协议、流程、角色与权限）。
- 研发侧：重点阅读第 10-15 章（状态机、架构、子系统需求、DDD 模型、API/DTO、事件与通知）。
- 测试/交付侧：重点阅读第 16-19 章（风控审计、非功能、运维、里程碑与验收清单）。
# 1. 背景与目标

备至智洗平台通过“智能钥匙柜 + 代客洗车履约 + 多租户平台化运营”，在小区/园区/商业综合体等场景提供可规模化的洗车服务。平台核心价值是：让车主在不等待的情况下完成洗车，让门店与代理在可控风险下提升周转与效率，并支持多方利益相关者的分润与对账。

## 1.1 目标与价值主张

- 车主：自助存取钥匙、透明排队与进度、可查看证据链（验车/还车）、可评价与投诉。
- 门店：统一排队/派单/多人协作、可配置代泊人员来源、可配置服务与定价、证据链与风控闭环。
- 代理/平台：可复制的招商与运维体系、设备在线与告警、结算与对账自动化、审计可追溯。
- 物业/园区（可选）：参与协同与分润，支持“物业代收再分配”等特殊商业要求。
## 1.2 成功指标（建议）

- 履约效率：平均等待时长、单车总周转时间、多人协作工时达标率。
- 服务质量：差评率/投诉率、证据链合规率、返工率。
- 运营增长：站点覆盖数、活跃门店/技师数、订单量、复购率。
- 设备运维：在线率、故障MTTR、应急开柜次数与合规率。
- 财务：对账差异率、结算出错率、退款处理时效。
# 2. 范围与边界

## 2.1 In-Scope（v5范围）

- 智能钥匙柜存取钥匙、格口管理、设备运维与 IoT 通信（含弱网/离线兜底）。
- 三种履约模式：A 车主自送入柜；B 代泊取送至洗车点；C 上门洗车。
- 服务商品与定价：普通洗车/精洗/打蜡等可扩展服务、增值项、差异定价。
- 多人协作：每单 2+ 技师（可配置），门店可配置洗车组/班组。
- 多方角色与多租户：平台/代理/加盟/门店/技师/代泊/物业/车主等。
- 商业模式：年费/设备费/按单抽佣/按单摊销运维费；定价权主体、收款方主体、分润与对账。
- 证据链：验车与还车拍照/视频、水印、审计日志、纠纷处理流程（不含 AI 识别）。
- 结算中心：规则引擎、月结/日结（可配置）、对账与差异工单、退款与审批流。
## 2.2 Out-of-Scope（明确不纳入）

- 第三阶段智能化：AI 验车算法、AR 室内导航（本版不实现）。
- 洗车硬件设备（高压水枪等）本身的采购与维护流程（可通过工单扩展）。
- 与车厂/车机深度联动（如车机钥匙授权等），如需另立项目。
## 2.3 关键假设与前置条件

- 门店具备洗车场地或合作洗车点；上门模式需具备移动洗车资源，且服务覆盖区域需部署车辆附近的智能钥匙柜用于钥匙托管与监管。
- 智能钥匙柜支持远程开格口、格口状态上报；若支持 BLE 离线开锁，应具备安全令牌机制。
- 支付渠道与短信/推送能力可对接第三方服务；如物业代收，需明确资金与开票责任主体。
# 3. 术语与核心概念

- 站点（Site）：具体服务发生的物理地点（小区/园区/商场），包含一个或多个智能钥匙柜。
- 门店（Store）：运营实体，可管理多个站点/柜体/人员/服务与定价。
- 智能钥匙柜（Cabinet）：具备多个格口（Slot），支持扫码/远程/离线（可选）开锁与日志上报。
- 订单（Order）：车主一次服务请求，包含服务项、履约模式、价格快照、任务与证据链。
- 任务（Task）：履约拆分单元（取钥匙/代泊取车/预检/洗车/完工/还钥匙等），支持多人协作。
- 钥匙托管（Key Custody）：钥匙在柜体/门店/人员之间的托管状态与责任归属。
- 车辆托管（Vehicle Custody）：车辆在代泊/门店/车主之间的托管状态与责任归属。
- 商业协议（Commercial Agreement）：定义定价权主体、收款方主体、分润规则、运维费扣取等。
# 4. 真实运营事实与关键业务规则（融入）

## 4.1 多人协作（2+ 技师）

- 默认每单需要 2 位技师，门店可按服务类型配置需要人数（例如：精洗/打蜡可需要更多人）。
- 门店可维护“洗车组/班组”，并配置成员、班次、技能标签；订单派单可按班组分派。
- 任务完成策略可配置：全部成员确认完成 / 达到人数阈值 / 组长确认。
## 4.2 代客泊车人员来源多样

- 代泊人员可由：技师兼任、门店专职代泊、园区物业人员担任。
- 门店管理员可添加/授权代泊人员，并配置其可执行范围（站点、时间窗、是否允许开柜/是否仅驾驶）。
- 高风险动作（开柜取钥匙、应急开柜）必须按单授权 + 强审计；物业人员默认不应拥有高风险权限。
## 4.3 多履约模式并存

- 模式A：车主到站点扫码存钥匙，门店取钥匙洗车，完工后停回指定车位并还钥匙入柜。
- 模式B：代泊人员上门/园区内取车，将车开至洗车点（可能跨站点），完工后再送回并还钥匙。
- 模式C：上门洗车（移动履约），仍基于钥匙柜托管：车主将钥匙存入车辆附近/指定钥匙柜，服务人员到柜取钥匙后在车辆所在车位/附近施工，完工后钥匙回柜，全程状态与证据链同步给车主。
## 4.4 服务类型可扩展

- 支持普通洗车、精洗、打蜡等服务档位，并支持可扩展的定制服务内容（Service Catalog）。
- 每个服务可配置：价格、预计时长、建议人数、可用履约模式、必拍清单、质检规则。
## 4.5 商业模式与权责拆分（定价权/收款方/收益分配）

- 备至可通过：设备销售、年度平台/设备运维费、按单抽佣、或按单摊销运维费获取收益。
- “谁买设备/谁付运维费”通常拥有定价权与主要收益分配权，但收款账户未必属于该主体。
- 收款方主体可能是：备至、运营合伙方、区域代理、洗车店、或园区物业集团（物业代收再分配）。
- 平台必须支持“定价权主体”与“收款方主体”解耦，并在下单时固化协议与价格快照。
- 区域代理合作伙伴以线下拓展与运营管理为主，不一定承担设备购买或运维付费；其收益更多来自招商/运营分成与区域管理服务。
- 智能钥匙柜的投资方（设备投资认领方）可为：备至科技、区域代理合作伙伴、加盟商/门店、或第三方设备投资方；同一投资方可认领多台柜体并投放到不同门店/园区使用。
- 设备投资认领方对其所认领柜体拥有：该柜体资产与运营数据的可见权（设备健康、格口使用、订单量/收入等脱敏数据）、分成权益、以及（可选）调拨/撤场/转让等资产处置权；但不应拥有门店日常调度与车主隐私数据访问权，必须通过权限与脱敏策略严格隔离。
## 4.6 柜体作为引流入口可共享多门店（新增）

- 智能钥匙柜不仅是“钥匙托管设备”，也是站点的订单引流入口（Traffic Hub）：车主扫码/存钥匙后形成订单流入履约系统。
- 在区域代理/平台的配置下，同一台钥匙柜可向 1..N 家洗车门店开放接单（共享柜体），也可配置为仅服务单一门店（独占柜体）。因此柜体与门店关系需支持：一对一、以及多对多。
- 共享柜体必须具备“门店选择/路由”能力：可由车主在下单/扫码时选择履约门店（展示价格、排队、评分），也可按代理配置自动分配（按权重/按队列/按时段独占等）。订单需固化最终履约门店（fulfillment_store_id）。
- 收益归因与分成：每笔通过某柜体产生的订单必须固化 origin_cabinet_id，并固化柜体资产归属与分成规则快照；无论该柜体绑定多少门店，设备投资方分成均按该柜体归因口径计算（与履约门店、代理、物业分润并行）。
- 共享绑定的变更（新增/停用门店、权重与时段、独占/共享切换）属于高风险配置，必须：审批（可选）、强审计、可回溯历史与生效时间窗；并对受影响门店/投资方发出变更通知。
# 5. 组织、租户与商业协议模型

## 5.1 组织层级（多租户）

- 平台（Platform）：全局管理与监管。
- 区域代理（Agent）：管理辖区加盟门店与站点运维。
- 加盟方/门店（Franchise/Store）：管理站点、柜体、人员、服务与定价、结算。
- 物业集团/园区（Property，选配）：参与协同与分润或代收。
## 5.2 商业协议（Commercial Agreement）

商业协议用于描述：定价权主体（Price Authority）、收款方主体（Collector/Settlement Account）、各利益相关方分润规则、平台运维费/抽佣策略、以及发票主体与税务口径等。建议以“协议模板 + 生效版本 + 订单快照”的方式实现。

## 5.3 定价权与收益分配权规则（可配置）

- 设备所有权（DeviceOwner）或运维费承担方（MaintenancePayer）默认拥有定价权；也支持协议指定。
- 若物业集团要求代收：订单收款进入物业账户，但定价权仍可属于设备方/门店；分润按协议再分配。
- 平台需支持按订单维度的“规则快照”，避免协议变更影响历史订单结算。
## 5.4 设备资产投资认领与分成权益（新增）

为适配“区域代理不一定买设备、设备可由第三方投资并享受分成”的真实商业模式，平台需引入设备资产投资/认领模型（Cabinet Investment & Ownership）。该模型与商业协议（Commercial Agreement）联动：订单在生成价格快照与协议快照时，同时固化柜体资产归属与设备投资分成规则快照。

关键定义：

- 设备投资方（Device Investor）：认领/投资某台智能钥匙柜资产的主体（Organization），可为平台/代理/门店/第三方。
- 设备资产所有权（CabinetOwner/AssetOwner）：柜体的资产归属主体；默认与设备投资方一致，但允许“代持/托管/融资租赁”等场景（可选）。
- 设备投放/部署（Cabinet Deployment）：某台柜体在某时段部署到某个小区/园区站点（物理位置）用于运营；部署变更（调拨/撤场/迁移）须留痕与可追溯。
- 柜体-门店运营绑定（Cabinet-Store Binding）：柜体作为引流入口，可在同一生效时段绑定 1..N 家门店提供履约服务；支持独占/共享/按时段独占与权重分配；绑定由区域代理协同设备投资方/门店配置（或审批），并需强审计与订单快照固化。
- 设备投资分成（Device Investor Share）：按订单或按周期从可分配收入中切分给设备投资方的份额（比例/固定额/阶梯/封顶等规则），与代理/门店/物业分润并列为 Stakeholder Split。
- 数据可见性边界：设备投资方可查看其柜体的健康与使用/收益数据（脱敏），但不得访问车主身份信息、车辆证件、人脸等敏感数据；订单明细仅展示最小必要字段（order_no、金额、时间、状态、站点），并支持进一步脱敏配置。
实现建议：引入 CabinetOwnership（资产归属/投资认领历史）与 CabinetDeploymentHistory（投放部署历史），并新增 CabinetStoreBinding（柜体-门店运营绑定，支持多对多/一对一与生效时间窗/权重）。订单在创建与存钥匙确认时必须固化 origin_cabinet_id、fulfillment_store_id，以及 ownership_snapshot_id/binding_snapshot_id（或等价快照字段），以便后续分润与对账按快照可重跑；柜体认领/转让/部署/绑定变更均需强审计（可选审批）。

## 5.5 柜体-门店多对多绑定与订单引流归因（新增）

在“共享柜体”模式下，智能钥匙柜位于某个站点（物理位置），但可同时向多个洗车门店开放接单。平台需将“柜体物理部署”与“柜体运营门店绑定/路由”解耦建模，以满足多方协商的真实运营方式。

### 5.5.1 关系与核心字段（建议）

- Cabinet（柜体）：物理资产，归属某站点 site_id（可迁移，记录 DeploymentHistory）。
- Store（门店）：提供履约能力与人员/班组/定价。一个门店可服务多个站点；一个站点也可被多个门店服务（可选，取决于绑定）。
- CabinetStoreBinding（柜体-门店绑定）：连接 Cabinet 与 Store 的关联实体（多对多）。建议字段：binding_id、cabinet_id、store_id、status、binding_type（EXCLUSIVE/SHARED/TIME_SLICED）、priority、weight、effective_from、effective_to、created_by、approved_by（可选）、remark。
- CabinetRoutingPolicy（路由策略，可作为 binding 的配置或独立实体）：selection_mode（CUSTOMER_SELECT/AUTO_WEIGHTED/AUTO_LOWEST_QUEUE/AUTO_BY_TIME）、fallback_store_id、max_orders_per_day（可选）、pause_on_fault（建议默认开启）。
### 5.5.2 下单/扫码交互（建议口径）

- 方式1（推荐，用户选择门店）：用户在 APP 看到“某柜体可服务的门店列表”（价格/排队/评分），选择门店与服务档位后创建订单；扫码开柜存钥匙时校验该柜体与所选门店存在 ACTIVE 绑定。
- 方式2（自动分配）：若代理配置 selection_mode=AUTO_*，则用户只选择服务档位/模式，系统根据绑定权重/队列/时段规则自动选定 fulfillment_store_id，并在订单中展示；需要提供“为什么被分配到该门店”的解释字段（可选，便于客服与争议处理）。
### 5.5.3 订单归因与结算口径（必须）

每笔通过钥匙柜触发的订单必须写入 origin_cabinet_id；同时固化 fulfillment_store_id（最终履约门店）与 binding_snapshot（绑定规则/权重/生效窗口快照）。设备投资方分成以 origin_cabinet_id 为归因口径计算；门店与代理/物业分润以订单快照（AgreementSnapshot + BindingSnapshot）为准，可重跑对账。

### 5.5.4 变更、审批与审计（必须）

绑定变更（新增/停用门店、权重调整、独占/共享切换、时段调整）属于高风险配置：必须记录 effective_from/to、变更原因、操作者与审批人（如启用审批），并写入 AuditLog；对受影响门店/投资方发出变更通知。

# 6. 服务商品、可扩展定制与定价体系

## 6.1 服务商品模型（建议）

- ServiceCatalog：门店/平台级服务目录（可继承与覆盖）。
- ServiceItem：服务项（例如：外观洗车、精洗、打蜡）。
- Variant（档位/规格）：同一服务项的不同档位（普通/加急/深度），包含定价与工时/人数建议。
- Addon（增值项）：内饰清洁、发动机舱清洁等可叠加项目。
## 6.2 定价来源与优先级

- 优先级建议：协议指定价格（按主体） > 门店自定义价格 > 平台建议价 > 默认基准价。
- 支持按车辆类型（轿车/SUV/MPV）、时间段（高峰/低峰）、履约模式（上门/代泊）差异定价。
- 支持优惠：门店券/平台券、会员折扣（可选）。
# 7. 履约模式与端到端流程

## 7.1 共性流程骨架（适用于所有模式）

1. 创建订单：选择站点/服务档位/履约模式/时间窗（可选）→ 生成价格快照。
1. 派单与执行：按门店规则派单到班组/人员，生成任务清单与 SLA。
1. 证据链：在关键节点采集照片/视频/定位/时间戳，写入证据链。
1. 完工交付：车辆归位/钥匙归还/完成确认→ 支付/结算/评价。
1. 异常兜底：无法取车/无法开柜/车辆故障/纠纷等 → 工单与审批流。
## 7.2 模式A：车主自送钥匙入柜

- 车主到站点：如该柜体绑定多门店，先在 APP 选择履约门店（或系统按路由规则自动分配）→ 扫码→ 格口打开→ 放入钥匙→ 关门确认→ 订单进入排队（归属到 fulfillment_store_id）。
- 轮到该车：服务人员/代泊扫码取钥匙（按单授权）→ 取车→ 预检留证→ 开始洗车。
- 完工：车辆停回指定车位（或用户指定车位）→ 车位拍照→ 还钥匙入柜→ 通知车主取钥匙。
- 车主取回：扫码取钥匙→ 关门确认→ 订单完成。
## 7.3 模式B：代泊取送至洗车点（可能跨站点）

- 派单：生成代泊任务（取车）与洗车任务（施工），可由不同人员承担。
- 取车：代泊人员到车位取车（可先取钥匙/或车主交接）→ 预检留证→ 驶往洗车点。
- 施工：洗车点多人协作完成服务→ 完工留证。
- 送回：代泊人员将车送回指定车位→ 车位拍照→ 钥匙归还（入柜/交接给车主）。
## 7.4 模式C：上门洗车（移动履约）

- 交接：若车辆附近钥匙柜绑定多门店，用户需选择提供上门履约的门店（或系统自动分配）后扫码存钥匙（预占格口），并填写车位/门禁备注（脱敏展示）。
- 派单：生成上门施工任务（可多人协作），并绑定取钥匙柜体/格口与有效期。
- 取钥匙与找车：服务人员到柜扫码开格口取钥匙→按车位信息找到车辆→预检留证→开始施工。
- 施工与完工：在车辆所在车位/附近完成洗车与增值服务→完工留证（照片/视频、水印）。
- 钥匙归还与结束：将钥匙放回同柜或指定柜→关门确认→状态同步；可选用户确认后关单。
# 8. 任务协作、人员与岗位模型

## 8.1 人员类型（StaffType）

- Technician：洗车技师（可多人协作）。
- ValetDriver：代客泊车人员（可由技师/门店员工/物业人员担任）。
- Dispatcher：门店调度/排队管理员。
- QA/CS：门店质检/客服。
## 8.2 洗车组（WashTeam）

- WashTeam 支持：名称、站点范围、默认人数、组长、成员列表、班次、技能标签。
- 派单策略：按组派单/按人派单/抢单；可配置组员上限与并发任务数。
## 8.3 任务模型（Task）

- Task 类型建议：KEY_PICKUP（取钥匙）、VEHICLE_PICKUP（取车）、PRECHECK（预检）、WASH（洗车）、DELIVERY（送回）、KEY_RETURN（还钥匙）、ON_SITE（上门）。
- 说明：模式C上门洗车同样通过钥匙柜完成 KEY_PICKUP/KEY_RETURN 交接；通常不需要 VEHICLE_PICKUP/DELIVERY（如需短距挪车可启用并强化审计）。
- Task 支持多人：TaskAssignee 记录多名执行人；可配置 required_count 与完成策略。
- 跨组织协作：允许将 ValetDriver 绑定为物业人员，但权限受限且强审计。
# 9. 角色类别与角色功能（v5 完整版）

角色分为平台总部、区域代理、加盟/门店、执行人员、车主用户、物业集团，以及系统对接账号。平台采用 RBAC 管模块与动作权限，并对高风险动作采用 ABAC 按单授权与审批流。

## 9.1 平台总部角色

- PLATFORM_SUPER_ADMIN：租户与权限顶层、系统安全基线、最高风险操作授权。
- PLATFORM_ADMIN：全局配置、全网门店/设备/订单监管、审计与合规。
- PLATFORM_OPS：运营大盘、异常治理、质量抽检与整改跟踪。
- PLATFORM_IOT_OPS：设备在线/告警/OTA/日志回放、应急开柜流程。
- PLATFORM_FINANCE：抽佣与运维费策略、结算批次、对账差异、退款终审（可选）。
- PLATFORM_CS：客服工单、纠纷处理与理赔协助。
## 9.2 区域代理角色

- AGENT_ADMIN：辖区门店生命周期、设备运维协同、区域经营看板、代理分润与对账。
- AGENT_OPS/AGENT_FINANCE（可拆分）：运营督导与财务对账。
## 9.3 加盟/门店角色

- FRANCHISE_ADMIN：多门店服务目录继承、人员权限、经营分析。
- STORE_ADMIN：本站点排队与派单、人员与班组、设备与格口、服务与定价、异常处理。
- STORE_DISPATCHER：仅调度派单与队列管理（不含财务高权限）。
- STORE_FINANCE：退款初审、结算对账、导出与开票协同。
- STORE_QA/STORE_CS（可选）：质检抽检、门店侧投诉协同。
## 9.4 执行人员与车主/物业

- TECHNICIAN：按单授权执行洗车任务、证据链采集、状态流转。
- VALET_DRIVER：按单授权执行取送车任务、车位拍照、钥匙交接记录（权限默认更低）。
- CUSTOMER：下单、存/取钥匙、查看进度与证据链、支付与评价。
- PROPERTY_ADMIN/PROPERTY_FINANCE（可选）：协同工单与分润/代收对账（脱敏）。
- 系统对接账号：支付/短信/推送/发票/保险/IoT 设备身份等。
## 9.5 设备投资方角色（新增）

设备投资方（Device Investor）是认领/投资智能钥匙柜资产的主体（Organization）。其核心诉求是：对所投柜体的运营数据可见、按期获得分成对账与结算，并在必要时发起资产调拨/撤场/转让流程。设备投资方可同时也是平台/区域代理/门店主体，也可能是独立第三方。

- DEVICE_INVESTOR_ADMIN：查看所投柜体资产列表、物理投放站点、绑定门店清单（共享/独占/时段）、设备健康与格口使用、订单量/收入等脱敏运营数据；可发起柜体认领/转让/部署（投放/撤场/迁移）申请（如协议允许），并可协同或审批柜体-门店绑定变更（如启用审批）；可查看设备分成规则与结算批次（只读或按权限可编辑）。
- DEVICE_INVESTOR_FINANCE：查看分成明细、对账差异、结算单、收款账户信息；发起对账申诉；确认收款与开票协同（如需要）。
- 数据边界（必须）：不得访问车主身份信息、车辆证件/人脸、精确位置轨迹等敏感数据；订单维度仅展示最小必要字段，支持平台侧脱敏配置（如隐藏车牌、手机号、具体车位等）。
# 10. 状态机与一致性/幂等

## 10.1 订单状态（OrderStatus）

- DRAFT（草稿/待确认）→ CREATED（已创建）→ QUEUED（排队中）→ IN_SERVICE（服务中）→ COMPLETED（已完成）/CANCELLED（已取消）
- 异常分支：SUSPENDED（暂停/异常处理中）→ RESOLVED（异常已处理）→ 回到 IN_SERVICE 或 CLOSED（强制关单）
- 支付相关：UNPAID/PAID/REFUNDED 可作为 PaymentStatus 或订单子状态管理。
## 10.2 任务状态（TaskStatus）

- PENDING → ASSIGNED → IN_PROGRESS → DONE
- 异常：FAILED/ESCALATED（升级工单）→ 重新派单或取消
- 多人协作：DONE 条件由完成策略决定（全员/阈值/组长确认）。
## 10.3 钥匙托管状态（KeyCustodyStatus）

- WITH_CUSTOMER（车主）/IN_CABINET（柜内）/WITH_STAFF（人员）/IN_STORE_SAFE（门店保管）
- 关键规则：任何“从柜内取出”必须绑定订单与格口、具备短期授权令牌，且写入审计日志。
## 10.4 车辆托管状态（VehicleCustodyStatus）

- AT_PARKING（原车位）/IN_TRANSIT（行驶中）/AT_WASH_SITE（洗车点）/RETURNED（已送回）
- 与代泊任务绑定：取车、送回、车位拍照、里程碑时间戳。
## 10.5 幂等与一致性（必须）

- 所有关键写操作（开柜、取钥匙、状态流转、支付回调、结算生成）必须支持幂等键（Idempotency-Key 或 request_id）。
- 设备指令采用 request_id + ACK + 超时重试；服务端需防止重复 ACK 导致重复流转。
- 建议使用事务 Outbox 保障“数据库写入 + 事件发布”一致性。
# 11. 系统总体架构与子系统划分

## 11.1 子系统清单

- 用户端 APP/小程序（Customer App）
- 服务人员端 APP（Technician/Valet App）
- 门店/加盟方管理后台（Store/Franchise Portal）
- 区域代理后台（Agent Portal）
- 设备投资方控制台（Device Investor Portal，选配；也可作为 Platform Console 的租户角色视图）
- 平台总部后台：管理/运营/财务结算/设备运维/客服（Platform Console）
- IoT 设备接入与设备管理（Device Gateway + Device Twin）
- 智能钥匙柜固件/屏幕端（Cabinet Firmware/Screen，选配）
- 公共能力：身份认证、通知中心、证据链服务、审计与风控、结算对账服务。
## 11.2 关键设计原则

- 多租户隔离：tenant_id 贯穿数据层；跨租户访问必须显式授权。
- 最小权限：高风险动作按单授权（ABAC）+ 审批流（可配置）。
- 可观测性：关键链路全量日志/指标/追踪，设备事件可回放。
- 弱网容错：离线令牌、缓存补传、设备本地日志与时间校准。
- 可扩展服务：服务目录/定价/规则引擎可配置，避免硬编码。
# 12. 子系统需求说明（按角色）

## 12.1 用户端 APP/小程序（车主）

### 12.1.1 注册登录与合规

- 手机号注册/登录（短信验证码），支持三方登录（可选）。
- 车辆档案：车牌、车型、颜色、停车位信息；可上传行驶证/驾照（按合规要求）。
- 隐私协议与授权管理：定位、相机、通知权限。
### 12.1.2 站点发现与服务大厅

- 附近站点列表/地图：距离、营业时间、排队人数、价格区间、评分与订单量。
- 站点详情：服务档位/增值项、可用格口数、预计等待（ETA）、可支持履约模式；如该站点/钥匙柜支持多个门店（共享柜体），需展示可服务门店列表（价格、排队、评分）供选择或自动分配。
### 12.1.3 下单与交接（模式A/B/C）

- 选择服务（普通/精洗/打蜡等）、履约模式、车辆与车位信息，展示价格快照与预计时长。
- 共享柜体选择：若同一钥匙柜绑定多个门店，用户需在下单/扫码时选择履约门店（展示各门店价格/排队/评分），或按代理配置由系统自动分配履约门店；订单中固化 fulfillment_store_id。
- 模式A：扫码开格口→放钥匙→关门确认→进入排队；支持预约锁定格口（TTL 可配置）。
- 模式B：预约取车/代泊服务（时间窗可选），展示取车/送回规则与责任说明。
- 模式C：仍需车主在车辆附近钥匙柜扫码存钥匙（预占格口），并填写车位/门禁备注；支持到达通知、进度追踪与钥匙取还状态同步。
### 12.1.4 订单追踪、证据链与通知

- 订单列表与详情：时间轴（排队、取钥匙、预检、开始、完工、归位、还钥匙）。
- 证据链：预检/完工/车位照片与视频、水印信息；可下载/分享给客服（受控）。
- 通知：关键状态变更推送；未取钥匙超时提醒（可配置循环提醒）。
### 12.1.5 支付、发票、评价与投诉

- 支付：微信/支付宝等（按渠道），支持优惠券与会员（可选）。
- 发票：抬头管理、开票申请、开票状态查询（视主体而定）。
- 评价：星级+标签+文字；投诉/工单入口。
## 12.2 服务人员端 APP（技师/代泊）

### 12.2.1 上岗与身份验证

- 账号登录（工号/手机号），上岗状态切换；支持人脸/动态口令（可选）。
- 权限按单授予：仅能看到分配给自己的任务与必要信息（脱敏）。
### 12.2.2 任务列表与多人协作

- 任务池：待处理/进行中/已完成/异常；支持按站点/班组筛选。
- 多人任务：显示同组协作人、当前完成进度；支持组长确认（如启用）。
### 12.2.3 取钥匙/开柜（高风险）

- 扫码柜体 → 获取按单授权令牌 → 开格口 → 取钥匙 → 关门确认。
- 离线兜底（如启用 BLE）：近场解锁；记录离线日志，联网后补传与校验。
- 异常处理：格口打不开/钥匙不在/关门失败 → 自动生成工单并提示升级。
### 12.2.4 取车、预检、施工与完工

- 取车：按车位提示定位，支持导航（非AR）与车位拍照。
- 预检：必拍清单（四角/仪表等，可配置），支持损伤标注与水印。
- 施工：开始/暂停/继续/完成，记录工时与人员；支持增项建议（可选）。
- 完工：完工照片/视频、车辆归位/送回、还钥匙入柜或交接记录。
## 12.3 门店/加盟方管理后台（SaaS）

### 12.3.1 站点与设备（钥匙柜）管理

- 站点信息：地址、营业时间、服务范围、支持的履约模式。
- 柜体管理：设备绑定、格口状态、使用记录、故障统计、OTA（如支持）。
- 共享柜体接单（新增）：查看哪些钥匙柜向本门店开放接单（绑定关系、时段、权重、路由方式）；如无配置权限，可发起“绑定申请/变更申请”并跟踪审批结果。
- 容量控制：可用格口不足时的熔断策略、预约锁定 TTL 配置。
### 12.3.2 订单与排队调度

- 订单看板（泳道/队列）：可按服务档位/模式/优先级展示。
- 派单：按班组/按人/抢单；支持改派与暂停；支持代泊与洗车任务拆分。
- 异常与工单：处理无法取车、车辆故障、钥匙问题、客户纠纷等；支持升级到平台/物业。
### 12.3.3 人员与班组

- 员工管理：技师/代泊/调度/质检等；可为物业人员创建“外部协作账号并授权”。
- 班组管理：组员配置、班次、并发限制、绩效统计。
### 12.3.4 服务目录与定价

- 服务项/档位/增值项管理：价格、工时、人数建议、可用模式、必拍规则。
- 定价权主体规则：当门店不具备定价权时，显示“只读价格”与上级主体价格来源。
### 12.3.5 财务、分润与退款（按权限）

- 订单收款与分润预览：按订单展示各方分成、平台抽佣/运维扣费。
- 结算单：按日/周/月生成与导出；差异工单处理。
- 退款：门店发起/初审（可选），平台终审（可选）；留存原因与证据。
## 12.4 区域代理后台

- 门店生命周期：入驻审核、合同与分润比例、上线验收。
- 区域运营看板：订单、评分、履约时效、设备在线率。
- 辖区设备运维协同：告警、工单派发、巡检记录（可选）。
- 代理分润与对账：结算批次、明细导出、差异申诉。
- 共享柜体配置（新增）：在设备投资方授权/协同下，为辖区站点/柜体配置可服务的门店清单（多对多绑定）、独占/共享/时段规则、权重与路由策略（用户选择/自动分配）；可发起绑定变更审批与通知。
## 12.5 平台总部后台（管理/运营/财务/运维/客服）

- 全局监管：租户/组织/站点/设备/用户/订单的全量监管与查询。
- 风控审计：开柜、应急开柜、退款、分润调整、权限变更等关键动作可追溯。
- 结算中心：规则引擎、批次生成、对账、差异处理、代收场景结算链路。
- 设备运维：在线率、告警、OTA、事件回放、远程诊断。
- 客服工单：用户投诉/纠纷/理赔材料导出，协同门店与物业闭环。
## 12.6 智能钥匙柜与 IoT/固件

### 12.6.1 设备能力与接口

- 设备注册与绑定：设备 ID、站点、格口编号、固件版本、网络能力（4G/5G/Wi-Fi/BLE）。
- 核心指令：open_slot（开格口）、query_status（查状态）、reboot（重启）、upgrade（升级）。
- 事件上报：door_open/door_close、slot_occupied（可选）、tamper（防撬）、offline/online。
### 12.6.2 弱网/离线与安全

- 指令链路：request_id + 签名 + TTL + ACK；服务端重试与幂等处理。
- BLE 离线开锁（如支持）：短期离线令牌、近场限制、事后补传与对账。；本地日志：设备侧缓存 48h（建议），联网后补传；时间戳校准。
## 12.7 设备投资方控制台（Device Investor Portal，选配）

设备投资方控制台用于满足“柜体投资方不一定是代理/门店本体，但需要看到所投柜体运营数据并按期结算分成”的场景。该控制台可独立部署为子系统，也可作为平台控制台的一个租户角色视图（同一前端代码按角色呈现不同菜单）。

- 资产总览：所投柜体列表、投放门店/站点、在线率、告警、格口利用率、周转率等。
- 运营数据（脱敏）：按柜体/站点维度的订单量、收入、客单价、履约时效、超时未取等指标；可导出报表。
- 结算与对账：设备分成规则只读（或按授权可调整）；月结/周结批次列表、明细、差异申诉、结算单下载。
- 收款账户：绑定/更新投资方收款账户（需平台审核）；查看打款状态。
- 资产投放/调拨与门店绑定（可选）：发起柜体投放/撤场/转让/迁移申请；在共享柜体模式下，可发起“柜体-门店绑定/解绑、权重与时段调整”申请（按权限可直接配置或走审批）；全程审批流与审计记录；与门店上线/下线 SOP 联动。
- 权限与审计：仅能访问 owner_org_id/investor_org_id 指向自身的柜体数据；所有导出/查看明细/规则变更均审计。
# 13. 数据与领域模型（DDD）及模型关系

## 13.1 建议的限界上下文（Bounded Context）

建议按“高内聚-低耦合”的业务域拆分限界上下文，以便后续按模块独立开发、测试与部署（起步可先做模块化单体，后续再拆服务）。

- Identity & Access：账号、认证、角色权限（RBAC）、按单授权（ABAC/短期令牌）、MFA、设备/人员可信度。
- Catalog & Pricing：服务目录、服务档位/增值项、定价权主体、价格快照、优惠/券（可选）。
- Order & Commerce：下单、改价（授权）、取消、支付/退款、订单快照固化。
- Fulfillment：排队、派单、多人协作任务（2+技师/组）、履约模式A/B/C、车辆交接、停车位回执。
- Cabinet & IoT：柜体/格口资产、设备接入、开柜指令、ACK回执、告警、离线(BLE)兜底。
- Evidence & Audit：验车/还车证据链（照片/视频/水印）、操作审计、纠纷材料导出。
- Settlement & Finance：收款方(Collector)配置、分润规则引擎、物业代收二次分配、结算批次、对账与差异工单。
- Support：客服工单、异常处置、理赔协助、SLA与回访（可选）。
建议的上下文拆分与主要聚合（示例）：

|限界上下文|主要聚合根|对外契约（API/事件）|典型数据一致性|
|---|---|---|---|
|Identity & Access|User, Role, Permission, Session, AccessGrant|Auth API, Permission APIs, AccessGranted事件|强一致（权限变更立即生效或有明确延迟策略）|
|Catalog & Pricing|ServiceCatalog, ServiceVariant, PricePlan, PriceSnapshot|Catalog/Pricing API, PriceSnapshotCreated事件|强一致（下单必须固化价格快照）|
|Order & Commerce|Order, Payment, Refund|Order API, Payment/Refund API, OrderCreated/Cancelled事件|强一致（订单状态与支付状态）|
|Fulfillment|Task, Assignment, Queue, VehicleHandover|Task API, Queue API, TaskAssigned/Started/Completed事件|最终一致（队列与ETA允许延迟）|
|Cabinet & IoT|Cabinet, Slot, DeviceCommand, DeviceEvent|MQTT主题, DeviceCommandAck事件|最终一致（设备回执到达后对账）|
|Evidence & Audit|EvidenceBundle, AuditLog, DisputeCase|Evidence API, Audit Export, EvidenceUploaded事件|只增不改（WORM/防篡改）|
|Settlement & Finance|Agreement, SettlementRule, SettlementBatch, Ledger|Settlement API, Payout events|强一致（账务）+ 最终一致（外部支付回单）|
|Support|Ticket, SLA, Comment|Ticket API, Notification events|强一致（工单状态机）|
|Asset & Investment|CabinetOwnership, CabinetDeployment, CabinetStoreBinding, InvestorPayoutAccount, DeviceRevenueShareRule|Asset API（认领/转让/部署/撤场/门店绑定/路由配置）, Investor APIs（看板/对账/结算）, CabinetOwnershipChanged, CabinetDeployed, CabinetStoreBindingChanged|强一致（资产归属/部署/绑定变更需审计+可选审批）；结算采用快照与可重跑|

## 13.2 核心聚合根（Aggregate Roots）与职责

下表按聚合根给出职责边界、关键字段、典型命令与产生的领域事件（用于DDD建模与实现落地）。

|聚合根|职责/边界|关键字段（示例）|典型命令（Command）|典型事件（Event）|
|---|---|---|---|---|
|Organization/Tenant|多租户组织树、合同主体、收款账户、角色模板归属|tenant_id, org_id, org_type, legal_entity, payout_accounts[]|CreateOrg, BindPayoutAccount, SetRoleTemplate|OrgCreated, PayoutAccountBound|
|Store/Site|门店/站点基础信息、营业时间、履约模式开关、容量限制|store_id, site_id, address, open_hours, modes_enabled, capacity|CreateSite, UpdateCapacity, ToggleMode|SiteCreated, SiteConfigUpdated|
|Cabinet|柜体资产、设备身份、固件版本、在线状态、告警；与资产归属/投放指派通过 CabinetOwnership/Assignment 聚合关联|cabinet_id, device_id, fw_version, connectivity, last_seen, current_owner_org_id（只读/通过Ownership聚合维护）|RegisterCabinet, PushOta, DisableCabinet|CabinetRegistered, OtaPushed, CabinetDisabled|
|Slot|格口状态、占用/锁定、开柜/关门检测、钥匙托管标记|slot_id, slot_no, status, locked_until, occupied, last_open_ts|LockSlot, OpenSlot, MarkOccupied, MarkReturned|SlotLocked, SlotOpened, SlotOccupied, SlotReturned|
|Customer/Vehicle|车主与车辆档案、实名/KYC、车辆偏好（车位）|customer_id, phone, kyc_status, vehicles[]|Register, AddVehicle, UpdateKyc|CustomerRegistered, VehicleAdded|
|Staff|人员档案、岗位（技师/代泊/店员/物业）、上岗状态、资质|staff_id, org_id, roles[], shift_status, certifications|EnrollStaff, AssignRole, ClockIn/Out|StaffEnrolled, StaffRoleAssigned, StaffClockedIn|
|WashTeam|洗车组/班组配置（2+协作）、组员与默认人数|team_id, site_id, members[], default_required_count|CreateTeam, AddMember, SetRequiredCount|WashTeamUpdated|
|ServiceCatalog|服务目录、服务项/档位/增值项、适用履约模式|catalog_id, items[], variants[], addons[], enabled_modes|CreateServiceItem, UpdateVariant, ToggleAddon|CatalogUpdated|
|PricePlan/PriceSnapshot|定价规则与下单价格快照（防改价争议）|plan_id, owner_org_id, rules, snapshot_id, quote|UpsertPricePlan, QuotePrice, CreateSnapshot|PriceQuoted, PriceSnapshotCreated|
|Order|订单主状态机、履约模式、价格快照、队列/ETA、关键审计|order_id, status, fulfillment_mode, price_snapshot, queue_no, tasks[]|CreateOrder, CancelOrder, ConfirmDeposit, ForceClose|OrderCreated, OrderCancelled, KeyDeposited, OrderForcedClosed|
|Task|任务状态机；支持多人协作（2+技师）；支持分工（取车/洗车/还车/还钥匙）|task_id, order_id, type, status, required_count, assignees[]|AssignStaff, StartTask, CompleteTask, ReportException|TaskAssigned, TaskStarted, TaskCompleted, TaskExceptionReported|
|EvidenceBundle|证据链集合：验车/还车/停车位照片、视频；只增不改；与订单/任务关联|bundle_id, order_id, type, items[], hash_chain|UploadEvidence, SealBundle|EvidenceUploaded, EvidenceSealed|
|CommercialAgreement|商业协议：定价权主体、收款方、分润规则、运维费/抽佣策略|agreement_id, price_authority_org, collector_account, stakeholders[]|CreateAgreement, UpdateSettlementRule|AgreementActivated, SettlementRuleUpdated|
|SettlementBatch/Ledger|结算批次、分润明细、对账差异、物业代收二次分配|batch_id, period, lines[], status, discrepancies[]|GenerateBatch, Reconcile, ConfirmPayout|SettlementBatchGenerated, ReconciliationCompleted|
|Ticket|工单/投诉/纠纷；与订单/设备/人员关联；SLA|ticket_id, type, status, related_ids, sla_deadline|CreateTicket, AssignTicket, CloseTicket|TicketCreated, TicketClosed|
|CabinetOwnership/Deployment|柜体资产投资认领与归属（owner/investor）、设备分成规则与收款账户绑定；以及柜体物理投放/撤场/迁移（DeploymentHistory）。不直接承载“可服务门店清单”，该能力由 CabinetStoreBinding 聚合负责。|cabinet_id, owner_org_id, investor_org_id, share_rule_id, payout_account_id, status, deployed_site_id, effective_from/to, approval_flow_id|ClaimOwnership, TransferOwnership, DeployToSite, Relocate, Retire, BindInvestorPayoutAccount|CabinetOwnershipClaimed, CabinetOwnershipTransferred, CabinetDeployed, CabinetRelocated, CabinetRetired, InvestorPayoutAccountBound|
|CabinetStoreBinding|柜体-门店运营绑定（多对多）：定义某柜体在某生效窗口可由哪些门店接单，以及独占/共享/时段与权重/路由策略；变更需审计并可选审批；下单需固化绑定快照。|binding_id, cabinet_id, store_id, binding_type, status, priority, weight, routing_mode, effective_from/to, created_by, approved_by|CreateBinding, UpdateBinding, DisableBinding, ChangeWeight, ChangeRoutingMode|CabinetStoreBindingCreated, CabinetStoreBindingUpdated, CabinetStoreBindingDisabled|

## 13.3 核心实体关系（ER）与模型关系表

下表用于数据库建模与服务边界核对；建议所有业务表带 tenant_id，且以 (tenant_id, id) 作为逻辑主键域。

|关系|说明|关键约束/索引建议|
|---|---|---|
|Tenant 1..N Organization/Store/Site|多租户隔离边界|所有业务表必须带 tenant_id；跨租户查询默认禁止|
|Store 1..N Site|门店包含多个站点（小区/园区）|site_code 站点编码在同一 store 内唯一|
|Site 1..N Cabinet 1..N Slot|站点下柜体与格口|slot_no 在同一 cabinet 内唯一；索引(cabinet_id, slot_no)|
|Site 1..N WashTeam 1..N WashTeamMember|站点可配置多个洗车组及成员|同一 staff 允许属于多个 team（可选）或限制唯一（可配置）|
|Customer 1..N Vehicle|一个用户可绑定多辆车|车牌在同一 tenant 内唯一（可配置）|
|ServiceCatalog 1..N ServiceItem 1..N Variant|服务项与档位/规格|variant_code 在 item 内唯一；索引(item_id, variant_code)|
|PricePlan 1..N PriceRule|定价规则集合|规则版本化；生效时间窗不可重叠（同一plan）|
|Order 1..N OrderItem|订单包含服务项与增值项|订单创建后 items 不可直接改写，需生成修订记录|
|Order 1..N Task 1..N TaskAssignee|订单任务支持多人协作（2+）|required_count >= 最低执行人数；完成策略可配置|
|Order 0..1 Cabinet/Slot|订单与钥匙柜交接关联（origin_cabinet_id/slot_id）；共享柜体场景需同时固化 fulfillment_store_id 与 binding_snapshot_id|模式A/C 必关联；模式B 在发生“入柜交接”时关联；索引(order_id, origin_cabinet_id)|
|Slot 0..N SlotAccessLog|格口开门/关门/失败等日志|必须含 request_id, actor_id, order_id（如有）用于审计与追溯|
|Order 0..N EvidenceBundle|订单证据链|证据只增不改；写入后生成 hash/签名（建议）|
|CommercialAgreement 1..N SettlementRule|协议对应分润与扣费规则|规则变更需版本化；订单需固化 snapshot_id|
|Order 1..1 AgreementSnapshot|下单固化协议/价格快照|快照不可变；用于事后对账与仲裁|
|Payment 0..N Refund|支付与退款|幂等键：external_txn_id；退款支持部分/全额|
|SettlementBatch 1..N SettlementLine|结算批次与明细|line 需可追溯到 order_id/pay_txn；支持物业代收二次分配|
|Ticket 0..1 Order/Device/Staff|工单可关联订单/设备/人员|索引：status, sla_deadline, related_id|
|Organization(DeviceInvestor) 1..N CabinetOwnership (History)|设备投资方/资产方认领的柜体归属历史链路（含转让/退役）；与分成规则和收款账户绑定|索引：(tenant_id, investor_org_id), (tenant_id, cabinet_id, effective_from)；current 记录 effective_to 为空|
|Cabinet 1..N CabinetOwnership (History) & CabinetDeploymentHistory|柜体资产归属与物理投放部署历史，可追溯转让、撤场与迁移|约束：同一 cabinet 在同一时刻只能有一个 current ownership；同一时刻只能有一个 current deployment|
|SettlementLine 0..N Split(Stakeholder=DeviceInvestor)|每笔订单/周期结算对设备投资方的分成行（与代理/门店/物业并列）|索引：(tenant_id, stakeholder_org_id, period); 分润规则以订单快照为准，可重跑|
|Cabinet N..N Store via CabinetStoreBinding|共享柜体场景：同一柜体可绑定多个门店接单；同一门店也可接入多台柜体（跨站点）。绑定支持独占/共享/时段与权重/路由。|索引：(tenant_id, cabinet_id, status), (tenant_id, store_id)；约束：同一(cabinet_id, store_id)生效时间窗不可重叠；binding_type=EXCLUSIVE时同一时刻仅允许一个ACTIVE绑定|

## 13.4 关键业务约束（用于一致性与并发控制）

以下约束建议在聚合内强校验；跨聚合采用事件驱动 + 补偿机制。

- 幂等：所有“开柜/锁定格口/确认入柜/确认取钥匙/开始洗车/完成洗车/还钥匙/取回钥匙/退款”等写操作必须支持幂等键（Idempotency-Key）或 request_id，重复提交不产生副作用。
- 状态机：订单、任务、格口、钥匙托管、支付/退款、结算等均必须显式状态机；禁止跳状态。
- 按单授权（ABAC）：开柜/取钥匙必须绑定 order_id + slot_id + actor_id + TTL，超时失效；离线(BLE)令牌同样需TTL并在联网后补审计。
- 多人协作：同一任务允许 2+ 技师；完成条件须可配置（全部完成/达到人数阈值/主管确认）。
- 价格与协议快照：下单即固化 PriceSnapshot + AgreementSnapshot；后续规则变更不影响已成交订单。
- 代收与分润：收款方(Collector)与定价权主体可解耦；每笔入账必须可追溯到订单与分润明细；物业代收需支持二次分配对账。
- 证据链：证据只增不改；删除需走合规流程并记录审计；建议对象存储开启版本或WORM策略。
- 容量控制：站点/格口可配置“预约锁定TTL”和“满载熔断”；防止占用导致死锁。
## 13.5 领域服务与策略（Domain Services & Policies）

下列逻辑通常不适合放在单一实体内，建议实现为领域服务/策略，并保证可测试与可配置。

|领域服务/策略|输入|输出|关键规则|
|---|---|---|---|
|PricingService|service_items, fulfillment_mode, user_level, time_window, price_authority|PriceQuote/PriceSnapshot|优先级：协议价、门店价、平台指导价（由高到低）；快照不可变|
|QueueEstimator|current_queue, staff_capacity, avg_duration_by_service|eta, queue_no|允许最终一致；异常时返回区间值|
|AssignmentPolicy|order/tasks, staff_skills, wash_team, shift_status|task_assignees|支持2+协作；支持代泊与技师不同人|
|CabinetAccessPolicy|actor, order, slot, location(optional), risk_score|grant/deny + ttl_token|二次验证、地理/时间/风险阈值、审计必写|
|SettlementEngine|order_snapshot, pay_receipt, agreement_snapshot|settlement_lines|支持运维费按单扣、比例抽佣、物业代收二次分配|
|EvidencePolicy|service_type, fulfillment_mode, risk_level|required_photos/videos checklist|固定角度必拍；缺失禁止完结或进入质检|

# 14. 接口与数据对象（API/DTO）

本章给出可直接指导研发的接口契约建议（REST + MQTT/事件）。实际落地可通过 OpenAPI/AsyncAPI 自动生成服务端与前端 SDK。

## 14.0 API 总体约定（必须遵守）

- 认证：OAuth2/JWT（建议），Web端支持MFA；设备侧使用设备证书/密钥 + 签名。
- 多租户：所有请求必须在 token 中携带 tenant_id（或在网关注入）；服务端强校验数据隔离。
- 幂等：所有写操作支持 Header: Idempotency-Key；设备指令使用 request_id。
- 分页：统一使用 page/size 或 cursor；响应返回 total（可选）与 next_cursor。
- 时间：统一使用 ISO-8601（UTC）存储；前端按用户时区展示。
- 错误码：统一 error_code + message + trace_id；禁止返回内部堆栈。
- 审计：所有影响资金/钥匙/权限/设备的写操作必须写 AuditLog。
## 14.1 API 分组与清单（矩阵-建议覆盖范围）

说明：下表为“最小可用+可扩展”清单，覆盖平台/代理/门店/技师/车主/设备全链路。

|分组|接口|方法|鉴权角色|幂等|说明/关键点|
|---|---|---|---|---|---|
|Auth|/auth/login | /auth/logout | /auth/refresh|POST|All|N/A|登录/登出/刷新；支持MFA；设备侧独立认证|
|User & RBAC|/users | /roles | /permissions | /access-grants|GET/POST/PUT|Platform/OrgAdmin|Y|角色权限；按单授权发放与回收|
|Org/Tenant|/tenants | /orgs | /stores | /sites|GET/POST/PUT|Platform/Admin/Agent|Y|租户与组织、门店与站点配置|
|Staff|/staff | /wash-teams | /shifts|GET/POST/PUT|Store/Admin|Y|人员、洗车组(2+)、上岗/班次|
|Catalog|/catalogs | /services | /variants | /addons|GET/POST/PUT|Store/Admin|Y|服务目录、档位、增值项|
|Pricing|/price-plans | /quotes | /price-snapshots|GET/POST/PUT|PriceAuthority|Y|定价权主体维护；下单固化快照|
|Order|/orders | /orders/{id}|GET/POST/PUT|Customer/Store/Platform|Y|创建、详情、取消、强制关单（授权）|
|Queue|/sites/{id}/queue | /orders/{id}/queue|GET/PUT|All|N|排队、ETA；允许最终一致|
|Cabinet|/cabinets | /cabinets/{id}/slots|GET/POST/PUT|Ops/Store|Y|柜体/格口资产、状态、记录|
|Cabinet Access|/slots/{id}/lock | /slots/{id}/open|POST|Customer/Staff|Y|锁定TTL、开柜；与订单绑定；必须审计|
|Tasks|/orders/{id}/tasks | /tasks/{id}|GET/POST/PUT|Store/Staff|Y|派单、多人协作、开始/完成/异常|
|Evidence|/orders/{id}/evidence | /evidence/{id}|POST/GET|Customer/Store/Platform|Y|上传与查询；只增不改|
|Payment|/pay/create | /pay/notify|POST|Customer/System|Y|发起支付；异步回调；对账使用 external_txn_id|
|Refund|/refunds | /refunds/{id}/approve|POST/PUT|Store/Finance/Platform|Y|退款申请、审批、状态同步|
|Settlement|/agreements | /settlement/batches | /settlement/lines|GET/POST|Finance|Y|协议、结算批次、明细；物业代收二次分配|
|Reconcile|/reconcile/import | /reconcile/diffs | /reconcile/tickets|POST/GET|Finance/Ops|Y|对账导入、差异、差异工单|
|Tickets|/tickets | /tickets/{id}/comments|GET/POST/PUT|CS/Ops/Store|Y|工单与纠纷；SLA|
|Notify|/notify/templates | /notify/send|GET/POST|Ops|Y|模板与发送；支持短信/推送/站内信|
|IoT MQTT|topic: beizhi/{tenant}/{cabinet}/cmd/open_slot|PUB|Device|request_id|下发开柜；设备ACK回执；支持离线补传|
|IoT MQTT|topic: beizhi/{tenant}/{cabinet}/evt/slot_state|PUB|Device|event_id|格口状态上报：开门/关门/占用/异常|
|IoT MQTT|topic: beizhi/{tenant}/{cabinet}/evt/heartbeat|PUB|Device|event_id|心跳、网络、电量、固件版本、传感器状态|
|Asset & Investment|/asset/cabinets/{cabinet_id}/ownership（认领/转让）|GET/PUT|Platform/Admin, DeviceInvestor/Admin|PUT: 是（request_id）|维护柜体 current_owner/current_investor；转让/变更需审批（可选）；全量审计。|
|Asset & Investment|/asset/cabinets/{cabinet_id}/deployment（投放部署/迁移/撤场）|GET/POST/PUT|Platform/Admin, DeviceInvestor/Admin, Agent/Admin(协同)|是（request_id）|管理柜体物理部署到 site（投放/迁移/撤场）；变更需留痕与审计；影响扫码位置、容量与运维看板。|
|Asset & Investment|/investor/cabinets | /investor/cabinets/{id}/metrics|GET|DeviceInvestor/Admin, DeviceInvestor/Finance|N/A|设备投资方仅可见自己柜体的脱敏运营数据（订单量/收入/利用率/告警）。|
|Asset & Investment|/investor/settlement/batches | /investor/settlement/lines|GET|DeviceInvestor/Finance|N/A|查看设备分成结算批次与明细；支持导出与差异申诉（不含车主PII）。|
|Asset & Investment|/investor/payout-accounts|GET/PUT|DeviceInvestor/Finance, Platform/Finance(审核)|PUT: 是|绑定/更新投资方收款账户；敏感字段加密存储；变更需审计。|
|Asset & Investment|/asset/cabinets/{cabinet_id}/store-bindings（绑定门店/权重/时段/路由）|GET/POST/PUT|Agent/Admin, Platform/Admin, DeviceInvestor/Admin(可审批), Store/Admin(申请)|是（request_id）|配置共享柜体可服务的门店清单（多对多绑定），支持独占/共享/时段与权重、路由模式；变更强审计并可选审批；订单需固化 binding_snapshot。|
|Cabinet Routing|/cabinets/{cabinet_id}/providers（可接单门店列表）|GET|Customer/App|N/A|扫码/下单时查询该柜体当前可接单门店列表（价格/排队/评分摘要）；用于用户选择或系统自动路由。|

## 14.2 DTO 与字段级定义（用于直接开发）

以下为核心对象字段级建议（可作为数据库字段与接口字段的统一来源）。

|DTO/实体|字段（类型）|约束/校验|说明|
|---|---|---|---|
|OrderCreateRequest|store_id, (site_id|origin_cabinet_id), vehicle_id, fulfillment_mode, service_items[], contact, notes, preferred_time_window?|items>=1；Idempotency-Key必带；共享柜体场景需校验 origin_cabinet_id 与 store_id 存在有效绑定|创建订单并固化价格/协议/绑定快照（部分在存钥匙确认时补全）|
|Order|order_id, order_no, status, payment_status, total_amount, origin_cabinet_id, fulfillment_store_id, price_snapshot, agreement_snapshot, tasks[]|状态机校验；跨门店/共享柜体必须固化 fulfillment_store_id|订单聚合视图（含归因与快照）|
|Task|task_id, type, status, required_count, assignees[]|支持2+|任务/多人协作|
|AccessGrant|grant_id, actor_id, order_id, slot_id, action, ttl|ttl受限|按单授权令牌|
|EvidenceItem|evidence_id, type, url, hash, watermark|只增不改|证据链条目|
|AgreementSnapshot|collector_account_id, stakeholders[], fee_rules|不可变|下单固化|
|SettlementLine|order_id, gross, fees[], splits[], net|校验|分润明细|
|CabinetOwnership|cabinet_id, owner_org_id, investor_org_id, payout_account_id, share_rule_id, ownership_status, deployed_site_id, effective_from/to, approval_flow_id|同一cabinet仅允许一个current ownership；部署历史可多条但同一时刻仅一条current；变更需request_id；审计必写|柜体资产认领/归属与物理部署历史（含投资方与分成规则绑定）|
|InvestorSettlementSummary|period, investor_org_id, cabinets[], gross_base, device_share, adjustments, payout_status|只读；来源可重跑；对账差异用 ticket 记录|设备投资方的周期性结算汇总视图（脱敏）|
|CabinetStoreBinding|binding_id, cabinet_id, store_id, binding_type, status, priority, weight, routing_mode, effective_from/to, created_by, approved_by?|同一(cabinet_id, store_id)生效时间窗不可重叠；binding_type=EXCLUSIVE时同一时刻仅允许一个ACTIVE绑定；变更需request_id+审计|共享柜体运营绑定配置对象（影响订单路由与结算快照）|

## 14.3 MQTT/设备协议（AsyncAPI 口径）

- Topic命名：beizhi/{tenant_id}/{cabinet_id}/cmd/* 与 beizhi/{tenant_id}/{cabinet_id}/evt/*
- 消息必须包含：event_id/request_id, ts, nonce, signature, firmware_version, trace_id
- 平台下发cmd必须等待ACK；超时重试（可配置），记录到 DeviceCommand 表
- 设备离线期间缓存事件，恢复网络后补传；平台按 event_id 幂等
- 设备密钥轮换、签名校验失败即丢弃并告警；可选mTLS
# 15. 事件、消息与通知（Eventing & Notifications）

建议采用事务Outbox + 消息总线发布领域事件，通知服务订阅事件并下发到Push/短信/站内信。

## 15.1 事件信封（Event Envelope）与幂等

|字段|示例|说明|
|---|---|---|
|event_id|evt_20260225_xxx|全局唯一；用于幂等去重|
|event_type|Order.KeyDeposited|采用命名空间：Domain.Action|
|occurred_at|2026-02-25T12:34:56Z|事件发生时间(UTC)|
|tenant_id|t_001|多租户隔离|
|aggregate_type/aggregate_id|Order/ord_123|关联聚合|
|correlation_id/causation_id|trace_abc/cmd_789|链路追踪/触发来源|
|version|1|事件版本；向后兼容|
|payload|{...}|事件内容(JSON)|

## 15.2 必备领域事件清单（按业务域）

|域|事件类型|触发点|订阅方（示例）|
|---|---|---|---|
|Order|Order.Created|下单成功|通知/结算/运营|
|Cabinet|Order.KeyDeposited|存钥匙确认|通知/履约/风控|
|Cabinet|Order.KeyCheckedOut|取钥匙成功|通知/履约/风控|
|Fulfillment|Task.Assigned|派单/多人协作分配|技师端/门店看板|
|Fulfillment|Order.WashCompleted|整单履约完成|通知/结算|
|Cabinet|Order.KeyReturned|还钥匙确认|通知/超时提醒|
|Cabinet|Order.KeyPickedUp|车主取回|关单/结算触发|
|Device|Device.Offline|柜体离线|运维告警/暂停接单(可配)|
|Evidence|Evidence.Uploaded|证据上传|质检/客服|
|Payment|Payment.Paid|支付成功|订单/结算|
|Refund|Refund.Succeeded|退款成功|通知/对账|
|Settlement|Settlement.BatchGenerated|结算批次生成|财务/代理/门店|
|Reconcile|Reconcile.DiffFound|对账差异|财务/工单|
|Risk|Risk.High|风控命中|风控/门店/平台|
|Asset|Cabinet.OwnershipChanged|柜体认领/转让/投资方变更生效|设备投资方/财务/审计|
|Asset|Cabinet.DeployedToSite|柜体投放部署到站点（或迁移/撤场）|门店/运营/运维/设备投资方|
|Settlement|Settlement.InvestorPayoutGenerated|设备投资方分成结算批次生成|设备投资方/财务/通知|
|Asset|Cabinet.StoreBindingChanged|共享柜体门店绑定/权重/时段/路由变更生效|门店/区域代理/设备投资方/运营/审计|
|Order|Order.RoutedToStore|订单在共享柜体场景下被路由/分配到某门店|门店履约系统/通知/客服/对账|

## 15.3 通知触发点与渠道策略（必须支持）

- 车主必达：存钥匙成功、钥匙被取、开始洗车、洗车完成、钥匙已还、超时未取提醒、退款结果。
- 技师必达：接单/派单、开柜授权失败、任务即将超时、格口故障/设备离线、异常工单指派。
- 门店/加盟必达：设备告警、异常率超阈值、对账差异、结算批次失败、退款待审。
- 渠道优先级建议：App Push/站内信 > 短信（关键节点/未装App）> 语音（可选，超时升级）。
# 16. 风控、审计与合规（重点）

本章给出可直接落地的安全/合规控制清单：钥匙受监管、行为可追溯、资金可对账、隐私可保护。

## 16.1 身份认证与访问控制（RBAC + ABAC/按单授权）

- 后台建议强制MFA；角色-权限点矩阵覆盖菜单/按钮/API；默认最小权限。
- 高危操作（开柜/应急开柜/改价/退款/强制关单/分润规则变更）必须二次确认或审批。
- 按单授权token绑定 order_id + slot_id + actor_id + TTL；离线(BLE)令牌同样需TTL并在联网后补审计。
- 接口防刷：IP/账号/设备维度限流；对扫描开柜接口重点保护。
## 16.2 钥匙托管与开柜安全（控制点表）

|风险场景|后果|控制措施（必须）|审计字段（必须）|
|---|---|---|---|
|非授权人员开柜|资产风险/纠纷|按单授权+TTL；二次验证（可选）；失败锁定；告警|order_id, slot_id, actor_id, request_id, result, device_id, ts|
|离线令牌重放|重复开柜|一次性或短期令牌；nonce+签名；设备端记录已用token|token_id, nonce, used_at|
|错取错还|严重纠纷|格口-订单强绑定；取还扫码确认；可选钥匙标签二次校验|expected_slot, actual_slot, operator, photos|
|应急开柜滥用|资产风险|审批流；现场取证；自动生成工单与报告|approver_id, reason, evidence, ack|
|设备投资方数据越权（查看车主PII/车辆证件）|合规风险/投诉/监管处罚|最小化数据集（脱敏字段集）；按资产范围授权；导出审批与水印；访问频控与异常告警|actor_id, investor_org_id, cabinet_id, query_scope, export_flag, reason, result, request_id|
|共享柜体门店绑定配置错误/被篡改（多对多绑定、权重、时段、独占/共享切换）|订单被路由到错误门店、收益归因与分成纠纷、跨门店数据越权风险、客户体验下降|绑定变更视为高风险：最小权限；可选审批（投资方/平台）；生效时间窗与回滚；变更影响评估（预计订单/金额）；异常告警；订单固化 binding_snapshot；对账争议走工单|actor_id, cabinet_id, store_id, change_type, old_value, new_value, effective_from/to, reason, approver_id, request_id, result, trace_id|

## 16.3 证据链与不可抵赖（无AI）

- 必拍清单可配置；缺失则禁止完结或进入质检。
- 水印：时间/站点/订单/人员；原文件hash入库；证据只增不改。
- 对象存储使用临时签名URL访问；查看/下载写审计。
## 16.4 隐私与合规

建议遵循《个人信息保护法》《数据安全法》《网络安全法》进行合规评估；政企客户可评估等保要求。

- 数据最小化：车位/电话默认脱敏；仅履约所需人员在时间窗内可见明文。
- 设备投资方可见性：仅可访问其认领柜体的脱敏运营数据与结算明细（不含车主身份/证件/人脸/精确车位）；导出需审批并加水印；访问频控与异常告警。
- 敏感字段列级加密；日志禁止明文PII；支持用户数据导出与注销。
## 16.5 审计日志（AuditLog）落库规范

|字段|示例|说明|
|---|---|---|
|audit_id|uuid|主键|
|tenant_id|t_001|租户|
|actor_type/actor_id|STAFF/staff_123|操作者|
|action|SLOT_OPEN|动作枚举|
|resource_type/resource_id|Order/ord_123|资源|
|request_id/idem_key|req_xxx|幂等追踪|
|result|SUCCESS/FAIL|结果|
|created_at|ISO-8601|时间|

## 16.6 资金风控与对账（含物业代收）

- 订单-支付回单-结算明细-实际分配全链路可追溯。
- 日对账+差异工单；月结/周结批次；退款冲正影响分润。
- 物业代收：导入物业回单或API对接，按 AgreementSnapshot 计算二次分配明细。
# 17. 非功能性要求（NFR）

## 17.1 可用性与SLO（建议）

- 核心API可用性>=99.9%（按月）；IoT开柜链路单独统计。
- 开柜指令端到端P95<=5s（在线）；关键通知P95<=30s（Push）。
## 17.2 性能与容量（建议，可压测）

|场景|目标|并发口径|备注|
|---|---|---|---|
|站点列表/排队查询|P95<1s|500-2000|可缓存，最终一致|
|订单详情|P95<800ms|200-500|证据链分页|
|派单/任务写|P95<500ms|200|幂等+强审计|
|结算批处理|10万单<30min|离线任务|异步进度|

## 17.3 可靠性、灾备与观测

- 消息至少一次投递；消费者幂等；失败进入DLQ可重放。
- 数据库每日备份；建议RPO<=15min、RTO<=2h；季度恢复演练（建议）。
- 可观测性：日志/指标/链路追踪；关键指标：开柜成功率、设备在线率、证据齐全率、对账差异率。
# 18. 运营与运维要求（详细）

## 18.1 门店上线与培训（SOP）

- 上线：资质审核->协议签署->设备安装绑定->试运行->验收上线。
- 培训：验车/还车必拍、钥匙交接、代泊规范、异常处理、客户沟通话术。
- 抽检与考核：证据链质量/投诉率不达标可限制接单（可配置）。
## 18.2 设备运维

- 巡检：门磁/格口/网络/电源/续航；告警：离线、开柜失败率、卡门、传感异常、电量低。
- OTA灰度发布与回滚；升级窗口与营业时间协调。
## 18.3 财务对账与结算运营

- 日对账：支付渠道/物业回单；差异自动工单。
- 结算批次：周/月；支持多方确认；导出加水印并记录审计。
- 设备投资方结算：按协议周期生成“设备分成”批次与明细；支持投放柜体维度汇总、差异申诉与收款账户打款状态跟踪。
## 18.4 事故应急预案（必须）

- 柜体故障：远程诊断->应急开柜审批->现场取证->处理->报告。
- 钥匙丢失：冻结权限->启动理赔协助->证据导出->闭环。
- 资金异常：暂停结算->差异工单->核对回单->冲正/补分配。
# 19. 上线与迭代里程碑（建议）

## 19.1 MVP（4-8周，建议）

- 模式A闭环：下单-存钥匙-取钥匙-洗车-还钥匙-取回；基础门店后台与技师端；基础设备指令。
- 基础证据链与审计；基础支付与退款；基础通知。
## 19.2 一期（规模化落地）

- 多人协作与班组；模式B代泊取送；容量控制与预约锁定；弱网离线兜底。
- 结算与对账、分润规则；代理后台；平台运维与告警。
## 19.3 二期（商业模式扩展）

- 模式C上门洗车；物业代收再分配；发票/保险深度对接；更细粒度风控策略。
- （明确不含）AI验车与AR导航，作为后续阶段规划。
# 20. 附录（摘要）

## 20.1 状态枚举建议（示例）

本节给出关键状态枚举建议（可作为后端常量与前端字典的统一来源）。不同租户可通过配置调整“可达状态/可触发动作”，但不建议修改枚举值本身。

FulfillmentMode（履约模式）

- A_SELF_DROP：车主自送钥匙入柜（站点洗车+可代泊回位）
- B_VALET_TRANSFER：代泊取送至洗车点（可跨站点/集中洗车）
- C_ONSITE_MOBILE_CABINET：上门洗车（移动履约），但仍基于就近钥匙柜托管闭环（车主先入柜→服务方取钥匙找车→还钥匙入柜）
|枚举值|中文说明|进入条件/触发|常见下一步/约束|
|---|---|---|---|
|A_CABINET_DROP|车主自送钥匙入柜|车主扫码开格口并存钥匙|派单->取钥匙->洗车->还钥匙->取回|
|B_VALET_TRANSFER|代客取送至洗车点|代泊人员取车/送车（可跨站点）|可选入柜交接；仍需证据链与审计|
|C_ON_SITE_CABINET|上门/就近洗车（仍基于钥匙柜）|车主先将钥匙存入车附近钥匙柜|服务方到柜取钥匙找车洗车，完工后钥匙回柜|

OrderStatus（订单主状态）

- CREATED：已创建（待存钥匙/待确认）
- KEY_DEPOSITED：钥匙已存入（入柜确认）
- QUEUED：排队中/待派单
- ASSIGNED：已派单（生成任务）
- IN_SERVICE：服务中（已取钥匙/已开工）
- WASH_COMPLETED：洗车完成（待还钥匙/待回位）
- KEY_RETURNED：钥匙已归还柜体
- KEY_PICKED_UP：车主已取回钥匙
- CLOSED：已完成关单
- CANCELLED：已取消（含退款/无责取消）
- EXCEPTION：异常处理中（工单/风控介入）
|枚举值|中文说明|进入条件/触发|常见下一步/约束|
|---|---|---|---|
|CREATED|订单已创建|创建订单并固化价格/协议快照|等待存钥匙/进入排队；允许取消（按策略）|
|SLOT_LOCKED|格口已预占锁定|用户扫码锁定格口TTL（可选）|超时释放；存钥匙后进入KEY_DEPOSITED|
|KEY_DEPOSITED|钥匙已入柜|关门检测/用户确认入柜|进入QUEUED/ASSIGNED|
|QUEUED|排队中|站点排队启用|派单后进入ASSIGNED|
|ASSIGNED|已派单/已分配执行组|调度派单或技师接单|取钥匙后进入KEY_CHECKED_OUT|
|KEY_CHECKED_OUT|钥匙已被取出|按单授权开柜取钥匙成功|开始洗车进入IN_SERVICE；异常进入EXCEPTION|
|IN_SERVICE|服务进行中|开始洗车/代泊开始|完成进入SERVICE_COMPLETED|
|SERVICE_COMPLETED|服务完成|确认完工并满足证据策略|还钥匙入柜进入KEY_RETURNED|
|KEY_RETURNED|钥匙已还柜|还钥匙确认/关门检测|进入WAIT_PICKUP|
|WAIT_PICKUP|等待取回|钥匙已还柜|车主取回进入CLOSED；超时提醒/滞留费|
|CLOSED|订单闭环完成|车主取回或授权代取完成|进入结算/对账|
|CANCELLED|已取消|取消成功（未开始履约）|如已支付则走退款|
|FORCE_CLOSED|强制关闭|授权人员强制完结|必须写原因与审计|
|EXCEPTION|异常处理中|设备/车辆/纠纷等异常|进入工单；可回退或强制处理|

KeyCustodyStatus（钥匙托管状态）

- NOT_DEPOSITED：未托管
- IN_CABINET：在柜（车主已存）
- CHECKED_OUT：已取出（技师/代泊取钥匙）
- IN_STORE_SAFE：在店保管（洗车点内）
- RETURNED_TO_CABINET：已回柜（待车主取回）
- PICKED_UP_BY_CUSTOMER：车主已取回
- LOST：遗失（触发应急与理赔）
|枚举值|中文说明|进入条件/触发|常见下一步/约束|
|---|---|---|---|
|IN_SLOT|钥匙在格口内|用户存入或技师归还|可被按单授权取出|
|CHECKED_OUT|钥匙已取出|开柜取钥匙成功|可转IN_STORE_SAFE或RETURNED_IN_SLOT|
|IN_STORE_SAFE|门店临时保管|门店登记保管（可选）|必须归还入柜或异常处理|
|RETURNED_IN_SLOT|钥匙已还回格口|还钥匙确认|等待车主取回|
|PICKED_UP_BY_CUSTOMER|车主已取回|车主扫码/授权码取回|订单可闭环|
|LOST|钥匙丢失|稽核确认|冻结权限+工单+理赔协助|

SlotStatus（格口状态）

- EMPTY：空闲
- RESERVED：已预占（TTL 内）
- OCCUPIED：占用（有钥匙）
- LOCKED：锁定（风控/维护）
- FAULT：故障（不可用）
|枚举值|中文说明|进入条件/触发|常见下一步/约束|
|---|---|---|---|
|AVAILABLE|可用|空闲|可锁定/开门|
|LOCKED|已锁定|预占TTL|超时回AVAILABLE；存入后OCCUPIED|
|OPEN|门已开|设备上报|关门后进入OCCUPIED或AVAILABLE|
|OCCUPIED|已占用|检测到钥匙/确认入柜|取出后回AVAILABLE|
|FAULT|故障|卡门/传感异常/失败率高|进入维护/工单|
|MAINTENANCE|维护中|运维置维护|维护完成回AVAILABLE|

TaskType（任务类型）

- WASH：洗车任务
- VALET_PICKUP：代泊取车
- VALET_DROPOFF：代泊送回/归位
- QC：质检/复检（可选）
- SUPPORT：异常协助/救援（可选）
|枚举值|中文说明|进入条件/触发|常见下一步/约束|
|---|---|---|---|
|KEY_DEPOSIT|存钥匙确认|车主端|确认入柜|
|VALET_PICKUP|代泊取车|代泊人员|取车/驶向洗车点|
|WASH|洗车/精洗/打蜡|技师组|多人协作|
|PARKING|车辆归位/停车|代泊/技师|拍照车位|
|KEY_RETURN|还钥匙入柜|代泊/技师|归还确认|
|CUSTOMER_PICKUP|车主取回钥匙|车主|闭环|
|EXCEPTION_HANDLE|异常处理|店长/客服|工单闭环|

TaskStatus（任务状态）

- PENDING：待接单/待开始
- ACCEPTED：已接单
- IN_PROGRESS：执行中
- COMPLETED：已完成
- FAILED：失败/中断（进入异常）
- CANCELLED：已取消/作废
|枚举值|中文说明|进入条件/触发|常见下一步/约束|
|---|---|---|---|
|PENDING|待分配|任务生成|派单->ASSIGNED|
|ASSIGNED|已分配|分配到人员/组|接单->ACCEPTED/执行->IN_PROGRESS|
|ACCEPTED|已接单|执行人确认|开始->IN_PROGRESS|
|IN_PROGRESS|执行中|开始执行|完成->COMPLETED/异常->FAILED|
|COMPLETED|已完成|完成并通过校验|推进订单|
|FAILED|失败/异常|无法执行|触发工单/重派|
|CANCELLED|取消|订单取消/改派|不可恢复|

PaymentStatus（支付状态）

- UNPAID：未支付
- PAID：已支付
- PARTIALLY_REFUNDED：部分退款
- REFUNDED：已全额退款
- FAILED：支付失败/关闭
|枚举值|中文说明|进入条件/触发|常见下一步/约束|
|---|---|---|---|
|UNPAID|未支付|下单后|可取消/发起支付|
|PAYING|支付中|跳转渠道|PAID/FAILED|
|PAID|已支付|回调成功|可结算/可退款|
|FAILED|失败|回调失败|可重试|
|REFUNDED|已退款|全额退款成功|结束|
|PART_REFUNDED|部分退款|部分退款成功|保留明细|

RefundStatus（退款状态）

- REQUESTED：已申请
- STORE_APPROVED：门店初审通过
- STORE_REJECTED：门店驳回
- PLATFORM_APPROVED：平台终审通过
- PLATFORM_REJECTED：平台驳回
- PROCESSING：退款处理中
- SUCCEEDED：退款成功
- FAILED：退款失败
- REVERSED：冲正/撤销
|枚举值|中文说明|进入条件/触发|常见下一步/约束|
|---|---|---|---|
|REQUESTED|已申请|提交申请|APPROVING|
|APPROVING|审批中|审批流|APPROVED/REJECTED|
|APPROVED|已批准|审批通过|PROCESSING|
|REJECTED|已拒绝|审批拒绝|结束|
|PROCESSING|处理中|调用渠道退款|SUCCEEDED/FAILED|
|SUCCEEDED|成功|渠道回执|同步支付状态|
|FAILED|失败|渠道失败|可重试/人工|

SettlementStatus（结算状态）

- PENDING：待结算
- CALCULATED：已计算（可重跑）
- CONFIRMED：已确认（多方确认可选）
- PAID_OUT：已打款
- RECONCILED：已对账
- ADJUSTED：已调整（补差/冲正）
|枚举值|中文说明|进入条件/触发|常见下一步/约束|
|---|---|---|---|
|NOT_READY|未就绪|订单未闭环或未支付|READY|
|READY|可结算|满足结算条件|GENERATED|
|GENERATED|批次已生成|批次生成成功|RECONCILING/CONFIRMED|
|RECONCILING|对账中|等待回单|差异工单/CONFIRMED|
|CONFIRMED|已确认|各方确认|PAID_OUT|
|PAID_OUT|已出款/已分配|出款或代收分配完成|CLOSED|
|CLOSED|已关闭|周期结束|归档|

AccessGrantStatus（按单授权状态）

- ISSUED：已签发
- USED：已使用
- EXPIRED：已过期
- REVOKED：已撤销
|枚举值|中文说明|进入条件/触发|常见下一步/约束|
|---|---|---|---|
|ISSUED|已签发|生成token|USED/EXPIRED/REVOKED|
|USED|已使用|开柜成功或动作完成|不可再次使用（默认）|
|EXPIRED|已过期|TTL到期|需重新签发|
|REVOKED|已撤销|管理员撤销|不可使用|

DeviceConnectivity（设备在线状态）

- ONLINE：在线
- OFFLINE：离线
- DEGRADED：弱网/不稳定（可选）
|枚举值|中文说明|进入条件/触发|常见下一步/约束|
|---|---|---|---|
|ONLINE|在线|心跳正常|状态变化触发告警|
|OFFLINE|离线|超过阈值未心跳|告警/可能暂停接单|
|DEGRADED|降级|弱网/高失败率|限制开柜频率/提示|

TicketStatus（工单状态）

- OPEN：已创建
- IN_PROGRESS：处理中
- WAITING_EXTERNAL：待外部反馈（门店/物业/支付）
- RESOLVED：已解决待确认
- CLOSED：已关闭
CabinetOwnershipStatus（柜体认领状态）

- CLAIMED：已认领（生效）
- TRANSFER_PENDING：转让审批中（可选）
- TRANSFERRED：已转让（历史记录）
- RETIRED：已退役/撤场（不参与新订单）
AssetDeploymentStatus（投放部署状态）

- ASSIGNED：已投放生效（绑定站点）
- UNASSIGNED：未投放/已撤场
- PENDING_APPROVAL：变更审批中（可选）
CabinetStoreBindingStatus（柜体-门店绑定状态）

ACTIVE：已生效（可接单）

INACTIVE：已停用（不可接单）

PENDING_APPROVAL：待审批（可选）

EXPIRED：已过期（按 effective_to 自动失效）

CabinetBindingType（绑定类型）

EXCLUSIVE：独占（同一时刻仅允许一个门店绑定）

SHARED：共享（同一时刻允许多个门店绑定）

TIME_SLICED：按时段独占（不同时间窗分配给不同门店）

InvestorPayoutStatus（设备投资方结算打款状态）

- PENDING：待生成/待确认
- READY：可打款（结算批次已生成）
- PAID：已打款
- FAILED：打款失败（需重试/工单）
- DISPUTED：争议中（对账差异/申诉）
|枚举值|中文说明|进入条件/触发|常见下一步/约束|
|---|---|---|---|
|OPEN|已创建|新工单|ASSIGNED|
|ASSIGNED|已分派|分派责任人|IN_PROGRESS|
|IN_PROGRESS|处理中|处理推进|RESOLVED/PENDING_USER|
|PENDING_USER|等待用户|需要补充信息|IN_PROGRESS/RESOLVED|
|RESOLVED|已解决待确认|处理完成|CLOSED|
|CLOSED|已关闭|关闭|归档|

## 20.2 通知模板占位（示例）

本节给出通知模板占位建议（可配置多渠道）。模板建议使用 template_code 作为唯一键，支持按租户/站点覆盖。

|template_code|触发事件|接收人|渠道|标题（可选）|内容模板（中文）|频控/备注|
|---|---|---|---|---|---|---|
|CUST_KEY_DEPOSITED|Order.KeyDeposited|车主|Push/站内信/短信|存钥匙成功|您已在{site_name}存入钥匙（{cabinet_name}-{slot_no}），当前排队{queue_no}，预计{eta}。|下单后必达|
|CUST_KEY_CHECKED_OUT|Order.KeyCheckedOut|车主|Push/站内信|钥匙已取出|您的钥匙已被服务人员取出，服务即将开始。如有疑问联系{support_phone}。|必达|
|CUST_WASH_STARTED|Task.Started|车主|Push/站内信|开始服务|您的车辆已开始{service_name}，预计耗时{duration_min}分钟。|可选开关|
|CUST_WASH_COMPLETED|Order.WashCompleted|车主|Push/站内信|服务完成|您的车辆已完成{service_name}，车辆停放在{parking_desc}（见照片）。|必达|
|CUST_KEY_RETURNED|Order.KeyReturned|车主|Push/站内信/短信|钥匙已归还|钥匙已放回{site_name} {cabinet_name}-{slot_no}，请尽快取回。|必达；超时升级短信|
|CUST_PICKUP_REMINDER|Timer.KeyNotPickedUp|车主|短信/Push|超时未取提醒|您的钥匙已在柜内超过{hours}小时。请尽快取回，超时可能产生{storage_fee_rule}。如需协助联系{support_phone}。|每{interval_hours}小时一次，夜间免打扰|
|CUST_PAYMENT_SUCCESS|Payment.Paid|车主|Push/站内信|支付成功|支付成功：{amount}元，订单{order_id}。|必达|
|CUST_REFUND_RESULT|Refund.Succeeded/Failed|车主|Push/站内信/短信|退款结果|退款{refund_status}：{refund_amount}元。原因：{refund_reason}。|必达|
|STAFF_TASK_ASSIGNED|Task.Assigned|技师/代泊|Push/站内信|新任务|您有新任务：{task_type}（订单{order_id}），请在{accept_deadline}前处理。|必达+频控|
|STAFF_CABINET_GRANT_FAIL|CabinetAccessDenied|技师/代泊|Push|开柜授权失败|开柜授权失败：{deny_reason}。请联系店长/客服。|必达|
|STORE_REFUND_PENDING|Refund.Requested|门店财务/店长|站内信/短信|退款待审|有新的退款待审：订单{order_id}，金额{refund_amount}元。|必达|
|OPS_DEVICE_OFFLINE|Device.Offline|运维/门店|短信/企业IM/站内信|设备离线告警|柜体{cabinet_name}已离线{minutes}分钟，请尽快处理。|告警抑制/合并|
|OPS_SLOT_FAULT|Slot.Fault|运维/门店|短信/站内信|格口故障|格口{cabinet_name}-{slot_no}异常：{fault_type}。建议停止使用并发起工单。|必达|
|FIN_RECONCILE_DIFF|Reconcile.DiffFound|财务|站内信/邮件(可选)|对账差异|发现对账差异：批次{batch_id}，差异金额{diff_amount}。请查看差异工单。|必达|
|STORE_CABINET_BINDING_CHANGED|Cabinet.StoreBindingChanged|门店管理员|站内信/Push|柜体接单配置变更|您门店的柜体接单配置已变更：{cabinet_name}（{site_name}），绑定类型{binding_type}，生效{effective_from}至{effective_to}。变更摘要：{change_summary}。|高风险变更；可配置是否必达|
|INVESTOR_CABINET_BINDING_CHANGED|Cabinet.StoreBindingChanged|设备投资方|站内信/Email（可选）|所投柜体门店绑定变更|您所投柜体{cabinet_name}（{site_name}）的门店绑定配置已变更：绑定类型{binding_type}，生效{effective_from}至{effective_to}。变更摘要：{change_summary}。|仅展示脱敏信息；导出需审批|

变量占位统一规则：使用 {snake_case}；未提供变量时展示默认值或隐藏该段。常用变量示例：

常用变量示例（建议）：

- {order_no} 订单号/展示号
- {site_name} 站点名称
- {store_name} 门店名称
- {service_name} 服务名称
- {fulfillment_mode} 履约模式
- {cabinet_name} 钥匙柜名称
- {slot_no} 格口号
- {eta_minutes} 预计等待分钟数
- {operator_name} 服务人员/技师/代泊名称（可选）
- {amount} 金额（分/元按前端格式化）
- {due_time} 截止时间（超时提醒）
- {period} 结算周期（如 2026-02）
- {payout_amount} 打款金额
- {investor_name} 设备投资方名称
- {ticket_no} 工单号
推荐模板（示例，template_code 为唯一键）：

- ORDER_CREATED：订单创建成功（Customer）；渠道：站内信/推送/短信；变量：{order_no},{site_name},{service_name},{amount}
- KEY_DEPOSIT_REMINDER：提醒车主存钥匙入柜（Customer）；变量：{order_no},{cabinet_name}
- KEY_DEPOSITED：钥匙已存入（Customer/Store）；变量：{order_no},{cabinet_name},{slot_no}
- QUEUE_UPDATE：排队进度变化（Customer）；变量：{order_no},{eta_minutes}
- TASK_ASSIGNED：任务已分配（Technician/Valet）；变量：{order_no},{site_name},{task_type}
- KEY_CHECKED_OUT：服务方已取钥匙（Customer/Store）；变量：{order_no}
- WASH_STARTED：开始服务（Customer）；变量：{order_no},{operator_name}
- WASH_COMPLETED：洗车完成（Customer/Store）；变量：{order_no}
- KEY_RETURNED：钥匙已回柜（Customer）；变量：{order_no},{cabinet_name},{slot_no}
- KEY_PICKUP_REMINDER_24H：钥匙超时未取提醒（循环，可配置）（Customer）；变量：{order_no},{due_time}
- ORDER_CLOSED：订单已完成（Customer）；变量：{order_no}
- DEVICE_OFFLINE_ALERT：设备离线告警（Ops/Store/Investor可选）；变量：{cabinet_name}
- INVESTOR_PAYOUT_READY：设备投资方结算可打款（DeviceInvestor）；变量：{period},{payout_amount}
- INVESTOR_PAYOUT_PAID：设备投资方已打款（DeviceInvestor）；变量：{period},{payout_amount}
- OWNERSHIP_CHANGED：柜体归属/投放变更通知（DeviceInvestor/Store/Ops，可配）；变量：{cabinet_name},{site_name}
- TICKET_UPDATED：工单状态更新（相关责任人）；变量：{ticket_no},{ticket_status}
|变量|说明|
|---|---|
|site_name|站点名称|
|cabinet_name|柜体名称/编号|
|slot_no|格口编号|
|queue_no|排队序号|
|eta|预计开始时间/区间|
|service_name|服务名称（普通/精洗/打蜡等）|
|parking_desc|停车位描述（脱敏）|
|support_phone|客服电话/门店电话（可虚拟号）|
|hours|超时小时数|
|storage_fee_rule|滞留费规则摘要|
|order_id|订单号|
|amount|金额|
|refund_amount|退款金额|
|refund_status|退款状态（成功/失败）|
|deny_reason|拒绝原因|
|binding_type|绑定类型（EXCLUSIVE/SHARED/TIME_SLICED）|
|effective_from|生效开始时间|
|effective_to|生效结束时间（为空表示长期）|
|change_summary|变更摘要（例如新增/停用门店、权重调整、时段调整）|

