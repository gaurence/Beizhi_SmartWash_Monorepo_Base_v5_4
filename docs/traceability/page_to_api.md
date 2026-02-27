# 页面 → API/DTO → 权限点 追踪矩阵（v5.4）

本文件为“高保真页面（SVG/MD） ↔ OpenAPI/DTO ↔ 权限点（RBAC/ABAC）”的统一对照表。

- `api_tag`：对应 `docs/contracts/openapi.yaml` 中 tag。
- `permission_key`：建议权限点命名（最终以权限系统落地为准）。
- `api_endpoints`：从每页 MD 的“相关接口（API Mapping）”自动提取（起步版）。

## Agent_Admin_Portal
| page_key | 页面中文名 | 角色 | 模块 | 入口 | 父页面 | api_tag | permission_key | api_endpoints |
|---|---|---|---|---|---|---|---|---|
| AA_01_Login | 备至·智洗平台 | AGENT_ADMIN | Login | 入口-登录 |  | identity | ui.Agent_Admin_Portal.Login.view | POST /auth/login; POST /auth/mfa/verify; POST /auth/logout |
| AA_02_Region_Dashboard | 区域运营大盘 | AGENT_ADMIN | Region | 入口-首页 |  | ui-static | ui.Agent_Admin_Portal.Region.view |  |
| AA_03_1_Store_Create | 新增门店 | AGENT_ADMIN | Store | 新增 | AA_03_Stores_List | ui-static | ui.Agent_Admin_Portal.Store.manage |  |
| AA_03_Stores_List | 加盟店管理 | AGENT_ADMIN | Stores | 菜单入口 |  | ui-static | ui.Agent_Admin_Portal.Stores.view |  |
| AA_04_1_Store_Edit | 编辑门店 | AGENT_ADMIN | Store | 编辑 | AA_04_Store_Detail | ui-static | ui.Agent_Admin_Portal.Store.manage |  |
| AA_04_2_Store_Contracts | 门店合同与分润 | AGENT_ADMIN | Store | 子页面入口 | AA_04_Store_Detail | ui-static | ui.Agent_Admin_Portal.Store.view |  |
| AA_04_Store_Detail | 门店详情与KPI | AGENT_ADMIN | Store | 菜单入口 |  | ui-static | ui.Agent_Admin_Portal.Store.view |  |
| AA_05_1_Onboarding_Step1_Basic | 门店入驻流程 | AGENT_ADMIN | Onboarding | 子页面入口 | AA_05_Store_Onboarding | ui-static | ui.Agent_Admin_Portal.Onboarding.view |  |
| AA_05_2_Onboarding_Step2_Contract | 门店入驻流程 | AGENT_ADMIN | Onboarding | 子页面入口 | AA_05_Store_Onboarding | ui-static | ui.Agent_Admin_Portal.Onboarding.view |  |
| AA_05_3_Onboarding_Step3_DeviceBind | 门店入驻流程 | AGENT_ADMIN | Onboarding | 绑定/配置 | AA_05_Store_Onboarding | asset-investment | ui.Agent_Admin_Portal.Onboarding.manage | GET /assets/cabinets; GET /assets/cabinets/{cabinetId}; GET /assets/cabinets/{cabinetId}/slots; POST /iot/cabinets/{cabinetId}/commands/open-slot; GET /iot/cabinets/{cabinetId}/events |
| AA_05_4_Onboarding_Step4_GoLive | 门店入驻流程 | AGENT_ADMIN | Onboarding | 子页面入口 | AA_05_Store_Onboarding | ui-static | ui.Agent_Admin_Portal.Onboarding.view |  |
| AA_05_Store_Onboarding | 门店入驻/上线流程 | AGENT_ADMIN | Store | 菜单入口 |  | ui-static | ui.Agent_Admin_Portal.Store.view |  |
| AA_06_1_Device_Detail | 设备健康度 | AGENT_ADMIN | Device | 进入详情 | AA_06_Devices_Health | asset-investment | ui.Agent_Admin_Portal.Device.view | GET /assets/cabinets; GET /assets/cabinets/{cabinetId}; GET /assets/cabinets/{cabinetId}/slots; POST /iot/cabinets/{cabinetId}/commands/open-slot; GET /iot/cabinets/{cabinetId}/events |
| AA_06_Devices_Health | 设备健康度 | AGENT_ADMIN | Devices | 菜单入口 |  | asset-investment | ui.Agent_Admin_Portal.Devices.view | GET /assets/cabinets; GET /assets/cabinets/{cabinetId}; GET /assets/cabinets/{cabinetId}/slots; POST /iot/cabinets/{cabinetId}/commands/open-slot; GET /iot/cabinets/{cabinetId}/events |
| AA_07_1_Settlement_Detail | 结算分润详情 | AGENT_ADMIN | Settlement | 进入详情 | AA_07_Settlements_Shares | finance | ui.Agent_Admin_Portal.Settlement.manage | GET /finance/settlements; GET /finance/settlements/{settlementId}; GET /finance/settlements/{settlementId}/lines; POST /finance/settlements/{settlementId}/export |
| AA_07_Settlements_Shares | 结算分润（代理） | AGENT_ADMIN | Settlements | 菜单入口 |  | finance | ui.Agent_Admin_Portal.Settlements.manage | GET /finance/settlements; GET /finance/settlements/{settlementId}; GET /finance/settlements/{settlementId}/lines; POST /finance/settlements/{settlementId}/export |
| AA_08_1_Ticket_Detail | 对账工单详情 | AGENT_ADMIN | Ticket | 进入详情 | AA_08_Reconciliation_Tickets | finance | ui.Agent_Admin_Portal.Ticket.view | GET /finance/reconciliation/tickets; POST /finance/reconciliation/tickets; GET /finance/reconciliation/tickets/{ticketId} |
| AA_08_Reconciliation_Tickets | 对账工单（辖区） | AGENT_ADMIN | Reconciliation | 菜单入口 |  | finance | ui.Agent_Admin_Portal.Reconciliation.view | GET /finance/reconciliation/tickets; POST /finance/reconciliation/tickets; GET /finance/reconciliation/tickets/{ticketId} |
| AA_09_1_WorkOrder_Create | 新建工单 | AGENT_ADMIN | WorkOrder | 新增 | AA_09_WorkOrders | order | ui.Agent_Admin_Portal.WorkOrder.manage | GET /orders; GET /orders/{orderId}; POST /orders; POST /orders/{orderId}/cancel; POST /orders/{orderId}/dispatch; GET /orders/{orderId}/timeline |
| AA_09_2_WorkOrder_Detail | 工单详情 | AGENT_ADMIN | WorkOrder | 进入详情 | AA_09_WorkOrders | order | ui.Agent_Admin_Portal.WorkOrder.view | GET /orders; GET /orders/{orderId}; POST /orders; POST /orders/{orderId}/cancel; POST /orders/{orderId}/dispatch; GET /orders/{orderId}/timeline |
| AA_09_WorkOrders | 工单中心 | AGENT_ADMIN | WorkOrders | 菜单入口 |  | order | ui.Agent_Admin_Portal.WorkOrders.view | GET /orders; GET /orders/{orderId}; POST /orders; POST /orders/{orderId}/cancel; POST /orders/{orderId}/dispatch; GET /orders/{orderId}/timeline |
| AA_10_1_Report_Filter_Drawer | 报表中心 | AGENT_ADMIN | Report | 子页面入口 | AA_10_Reports | reports | ui.Agent_Admin_Portal.Report.view | GET /reports/summary; POST /reports/export |
| AA_10_Reports | 报表中心 | AGENT_ADMIN | Reports | 菜单入口 |  | reports | ui.Agent_Admin_Portal.Reports.view | GET /reports/summary; POST /reports/export |
| AA_11_1_ChangePassword | 修改密码 | AGENT_ADMIN | ChangePassword | 子页面入口 | AA_11_Profile | ui-static | ui.Agent_Admin_Portal.ChangePassword.view |  |
| AA_11_Profile | 我的账号 | AGENT_ADMIN | Profile | 菜单入口 |  | ui-static | ui.Agent_Admin_Portal.Profile.view |  |
| AA_12_1_Binding_Detail | 设备健康度 | AGENT_ADMIN | Binding | 进入详情 | AA_12_CabinetStoreBindings | asset | ui.Agent_Admin_Portal.Binding.manage | GET /asset/cabinets/{cabinetId}/store-bindings; POST /asset/cabinets/{cabinetId}/store-bindings; PATCH /asset/cabinet-store-bindings/{bindingId}; POST /asset/cabinet-store-bindings/{bindingId}/approve |
| AA_12_2_Binding_Request_Create | 新建工单 | AGENT_ADMIN | Binding | 新增 | AA_12_CabinetStoreBindings | asset | ui.Agent_Admin_Portal.Binding.manage | GET /asset/cabinets/{cabinetId}/store-bindings; POST /asset/cabinets/{cabinetId}/store-bindings; PATCH /asset/cabinet-store-bindings/{bindingId}; POST /asset/cabinet-store-bindings/{bindingId}/approve |
| AA_12_CabinetStoreBindings | 共享柜体绑定度 | AGENT_ADMIN | CabinetStoreBindings | 菜单入口 |  | asset-investment | ui.Agent_Admin_Portal.CabinetStoreBindings.manage | GET /assets/cabinets; GET /assets/cabinets/{cabinetId}; GET /assets/cabinets/{cabinetId}/slots; POST /iot/cabinets/{cabinetId}/commands/open-slot; GET /iot/cabinets/{cabinetId}/events; GET /asset/cabinets/{cabinetId}/store-bindings; POST /asset/cabinets/{cabinetId}/store-bindings; PATCH /asset/cabinet-store-bindings/{bindingId}; POST /asset/cabinet-store-bindings/{bindingId}/approve |

## Cabinet_Screen
| page_key | 页面中文名 | 角色 | 模块 | 入口 | 父页面 | api_tag | permission_key | api_endpoints |
|---|---|---|---|---|---|---|---|---|
| CK_00_Select_Mode | CK_00_Select_Mode |  | Select | 菜单入口 |  | ui-static | ui.Cabinet_Screen.Select.view |  |
| CK_01_Welcome | CK_01_Welcome |  | Welcome | 菜单入口 |  | ui-static | ui.Cabinet_Screen.Welcome.view |  |
| CK_02_Scan_QR | CK_02_Scan_QR |  | Scan | 菜单入口 |  | ui-static | ui.Cabinet_Screen.Scan.view |  |
| CK_03_Open_Slot | CK_03_Open_Slot |  | Open | 菜单入口 |  | asset-investment | ui.Cabinet_Screen.Open.view | GET /assets/cabinets; GET /assets/cabinets/{cabinetId}; GET /assets/cabinets/{cabinetId}/slots; POST /iot/cabinets/{cabinetId}/commands/open-slot; GET /iot/cabinets/{cabinetId}/events |
| CK_04_Deposit_Place_Key | CK_04_Deposit_Place_Key |  | Deposit | 菜单入口 |  | ui-static | ui.Cabinet_Screen.Deposit.view |  |
| CK_05_Deposit_Success | CK_05_Deposit_Success |  | Deposit | 菜单入口 |  | ui-static | ui.Cabinet_Screen.Deposit.view |  |
| CK_06_Retrieve_Intro | CK_06_Retrieve_Intro |  | Retrieve | 菜单入口 |  | ui-static | ui.Cabinet_Screen.Retrieve.view |  |
| CK_07_Retrieve_Open | CK_07_Retrieve_Open |  | Retrieve | 菜单入口 |  | ui-static | ui.Cabinet_Screen.Retrieve.view |  |
| CK_08_Retrieve_Success | CK_08_Retrieve_Success |  | Retrieve | 菜单入口 |  | ui-static | ui.Cabinet_Screen.Retrieve.view |  |
| CK_09_Error_Offline | CK_09_Error_Offline |  | Error | 菜单入口 |  | ui-static | ui.Cabinet_Screen.Error.view |  |
| CK_10_Error_SlotFault | CK_10_Error_SlotFault |  | Error | 菜单入口 |  | asset-investment | ui.Cabinet_Screen.Error.view | GET /assets/cabinets; GET /assets/cabinets/{cabinetId}; GET /assets/cabinets/{cabinetId}/slots; POST /iot/cabinets/{cabinetId}/commands/open-slot; GET /iot/cabinets/{cabinetId}/events |
| CK_11_Maintenance | CK_11_Maintenance |  | Maintenance | 菜单入口 |  | ui-static | ui.Cabinet_Screen.Maintenance.view |  |
| CK_12_DoorOpen_Timeout | CK_12_DoorOpen_Timeout |  | DoorOpen | 菜单入口 |  | ui-static | ui.Cabinet_Screen.DoorOpen.view |  |
| CK_13_UVC_Running | CK_13_UVC_Running |  | UVC | 菜单入口 |  | ui-static | ui.Cabinet_Screen.UVC.view |  |
| CK_14_Staff_Mode_Login | 工号/口令 |  | Staff | 入口-登录 |  | identity | ui.Cabinet_Screen.Staff.view | POST /auth/login; POST /auth/mfa/verify; POST /auth/logout |
| CK_15_Staff_Mode_OpenSlot | CK_15_Staff_Mode_OpenSlot |  | Staff | 菜单入口 |  | asset-investment | ui.Cabinet_Screen.Staff.view | GET /assets/cabinets; GET /assets/cabinets/{cabinetId}; GET /assets/cabinets/{cabinetId}/slots; POST /iot/cabinets/{cabinetId}/commands/open-slot; GET /iot/cabinets/{cabinetId}/events |

## Customer_App
| page_key | 页面中文名 | 角色 | 模块 | 入口 | 父页面 | api_tag | permission_key | api_endpoints |
|---|---|---|---|---|---|---|---|---|
| CA_01_1_Privacy_Consent | 隐私与授权 | CUSTOMER | Privacy | 子页面入口 | CA_01_Splash | ui-static | ui.Customer_App.Privacy.view |  |
| CA_01_Splash | 备至代客洗车 | CUSTOMER | Splash | 菜单入口 |  | ui-static | ui.Customer_App.Splash.view |  |
| CA_02_1_Login_Wechat | 登录方式 | CUSTOMER | Login | 入口-登录 | CA_02_Login_SMS | app | ui.Customer_App.Login.view | POST /app/auth/sms/send; POST /app/auth/sms/login; POST /app/auth/logout |
| CA_02_Login_SMS | 手机号登录 | CUSTOMER | Login | 入口-登录 |  | app | ui.Customer_App.Login.view | POST /app/auth/sms/send; POST /app/auth/sms/login; POST /app/auth/logout |
| CA_03_1_KYC_ID_Capture | 实名认证 | CUSTOMER | KYC | 子页面入口 | CA_03_KYC_RealName | ui-static | ui.Customer_App.KYC.view |  |
| CA_03_2_KYC_DriverLicense | 实名认证 | CUSTOMER | KYC | 子页面入口 | CA_03_KYC_RealName | ui-static | ui.Customer_App.KYC.view |  |
| CA_03_3_KYC_Review_Pending | 实名认证 | CUSTOMER | KYC | 子页面入口 | CA_03_KYC_RealName | ui-static | ui.Customer_App.KYC.view |  |
| CA_03_KYC_RealName | 实名认证 | CUSTOMER | KYC | 菜单入口 |  | ui-static | ui.Customer_App.KYC.view |  |
| CA_04_1_Vehicle_Detail | 车辆详情 | CUSTOMER | Vehicle | 进入详情 | CA_04_Vehicle_List | ui-static | ui.Customer_App.Vehicle.view |  |
| CA_04_Vehicle_List | 我的车辆 | CUSTOMER | Vehicle | 菜单入口 |  | ui-static | ui.Customer_App.Vehicle.view |  |
| CA_05_1_Vehicle_Edit | 编辑车辆 | CUSTOMER | Vehicle | 编辑 | CA_05_Vehicle_Add | ui-static | ui.Customer_App.Vehicle.manage |  |
| CA_05_Vehicle_Add | 添加车辆 | CUSTOMER | Vehicle | 菜单入口 |  | ui-static | ui.Customer_App.Vehicle.view |  |
| CA_06_1_Home_Map | 附近站点地图 | CUSTOMER | Home | 入口-首页 | CA_06_Home_Sites | app | ui.Customer_App.Home.view | GET /app/sites/nearby; GET /app/sites/{siteId}; GET /app/sites/{siteId}/queue |
| CA_06_Home_Sites | 附近站点 | CUSTOMER | Home | 入口-首页 |  | app | ui.Customer_App.Home.view | GET /app/sites/nearby; GET /app/sites/{siteId}; GET /app/sites/{siteId}/queue |
| CA_07_1_Site_Guide | 站点指引 | CUSTOMER | Site | 子页面入口 | CA_07_Site_Detail | ui-static | ui.Customer_App.Site.view |  |
| CA_07_Site_Detail | 站点详情 | CUSTOMER | Site | 菜单入口 |  | ui-static | ui.Customer_App.Site.view |  |
| CA_08_0_Fulfillment_Mode_Select | 下单 | CUSTOMER | Fulfillment | 子页面入口 | CA_08_Create_Order | ui-static | ui.Customer_App.Fulfillment.view |  |
| CA_08_1_Select_Service_Package | 选择服务 | CUSTOMER | Select | 子页面入口 | CA_08_Create_Order | ui-static | ui.Customer_App.Select.view |  |
| CA_08_2_AddOns | 增值服务 | CUSTOMER | AddOns | 子页面入口 | CA_08_Create_Order | ui-static | ui.Customer_App.AddOns.view |  |
| CA_08_3_Coupon_Select | 选择优惠 | CUSTOMER | Coupon | 子页面入口 | CA_08_Create_Order | ui-static | ui.Customer_App.Coupon.view |  |
| CA_08_4_Order_Confirm | 确认下单 | CUSTOMER | Order | 子页面入口 | CA_08_Create_Order | app | ui.Customer_App.Order.view | POST /app/orders; GET /app/orders; GET /app/orders/{orderId}; GET /app/orders/{orderId}/timeline |
| CA_08_5_Onsite_Location_Input | 上门洗车位置 | CUSTOMER | Onsite | 子页面入口 | CA_08_Create_Order | ui-static | ui.Customer_App.Onsite.view |  |
| CA_08_6_Onsite_KeyCabinet_Select | 附近钥匙柜 | CUSTOMER | Onsite | 子页面入口 | CA_08_Create_Order | app | ui.Customer_App.Onsite.view | POST /app/cabinets/scan; POST /app/orders/{orderId}/deposit-key; POST /app/orders/{orderId}/retrieve-key |
| CA_08_7_Valet_Pickup_Details | 代客取车信息 | CUSTOMER | Valet | 进入详情 | CA_08_Create_Order | ui-static | ui.Customer_App.Valet.view |  |
| CA_08_Create_Order | 下单 | CUSTOMER | Create | 菜单入口 |  | app | ui.Customer_App.Create.manage | POST /app/orders; GET /app/orders; GET /app/orders/{orderId}; GET /app/orders/{orderId}/timeline |
| CA_09_1_Slot_Reserve_Timeout | 格口预约 | CUSTOMER | Slot | 子页面入口 | CA_09_Slot_Reserve | ui-static | ui.Customer_App.Slot.view |  |
| CA_09_Slot_Reserve | 预占格口 | CUSTOMER | Slot | 菜单入口 |  | ui-static | ui.Customer_App.Slot.view |  |
| CA_10_Scan_Deposit | 扫码存钥匙 | CUSTOMER | Scan | 菜单入口 |  | app | ui.Customer_App.Scan.view | POST /app/cabinets/scan; POST /app/orders/{orderId}/deposit-key; POST /app/orders/{orderId}/retrieve-key |
| CA_11_Deposit_Instructions | 存钥匙指引 | CUSTOMER | Deposit | 菜单入口 |  | ui-static | ui.Customer_App.Deposit.view |  |
| CA_12_1_Queue_Detail | 排队详情 | CUSTOMER | Queue | 进入详情 | CA_12_Deposit_Success | ui-static | ui.Customer_App.Queue.view |  |
| CA_12_Deposit_Success | 已排队 | CUSTOMER | Deposit | 菜单入口 |  | ui-static | ui.Customer_App.Deposit.view |  |
| CA_13_Order_List | 我的订单 | CUSTOMER | Order | 菜单入口 |  | app | ui.Customer_App.Order.view | POST /app/orders; GET /app/orders; GET /app/orders/{orderId}; GET /app/orders/{orderId}/timeline |
| CA_14_1_Order_Cancel_Confirm | 取消订单 | CUSTOMER | Order | 子页面入口 | CA_14_Order_Detail_Timeline | app | ui.Customer_App.Order.view | POST /app/orders; GET /app/orders; GET /app/orders/{orderId}; GET /app/orders/{orderId}/timeline |
| CA_14_2_Order_Cancel_Result | 订单已取消 | CUSTOMER | Order | 子页面入口 | CA_14_Order_Detail_Timeline | app | ui.Customer_App.Order.view | POST /app/orders; GET /app/orders; GET /app/orders/{orderId}; GET /app/orders/{orderId}/timeline |
| CA_14_3_Onsite_Timeline | 订单详情 | CUSTOMER | Onsite | 子页面入口 | CA_14_Order_Detail_Timeline | ui-static | ui.Customer_App.Onsite.view |  |
| CA_14_Order_Detail_Timeline | 订单详情 | CUSTOMER | Order | 菜单入口 |  | app | ui.Customer_App.Order.view | POST /app/orders; GET /app/orders; GET /app/orders/{orderId}; GET /app/orders/{orderId}/timeline |
| CA_15_Evidence_Gallery | 证据链 | CUSTOMER | Evidence | 菜单入口 |  | ui-static | ui.Customer_App.Evidence.view |  |
| CA_16_Retrieve_Reminder | 可取钥匙提醒 | CUSTOMER | Retrieve | 菜单入口 |  | ui-static | ui.Customer_App.Retrieve.view |  |
| CA_17_1_Retrieve_Instructions | 取钥匙指引 | CUSTOMER | Retrieve | 子页面入口 | CA_17_Scan_Retrieve | ui-static | ui.Customer_App.Retrieve.view |  |
| CA_17_2_Delegate_Retrieve_Code | 授权代取 | CUSTOMER | Delegate | 子页面入口 | CA_17_Scan_Retrieve | ui-static | ui.Customer_App.Delegate.view |  |
| CA_17_Scan_Retrieve | 扫码取钥匙 | CUSTOMER | Scan | 菜单入口 |  | app | ui.Customer_App.Scan.view | POST /app/cabinets/scan; POST /app/orders/{orderId}/deposit-key; POST /app/orders/{orderId}/retrieve-key |
| CA_18_1_Rate_Order | 评价订单 | CUSTOMER | Rate | 子页面入口 | CA_18_Retrieve_Success | app | ui.Customer_App.Rate.view | POST /app/orders; GET /app/orders; GET /app/orders/{orderId}; GET /app/orders/{orderId}/timeline |
| CA_18_2_Rate_Success | 评价成功 | CUSTOMER | Rate | 子页面入口 | CA_18_Retrieve_Success | ui-static | ui.Customer_App.Rate.view |  |
| CA_18_Retrieve_Success | 已关单 | CUSTOMER | Retrieve | 菜单入口 |  | ui-static | ui.Customer_App.Retrieve.view |  |
| CA_19_1_Payment_Success | 支付成功 | CUSTOMER | Payment | 子页面入口 | CA_19_Settlement_Pay | ui-static | ui.Customer_App.Payment.manage |  |
| CA_19_2_Payment_Failed | 支付失败 | CUSTOMER | Payment | 子页面入口 | CA_19_Settlement_Pay | ui-static | ui.Customer_App.Payment.manage |  |
| CA_19_3_Invoice_Request | 申请发票 | CUSTOMER | Invoice | 子页面入口 | CA_19_Settlement_Pay | ui-static | ui.Customer_App.Invoice.view |  |
| CA_19_4_Invoice_Headers | 发票抬头 | CUSTOMER | Invoice | 子页面入口 | CA_19_Settlement_Pay | ui-static | ui.Customer_App.Invoice.view |  |
| CA_19_Settlement_Pay | 结算支付 | CUSTOMER | Settlement | 菜单入口 |  | ui-static | ui.Customer_App.Settlement.manage |  |
| CA_20_Refund_Request | 申请退款 | CUSTOMER | Refund | 菜单入口 |  | ui-static | ui.Customer_App.Refund.manage |  |
| CA_21_Retention_Fee | 滞留费用 | CUSTOMER | Retention | 菜单入口 |  | ui-static | ui.Customer_App.Retention.view |  |
| CA_22_1_Message_Detail | 消息详情 | CUSTOMER | Message | 进入详情 | CA_22_Messages | ui-static | ui.Customer_App.Message.view |  |
| CA_22_Messages | 消息中心 | CUSTOMER | Messages | 菜单入口 |  | ui-static | ui.Customer_App.Messages.view |  |
| CA_23_1_Ticket_Detail | 工单详情 | CUSTOMER | Ticket | 进入详情 | CA_23_Support_Tickets | ui-static | ui.Customer_App.Ticket.view |  |
| CA_23_Support_Tickets | 客服与工单 | CUSTOMER | Support | 菜单入口 |  | ui-static | ui.Customer_App.Support.view |  |
| CA_24_Create_Ticket | 提交工单 | CUSTOMER | Create | 菜单入口 |  | ui-static | ui.Customer_App.Create.manage |  |
| CA_25_1_Settings_Notifications | 通知设置 | CUSTOMER | Settings | 子页面入口 | CA_25_Profile_Settings | ui-static | ui.Customer_App.Settings.manage |  |
| CA_25_2_Settings_AccountSecurity | 账号与安全 | CUSTOMER | Settings | 子页面入口 | CA_25_Profile_Settings | ui-static | ui.Customer_App.Settings.manage |  |
| CA_25_Profile_Settings | 我的 | CUSTOMER | Profile | 菜单入口 |  | ui-static | ui.Customer_App.Profile.manage |  |

## Device_Investor_Portal
| page_key | 页面中文名 | 角色 | 模块 | 入口 | 父页面 | api_tag | permission_key | api_endpoints |
|---|---|---|---|---|---|---|---|---|
| DI_01_Login | 备至·智洗平台 | DEVICE_INVESTOR | Login | 入口-登录 |  | identity | ui.Device_Investor_Portal.Login.view | POST /auth/login; POST /auth/mfa/verify; POST /auth/logout |
| DI_02_Investor_Dashboard | 资产总览 | DEVICE_INVESTOR | Investor | 入口-首页 |  | asset | ui.Device_Investor_Portal.Investor.view | GET /asset/device-investors; GET /asset/device-investors/{investorId}; GET /asset/device-investors/{investorId}/cabinets |
| DI_03_Cabinets_List | 柜体资产库 | DEVICE_INVESTOR | Cabinets | 菜单入口 |  | asset-investment | ui.Device_Investor_Portal.Cabinets.view | GET /assets/cabinets; GET /assets/cabinets/{cabinetId}; GET /assets/cabinets/{cabinetId}/slots; POST /iot/cabinets/{cabinetId}/commands/open-slot; GET /iot/cabinets/{cabinetId}/events |
| DI_04_Cabinet_Detail_Overview | 柜体部署详情 | DEVICE_INVESTOR | Cabinet | 菜单入口 |  | asset-investment | ui.Device_Investor_Portal.Cabinet.view | GET /assets/cabinets; GET /assets/cabinets/{cabinetId}; GET /assets/cabinets/{cabinetId}/slots; POST /iot/cabinets/{cabinetId}/commands/open-slot; GET /iot/cabinets/{cabinetId}/events |
| DI_05_Reports | 运营报表 | DEVICE_INVESTOR | Reports | 菜单入口 |  | reports | ui.Device_Investor_Portal.Reports.view | GET /reports/summary; POST /reports/export |
| DI_06_Settlements_List | 结算分润（代理） | DEVICE_INVESTOR | Settlements | 菜单入口 |  | finance | ui.Device_Investor_Portal.Settlements.manage | GET /finance/settlements; GET /finance/settlements/{settlementId}; GET /finance/settlements/{settlementId}/lines; POST /finance/settlements/{settlementId}/export |
| DI_07_Settlement_Detail | 结算分润详情 | DEVICE_INVESTOR | Settlement | 菜单入口 |  | finance | ui.Device_Investor_Portal.Settlement.manage | GET /finance/settlements; GET /finance/settlements/{settlementId}; GET /finance/settlements/{settlementId}/lines; POST /finance/settlements/{settlementId}/export |
| DI_08_Reconciliation_Tickets | 差异申诉/对账工单（辖区） | DEVICE_INVESTOR | Reconciliation | 菜单入口 |  | finance | ui.Device_Investor_Portal.Reconciliation.view | GET /finance/reconciliation/tickets; POST /finance/reconciliation/tickets; GET /finance/reconciliation/tickets/{ticketId} |
| DI_09_Ticket_Detail | 对账工单详情（投资方） | DEVICE_INVESTOR | Ticket | 菜单入口 |  | finance | ui.Device_Investor_Portal.Ticket.view | GET /finance/reconciliation/tickets; POST /finance/reconciliation/tickets; GET /finance/reconciliation/tickets/{ticketId} |
| DI_10_Payout_Accounts | 收款账户（投资方）管理 | DEVICE_INVESTOR | Payout | 菜单入口 |  | finance | ui.Device_Investor_Portal.Payout.view | GET /finance/payout-accounts; POST /finance/payout-accounts; PATCH /finance/payout-accounts/{accountId}; POST /finance/payout-accounts/{accountId}/submit-review |
| DI_11_Payout_Account_Detail | 收款账户详情（投资方） | DEVICE_INVESTOR | Payout | 菜单入口 |  | finance | ui.Device_Investor_Portal.Payout.view | GET /finance/payout-accounts; POST /finance/payout-accounts; PATCH /finance/payout-accounts/{accountId}; POST /finance/payout-accounts/{accountId}/submit-review |
| DI_12_Payout_Account_Edit | 新增账户 | DEVICE_INVESTOR | Payout | 菜单入口 |  | finance | ui.Device_Investor_Portal.Payout.manage | GET /finance/payout-accounts; POST /finance/payout-accounts; PATCH /finance/payout-accounts/{accountId}; POST /finance/payout-accounts/{accountId}/submit-review |
| DI_13_Deployment_Binding_Requests | 投放/撤场/绑定申请 | DEVICE_INVESTOR | Deployment | 菜单入口 |  | asset | ui.Device_Investor_Portal.Deployment.manage | GET /asset/cabinets/{cabinetId}/store-bindings; POST /asset/cabinets/{cabinetId}/store-bindings; PATCH /asset/cabinet-store-bindings/{bindingId}; POST /asset/cabinet-store-bindings/{bindingId}/approve |
| DI_14_Request_Create | 新建工单 | DEVICE_INVESTOR | Request | 菜单入口 |  | ui-static | ui.Device_Investor_Portal.Request.manage |  |
| DI_15_Request_Detail | 申请详情（投放/绑定） | DEVICE_INVESTOR | Request | 菜单入口 |  | ui-static | ui.Device_Investor_Portal.Request.view |  |
| DI_16_Audit_Log | 订单详情 | DEVICE_INVESTOR | Audit | 菜单入口 |  | audit | ui.Device_Investor_Portal.Audit.view | GET /audit/logs; GET /audit/logs/{logId}; POST /audit/exports |
| DI_17_Profile | 我的账号 | DEVICE_INVESTOR | Profile | 菜单入口 |  | ui-static | ui.Device_Investor_Portal.Profile.view |  |

## Platform_Admin_Portal
| page_key | 页面中文名 | 角色 | 模块 | 入口 | 父页面 | api_tag | permission_key | api_endpoints |
|---|---|---|---|---|---|---|---|---|
| PA_01_Login | 备至·智洗平台 | PLATFORM_ADMIN | Login | 入口-登录 |  | identity | ui.Platform_Admin_Portal.Login.view | POST /auth/login; POST /auth/mfa/verify; POST /auth/logout |
| PA_02_Global_Dashboard | 运营大盘 | PLATFORM_ADMIN | Global | 入口-首页 |  | ui-static | ui.Platform_Admin_Portal.Global.view |  |
| PA_03_1_Tenant_Create | 新增租户/组织 | PLATFORM_ADMIN | Tenant | 新增 | PA_03_Tenant_Management | ui-static | ui.Platform_Admin_Portal.Tenant.manage |  |
| PA_03_2_Tenant_Detail_Overview | 租户详情 | PLATFORM_ADMIN | Tenant | 进入详情 | PA_03_Tenant_Management | ui-static | ui.Platform_Admin_Portal.Tenant.view |  |
| PA_03_3_Tenant_Detail_Stores | 租户详情 | PLATFORM_ADMIN | Tenant | 进入详情 | PA_03_Tenant_Management | ui-static | ui.Platform_Admin_Portal.Tenant.view |  |
| PA_03_4_Tenant_Detail_Policies | 租户详情 | PLATFORM_ADMIN | Tenant | 进入详情 | PA_03_Tenant_Management | ui-static | ui.Platform_Admin_Portal.Tenant.view |  |
| PA_03_5_Tenant_Edit | 编辑租户/组织 | PLATFORM_ADMIN | Tenant | 编辑 | PA_03_Tenant_Management | ui-static | ui.Platform_Admin_Portal.Tenant.manage |  |
| PA_03_6_Tenant_Audit | 租户详情 | PLATFORM_ADMIN | Tenant | 子页面入口 | PA_03_Tenant_Management | audit | ui.Platform_Admin_Portal.Tenant.view | GET /audit/logs; GET /audit/logs/{logId}; POST /audit/exports |
| PA_03_7_Org_Tree | 组织树 | PLATFORM_ADMIN | Org | 子页面入口 | PA_03_Tenant_Management | ui-static | ui.Platform_Admin_Portal.Org.view |  |
| PA_03_Tenant_Management | 租户/组织管理 | PLATFORM_ADMIN | Tenant | 菜单入口 |  | ui-static | ui.Platform_Admin_Portal.Tenant.view |  |
| PA_04_1_User_Create | 新增用户 | PLATFORM_ADMIN | User | 新增 | PA_04_User_Management | ui-static | ui.Platform_Admin_Portal.User.manage |  |
| PA_04_2_User_Detail | 用户详情 | PLATFORM_ADMIN | User | 进入详情 | PA_04_User_Management | ui-static | ui.Platform_Admin_Portal.User.manage |  |
| PA_04_3_User_Edit | 编辑用户 | PLATFORM_ADMIN | User | 编辑 | PA_04_User_Management | ui-static | ui.Platform_Admin_Portal.User.manage |  |
| PA_04_4_User_Roles_Scopes | 用户权限配置 | PLATFORM_ADMIN | User | 子页面入口 | PA_04_User_Management | ui-static | ui.Platform_Admin_Portal.User.manage |  |
| PA_04_User_Management | 用户管理 | PLATFORM_ADMIN | User | 菜单入口 |  | ui-static | ui.Platform_Admin_Portal.User.manage |  |
| PA_05_1_Role_Management | 角色管理 | PLATFORM_ADMIN | Role | 子页面入口 | PA_05_RBAC_ABAC_Matrix | ui-static | ui.Platform_Admin_Portal.Role.manage |  |
| PA_05_3_ABAC_Policy_Editor | ABAC 策略编辑器 | PLATFORM_ADMIN | ABAC | 编辑 | PA_05_RBAC_ABAC_Matrix | ui-static | ui.Platform_Admin_Portal.ABAC.manage |  |
| PA_05_RBAC_ABAC_Matrix | 权限矩阵（RBAC+ABAC） | PLATFORM_ADMIN | RBAC | 菜单入口 |  | ui-static | ui.Platform_Admin_Portal.RBAC.view |  |
| PA_06_1_Cabinet_Import_Batch | 批量导入柜体资产 | PLATFORM_ADMIN | Cabinet | 导入 | PA_06_Cabinet_Asset_Registry | asset-investment | ui.Platform_Admin_Portal.Cabinet.manage | GET /assets/cabinets; GET /assets/cabinets/{cabinetId}; GET /assets/cabinets/{cabinetId}/slots; POST /iot/cabinets/{cabinetId}/commands/open-slot; GET /iot/cabinets/{cabinetId}/events |
| PA_06_Cabinet_Asset_Registry | 柜体资产库 | PLATFORM_ADMIN | Cabinet | 菜单入口 |  | asset-investment | ui.Platform_Admin_Portal.Cabinet.view | GET /assets/cabinets; GET /assets/cabinets/{cabinetId}; GET /assets/cabinets/{cabinetId}/slots; POST /iot/cabinets/{cabinetId}/commands/open-slot; GET /iot/cabinets/{cabinetId}/events |
| PA_07_Cabinet_Register | 注册柜体资产 | PLATFORM_ADMIN | Cabinet | 菜单入口 |  | asset-investment | ui.Platform_Admin_Portal.Cabinet.view | GET /assets/cabinets; GET /assets/cabinets/{cabinetId}; GET /assets/cabinets/{cabinetId}/slots; POST /iot/cabinets/{cabinetId}/commands/open-slot; GET /iot/cabinets/{cabinetId}/events |
| PA_08_Cabinet_Scan_BindStore | 扫码核验并绑定门店 | PLATFORM_ADMIN | Cabinet | 菜单入口 |  | asset-investment | ui.Platform_Admin_Portal.Cabinet.manage | GET /assets/cabinets; GET /assets/cabinets/{cabinetId}; GET /assets/cabinets/{cabinetId}/slots; POST /iot/cabinets/{cabinetId}/commands/open-slot; GET /iot/cabinets/{cabinetId}/events |
| PA_09_1_Cabinet_Slots | 柜体详情 | PLATFORM_ADMIN | Cabinet | 子页面入口 | PA_09_Cabinet_Detail | asset-investment | ui.Platform_Admin_Portal.Cabinet.view | GET /assets/cabinets; GET /assets/cabinets/{cabinetId}; GET /assets/cabinets/{cabinetId}/slots; POST /iot/cabinets/{cabinetId}/commands/open-slot; GET /iot/cabinets/{cabinetId}/events |
| PA_09_2_Cabinet_Logs_Events | 柜体详情 | PLATFORM_ADMIN | Cabinet | 子页面入口 | PA_09_Cabinet_Detail | asset-investment | ui.Platform_Admin_Portal.Cabinet.view | GET /assets/cabinets; GET /assets/cabinets/{cabinetId}; GET /assets/cabinets/{cabinetId}/slots; POST /iot/cabinets/{cabinetId}/commands/open-slot; GET /iot/cabinets/{cabinetId}/events |
| PA_09_3_Cabinet_OTA_RemoteCommand | 柜体详情 | PLATFORM_ADMIN | Cabinet | 子页面入口 | PA_09_Cabinet_Detail | asset-investment | ui.Platform_Admin_Portal.Cabinet.manage | GET /assets/cabinets; GET /assets/cabinets/{cabinetId}; GET /assets/cabinets/{cabinetId}/slots; POST /iot/cabinets/{cabinetId}/commands/open-slot; GET /iot/cabinets/{cabinetId}/events |
| PA_09_Cabinet_Detail | 柜体部署详情 | PLATFORM_ADMIN | Cabinet | 菜单入口 |  | asset-investment | ui.Platform_Admin_Portal.Cabinet.view | GET /assets/cabinets; GET /assets/cabinets/{cabinetId}; GET /assets/cabinets/{cabinetId}/slots; POST /iot/cabinets/{cabinetId}/commands/open-slot; GET /iot/cabinets/{cabinetId}/events |
| PA_10_1_Orders_Advanced_Filter_Drawer | 订单监控 | PLATFORM_ADMIN | Orders | 子页面入口 | PA_10_Global_Orders_Monitor | order | ui.Platform_Admin_Portal.Orders.view | GET /orders; GET /orders/{orderId}; POST /orders; POST /orders/{orderId}/cancel; POST /orders/{orderId}/dispatch; GET /orders/{orderId}/timeline |
| PA_10_Global_Orders_Monitor | 全局订单监控 | PLATFORM_ADMIN | Global | 菜单入口 |  | order | ui.Platform_Admin_Portal.Global.view | GET /orders; GET /orders/{orderId}; POST /orders; POST /orders/{orderId}/cancel; POST /orders/{orderId}/dispatch; GET /orders/{orderId}/timeline |
| PA_11_1_Order_Evidence | 订单详情 | PLATFORM_ADMIN | Order | 子页面入口 | PA_11_Order_Detail | order | ui.Platform_Admin_Portal.Order.view | GET /orders; GET /orders/{orderId}; POST /orders; POST /orders/{orderId}/cancel; POST /orders/{orderId}/dispatch; GET /orders/{orderId}/timeline; POST /evidence/upload-url; POST /evidence/records; GET /orders/{orderId}/evidence |
| PA_11_2_Order_Settlement_Payment | 订单详情 | PLATFORM_ADMIN | Order | 子页面入口 | PA_11_Order_Detail | finance | ui.Platform_Admin_Portal.Order.manage | GET /finance/settlements; GET /finance/settlements/{settlementId}; GET /finance/settlements/{settlementId}/lines; POST /finance/settlements/{settlementId}/export; GET /orders; GET /orders/{orderId}; POST /orders; POST /orders/{orderId}/cancel; POST /orders/{orderId}/dispatch; GET /orders/{orderId}/timeline |
| PA_11_3_Order_Audit_Log | 订单详情 | PLATFORM_ADMIN | Order | 子页面入口 | PA_11_Order_Detail | order | ui.Platform_Admin_Portal.Order.view | GET /orders; GET /orders/{orderId}; POST /orders; POST /orders/{orderId}/cancel; POST /orders/{orderId}/dispatch; GET /orders/{orderId}/timeline; GET /audit/logs; GET /audit/logs/{logId}; POST /audit/exports |
| PA_11_4_Order_Assign_Modal | 订单详情 | PLATFORM_ADMIN | Order | 子页面入口 | PA_11_Order_Detail | order | ui.Platform_Admin_Portal.Order.manage | GET /orders; GET /orders/{orderId}; POST /orders; POST /orders/{orderId}/cancel; POST /orders/{orderId}/dispatch; GET /orders/{orderId}/timeline |
| PA_11_5_Order_ForceClose_Modal | 订单详情 | PLATFORM_ADMIN | Order | 子页面入口 | PA_11_Order_Detail | order | ui.Platform_Admin_Portal.Order.view | GET /orders; GET /orders/{orderId}; POST /orders; POST /orders/{orderId}/cancel; POST /orders/{orderId}/dispatch; GET /orders/{orderId}/timeline |
| PA_11_6_Order_Close_Modal | 订单详情 | PLATFORM_ADMIN | Order | 子页面入口 | PA_11_Order_Detail | order | ui.Platform_Admin_Portal.Order.view | GET /orders; GET /orders/{orderId}; POST /orders; POST /orders/{orderId}/cancel; POST /orders/{orderId}/dispatch; GET /orders/{orderId}/timeline |
| PA_11_Order_Detail | 订单详情 | PLATFORM_ADMIN | Order | 菜单入口 |  | order | ui.Platform_Admin_Portal.Order.view | GET /orders; GET /orders/{orderId}; POST /orders; POST /orders/{orderId}/cancel; POST /orders/{orderId}/dispatch; GET /orders/{orderId}/timeline |
| PA_12_1_ServiceFee_Edit | 服务费策略 | PLATFORM_ADMIN | ServiceFee | 编辑 | PA_12_ServiceFee_Policy | ui-static | ui.Platform_Admin_Portal.ServiceFee.manage |  |
| PA_12_ServiceFee_Policy | 服务费策略（Service Fee Policy） | PLATFORM_ADMIN | ServiceFee | 菜单入口 |  | ui-static | ui.Platform_Admin_Portal.ServiceFee.manage |  |
| PA_13_1_RevenueShare_Edit | 分润策略 | PLATFORM_ADMIN | RevenueShare | 编辑 | PA_13_RevenueShare_Policy | ui-static | ui.Platform_Admin_Portal.RevenueShare.manage |  |
| PA_13_RevenueShare_Policy | 分润策略（Revenue Share） | PLATFORM_ADMIN | RevenueShare | 菜单入口 |  | ui-static | ui.Platform_Admin_Portal.RevenueShare.manage |  |
| PA_14_1_Retention_Edit | 滞留策略 | PLATFORM_ADMIN | Retention | 编辑 | PA_14_Retention_Policy | ui-static | ui.Platform_Admin_Portal.Retention.manage |  |
| PA_14_2_Retention_Reminders_Log | 滞留策略 | PLATFORM_ADMIN | Retention | 子页面入口 | PA_14_Retention_Policy | ui-static | ui.Platform_Admin_Portal.Retention.view |  |
| PA_14_Retention_Policy | 滞留策略（Retention Policy） | PLATFORM_ADMIN | Retention | 菜单入口 |  | ui-static | ui.Platform_Admin_Portal.Retention.manage |  |
| PA_15_1_Settlement_Run_Create | 结算中心 | PLATFORM_ADMIN | Settlement | 新增 | PA_15_Settlements | finance | ui.Platform_Admin_Portal.Settlement.manage | GET /finance/settlements; GET /finance/settlements/{settlementId}; GET /finance/settlements/{settlementId}/lines; POST /finance/settlements/{settlementId}/export |
| PA_15_Settlements | 结算中心 | PLATFORM_ADMIN | Settlements | 菜单入口 |  | finance | ui.Platform_Admin_Portal.Settlements.manage | GET /finance/settlements; GET /finance/settlements/{settlementId}; GET /finance/settlements/{settlementId}/lines; POST /finance/settlements/{settlementId}/export |
| PA_16_1_Settlement_Shares_Detail | 结算详情 | PLATFORM_ADMIN | Settlement | 进入详情 | PA_16_Settlement_Detail | finance | ui.Platform_Admin_Portal.Settlement.manage | GET /finance/settlements; GET /finance/settlements/{settlementId}; GET /finance/settlements/{settlementId}/lines; POST /finance/settlements/{settlementId}/export |
| PA_16_Settlement_Detail | 结算详情与分账明细 | PLATFORM_ADMIN | Settlement | 菜单入口 |  | finance | ui.Platform_Admin_Portal.Settlement.manage | GET /finance/settlements; GET /finance/settlements/{settlementId}; GET /finance/settlements/{settlementId}/lines; POST /finance/settlements/{settlementId}/export |
| PA_17_Refunds | 退款审批中心 | PLATFORM_ADMIN | Refunds | 菜单入口 |  | finance | ui.Platform_Admin_Portal.Refunds.manage | GET /finance/refunds; POST /finance/refunds; POST /finance/refunds/{refundId}/approve |
| PA_18_1_Refund_Approve_Modal | 退款单详情 | PLATFORM_ADMIN | Refund | 子页面入口 | PA_18_Refund_Detail | finance | ui.Platform_Admin_Portal.Refund.manage | GET /finance/refunds; POST /finance/refunds; POST /finance/refunds/{refundId}/approve |
| PA_18_2_Refund_Reject_Modal | 退款单详情 | PLATFORM_ADMIN | Refund | 子页面入口 | PA_18_Refund_Detail | finance | ui.Platform_Admin_Portal.Refund.manage | GET /finance/refunds; POST /finance/refunds; POST /finance/refunds/{refundId}/approve |
| PA_18_3_Refund_Callback_Log | 退款单详情 | PLATFORM_ADMIN | Refund | 子页面入口 | PA_18_Refund_Detail | finance | ui.Platform_Admin_Portal.Refund.manage | GET /finance/refunds; POST /finance/refunds; POST /finance/refunds/{refundId}/approve |
| PA_18_Refund_Detail | 退款单详情（审批/拒绝） | PLATFORM_ADMIN | Refund | 菜单入口 |  | finance | ui.Platform_Admin_Portal.Refund.manage | GET /finance/refunds; POST /finance/refunds; POST /finance/refunds/{refundId}/approve |
| PA_19_1_RiskPolicy_Edit | 风控策略 | PLATFORM_ADMIN | RiskPolicy | 编辑 | PA_19_Risk_Policies | ui-static | ui.Platform_Admin_Portal.RiskPolicy.manage |  |
| PA_19_Risk_Policies | 风控策略配置 | PLATFORM_ADMIN | Risk | 菜单入口 |  | ui-static | ui.Platform_Admin_Portal.Risk.view |  |
| PA_20_1_RiskEvaluation_Create | 风控评估 | PLATFORM_ADMIN | RiskEvaluation | 新增 | PA_20_Risk_Evaluations | ui-static | ui.Platform_Admin_Portal.RiskEvaluation.manage |  |
| PA_20_2_RiskEvaluation_Detail | 风控评估详情 | PLATFORM_ADMIN | RiskEvaluation | 进入详情 | PA_20_Risk_Evaluations | ui-static | ui.Platform_Admin_Portal.RiskEvaluation.view |  |
| PA_20_Risk_Evaluations | 风控评估记录 | PLATFORM_ADMIN | Risk | 菜单入口 |  | ui-static | ui.Platform_Admin_Portal.Risk.view |  |
| PA_21_1_InsurancePolicy_Edit | 保险配置 | PLATFORM_ADMIN | InsurancePolicy | 编辑 | PA_21_Insurance_Policies | ui-static | ui.Platform_Admin_Portal.InsurancePolicy.manage |  |
| PA_21_Insurance_Policies | 保险产品/保单配置 | PLATFORM_ADMIN | Insurance | 菜单入口 |  | ui-static | ui.Platform_Admin_Portal.Insurance.view |  |
| PA_22_1_Claim_Create | 发起理赔 | PLATFORM_ADMIN | Claim | 新增 | PA_22_Insurance_Claims | ui-static | ui.Platform_Admin_Portal.Claim.manage |  |
| PA_22_Insurance_Claims | 理赔中心 | PLATFORM_ADMIN | Insurance | 菜单入口 |  | ui-static | ui.Platform_Admin_Portal.Insurance.view |  |
| PA_23_2_Claim_Callback_Log | 理赔详情 | PLATFORM_ADMIN | Claim | 子页面入口 | PA_23_Insurance_Claim_Detail | ui-static | ui.Platform_Admin_Portal.Claim.view |  |
| PA_23_Insurance_Claim_Detail | 理赔详情 | PLATFORM_ADMIN | Insurance | 菜单入口 |  | ui-static | ui.Platform_Admin_Portal.Insurance.view |  |
| PA_24_1_NotificationTemplate_Edit | 通知模板编辑 | PLATFORM_ADMIN | NotificationTemplate | 编辑 | PA_24_Notification_Templates | ui-static | ui.Platform_Admin_Portal.NotificationTemplate.manage |  |
| PA_24_2_Channel_Config | 通知渠道配置 | PLATFORM_ADMIN | Channel | 子页面入口 | PA_24_Notification_Templates | ui-static | ui.Platform_Admin_Portal.Channel.manage |  |
| PA_24_Notification_Templates | 通知模板管理 | PLATFORM_ADMIN | Notification | 菜单入口 |  | ui-static | ui.Platform_Admin_Portal.Notification.view |  |
| PA_25_1_DLQ_Message_Detail | DLQ 消息详情 | PLATFORM_ADMIN | DLQ | 进入详情 | PA_25_Notifications_DLQ | ui-static | ui.Platform_Admin_Portal.DLQ.view |  |
| PA_25_2_DLQ_Retry_Confirm_Modal | DLQ死信队列 | PLATFORM_ADMIN | DLQ | 子页面入口 | PA_25_Notifications_DLQ | ui-static | ui.Platform_Admin_Portal.DLQ.view |  |
| PA_25_3_Process_Due_Run_Modal | 通知中心 | PLATFORM_ADMIN | Process | 子页面入口 | PA_25_Notifications_DLQ | ui-static | ui.Platform_Admin_Portal.Process.view |  |
| PA_25_Notifications_DLQ | 通知死信队列（DLQ） | PLATFORM_ADMIN | Notifications | 菜单入口 |  | ui-static | ui.Platform_Admin_Portal.Notifications.view |  |
| PA_26_1_Reconciliation_Run_Config | 对账跑批 | PLATFORM_ADMIN | Reconciliation | 子页面入口 | PA_26_Reconciliation_Run_Import | finance | ui.Platform_Admin_Portal.Reconciliation.manage | GET /finance/reconciliation/tickets; POST /finance/reconciliation/tickets; GET /finance/reconciliation/tickets/{ticketId} |
| PA_26_2_Reconciliation_Import_Drawer | 对账导入 | PLATFORM_ADMIN | Reconciliation | 导入 | PA_26_Reconciliation_Run_Import | finance | ui.Platform_Admin_Portal.Reconciliation.manage | GET /finance/reconciliation/tickets; POST /finance/reconciliation/tickets; GET /finance/reconciliation/tickets/{ticketId} |
| PA_26_Reconciliation_Run_Import | 对账：导入渠道账单与跑批 | PLATFORM_ADMIN | Reconciliation | 菜单入口 |  | finance | ui.Platform_Admin_Portal.Reconciliation.manage | GET /finance/reconciliation/tickets; POST /finance/reconciliation/tickets; GET /finance/reconciliation/tickets/{ticketId} |
| PA_27_Reconciliation_Tickets | 对账差异工单 | PLATFORM_ADMIN | Reconciliation | 菜单入口 |  | finance | ui.Platform_Admin_Portal.Reconciliation.view | GET /finance/reconciliation/tickets; POST /finance/reconciliation/tickets; GET /finance/reconciliation/tickets/{ticketId} |
| PA_28_Reconciliation_Ticket_Detail | 对账工单详情 | PLATFORM_ADMIN | Reconciliation | 菜单入口 |  | finance | ui.Platform_Admin_Portal.Reconciliation.view | GET /finance/reconciliation/tickets; POST /finance/reconciliation/tickets; GET /finance/reconciliation/tickets/{ticketId} |
| PA_29_1_Audit_Export_History | 审计导出 | PLATFORM_ADMIN | Audit | 子页面入口 | PA_29_Audit_Export | audit | ui.Platform_Admin_Portal.Audit.view | GET /audit/logs; GET /audit/logs/{logId}; POST /audit/exports |
| PA_29_Audit_Export | 审计日志导出（不可变审计） | PLATFORM_ADMIN | Audit | 菜单入口 |  | audit | ui.Platform_Admin_Portal.Audit.view | GET /audit/logs; GET /audit/logs/{logId}; POST /audit/exports |
| PA_30_1_MFA_Devices | MFA 设备管理 | PLATFORM_ADMIN | MFA | 子页面入口 | PA_30_System_Settings | asset-investment | ui.Platform_Admin_Portal.MFA.view | GET /assets/cabinets; GET /assets/cabinets/{cabinetId}; GET /assets/cabinets/{cabinetId}/slots; POST /iot/cabinets/{cabinetId}/commands/open-slot; GET /iot/cabinets/{cabinetId}/events |
| PA_30_2_Integrations | 集成配置 | PLATFORM_ADMIN | Integrations | 子页面入口 | PA_30_System_Settings | ui-static | ui.Platform_Admin_Portal.Integrations.view |  |
| PA_30_3_Data_Retention_Settings | 数据保留与脱敏 | PLATFORM_ADMIN | Data | 子页面入口 | PA_30_System_Settings | ui-static | ui.Platform_Admin_Portal.Data.manage |  |
| PA_30_System_Settings | 系统设置（安全基线/MFA） | PLATFORM_ADMIN | System | 菜单入口 |  | ui-static | ui.Platform_Admin_Portal.System.manage |  |
| PA_31_1_Profile_ChangePassword | 修改密码 | PLATFORM_ADMIN | Profile | 子页面入口 | PA_31_Profile | ui-static | ui.Platform_Admin_Portal.Profile.view |  |
| PA_31_2_Profile_MFA_Setup | MFA 设置 | PLATFORM_ADMIN | Profile | 子页面入口 | PA_31_Profile | ui-static | ui.Platform_Admin_Portal.Profile.view |  |
| PA_31_Profile | 我的账号 | PLATFORM_ADMIN | Profile | 菜单入口 |  | ui-static | ui.Platform_Admin_Portal.Profile.view |  |
| PA_32_1_Agreement_Create_Wizard | 新增商业协议 | PLATFORM_ADMIN | Agreement | 新增 | PA_32_Commercial_Agreements | ui-static | ui.Platform_Admin_Portal.Agreement.manage |  |
| PA_32_Commercial_Agreements | 商业协议管理 | PLATFORM_ADMIN | Commercial | 菜单入口 |  | ui-static | ui.Platform_Admin_Portal.Commercial.view |  |
| PA_33_1_Agreement_PricingAuthority | 协议详情 | PLATFORM_ADMIN | Agreement | 子页面入口 | PA_33_Agreement_Detail_Overview | ui-static | ui.Platform_Admin_Portal.Agreement.view |  |
| PA_33_2_Agreement_CollectorAccount | 协议详情 | PLATFORM_ADMIN | Agreement | 子页面入口 | PA_33_Agreement_Detail_Overview | finance | ui.Platform_Admin_Portal.Agreement.view | GET /finance/payout-accounts; POST /finance/payout-accounts; PATCH /finance/payout-accounts/{accountId}; POST /finance/payout-accounts/{accountId}/submit-review |
| PA_33_3_Agreement_RevenueShareRules | 协议详情 | PLATFORM_ADMIN | Agreement | 子页面入口 | PA_33_Agreement_Detail_Overview | ui-static | ui.Platform_Admin_Portal.Agreement.view |  |
| PA_33_4_Agreement_SettlementSchedule | 协议详情 | PLATFORM_ADMIN | Agreement | 子页面入口 | PA_33_Agreement_Detail_Overview | finance | ui.Platform_Admin_Portal.Agreement.manage | GET /finance/settlements; GET /finance/settlements/{settlementId}; GET /finance/settlements/{settlementId}/lines; POST /finance/settlements/{settlementId}/export |
| PA_33_5_Agreement_Audit | 协议详情 | PLATFORM_ADMIN | Agreement | 子页面入口 | PA_33_Agreement_Detail_Overview | audit | ui.Platform_Admin_Portal.Agreement.view | GET /audit/logs; GET /audit/logs/{logId}; POST /audit/exports |
| PA_33_Agreement_Detail_Overview | 协议详情 | PLATFORM_ADMIN | Agreement | 菜单入口 |  | ui-static | ui.Platform_Admin_Portal.Agreement.view |  |
| PA_34_1_PricingAuthority_Detail | 定价权主体详情 | PLATFORM_ADMIN | PricingAuthority | 进入详情 | PA_34_PricingAuthority_List | ui-static | ui.Platform_Admin_Portal.PricingAuthority.view |  |
| PA_34_PricingAuthority_List | 定价权主体管理 | PLATFORM_ADMIN | PricingAuthority | 菜单入口 |  | ui-static | ui.Platform_Admin_Portal.PricingAuthority.view |  |
| PA_35_1_CollectorAccount_Detail | 收款账户详情 | PLATFORM_ADMIN | CollectorAccount | 进入详情 | PA_35_CollectorAccounts_List | finance | ui.Platform_Admin_Portal.CollectorAccount.view | GET /finance/payout-accounts; POST /finance/payout-accounts; PATCH /finance/payout-accounts/{accountId}; POST /finance/payout-accounts/{accountId}/submit-review |
| PA_35_2_CollectorAccount_Create | 新增账户 | PLATFORM_ADMIN | CollectorAccount | 新增 | PA_35_CollectorAccounts_List | finance | ui.Platform_Admin_Portal.CollectorAccount.manage | GET /finance/payout-accounts; POST /finance/payout-accounts; PATCH /finance/payout-accounts/{accountId}; POST /finance/payout-accounts/{accountId}/submit-review |
| PA_35_CollectorAccounts_List | 收款账户管理 | PLATFORM_ADMIN | CollectorAccounts | 菜单入口 |  | finance | ui.Platform_Admin_Portal.CollectorAccounts.view | GET /finance/payout-accounts; POST /finance/payout-accounts; PATCH /finance/payout-accounts/{accountId}; POST /finance/payout-accounts/{accountId}/submit-review |
| PA_36_1_Task_Detail | 任务详情 | PLATFORM_ADMIN | Task | 进入详情 | PA_36_Global_Tasks_Monitor | task | ui.Platform_Admin_Portal.Task.view | GET /tasks; POST /tasks/{taskId}/assign; POST /tasks/{taskId}/accept; POST /tasks/{taskId}/complete |
| PA_36_Global_Tasks_Monitor | 全局任务监控 | PLATFORM_ADMIN | Global | 菜单入口 |  | task | ui.Platform_Admin_Portal.Global.view | GET /tasks; POST /tasks/{taskId}/assign; POST /tasks/{taskId}/accept; POST /tasks/{taskId}/complete |
| PA_37_1_Ticket_Detail | 工单详情 | PLATFORM_ADMIN | Ticket | 进入详情 | PA_37_Tickets_Center | finance | ui.Platform_Admin_Portal.Ticket.view | GET /finance/reconciliation/tickets; POST /finance/reconciliation/tickets; GET /finance/reconciliation/tickets/{ticketId} |
| PA_37_Tickets_Center | 工单中心 | PLATFORM_ADMIN | Tickets | 菜单入口 |  | finance | ui.Platform_Admin_Portal.Tickets.view | GET /finance/reconciliation/tickets; POST /finance/reconciliation/tickets; GET /finance/reconciliation/tickets/{ticketId} |
| PA_38_Blacklist_Management | 黑名单管理配置 | PLATFORM_ADMIN | Blacklist | 菜单入口 |  | ui-static | ui.Platform_Admin_Portal.Blacklist.view |  |
| PA_39_1_DeviceInvestor_Detail | 设备投资方详情 | PLATFORM_ADMIN | DeviceInvestor | 进入详情 | PA_39_DeviceInvestors_List | asset-investment | ui.Platform_Admin_Portal.DeviceInvestor.view | GET /assets/cabinets; GET /assets/cabinets/{cabinetId}; GET /assets/cabinets/{cabinetId}/slots; POST /iot/cabinets/{cabinetId}/commands/open-slot; GET /iot/cabinets/{cabinetId}/events; GET /asset/device-investors; GET /asset/device-investors/{investorId}; GET /asset/device-investors/{investorId}/cabinets |
| PA_39_2_DeviceInvestor_PayoutAccount_Review | 新增账户 | PLATFORM_ADMIN | DeviceInvestor | 子页面入口 | PA_39_DeviceInvestors_List | asset-investment | ui.Platform_Admin_Portal.DeviceInvestor.view | GET /assets/cabinets; GET /assets/cabinets/{cabinetId}; GET /assets/cabinets/{cabinetId}/slots; POST /iot/cabinets/{cabinetId}/commands/open-slot; GET /iot/cabinets/{cabinetId}/events; GET /asset/device-investors; GET /asset/device-investors/{investorId}; GET /asset/device-investors/{investorId}/cabinets; GET /finance/payout-accounts; POST /finance/payout-accounts; PATCH /finance/payout-accounts/{accountId}; POST /finance/payout-accounts/{accountId}/submit-review |
| PA_39_DeviceInvestors_List | 设备投资方管理 | PLATFORM_ADMIN | DeviceInvestors | 菜单入口 |  | asset-investment | ui.Platform_Admin_Portal.DeviceInvestors.view | GET /assets/cabinets; GET /assets/cabinets/{cabinetId}; GET /assets/cabinets/{cabinetId}/slots; POST /iot/cabinets/{cabinetId}/commands/open-slot; GET /iot/cabinets/{cabinetId}/events; GET /asset/device-investors; GET /asset/device-investors/{investorId}; GET /asset/device-investors/{investorId}/cabinets |
| PA_40_CabinetOwnership_History | 柜体详情 | PLATFORM_ADMIN | CabinetOwnership | 菜单入口 |  | asset-investment | ui.Platform_Admin_Portal.CabinetOwnership.view | GET /assets/cabinets; GET /assets/cabinets/{cabinetId}; GET /assets/cabinets/{cabinetId}/slots; POST /iot/cabinets/{cabinetId}/commands/open-slot; GET /iot/cabinets/{cabinetId}/events |
| PA_41_1_CabinetStoreBinding_Create | 新增账户 | PLATFORM_ADMIN | CabinetStoreBinding | 新增 | PA_41_CabinetStoreBindings_List | asset-investment | ui.Platform_Admin_Portal.CabinetStoreBinding.manage | GET /assets/cabinets; GET /assets/cabinets/{cabinetId}; GET /assets/cabinets/{cabinetId}/slots; POST /iot/cabinets/{cabinetId}/commands/open-slot; GET /iot/cabinets/{cabinetId}/events; GET /asset/cabinets/{cabinetId}/store-bindings; POST /asset/cabinets/{cabinetId}/store-bindings; PATCH /asset/cabinet-store-bindings/{bindingId}; POST /asset/cabinet-store-bindings/{bindingId}/approve |
| PA_41_2_CabinetStoreBinding_Edit | 新增账户 | PLATFORM_ADMIN | CabinetStoreBinding | 编辑 | PA_41_CabinetStoreBindings_List | asset-investment | ui.Platform_Admin_Portal.CabinetStoreBinding.manage | GET /assets/cabinets; GET /assets/cabinets/{cabinetId}; GET /assets/cabinets/{cabinetId}/slots; POST /iot/cabinets/{cabinetId}/commands/open-slot; GET /iot/cabinets/{cabinetId}/events; GET /asset/cabinets/{cabinetId}/store-bindings; POST /asset/cabinets/{cabinetId}/store-bindings; PATCH /asset/cabinet-store-bindings/{bindingId}; POST /asset/cabinet-store-bindings/{bindingId}/approve |
| PA_41_3_CabinetStoreBinding_Approval | 绑定变更审批 | PLATFORM_ADMIN | CabinetStoreBinding | 绑定/配置 | PA_41_CabinetStoreBindings_List | asset-investment | ui.Platform_Admin_Portal.CabinetStoreBinding.manage | GET /assets/cabinets; GET /assets/cabinets/{cabinetId}; GET /assets/cabinets/{cabinetId}/slots; POST /iot/cabinets/{cabinetId}/commands/open-slot; GET /iot/cabinets/{cabinetId}/events; GET /asset/cabinets/{cabinetId}/store-bindings; POST /asset/cabinets/{cabinetId}/store-bindings; PATCH /asset/cabinet-store-bindings/{bindingId}; POST /asset/cabinet-store-bindings/{bindingId}/approve |
| PA_41_CabinetStoreBindings_List | 扫码核验并绑定门店 | PLATFORM_ADMIN | CabinetStoreBindings | 菜单入口 |  | asset-investment | ui.Platform_Admin_Portal.CabinetStoreBindings.manage | GET /assets/cabinets; GET /assets/cabinets/{cabinetId}; GET /assets/cabinets/{cabinetId}/slots; POST /iot/cabinets/{cabinetId}/commands/open-slot; GET /iot/cabinets/{cabinetId}/events; GET /asset/cabinets/{cabinetId}/store-bindings; POST /asset/cabinets/{cabinetId}/store-bindings; PATCH /asset/cabinet-store-bindings/{bindingId}; POST /asset/cabinet-store-bindings/{bindingId}/approve |
| PA_42_CabinetRoutingPolicy | 柜体订单路由策略 | PLATFORM_ADMIN | CabinetRoutingPolicy | 菜单入口 |  | asset-investment | ui.Platform_Admin_Portal.CabinetRoutingPolicy.manage | GET /assets/cabinets; GET /assets/cabinets/{cabinetId}; GET /assets/cabinets/{cabinetId}/slots; POST /iot/cabinets/{cabinetId}/commands/open-slot; GET /iot/cabinets/{cabinetId}/events |

## Platform_CS_Portal
| page_key | 页面中文名 | 角色 | 模块 | 入口 | 父页面 | api_tag | permission_key | api_endpoints |
|---|---|---|---|---|---|---|---|---|
| PC_01_Login | 备至·智洗平台 | PLATFORM_CS | Login | 入口-登录 |  | identity | ui.Platform_CS_Portal.Login.view | POST /auth/login; POST /auth/mfa/verify; POST /auth/logout |
| PC_02_CS_Dashboard | 运营大盘 | PLATFORM_CS | CS | 入口-首页 |  | ui-static | ui.Platform_CS_Portal.CS.view |  |
| PC_03_Tickets_List | 客服工单中心 | PLATFORM_CS | Tickets | 菜单入口 |  | finance | ui.Platform_CS_Portal.Tickets.view | GET /finance/reconciliation/tickets; POST /finance/reconciliation/tickets; GET /finance/reconciliation/tickets/{ticketId} |
| PC_04_Ticket_Detail | 投诉/纠纷工单详情（客服） | PLATFORM_CS | Ticket | 菜单入口 |  | finance | ui.Platform_CS_Portal.Ticket.view | GET /finance/reconciliation/tickets; POST /finance/reconciliation/tickets; GET /finance/reconciliation/tickets/{ticketId} |
| PC_05_Order_Search | 订单查询 | PLATFORM_CS | Order | 菜单入口 |  | order | ui.Platform_CS_Portal.Order.view | GET /orders; GET /orders/{orderId}; POST /orders; POST /orders/{orderId}/cancel; POST /orders/{orderId}/dispatch; GET /orders/{orderId}/timeline |
| PC_06_Order_Detail | 订单详情（客服） | PLATFORM_CS | Order | 菜单入口 |  | order | ui.Platform_CS_Portal.Order.view | GET /orders; GET /orders/{orderId}; POST /orders; POST /orders/{orderId}/cancel; POST /orders/{orderId}/dispatch; GET /orders/{orderId}/timeline |
| PC_07_Evidence_Export | 审计日志导出（不可变审计） | PLATFORM_CS | Evidence | 菜单入口 |  | evidence | ui.Platform_CS_Portal.Evidence.view | POST /evidence/upload-url; POST /evidence/records; GET /orders/{orderId}/evidence |
| PC_08_Insurance_Claims | 理赔中心 | PLATFORM_CS | Insurance | 菜单入口 |  | ui-static | ui.Platform_CS_Portal.Insurance.view |  |
| PC_09_Claim_Detail | 理赔详情（客服） | PLATFORM_CS | Claim | 菜单入口 |  | ui-static | ui.Platform_CS_Portal.Claim.view |  |
| PC_10_Refund_Assist | 退款审批中心 | PLATFORM_CS | Refund | 菜单入口 |  | finance | ui.Platform_CS_Portal.Refund.manage | GET /finance/refunds; POST /finance/refunds; POST /finance/refunds/{refundId}/approve |
| PC_11_Profile | 我的账号 | PLATFORM_CS | Profile | 菜单入口 |  | ui-static | ui.Platform_CS_Portal.Profile.view |  |

## Platform_Finance_Portal
| page_key | 页面中文名 | 角色 | 模块 | 入口 | 父页面 | api_tag | permission_key | api_endpoints |
|---|---|---|---|---|---|---|---|---|
| PF_01_Login | 备至·智洗平台 | PLATFORM_FINANCE | Login | 入口-登录 |  | identity | ui.Platform_Finance_Portal.Login.view | POST /auth/login; POST /auth/mfa/verify; POST /auth/logout |
| PF_02_Finance_Dashboard | 运营大盘 | PLATFORM_FINANCE | Finance | 入口-首页 |  | ui-static | ui.Platform_Finance_Portal.Finance.view |  |
| PF_03_Commercial_Agreements | 商业协议（财务视图） | PLATFORM_FINANCE | Commercial | 菜单入口 |  | ui-static | ui.Platform_Finance_Portal.Commercial.view |  |
| PF_04_Collector_Accounts | 收款账户（代收/入账） | PLATFORM_FINANCE | Collector | 菜单入口 |  | finance | ui.Platform_Finance_Portal.Collector.view | GET /finance/payout-accounts; POST /finance/payout-accounts; PATCH /finance/payout-accounts/{accountId}; POST /finance/payout-accounts/{accountId}/submit-review |
| PF_05_Settlements_List | 结算批次 | PLATFORM_FINANCE | Settlements | 菜单入口 |  | finance | ui.Platform_Finance_Portal.Settlements.manage | GET /finance/settlements; GET /finance/settlements/{settlementId}; GET /finance/settlements/{settlementId}/lines; POST /finance/settlements/{settlementId}/export |
| PF_06_Settlement_Detail | 结算批次详情与分账明细 | PLATFORM_FINANCE | Settlement | 菜单入口 |  | finance | ui.Platform_Finance_Portal.Settlement.manage | GET /finance/settlements; GET /finance/settlements/{settlementId}; GET /finance/settlements/{settlementId}/lines; POST /finance/settlements/{settlementId}/export |
| PF_07_Payout_Remittance | 打款：生成打款指令渠道账单与跑批 | PLATFORM_FINANCE | Payout | 菜单入口 |  | finance | ui.Platform_Finance_Portal.Payout.view | GET /finance/payout-accounts; POST /finance/payout-accounts; PATCH /finance/payout-accounts/{accountId}; POST /finance/payout-accounts/{accountId}/submit-review |
| PF_08_Refunds_List | 退款审批中心 | PLATFORM_FINANCE | Refunds | 菜单入口 |  | finance | ui.Platform_Finance_Portal.Refunds.manage | GET /finance/refunds; POST /finance/refunds; POST /finance/refunds/{refundId}/approve |
| PF_09_Refund_Detail | 退款单详情（审批/拒绝） | PLATFORM_FINANCE | Refund | 菜单入口 |  | finance | ui.Platform_Finance_Portal.Refund.manage | GET /finance/refunds; POST /finance/refunds; POST /finance/refunds/{refundId}/approve |
| PF_10_Reconciliation_Run | 对账：导入渠道账单与跑批 | PLATFORM_FINANCE | Reconciliation | 菜单入口 |  | finance | ui.Platform_Finance_Portal.Reconciliation.view | GET /finance/reconciliation/tickets; POST /finance/reconciliation/tickets; GET /finance/reconciliation/tickets/{ticketId} |
| PF_11_Reconciliation_Tickets | 对账差异工单（财务） | PLATFORM_FINANCE | Reconciliation | 菜单入口 |  | finance | ui.Platform_Finance_Portal.Reconciliation.view | GET /finance/reconciliation/tickets; POST /finance/reconciliation/tickets; GET /finance/reconciliation/tickets/{ticketId} |
| PF_12_Reconciliation_Ticket_Detail | 对账工单详情（财务） | PLATFORM_FINANCE | Reconciliation | 菜单入口 |  | finance | ui.Platform_Finance_Portal.Reconciliation.view | GET /finance/reconciliation/tickets; POST /finance/reconciliation/tickets; GET /finance/reconciliation/tickets/{ticketId} |
| PF_13_Invoices_List | 发票审批中心 | PLATFORM_FINANCE | Invoices | 菜单入口 |  | ui-static | ui.Platform_Finance_Portal.Invoices.view |  |
| PF_14_Invoice_Detail | 发票单详情（审批/拒绝） | PLATFORM_FINANCE | Invoice | 菜单入口 |  | ui-static | ui.Platform_Finance_Portal.Invoice.view |  |
| PF_15_Fee_Plans | 费率策略（Service Fee Policy） | PLATFORM_FINANCE | Fee | 菜单入口 |  | ui-static | ui.Platform_Finance_Portal.Fee.view |  |
| PF_16_Finance_Reports | 财务报表 | PLATFORM_FINANCE | Finance | 菜单入口 |  | reports | ui.Platform_Finance_Portal.Finance.view | GET /reports/summary; POST /reports/export |
| PF_17_Audit_Export | 审计日志导出（不可变审计） | PLATFORM_FINANCE | Audit | 菜单入口 |  | audit | ui.Platform_Finance_Portal.Audit.view | GET /audit/logs; GET /audit/logs/{logId}; POST /audit/exports |
| PF_18_Profile | 我的账号 | PLATFORM_FINANCE | Profile | 菜单入口 |  | ui-static | ui.Platform_Finance_Portal.Profile.view |  |

## Platform_IoTOps_Portal
| page_key | 页面中文名 | 角色 | 模块 | 入口 | 父页面 | api_tag | permission_key | api_endpoints |
|---|---|---|---|---|---|---|---|---|
| PI_01_Login | 备至·智洗平台 | PLATFORM_IOT_OPS | Login | 入口-登录 |  | identity | ui.Platform_IoTOps_Portal.Login.view | POST /auth/login; POST /auth/mfa/verify; POST /auth/logout |
| PI_02_IoT_Dashboard | 运营大盘 | PLATFORM_IOT_OPS | IoT | 入口-首页 |  | ui-static | ui.Platform_IoTOps_Portal.IoT.view |  |
| PI_03_Device_Fleet | 设备资产与在线率（柜体/在线率/告警） | PLATFORM_IOT_OPS | Device | 菜单入口 |  | asset-investment | ui.Platform_IoTOps_Portal.Device.view | GET /assets/cabinets; GET /assets/cabinets/{cabinetId}; GET /assets/cabinets/{cabinetId}/slots; POST /iot/cabinets/{cabinetId}/commands/open-slot; GET /iot/cabinets/{cabinetId}/events |
| PI_04_Device_Detail | 设备运维 | PLATFORM_IOT_OPS | Device | 菜单入口 |  | asset-investment | ui.Platform_IoTOps_Portal.Device.view | GET /assets/cabinets; GET /assets/cabinets/{cabinetId}; GET /assets/cabinets/{cabinetId}/slots; POST /iot/cabinets/{cabinetId}/commands/open-slot; GET /iot/cabinets/{cabinetId}/events |
| PI_05_OTA_RemoteCommand | 设备运维 | PLATFORM_IOT_OPS | OTA | 菜单入口 |  | ui-static | ui.Platform_IoTOps_Portal.OTA.manage |  |
| PI_06_IoT_Events | IoT 事件（MQTT）接入 | PLATFORM_IOT_OPS | IoT | 菜单入口 |  | ui-static | ui.Platform_IoTOps_Portal.IoT.view |  |
| PI_07_IoT_Event_Detail | IoT 事件 | PLATFORM_IOT_OPS | IoT | 菜单入口 |  | ui-static | ui.Platform_IoTOps_Portal.IoT.view |  |
| PI_08_IoT_Replay | IoT 事件回放（运维）（Replay） | PLATFORM_IOT_OPS | IoT | 菜单入口 |  | ui-static | ui.Platform_IoTOps_Portal.IoT.view |  |
| PI_09_Alerts_List | 告警/故障中心 | PLATFORM_IOT_OPS | Alerts | 菜单入口 |  | ui-static | ui.Platform_IoTOps_Portal.Alerts.view |  |
| PI_10_Alert_Detail | 异常告警详情 | PLATFORM_IOT_OPS | Alert | 菜单入口 |  | ui-static | ui.Platform_IoTOps_Portal.Alert.view |  |
| PI_11_Profile | 我的账号 | PLATFORM_IOT_OPS | Profile | 菜单入口 |  | ui-static | ui.Platform_IoTOps_Portal.Profile.view |  |

## Platform_Ops_Portal
| page_key | 页面中文名 | 角色 | 模块 | 入口 | 父页面 | api_tag | permission_key | api_endpoints |
|---|---|---|---|---|---|---|---|---|
| PO_01_Login | 备至·智洗平台 | PLATFORM_OPS | Login | 入口-登录 |  | identity | ui.Platform_Ops_Portal.Login.view | POST /auth/login; POST /auth/mfa/verify; POST /auth/logout |
| PO_02_Ops_Dashboard | 运营大盘 | PLATFORM_OPS | Ops | 入口-首页 |  | ui-static | ui.Platform_Ops_Portal.Ops.view |  |
| PO_03_Orders_Monitor | 订单监控 | PLATFORM_OPS | Orders | 菜单入口 |  | order | ui.Platform_Ops_Portal.Orders.view | GET /orders; GET /orders/{orderId}; POST /orders; POST /orders/{orderId}/cancel; POST /orders/{orderId}/dispatch; GET /orders/{orderId}/timeline |
| PO_04_1_Order_Evidence | 订单详情 | PLATFORM_OPS | Order | 子页面入口 | PO_04_Order_Detail | order | ui.Platform_Ops_Portal.Order.view | GET /orders; GET /orders/{orderId}; POST /orders; POST /orders/{orderId}/cancel; POST /orders/{orderId}/dispatch; GET /orders/{orderId}/timeline; POST /evidence/upload-url; POST /evidence/records; GET /orders/{orderId}/evidence |
| PO_04_2_Order_Audit_Log | 订单详情 | PLATFORM_OPS | Order | 子页面入口 | PO_04_Order_Detail | order | ui.Platform_Ops_Portal.Order.view | GET /orders; GET /orders/{orderId}; POST /orders; POST /orders/{orderId}/cancel; POST /orders/{orderId}/dispatch; GET /orders/{orderId}/timeline; GET /audit/logs; GET /audit/logs/{logId}; POST /audit/exports |
| PO_04_3_Order_ForceClose_Modal | 订单详情 | PLATFORM_OPS | Order | 子页面入口 | PO_04_Order_Detail | order | ui.Platform_Ops_Portal.Order.view | GET /orders; GET /orders/{orderId}; POST /orders; POST /orders/{orderId}/cancel; POST /orders/{orderId}/dispatch; GET /orders/{orderId}/timeline |
| PO_04_Order_Detail | 订单详情 | PLATFORM_OPS | Order | 菜单入口 |  | order | ui.Platform_Ops_Portal.Order.view | GET /orders; GET /orders/{orderId}; POST /orders; POST /orders/{orderId}/cancel; POST /orders/{orderId}/dispatch; GET /orders/{orderId}/timeline |
| PO_05_1_Device_Detail | 设备运维 | PLATFORM_OPS | Device | 进入详情 | PO_05_Device_Operations | asset-investment | ui.Platform_Ops_Portal.Device.view | GET /assets/cabinets; GET /assets/cabinets/{cabinetId}; GET /assets/cabinets/{cabinetId}/slots; POST /iot/cabinets/{cabinetId}/commands/open-slot; GET /iot/cabinets/{cabinetId}/events |
| PO_05_2_OTA_RemoteCommand | 设备运维 | PLATFORM_OPS | OTA | 子页面入口 | PO_05_Device_Operations | ui-static | ui.Platform_Ops_Portal.OTA.manage |  |
| PO_05_Device_Operations | 设备运维（柜体/在线率/告警） | PLATFORM_OPS | Device | 菜单入口 |  | asset-investment | ui.Platform_Ops_Portal.Device.view | GET /assets/cabinets; GET /assets/cabinets/{cabinetId}; GET /assets/cabinets/{cabinetId}/slots; POST /iot/cabinets/{cabinetId}/commands/open-slot; GET /iot/cabinets/{cabinetId}/events |
| PO_06_1_IoT_Event_Detail | IoT 事件 | PLATFORM_OPS | IoT | 进入详情 | PO_06_IoT_Events | ui-static | ui.Platform_Ops_Portal.IoT.view |  |
| PO_06_2_IoT_Publish_Drawer | IoT 事件 | PLATFORM_OPS | IoT | 子页面入口 | PO_06_IoT_Events | ui-static | ui.Platform_Ops_Portal.IoT.view |  |
| PO_06_IoT_Events | IoT 事件接入 | PLATFORM_OPS | IoT | 菜单入口 |  | ui-static | ui.Platform_Ops_Portal.IoT.view |  |
| PO_07_1_IoT_Replay_Confirm_Modal | 事件回放 | PLATFORM_OPS | IoT | 子页面入口 | PO_07_IoT_Replay | ui-static | ui.Platform_Ops_Portal.IoT.view |  |
| PO_07_IoT_Replay | IoT 事件回放（Replay） | PLATFORM_OPS | IoT | 菜单入口 |  | ui-static | ui.Platform_Ops_Portal.IoT.view |  |
| PO_08_1_Exception_Create | 异常工单 | PLATFORM_OPS | Exception | 新增 | PO_08_Exception_Tickets | ui-static | ui.Platform_Ops_Portal.Exception.manage |  |
| PO_08_Exception_Tickets | 异常工单中心 | PLATFORM_OPS | Exception | 菜单入口 |  | finance | ui.Platform_Ops_Portal.Exception.view | GET /finance/reconciliation/tickets; POST /finance/reconciliation/tickets; GET /finance/reconciliation/tickets/{ticketId} |
| PO_09_Exception_Ticket_Detail | 异常工单详情 | PLATFORM_OPS | Exception | 菜单入口 |  | finance | ui.Platform_Ops_Portal.Exception.view | GET /finance/reconciliation/tickets; POST /finance/reconciliation/tickets; GET /finance/reconciliation/tickets/{ticketId} |
| PO_10_1_Template_Edit | 通知模板 | PLATFORM_OPS | Template | 编辑 | PO_10_Notification_Templates | ui-static | ui.Platform_Ops_Portal.Template.manage |  |
| PO_10_Notification_Templates | 通知模板 | PLATFORM_OPS | Notification | 菜单入口 |  | ui-static | ui.Platform_Ops_Portal.Notification.view |  |
| PO_11_1_DLQ_Message_Detail | DLQ 消息详情 | PLATFORM_OPS | DLQ | 进入详情 | PO_11_Notifications_DLQ | ui-static | ui.Platform_Ops_Portal.DLQ.view |  |
| PO_11_2_DLQ_Retry_Confirm_Modal | DLQ死信 | PLATFORM_OPS | DLQ | 子页面入口 | PO_11_Notifications_DLQ | ui-static | ui.Platform_Ops_Portal.DLQ.view |  |
| PO_11_Notifications_DLQ | 通知死信（DLQ） | PLATFORM_OPS | Notifications | 菜单入口 |  | ui-static | ui.Platform_Ops_Portal.Notifications.view |  |
| PO_12_1_Run_Config | 对账跑批 | PLATFORM_OPS | Run | 子页面入口 | PO_12_Reconciliation_Run_Import | ui-static | ui.Platform_Ops_Portal.Run.manage |  |
| PO_12_2_Import_Drawer | 对账导入 | PLATFORM_OPS | Import | 导入 | PO_12_Reconciliation_Run_Import | ui-static | ui.Platform_Ops_Portal.Import.manage |  |
| PO_12_Reconciliation_Run_Import | 对账跑批 | PLATFORM_OPS | Reconciliation | 菜单入口 |  | finance | ui.Platform_Ops_Portal.Reconciliation.manage | GET /finance/reconciliation/tickets; POST /finance/reconciliation/tickets; GET /finance/reconciliation/tickets/{ticketId} |
| PO_13_1_Ticket_Detail | 对账工单详情 | PLATFORM_OPS | Ticket | 进入详情 | PO_13_Reconciliation_Tickets | finance | ui.Platform_Ops_Portal.Ticket.view | GET /finance/reconciliation/tickets; POST /finance/reconciliation/tickets; GET /finance/reconciliation/tickets/{ticketId} |
| PO_13_Reconciliation_Tickets | 对账工单 | PLATFORM_OPS | Reconciliation | 菜单入口 |  | finance | ui.Platform_Ops_Portal.Reconciliation.view | GET /finance/reconciliation/tickets; POST /finance/reconciliation/tickets; GET /finance/reconciliation/tickets/{ticketId} |
| PO_14_1_Audit_Export_History | 审计导出 | PLATFORM_OPS | Audit | 子页面入口 | PO_14_Audit_Export | audit | ui.Platform_Ops_Portal.Audit.view | GET /audit/logs; GET /audit/logs/{logId}; POST /audit/exports |
| PO_14_Audit_Export | 审计导出 | PLATFORM_OPS | Audit | 菜单入口 |  | audit | ui.Platform_Ops_Portal.Audit.view | GET /audit/logs; GET /audit/logs/{logId}; POST /audit/exports |
| PO_15_1_ChangePassword | 修改密码 | PLATFORM_OPS | ChangePassword | 子页面入口 | PO_15_Profile | ui-static | ui.Platform_Ops_Portal.ChangePassword.view |  |
| PO_15_Profile | 我的账号 | PLATFORM_OPS | Profile | 菜单入口 |  | ui-static | ui.Platform_Ops_Portal.Profile.view |  |

## Property_Portal
| page_key | 页面中文名 | 角色 | 模块 | 入口 | 父页面 | api_tag | permission_key | api_endpoints |
|---|---|---|---|---|---|---|---|---|
| PP_01_Login | 备至·智洗平台 | PROPERTY_FINANCE | Login | 入口-登录 |  | identity | ui.Property_Portal.Login.view | POST /auth/login; POST /auth/mfa/verify; POST /auth/logout |
| PP_02_Property_Dashboard | 运营大盘 | PROPERTY_FINANCE | Property | 入口-首页 |  | ui-static | ui.Property_Portal.Property.view |  |
| PP_03_Settlements_List | 物业结算批次 | PROPERTY_FINANCE | Settlements | 菜单入口 |  | finance | ui.Property_Portal.Settlements.manage | GET /finance/settlements; GET /finance/settlements/{settlementId}; GET /finance/settlements/{settlementId}/lines; POST /finance/settlements/{settlementId}/export |
| PP_04_Settlement_Detail | 物业结算批次详情与分账明细 | PROPERTY_FINANCE | Settlement | 菜单入口 |  | finance | ui.Property_Portal.Settlement.manage | GET /finance/settlements; GET /finance/settlements/{settlementId}; GET /finance/settlements/{settlementId}/lines; POST /finance/settlements/{settlementId}/export |
| PP_05_Remittance_Tasks | 打款：生成打款指令渠道账单与跑批 | PROPERTY_FINANCE | Remittance | 菜单入口 |  | task | ui.Property_Portal.Remittance.view | GET /tasks; POST /tasks/{taskId}/assign; POST /tasks/{taskId}/accept; POST /tasks/{taskId}/complete |
| PP_06_Reconciliation_Tickets | 对账差异工单（物业） | PROPERTY_FINANCE | Reconciliation | 菜单入口 |  | finance | ui.Property_Portal.Reconciliation.view | GET /finance/reconciliation/tickets; POST /finance/reconciliation/tickets; GET /finance/reconciliation/tickets/{ticketId} |
| PP_07_Ticket_Detail | 对账工单详情（物业） | PROPERTY_FINANCE | Ticket | 菜单入口 |  | finance | ui.Property_Portal.Ticket.view | GET /finance/reconciliation/tickets; POST /finance/reconciliation/tickets; GET /finance/reconciliation/tickets/{ticketId} |
| PP_08_Reports | 物业报表 | PROPERTY_FINANCE | Reports | 菜单入口 |  | reports | ui.Property_Portal.Reports.view | GET /reports/summary; POST /reports/export |
| PP_09_Profile | 我的账号 | PROPERTY_FINANCE | Profile | 菜单入口 |  | ui-static | ui.Property_Portal.Profile.view |  |

## Store_Admin_Portal
| page_key | 页面中文名 | 角色 | 模块 | 入口 | 父页面 | api_tag | permission_key | api_endpoints |
|---|---|---|---|---|---|---|---|---|
| SA_01_Login | 备至·智洗平台 | STORE_ADMIN | Login | 入口-登录 |  | identity | ui.Store_Admin_Portal.Login.view | POST /auth/login; POST /auth/mfa/verify; POST /auth/logout |
| SA_02_Store_Dashboard | 门店看板 | STORE_ADMIN | Store | 入口-首页 |  | ui-static | ui.Store_Admin_Portal.Store.view |  |
| SA_03_1_Order_Board_Filter_Drawer | 订单看板 | STORE_ADMIN | Order | 子页面入口 | SA_03_Order_Board | order | ui.Store_Admin_Portal.Order.view | GET /orders; GET /orders/{orderId}; POST /orders; POST /orders/{orderId}/cancel; POST /orders/{orderId}/dispatch; GET /orders/{orderId}/timeline |
| SA_03_Order_Board | 订单看板（队列/在洗/待取） | STORE_ADMIN | Order | 菜单入口 |  | order | ui.Store_Admin_Portal.Order.view | GET /orders; GET /orders/{orderId}; POST /orders; POST /orders/{orderId}/cancel; POST /orders/{orderId}/dispatch; GET /orders/{orderId}/timeline |
| SA_04_1_Order_Evidence | 订单详情 | STORE_ADMIN | Order | 子页面入口 | SA_04_Order_Detail | order | ui.Store_Admin_Portal.Order.view | GET /orders; GET /orders/{orderId}; POST /orders; POST /orders/{orderId}/cancel; POST /orders/{orderId}/dispatch; GET /orders/{orderId}/timeline; POST /evidence/upload-url; POST /evidence/records; GET /orders/{orderId}/evidence |
| SA_04_2_Order_Settlement | 订单详情 | STORE_ADMIN | Order | 子页面入口 | SA_04_Order_Detail | finance | ui.Store_Admin_Portal.Order.manage | GET /finance/settlements; GET /finance/settlements/{settlementId}; GET /finance/settlements/{settlementId}/lines; POST /finance/settlements/{settlementId}/export; GET /orders; GET /orders/{orderId}; POST /orders; POST /orders/{orderId}/cancel; POST /orders/{orderId}/dispatch; GET /orders/{orderId}/timeline |
| SA_04_3_Order_Audit | 订单详情 | STORE_ADMIN | Order | 子页面入口 | SA_04_Order_Detail | order | ui.Store_Admin_Portal.Order.view | GET /orders; GET /orders/{orderId}; POST /orders; POST /orders/{orderId}/cancel; POST /orders/{orderId}/dispatch; GET /orders/{orderId}/timeline; GET /audit/logs; GET /audit/logs/{logId}; POST /audit/exports |
| SA_04_Order_Detail | 订单详情（派单/状态/证据） | STORE_ADMIN | Order | 菜单入口 |  | order | ui.Store_Admin_Portal.Order.view | GET /orders; GET /orders/{orderId}; POST /orders; POST /orders/{orderId}/cancel; POST /orders/{orderId}/dispatch; GET /orders/{orderId}/timeline |
| SA_05_1_Assign_Drawer | 派单调度 | STORE_ADMIN | Assign | 子页面入口 | SA_05_Dispatch_Assign | ui-static | ui.Store_Admin_Portal.Assign.manage |  |
| SA_05_Dispatch_Assign | 派单调度（指派技师） | STORE_ADMIN | Dispatch | 菜单入口 |  | ui-static | ui.Store_Admin_Portal.Dispatch.manage |  |
| SA_06_1_Technician_Create | 新增技师 | STORE_ADMIN | Technician | 新增 | SA_06_Technicians_List | ui-static | ui.Store_Admin_Portal.Technician.manage |  |
| SA_06_Technicians_List | 技师管理 | STORE_ADMIN | Technicians | 菜单入口 |  | ui-static | ui.Store_Admin_Portal.Technicians.view |  |
| SA_07_1_Technician_Edit | 编辑技师 | STORE_ADMIN | Technician | 编辑 | SA_07_Technician_Detail | ui-static | ui.Store_Admin_Portal.Technician.manage |  |
| SA_07_2_Technician_Shifts | 技师班次 | STORE_ADMIN | Technician | 子页面入口 | SA_07_Technician_Detail | ui-static | ui.Store_Admin_Portal.Technician.view |  |
| SA_07_3_Technician_Credentials | 技师资质 | STORE_ADMIN | Technician | 子页面入口 | SA_07_Technician_Detail | ui-static | ui.Store_Admin_Portal.Technician.view |  |
| SA_07_Technician_Detail | 技师详情与绩效 | STORE_ADMIN | Technician | 菜单入口 |  | ui-static | ui.Store_Admin_Portal.Technician.view |  |
| SA_08_1_Slot_Detail | 格口详情 | STORE_ADMIN | Slot | 进入详情 | SA_08_Cabinets_Slots | asset-investment | ui.Store_Admin_Portal.Slot.view | GET /assets/cabinets; GET /assets/cabinets/{cabinetId}; GET /assets/cabinets/{cabinetId}/slots; POST /iot/cabinets/{cabinetId}/commands/open-slot; GET /iot/cabinets/{cabinetId}/events |
| SA_08_2_Manual_OpenSlot_Modal | 柜体与格口 | STORE_ADMIN | Manual | 子页面入口 | SA_08_Cabinets_Slots | asset-investment | ui.Store_Admin_Portal.Manual.view | GET /assets/cabinets; GET /assets/cabinets/{cabinetId}; GET /assets/cabinets/{cabinetId}/slots; POST /iot/cabinets/{cabinetId}/commands/open-slot; GET /iot/cabinets/{cabinetId}/events |
| SA_08_3_Device_Logs | 柜体与格口 | STORE_ADMIN | Device | 子页面入口 | SA_08_Cabinets_Slots | asset-investment | ui.Store_Admin_Portal.Device.view | GET /assets/cabinets; GET /assets/cabinets/{cabinetId}; GET /assets/cabinets/{cabinetId}/slots; POST /iot/cabinets/{cabinetId}/commands/open-slot; GET /iot/cabinets/{cabinetId}/events |
| SA_08_4_OTA_RemoteCommand | 柜体与格口 | STORE_ADMIN | OTA | 子页面入口 | SA_08_Cabinets_Slots | ui-static | ui.Store_Admin_Portal.OTA.manage |  |
| SA_08_Cabinets_Slots | 在线柜体与格口状态 | STORE_ADMIN | Cabinets | 菜单入口 |  | asset-investment | ui.Store_Admin_Portal.Cabinets.view | GET /assets/cabinets; GET /assets/cabinets/{cabinetId}; GET /assets/cabinets/{cabinetId}/slots; POST /iot/cabinets/{cabinetId}/commands/open-slot; GET /iot/cabinets/{cabinetId}/events |
| SA_09_1_Service_Create | 新增服务项目 | STORE_ADMIN | Service | 新增 | SA_09_Service_Catalog | ui-static | ui.Store_Admin_Portal.Service.manage |  |
| SA_09_2_Service_Edit | 编辑服务项目 | STORE_ADMIN | Service | 编辑 | SA_09_Service_Catalog | ui-static | ui.Store_Admin_Portal.Service.manage |  |
| SA_09_Service_Catalog | 价格与服务：服务项目与定价 | STORE_ADMIN | Service | 菜单入口 |  | ui-static | ui.Store_Admin_Portal.Service.view |  |
| SA_10_1_PricingRule_Create | 新增动态定价规则 | STORE_ADMIN | PricingRule | 新增 | SA_10_Dynamic_Pricing | ui-static | ui.Store_Admin_Portal.PricingRule.manage |  |
| SA_10_2_PricingRule_Edit | 编辑动态定价规则 | STORE_ADMIN | PricingRule | 编辑 | SA_10_Dynamic_Pricing | ui-static | ui.Store_Admin_Portal.PricingRule.manage |  |
| SA_10_Dynamic_Pricing | 价格与服务：动态定价规则 | STORE_ADMIN | Dynamic | 菜单入口 |  | ui-static | ui.Store_Admin_Portal.Dynamic.view |  |
| SA_11_Settlements_List | 结算与退款：结算单 | STORE_ADMIN | Settlements | 菜单入口 |  | finance | ui.Store_Admin_Portal.Settlements.manage | GET /finance/settlements; GET /finance/settlements/{settlementId}; GET /finance/settlements/{settlementId}/lines; POST /finance/settlements/{settlementId}/export |
| SA_12_1_Settlement_Export_Modal | 结算详情 | STORE_ADMIN | Settlement | 子页面入口 | SA_12_Settlement_Detail | finance | ui.Store_Admin_Portal.Settlement.manage | GET /finance/settlements; GET /finance/settlements/{settlementId}; GET /finance/settlements/{settlementId}/lines; POST /finance/settlements/{settlementId}/export |
| SA_12_Settlement_Detail | 结算详情 | STORE_ADMIN | Settlement | 菜单入口 |  | finance | ui.Store_Admin_Portal.Settlement.manage | GET /finance/settlements; GET /finance/settlements/{settlementId}; GET /finance/settlements/{settlementId}/lines; POST /finance/settlements/{settlementId}/export |
| SA_13_1_Refund_Create | 创建退款申请 | STORE_ADMIN | Refund | 新增 | SA_13_Refunds_List | finance | ui.Store_Admin_Portal.Refund.manage | GET /finance/refunds; POST /finance/refunds; POST /finance/refunds/{refundId}/approve |
| SA_13_Refunds_List | 结算与退款：退款申请 | STORE_ADMIN | Refunds | 菜单入口 |  | finance | ui.Store_Admin_Portal.Refunds.manage | GET /finance/refunds; POST /finance/refunds; POST /finance/refunds/{refundId}/approve |
| SA_14_Refund_Detail | 退款单详情 | STORE_ADMIN | Refund | 菜单入口 |  | finance | ui.Store_Admin_Portal.Refund.manage | GET /finance/refunds; POST /finance/refunds; POST /finance/refunds/{refundId}/approve |
| SA_15_1_Exception_Create | 创建异常工单 | STORE_ADMIN | Exception | 新增 | SA_15_Exception_Tickets | ui-static | ui.Store_Admin_Portal.Exception.manage |  |
| SA_15_Exception_Tickets | 异常工单 | STORE_ADMIN | Exception | 菜单入口 |  | finance | ui.Store_Admin_Portal.Exception.view | GET /finance/reconciliation/tickets; POST /finance/reconciliation/tickets; GET /finance/reconciliation/tickets/{ticketId} |
| SA_16_1_Assign_Modal | 异常工单详情 | STORE_ADMIN | Assign | 子页面入口 | SA_16_Exception_Ticket_Detail | ui-static | ui.Store_Admin_Portal.Assign.manage |  |
| SA_16_2_Remark_Drawer | 异常工单详情 | STORE_ADMIN | Remark | 子页面入口 | SA_16_Exception_Ticket_Detail | ui-static | ui.Store_Admin_Portal.Remark.view |  |
| SA_16_3_Resolve_Modal | 异常工单详情 | STORE_ADMIN | Resolve | 子页面入口 | SA_16_Exception_Ticket_Detail | ui-static | ui.Store_Admin_Portal.Resolve.view |  |
| SA_16_Exception_Ticket_Detail | 异常工单详情 | STORE_ADMIN | Exception | 菜单入口 |  | finance | ui.Store_Admin_Portal.Exception.view | GET /finance/reconciliation/tickets; POST /finance/reconciliation/tickets; GET /finance/reconciliation/tickets/{ticketId} |
| SA_17_1_Template_Edit | 通知模板 | STORE_ADMIN | Template | 编辑 | SA_17_Notification_Templates | ui-static | ui.Store_Admin_Portal.Template.manage |  |
| SA_17_Notification_Templates | 配置中心：通知模板 | STORE_ADMIN | Notification | 菜单入口 |  | ui-static | ui.Store_Admin_Portal.Notification.view |  |
| SA_18_1_Retention_Edit | 滞留策略 | STORE_ADMIN | Retention | 编辑 | SA_18_Retention_Policy | ui-static | ui.Store_Admin_Portal.Retention.manage |  |
| SA_18_2_Retention_Reminders | 滞留提醒记录 | STORE_ADMIN | Retention | 子页面入口 | SA_18_Retention_Policy | ui-static | ui.Store_Admin_Portal.Retention.view |  |
| SA_18_Retention_Policy | 配置中心：滞留策略 | STORE_ADMIN | Retention | 菜单入口 |  | ui-static | ui.Store_Admin_Portal.Retention.manage |  |
| SA_19_1_ChangePassword | 修改密码 | STORE_ADMIN | ChangePassword | 子页面入口 | SA_19_Profile | ui-static | ui.Store_Admin_Portal.ChangePassword.view |  |
| SA_19_Profile | 我的账号 | STORE_ADMIN | Profile | 菜单入口 |  | ui-static | ui.Store_Admin_Portal.Profile.view |  |
| SA_20_1_Report_Filter_Drawer | 门店报表 | STORE_ADMIN | Report | 子页面入口 | SA_20_Reports | reports | ui.Store_Admin_Portal.Report.view | GET /reports/summary; POST /reports/export |
| SA_20_Reports | 门店报表 | STORE_ADMIN | Reports | 菜单入口 |  | reports | ui.Store_Admin_Portal.Reports.view | GET /reports/summary; POST /reports/export |
| SA_21_1_WashTeam_Create | 新增洗车组 | STORE_ADMIN | WashTeam | 新增 | SA_21_WashTeams_List | ui-static | ui.Store_Admin_Portal.WashTeam.manage |  |
| SA_21_WashTeams_List | 洗车组管理 | STORE_ADMIN | WashTeams | 菜单入口 |  | ui-static | ui.Store_Admin_Portal.WashTeams.view |  |
| SA_22_1_WashTeam_Edit | 编辑洗车组 | STORE_ADMIN | WashTeam | 编辑 | SA_22_WashTeam_Detail | ui-static | ui.Store_Admin_Portal.WashTeam.manage |  |
| SA_22_2_WashTeam_Members_Assign | 派单调度 | STORE_ADMIN | WashTeam | 子页面入口 | SA_22_WashTeam_Detail | ui-static | ui.Store_Admin_Portal.WashTeam.manage |  |
| SA_22_WashTeam_Detail | 洗车组详情与绩效 | STORE_ADMIN | WashTeam | 菜单入口 |  | ui-static | ui.Store_Admin_Portal.WashTeam.view |  |
| SA_23_1_ValetDriver_Create | 新增代泊人员 | STORE_ADMIN | ValetDriver | 新增 | SA_23_Valet_Drivers_List | ui-static | ui.Store_Admin_Portal.ValetDriver.manage |  |
| SA_23_Valet_Drivers_List | 代泊人员管理 | STORE_ADMIN | Valet | 菜单入口 |  | ui-static | ui.Store_Admin_Portal.Valet.view |  |
| SA_24_1_ValetDriver_Edit | 编辑代泊人员 | STORE_ADMIN | ValetDriver | 编辑 | SA_24_ValetDriver_Detail | ui-static | ui.Store_Admin_Portal.ValetDriver.manage |  |
| SA_24_ValetDriver_Detail | 代泊人员详情与绩效 | STORE_ADMIN | ValetDriver | 菜单入口 |  | ui-static | ui.Store_Admin_Portal.ValetDriver.view |  |
| SA_25_Tasks_List | 任务看板（队列/在洗/待取） | STORE_ADMIN | Tasks | 菜单入口 |  | task | ui.Store_Admin_Portal.Tasks.view | GET /tasks; POST /tasks/{taskId}/assign; POST /tasks/{taskId}/accept; POST /tasks/{taskId}/complete |
| SA_26_1_Task_Collaboration | 任务详情 | STORE_ADMIN | Task | 子页面入口 | SA_26_Task_Detail | task | ui.Store_Admin_Portal.Task.view | GET /tasks; POST /tasks/{taskId}/assign; POST /tasks/{taskId}/accept; POST /tasks/{taskId}/complete |
| SA_26_Task_Detail | 任务详情（派单/状态/证据） | STORE_ADMIN | Task | 菜单入口 |  | task | ui.Store_Admin_Portal.Task.view | GET /tasks; POST /tasks/{taskId}/assign; POST /tasks/{taskId}/accept; POST /tasks/{taskId}/complete |
| SA_27_1_Shift_Create | 新增班次 | STORE_ADMIN | Shift | 新增 | SA_27_Shift_Schedule | ui-static | ui.Store_Admin_Portal.Shift.manage |  |
| SA_27_Shift_Schedule | 班次排班 | STORE_ADMIN | Shift | 菜单入口 |  | ui-static | ui.Store_Admin_Portal.Shift.view |  |
| SA_28_1_Binding_Request_Create | 价格与服务：申请绑定变更规则 | STORE_ADMIN | Binding | 新增 | SA_28_CabinetStoreBindings | asset | ui.Store_Admin_Portal.Binding.manage | GET /asset/cabinets/{cabinetId}/store-bindings; POST /asset/cabinets/{cabinetId}/store-bindings; PATCH /asset/cabinet-store-bindings/{bindingId}; POST /asset/cabinet-store-bindings/{bindingId}/approve |
| SA_28_2_Binding_History | 门店报表 | STORE_ADMIN | Binding | 绑定/配置 | SA_28_CabinetStoreBindings | asset | ui.Store_Admin_Portal.Binding.manage | GET /asset/cabinets/{cabinetId}/store-bindings; POST /asset/cabinets/{cabinetId}/store-bindings; PATCH /asset/cabinet-store-bindings/{bindingId}; POST /asset/cabinet-store-bindings/{bindingId}/approve |
| SA_28_3_Binding_RoutingPolicy_View | 我的账号 | STORE_ADMIN | Binding | 绑定/配置 | SA_28_CabinetStoreBindings | asset | ui.Store_Admin_Portal.Binding.manage | GET /asset/cabinets/{cabinetId}/store-bindings; POST /asset/cabinets/{cabinetId}/store-bindings; PATCH /asset/cabinet-store-bindings/{bindingId}; POST /asset/cabinet-store-bindings/{bindingId}/approve |
| SA_28_CabinetStoreBindings | 在线共享柜体绑定状态 | STORE_ADMIN | CabinetStoreBindings | 菜单入口 |  | asset-investment | ui.Store_Admin_Portal.CabinetStoreBindings.manage | GET /assets/cabinets; GET /assets/cabinets/{cabinetId}; GET /assets/cabinets/{cabinetId}/slots; POST /iot/cabinets/{cabinetId}/commands/open-slot; GET /iot/cabinets/{cabinetId}/events; GET /asset/cabinets/{cabinetId}/store-bindings; POST /asset/cabinets/{cabinetId}/store-bindings; PATCH /asset/cabinet-store-bindings/{bindingId}; POST /asset/cabinet-store-bindings/{bindingId}/approve |

## Technician_App
| page_key | 页面中文名 | 角色 | 模块 | 入口 | 父页面 | api_tag | permission_key | api_endpoints |
|---|---|---|---|---|---|---|---|---|
| TA_01_1_Face_Verify | 人脸验证 | TECHNICIAN | Face | 子页面入口 | TA_01_Login | ui-static | ui.Technician_App.Face.view |  |
| TA_01_2_Shift_PunchIn | 上岗打卡 | TECHNICIAN | Shift | 子页面入口 | TA_01_Login | ui-static | ui.Technician_App.Shift.view |  |
| TA_01_3_Shift_PunchOut | 下班打卡 | TECHNICIAN | Shift | 子页面入口 | TA_01_Login | ui-static | ui.Technician_App.Shift.view |  |
| TA_01_5_Authorization_Bind | 人脸验证 | TECHNICIAN | Authorization | 绑定/配置 | TA_01_Login | ui-static | ui.Technician_App.Authorization.manage |  |
| TA_01_Login | TA_01_Login |  | Login | 入口-登录 |  | app | ui.Technician_App.Login.view | POST /app/auth/sms/send; POST /app/auth/sms/login; POST /app/auth/logout |
| TA_02_1_Task_Filter | 任务筛选 | TECHNICIAN | Task | 子页面入口 | TA_02_Task_List | app | ui.Technician_App.Task.view | GET /app/tech/tasks; POST /app/tech/tasks/{taskId}/accept; POST /app/tech/tasks/{taskId}/complete |
| TA_02_2_Switch_Role_Mode | TECH-03 张三 |  | Switch | 子页面入口 | TA_02_Task_List | ui-static | ui.Technician_App.Switch.manage |  |
| TA_02_Task_List | 今日任务 | TECHNICIAN | Task | 菜单入口 |  | app | ui.Technician_App.Task.view | GET /app/tech/tasks; POST /app/tech/tasks/{taskId}/accept; POST /app/tech/tasks/{taskId}/complete |
| TA_03_1_Task_Evidence_Detail | 证据详情 | TECHNICIAN | Task | 进入详情 | TA_03_Task_Detail | app | ui.Technician_App.Task.view | GET /app/tech/tasks; POST /app/tech/tasks/{taskId}/accept; POST /app/tech/tasks/{taskId}/complete; POST /app/tech/evidence/upload-url; POST /app/tech/evidence/records |
| TA_03_2_Collaboration_Board | ORD20261008 | TECHNICIAN | Collaboration | 子页面入口 | TA_03_Task_Detail | ui-static | ui.Technician_App.Collaboration.view |  |
| TA_03_3_Collaboration_Invite | 邀请协作成员 | TECHNICIAN | Collaboration | 子页面入口 | TA_03_Task_Detail | ui-static | ui.Technician_App.Collaboration.view |  |
| TA_03_4_Collaboration_Member_Status | 成员状态 | TECHNICIAN | Collaboration | 子页面入口 | TA_03_Task_Detail | ui-static | ui.Technician_App.Collaboration.view |  |
| TA_03_Task_Detail | ORD20261008 | TECHNICIAN | Task | 菜单入口 |  | app | ui.Technician_App.Task.view | GET /app/tech/tasks; POST /app/tech/tasks/{taskId}/accept; POST /app/tech/tasks/{taskId}/complete |
| TA_04_1_PickUpKey_BLE_Handshake | 离线取钥匙 | TECHNICIAN | PickUpKey | 子页面入口 | TA_04_PickUpKey_Scan | app | ui.Technician_App.PickUpKey.view | POST /app/cabinets/scan; POST /app/orders/{orderId}/deposit-key; POST /app/orders/{orderId}/retrieve-key; POST /app/tech/orders/{orderId}/pickup-key; POST /app/tech/orders/{orderId}/return-key |
| TA_04_2_PickUpKey_BLE_Success | 离线取钥匙 | TECHNICIAN | PickUpKey | 子页面入口 | TA_04_PickUpKey_Scan | app | ui.Technician_App.PickUpKey.view | POST /app/cabinets/scan; POST /app/orders/{orderId}/deposit-key; POST /app/orders/{orderId}/retrieve-key; POST /app/tech/orders/{orderId}/pickup-key; POST /app/tech/orders/{orderId}/return-key |
| TA_04_PickUpKey_Scan | 取钥匙：扫码柜体 | TECHNICIAN | PickUpKey | 菜单入口 |  | app | ui.Technician_App.PickUpKey.view | POST /app/cabinets/scan; POST /app/orders/{orderId}/deposit-key; POST /app/orders/{orderId}/retrieve-key; POST /app/tech/orders/{orderId}/pickup-key; POST /app/tech/orders/{orderId}/return-key |
| TA_05_PickUpKey_Token | 授权令牌校验 | TECHNICIAN | PickUpKey | 菜单入口 |  | app | ui.Technician_App.PickUpKey.view | POST /app/cabinets/scan; POST /app/orders/{orderId}/deposit-key; POST /app/orders/{orderId}/retrieve-key; POST /app/tech/orders/{orderId}/pickup-key; POST /app/tech/orders/{orderId}/return-key |
| TA_06_PreCheck_Checklist | 预检：必拍四角度 | TECHNICIAN | PreCheck | 菜单入口 |  | ui-static | ui.Technician_App.PreCheck.view |  |
| TA_07_PreCheck_Upload | 预检上传中 | TECHNICIAN | PreCheck | 菜单入口 |  | ui-static | ui.Technician_App.PreCheck.view |  |
| TA_08_Start_Service | 开工确认 | TECHNICIAN | Start | 菜单入口 |  | ui-static | ui.Technician_App.Start.view |  |
| TA_09_Service_Evidence | 施工证据 | TECHNICIAN | Service | 菜单入口 |  | app | ui.Technician_App.Service.view | POST /app/tech/evidence/upload-url; POST /app/tech/evidence/records |
| TA_10_Complete_Service | 完工确认 | TECHNICIAN | Complete | 菜单入口 |  | ui-static | ui.Technician_App.Complete.view |  |
| TA_11_Park_Vehicle | 停车留证 | TECHNICIAN | Park | 菜单入口 |  | ui-static | ui.Technician_App.Park.view |  |
| TA_12_ReturnKey_Scan | 还钥匙：扫码柜体 | TECHNICIAN | ReturnKey | 菜单入口 |  | app | ui.Technician_App.ReturnKey.view | POST /app/cabinets/scan; POST /app/orders/{orderId}/deposit-key; POST /app/orders/{orderId}/retrieve-key; POST /app/tech/orders/{orderId}/pickup-key; POST /app/tech/orders/{orderId}/return-key |
| TA_13_ReturnKey_Success | 还钥匙成功 | TECHNICIAN | ReturnKey | 菜单入口 |  | app | ui.Technician_App.ReturnKey.view | POST /app/cabinets/scan; POST /app/orders/{orderId}/deposit-key; POST /app/orders/{orderId}/retrieve-key; POST /app/tech/orders/{orderId}/pickup-key; POST /app/tech/orders/{orderId}/return-key |
| TA_14_1_Exception_List | 异常列表 | TECHNICIAN | Exception | 子页面入口 | TA_14_Exception_Report | ui-static | ui.Technician_App.Exception.view |  |
| TA_14_2_Exception_Detail | 异常详情 | TECHNICIAN | Exception | 进入详情 | TA_14_Exception_Report | ui-static | ui.Technician_App.Exception.view |  |
| TA_14_Exception_Report | 异常上报 | TECHNICIAN | Exception | 菜单入口 |  | ui-static | ui.Technician_App.Exception.view |  |
| TA_15_Offline_Mode | 离线模式（BLE） | TECHNICIAN | Offline | 菜单入口 |  | ui-static | ui.Technician_App.Offline.view |  |
| TA_16_1_Earnings | 收入 | TECHNICIAN | Earnings | 子页面入口 | TA_16_Profile | ui-static | ui.Technician_App.Earnings.view |  |
| TA_16_2_Performance | 绩效 | TECHNICIAN | Performance | 子页面入口 | TA_16_Profile | ui-static | ui.Technician_App.Performance.view |  |
| TA_16_3_Credentials | 资质材料 | TECHNICIAN | Credentials | 子页面入口 | TA_16_Profile | ui-static | ui.Technician_App.Credentials.view |  |
| TA_16_Profile | TECH-03 张三 | TECHNICIAN | Profile | 菜单入口 |  | ui-static | ui.Technician_App.Profile.view |  |
| TA_17_1_Notifications_Prefs | 通知设置 | TECHNICIAN | Notifications | 子页面入口 | TA_17_Settings | ui-static | ui.Technician_App.Notifications.view |  |
| TA_17_2_Help_FAQ | 帮助与FAQ | TECHNICIAN | Help | 子页面入口 | TA_17_Settings | ui-static | ui.Technician_App.Help.view |  |
| TA_17_Settings | 设置 | TECHNICIAN | Settings | 菜单入口 |  | ui-static | ui.Technician_App.Settings.manage |  |

## _Common
| page_key | 页面中文名 | 角色 | 模块 | 入口 | 父页面 | api_tag | permission_key | api_endpoints |
|---|---|---|---|---|---|---|---|---|
| 00_Design_System | Primary |  | System | 菜单入口 |  | ui-static | ui._Common.System.view |  |

