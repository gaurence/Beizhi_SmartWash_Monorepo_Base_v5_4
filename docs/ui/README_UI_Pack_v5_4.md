# 备至代客洗车智洗平台 UI 高保真页面包（v5.4）

本目录基于《Beizhi_SmartWash_Platform_Requirements_v5_4.docx》输出。

## 目录结构（按子系统）

| 子系统目录 | SVG 页面数 | 说明 |
|---|---:|---|
| `Agent_Admin_Portal` | 28 | 区域代理后台（门店拓展、设备健康、共享绑定、分润对账） |
| `Cabinet_Screen` | 16 | 柜体屏幕（存取引导/维护） |
| `Customer_App` | 59 | 车主端 App/小程序（下单、存取钥匙、状态追踪、支付评价） |
| `Device_Investor_Portal` | 17 | 设备投资方控制台（柜体资产与收益分成、共享绑定申请） |
| `Platform_Admin_Portal` | 110 | 平台总部管理端（含资产/投资、共享绑定、商业协议、风控审计等） |
| `Platform_CS_Portal` | 11 | 平台客服端（工单/纠纷/证据导出） |
| `Platform_Finance_Portal` | 18 | 平台财务端（结算/对账/退款/收款账户） |
| `Platform_IoTOps_Portal` | 11 | 平台设备运维端（IoT 监控/OTA/远程指令） |
| `Platform_Ops_Portal` | 32 | 平台运营端（监控/告警/事件/工单） |
| `Property_Portal` | 9 | 物业端（代收/对账/结算协同） |
| `Store_Admin_Portal` | 66 | 门店/加盟方后台（排队派单、班组、服务定价、柜体与共享绑定） |
| `Technician_App` | 36 | 服务人员端 App（技师/代泊：取钥匙、多人协作、证据链、还钥匙） |
| `_Common` | 1 | 设计系统与组件规范 |

## SVG 与 MD 对照说明

- 每个页面均包含：
  - `*.svg`：高保真页面（中文）
  - `*.md`：页面说明（风格/布局/组件/功能/跳转/接口建议/权限审计/需求追溯）
- MD 文件与 SVG 文件 **同名同目录**，便于研发按页面快速定位接口与权限点。

## 使用建议

1. 研发对照：以 `*.svg` 作为视觉与布局基准；以同名 `*.md` 作为功能/接口/权限说明。
2. 接口对齐：最终以仓库 `docs/contracts/openapi.yaml` 与 `docs/contracts/asyncapi.yaml` 为准；MD 中列出的是该页面常用接口建议。
3. 需求追溯：MD 的“对应需求点”引用 v5.4 的章节，便于 DDD 与测试用例追踪。
