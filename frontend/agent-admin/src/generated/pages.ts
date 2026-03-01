export interface PageMeta {
  pageKey: string
  titleZh: string
  role: string
  module: string
  parentPage: string
  entry: string
  subsystem: string
  svgPath: string
  mdPath: string
  implStatus: string
  routePath: string
  permissionKey: string
  apiTag: string
  prototypeSvgUrl: string
  prototypeMdUrl: string
  apiEndpoints: string[]
}

export const pages: PageMeta[] = [
  {
    "pageKey": "AA_01_Login",
    "titleZh": "备至·智洗平台",
    "role": "AGENT_ADMIN",
    "module": "Login",
    "parentPage": "",
    "entry": "入口-登录",
    "subsystem": "Agent_Admin_Portal",
    "svgPath": "docs/ui/svg/Agent_Admin_Portal/AA_01_Login.svg",
    "mdPath": "docs/ui/md/Agent_Admin_Portal/AA_01_Login.md",
    "implStatus": "implemented",
    "routePath": "/pages/AA_01_Login",
    "permissionKey": "ui.Agent_Admin_Portal.Login.view",
    "apiTag": "identity",
    "prototypeSvgUrl": "/prototypes/AA_01_Login.svg",
    "prototypeMdUrl": "/prototypes/AA_01_Login.md",
    "apiEndpoints": [
      "POST /auth/login",
      "POST /auth/mfa/verify",
      "POST /auth/logout"
    ]
  },
  {
    "pageKey": "AA_02_Region_Dashboard",
    "titleZh": "区域运营大盘",
    "role": "AGENT_ADMIN",
    "module": "Region",
    "parentPage": "",
    "entry": "入口-首页",
    "subsystem": "Agent_Admin_Portal",
    "svgPath": "docs/ui/svg/Agent_Admin_Portal/AA_02_Region_Dashboard.svg",
    "mdPath": "docs/ui/md/Agent_Admin_Portal/AA_02_Region_Dashboard.md",
    "implStatus": "implemented",
    "routePath": "/pages/AA_02_Region_Dashboard",
    "permissionKey": "ui.Agent_Admin_Portal.Region.view",
    "apiTag": "ui-static",
    "prototypeSvgUrl": "/prototypes/AA_02_Region_Dashboard.svg",
    "prototypeMdUrl": "/prototypes/AA_02_Region_Dashboard.md",
    "apiEndpoints": []
  },
  {
    "pageKey": "AA_03_1_Store_Create",
    "titleZh": "新增门店",
    "role": "AGENT_ADMIN",
    "module": "Store",
    "parentPage": "AA_03_Stores_List",
    "entry": "新增",
    "subsystem": "Agent_Admin_Portal",
    "svgPath": "docs/ui/svg/Agent_Admin_Portal/AA_03_1_Store_Create.svg",
    "mdPath": "docs/ui/md/Agent_Admin_Portal/AA_03_1_Store_Create.md",
    "implStatus": "implemented",
    "routePath": "/pages/AA_03_1_Store_Create",
    "permissionKey": "ui.Agent_Admin_Portal.Store.manage",
    "apiTag": "tenant-org",
    "prototypeSvgUrl": "/prototypes/AA_03_1_Store_Create.svg",
    "prototypeMdUrl": "/prototypes/AA_03_1_Store_Create.md",
    "apiEndpoints": [
      "POST /tenants/{tenantId}/stores"
    ]
  },
  {
    "pageKey": "AA_03_Stores_List",
    "titleZh": "加盟店管理",
    "role": "AGENT_ADMIN",
    "module": "Stores",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Agent_Admin_Portal",
    "svgPath": "docs/ui/svg/Agent_Admin_Portal/AA_03_Stores_List.svg",
    "mdPath": "docs/ui/md/Agent_Admin_Portal/AA_03_Stores_List.md",
    "implStatus": "implemented",
    "routePath": "/pages/AA_03_Stores_List",
    "permissionKey": "ui.Agent_Admin_Portal.Stores.view",
    "apiTag": "tenant-org",
    "prototypeSvgUrl": "/prototypes/AA_03_Stores_List.svg",
    "prototypeMdUrl": "/prototypes/AA_03_Stores_List.md",
    "apiEndpoints": [
      "GET /tenants/{tenantId}/stores",
      "GET /stores/{storeId}"
    ]
  },
  {
    "pageKey": "AA_04_1_Store_Edit",
    "titleZh": "编辑门店",
    "role": "AGENT_ADMIN",
    "module": "Store",
    "parentPage": "AA_04_Store_Detail",
    "entry": "编辑",
    "subsystem": "Agent_Admin_Portal",
    "svgPath": "docs/ui/svg/Agent_Admin_Portal/AA_04_1_Store_Edit.svg",
    "mdPath": "docs/ui/md/Agent_Admin_Portal/AA_04_1_Store_Edit.md",
    "implStatus": "implemented",
    "routePath": "/pages/AA_04_1_Store_Edit",
    "permissionKey": "ui.Agent_Admin_Portal.Store.manage",
    "apiTag": "tenant-org",
    "prototypeSvgUrl": "/prototypes/AA_04_1_Store_Edit.svg",
    "prototypeMdUrl": "/prototypes/AA_04_1_Store_Edit.md",
    "apiEndpoints": [
      "GET /stores/{storeId}",
      "PUT /stores/{storeId}"
    ]
  },
  {
    "pageKey": "AA_04_2_Store_Contracts",
    "titleZh": "门店合同与分润",
    "role": "AGENT_ADMIN",
    "module": "Store",
    "parentPage": "AA_04_Store_Detail",
    "entry": "子页面入口",
    "subsystem": "Agent_Admin_Portal",
    "svgPath": "docs/ui/svg/Agent_Admin_Portal/AA_04_2_Store_Contracts.svg",
    "mdPath": "docs/ui/md/Agent_Admin_Portal/AA_04_2_Store_Contracts.md",
    "implStatus": "implemented",
    "routePath": "/pages/AA_04_2_Store_Contracts",
    "permissionKey": "ui.Agent_Admin_Portal.Store.view",
    "apiTag": "tenant-org",
    "prototypeSvgUrl": "/prototypes/AA_04_2_Store_Contracts.svg",
    "prototypeMdUrl": "/prototypes/AA_04_2_Store_Contracts.md",
    "apiEndpoints": [
      "GET /stores/{storeId}/agreements",
      "POST /stores/{storeId}/agreements"
    ]
  },
  {
    "pageKey": "AA_04_Store_Detail",
    "titleZh": "门店详情与KPI",
    "role": "AGENT_ADMIN",
    "module": "Store",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Agent_Admin_Portal",
    "svgPath": "docs/ui/svg/Agent_Admin_Portal/AA_04_Store_Detail.svg",
    "mdPath": "docs/ui/md/Agent_Admin_Portal/AA_04_Store_Detail.md",
    "implStatus": "implemented",
    "routePath": "/pages/AA_04_Store_Detail",
    "permissionKey": "ui.Agent_Admin_Portal.Store.view",
    "apiTag": "tenant-org",
    "prototypeSvgUrl": "/prototypes/AA_04_Store_Detail.svg",
    "prototypeMdUrl": "/prototypes/AA_04_Store_Detail.md",
    "apiEndpoints": [
      "GET /stores/{storeId}",
      "GET /stores/{storeId}/agreements"
    ]
  },
  {
    "pageKey": "AA_05_1_Onboarding_Step1_Basic",
    "titleZh": "门店入驻流程",
    "role": "AGENT_ADMIN",
    "module": "Onboarding",
    "parentPage": "AA_05_Store_Onboarding",
    "entry": "子页面入口",
    "subsystem": "Agent_Admin_Portal",
    "svgPath": "docs/ui/svg/Agent_Admin_Portal/AA_05_1_Onboarding_Step1_Basic.svg",
    "mdPath": "docs/ui/md/Agent_Admin_Portal/AA_05_1_Onboarding_Step1_Basic.md",
    "implStatus": "implemented",
    "routePath": "/pages/AA_05_1_Onboarding_Step1_Basic",
    "permissionKey": "ui.Agent_Admin_Portal.Onboarding.view",
    "apiTag": "tenant-org",
    "prototypeSvgUrl": "/prototypes/AA_05_1_Onboarding_Step1_Basic.svg",
    "prototypeMdUrl": "/prototypes/AA_05_1_Onboarding_Step1_Basic.md",
    "apiEndpoints": [
      "POST /tenants/{tenantId}/stores",
      "GET /stores/{storeId}"
    ]
  },
  {
    "pageKey": "AA_05_2_Onboarding_Step2_Contract",
    "titleZh": "门店入驻流程",
    "role": "AGENT_ADMIN",
    "module": "Onboarding",
    "parentPage": "AA_05_Store_Onboarding",
    "entry": "子页面入口",
    "subsystem": "Agent_Admin_Portal",
    "svgPath": "docs/ui/svg/Agent_Admin_Portal/AA_05_2_Onboarding_Step2_Contract.svg",
    "mdPath": "docs/ui/md/Agent_Admin_Portal/AA_05_2_Onboarding_Step2_Contract.md",
    "implStatus": "implemented",
    "routePath": "/pages/AA_05_2_Onboarding_Step2_Contract",
    "permissionKey": "ui.Agent_Admin_Portal.Onboarding.view",
    "apiTag": "tenant-org",
    "prototypeSvgUrl": "/prototypes/AA_05_2_Onboarding_Step2_Contract.svg",
    "prototypeMdUrl": "/prototypes/AA_05_2_Onboarding_Step2_Contract.md",
    "apiEndpoints": [
      "GET /stores/{storeId}/agreements",
      "POST /stores/{storeId}/agreements"
    ]
  },
  {
    "pageKey": "AA_05_3_Onboarding_Step3_DeviceBind",
    "titleZh": "门店入驻流程",
    "role": "AGENT_ADMIN",
    "module": "Onboarding",
    "parentPage": "AA_05_Store_Onboarding",
    "entry": "绑定/配置",
    "subsystem": "Agent_Admin_Portal",
    "svgPath": "docs/ui/svg/Agent_Admin_Portal/AA_05_3_Onboarding_Step3_DeviceBind.svg",
    "mdPath": "docs/ui/md/Agent_Admin_Portal/AA_05_3_Onboarding_Step3_DeviceBind.md",
    "implStatus": "implemented",
    "routePath": "/pages/AA_05_3_Onboarding_Step3_DeviceBind",
    "permissionKey": "ui.Agent_Admin_Portal.Onboarding.manage",
    "apiTag": "asset-investment",
    "prototypeSvgUrl": "/prototypes/AA_05_3_Onboarding_Step3_DeviceBind.svg",
    "prototypeMdUrl": "/prototypes/AA_05_3_Onboarding_Step3_DeviceBind.md",
    "apiEndpoints": [
      "GET /assets/cabinets",
      "GET /assets/cabinets/{cabinetId}",
      "GET /assets/cabinets/{cabinetId}/slots",
      "POST /iot/cabinets/{cabinetId}/commands/open-slot",
      "GET /iot/cabinets/{cabinetId}/events"
    ]
  },
  {
    "pageKey": "AA_05_4_Onboarding_Step4_GoLive",
    "titleZh": "门店入驻流程",
    "role": "AGENT_ADMIN",
    "module": "Onboarding",
    "parentPage": "AA_05_Store_Onboarding",
    "entry": "子页面入口",
    "subsystem": "Agent_Admin_Portal",
    "svgPath": "docs/ui/svg/Agent_Admin_Portal/AA_05_4_Onboarding_Step4_GoLive.svg",
    "mdPath": "docs/ui/md/Agent_Admin_Portal/AA_05_4_Onboarding_Step4_GoLive.md",
    "implStatus": "implemented",
    "routePath": "/pages/AA_05_4_Onboarding_Step4_GoLive",
    "permissionKey": "ui.Agent_Admin_Portal.Onboarding.view",
    "apiTag": "tenant-org",
    "prototypeSvgUrl": "/prototypes/AA_05_4_Onboarding_Step4_GoLive.svg",
    "prototypeMdUrl": "/prototypes/AA_05_4_Onboarding_Step4_GoLive.md",
    "apiEndpoints": [
      "GET /stores/{storeId}",
      "PUT /stores/{storeId}"
    ]
  },
  {
    "pageKey": "AA_05_Store_Onboarding",
    "titleZh": "门店入驻/上线流程",
    "role": "AGENT_ADMIN",
    "module": "Store",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Agent_Admin_Portal",
    "svgPath": "docs/ui/svg/Agent_Admin_Portal/AA_05_Store_Onboarding.svg",
    "mdPath": "docs/ui/md/Agent_Admin_Portal/AA_05_Store_Onboarding.md",
    "implStatus": "implemented",
    "routePath": "/pages/AA_05_Store_Onboarding",
    "permissionKey": "ui.Agent_Admin_Portal.Store.view",
    "apiTag": "tenant-org",
    "prototypeSvgUrl": "/prototypes/AA_05_Store_Onboarding.svg",
    "prototypeMdUrl": "/prototypes/AA_05_Store_Onboarding.md",
    "apiEndpoints": [
      "GET /tenants/{tenantId}/stores",
      "GET /stores/{storeId}",
      "GET /stores/{storeId}/agreements"
    ]
  },
  {
    "pageKey": "AA_06_1_Device_Detail",
    "titleZh": "设备健康度",
    "role": "AGENT_ADMIN",
    "module": "Device",
    "parentPage": "AA_06_Devices_Health",
    "entry": "进入详情",
    "subsystem": "Agent_Admin_Portal",
    "svgPath": "docs/ui/svg/Agent_Admin_Portal/AA_06_1_Device_Detail.svg",
    "mdPath": "docs/ui/md/Agent_Admin_Portal/AA_06_1_Device_Detail.md",
    "implStatus": "implemented",
    "routePath": "/pages/AA_06_1_Device_Detail",
    "permissionKey": "ui.Agent_Admin_Portal.Device.view",
    "apiTag": "asset-investment",
    "prototypeSvgUrl": "/prototypes/AA_06_1_Device_Detail.svg",
    "prototypeMdUrl": "/prototypes/AA_06_1_Device_Detail.md",
    "apiEndpoints": [
      "GET /assets/cabinets",
      "GET /assets/cabinets/{cabinetId}",
      "GET /assets/cabinets/{cabinetId}/slots",
      "POST /iot/cabinets/{cabinetId}/commands/open-slot",
      "GET /iot/cabinets/{cabinetId}/events"
    ]
  },
  {
    "pageKey": "AA_06_Devices_Health",
    "titleZh": "设备健康度",
    "role": "AGENT_ADMIN",
    "module": "Devices",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Agent_Admin_Portal",
    "svgPath": "docs/ui/svg/Agent_Admin_Portal/AA_06_Devices_Health.svg",
    "mdPath": "docs/ui/md/Agent_Admin_Portal/AA_06_Devices_Health.md",
    "implStatus": "implemented",
    "routePath": "/pages/AA_06_Devices_Health",
    "permissionKey": "ui.Agent_Admin_Portal.Devices.view",
    "apiTag": "asset-investment",
    "prototypeSvgUrl": "/prototypes/AA_06_Devices_Health.svg",
    "prototypeMdUrl": "/prototypes/AA_06_Devices_Health.md",
    "apiEndpoints": [
      "GET /assets/cabinets",
      "GET /assets/cabinets/{cabinetId}",
      "GET /assets/cabinets/{cabinetId}/slots",
      "POST /iot/cabinets/{cabinetId}/commands/open-slot",
      "GET /iot/cabinets/{cabinetId}/events"
    ]
  },
  {
    "pageKey": "AA_07_1_Settlement_Detail",
    "titleZh": "结算分润详情",
    "role": "AGENT_ADMIN",
    "module": "Settlement",
    "parentPage": "AA_07_Settlements_Shares",
    "entry": "进入详情",
    "subsystem": "Agent_Admin_Portal",
    "svgPath": "docs/ui/svg/Agent_Admin_Portal/AA_07_1_Settlement_Detail.svg",
    "mdPath": "docs/ui/md/Agent_Admin_Portal/AA_07_1_Settlement_Detail.md",
    "implStatus": "implemented",
    "routePath": "/pages/AA_07_1_Settlement_Detail",
    "permissionKey": "ui.Agent_Admin_Portal.Settlement.manage",
    "apiTag": "finance",
    "prototypeSvgUrl": "/prototypes/AA_07_1_Settlement_Detail.svg",
    "prototypeMdUrl": "/prototypes/AA_07_1_Settlement_Detail.md",
    "apiEndpoints": [
      "GET /finance/settlements",
      "GET /finance/settlements/{settlementId}",
      "GET /finance/settlements/{settlementId}/lines",
      "POST /finance/settlements/{settlementId}/export"
    ]
  },
  {
    "pageKey": "AA_07_Settlements_Shares",
    "titleZh": "结算分润（代理）",
    "role": "AGENT_ADMIN",
    "module": "Settlements",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Agent_Admin_Portal",
    "svgPath": "docs/ui/svg/Agent_Admin_Portal/AA_07_Settlements_Shares.svg",
    "mdPath": "docs/ui/md/Agent_Admin_Portal/AA_07_Settlements_Shares.md",
    "implStatus": "implemented",
    "routePath": "/pages/AA_07_Settlements_Shares",
    "permissionKey": "ui.Agent_Admin_Portal.Settlements.manage",
    "apiTag": "finance",
    "prototypeSvgUrl": "/prototypes/AA_07_Settlements_Shares.svg",
    "prototypeMdUrl": "/prototypes/AA_07_Settlements_Shares.md",
    "apiEndpoints": [
      "GET /finance/settlements",
      "GET /finance/settlements/{settlementId}",
      "GET /finance/settlements/{settlementId}/lines",
      "POST /finance/settlements/{settlementId}/export"
    ]
  },
  {
    "pageKey": "AA_08_1_Ticket_Detail",
    "titleZh": "对账工单详情",
    "role": "AGENT_ADMIN",
    "module": "Ticket",
    "parentPage": "AA_08_Reconciliation_Tickets",
    "entry": "进入详情",
    "subsystem": "Agent_Admin_Portal",
    "svgPath": "docs/ui/svg/Agent_Admin_Portal/AA_08_1_Ticket_Detail.svg",
    "mdPath": "docs/ui/md/Agent_Admin_Portal/AA_08_1_Ticket_Detail.md",
    "implStatus": "implemented",
    "routePath": "/pages/AA_08_1_Ticket_Detail",
    "permissionKey": "ui.Agent_Admin_Portal.Ticket.view",
    "apiTag": "finance",
    "prototypeSvgUrl": "/prototypes/AA_08_1_Ticket_Detail.svg",
    "prototypeMdUrl": "/prototypes/AA_08_1_Ticket_Detail.md",
    "apiEndpoints": [
      "GET /finance/reconciliation/tickets",
      "POST /finance/reconciliation/tickets",
      "GET /finance/reconciliation/tickets/{ticketId}"
    ]
  },
  {
    "pageKey": "AA_08_Reconciliation_Tickets",
    "titleZh": "对账工单（辖区）",
    "role": "AGENT_ADMIN",
    "module": "Reconciliation",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Agent_Admin_Portal",
    "svgPath": "docs/ui/svg/Agent_Admin_Portal/AA_08_Reconciliation_Tickets.svg",
    "mdPath": "docs/ui/md/Agent_Admin_Portal/AA_08_Reconciliation_Tickets.md",
    "implStatus": "implemented",
    "routePath": "/pages/AA_08_Reconciliation_Tickets",
    "permissionKey": "ui.Agent_Admin_Portal.Reconciliation.view",
    "apiTag": "finance",
    "prototypeSvgUrl": "/prototypes/AA_08_Reconciliation_Tickets.svg",
    "prototypeMdUrl": "/prototypes/AA_08_Reconciliation_Tickets.md",
    "apiEndpoints": [
      "GET /finance/reconciliation/tickets",
      "POST /finance/reconciliation/tickets",
      "GET /finance/reconciliation/tickets/{ticketId}"
    ]
  },
  {
    "pageKey": "AA_09_1_WorkOrder_Create",
    "titleZh": "新建工单",
    "role": "AGENT_ADMIN",
    "module": "WorkOrder",
    "parentPage": "AA_09_WorkOrders",
    "entry": "新增",
    "subsystem": "Agent_Admin_Portal",
    "svgPath": "docs/ui/svg/Agent_Admin_Portal/AA_09_1_WorkOrder_Create.svg",
    "mdPath": "docs/ui/md/Agent_Admin_Portal/AA_09_1_WorkOrder_Create.md",
    "implStatus": "implemented",
    "routePath": "/pages/AA_09_1_WorkOrder_Create",
    "permissionKey": "ui.Agent_Admin_Portal.WorkOrder.manage",
    "apiTag": "order",
    "prototypeSvgUrl": "/prototypes/AA_09_1_WorkOrder_Create.svg",
    "prototypeMdUrl": "/prototypes/AA_09_1_WorkOrder_Create.md",
    "apiEndpoints": [
      "GET /orders",
      "GET /orders/{orderId}",
      "POST /orders",
      "POST /orders/{orderId}/cancel",
      "POST /orders/{orderId}/dispatch",
      "GET /orders/{orderId}/timeline"
    ]
  },
  {
    "pageKey": "AA_09_2_WorkOrder_Detail",
    "titleZh": "工单详情",
    "role": "AGENT_ADMIN",
    "module": "WorkOrder",
    "parentPage": "AA_09_WorkOrders",
    "entry": "进入详情",
    "subsystem": "Agent_Admin_Portal",
    "svgPath": "docs/ui/svg/Agent_Admin_Portal/AA_09_2_WorkOrder_Detail.svg",
    "mdPath": "docs/ui/md/Agent_Admin_Portal/AA_09_2_WorkOrder_Detail.md",
    "implStatus": "implemented",
    "routePath": "/pages/AA_09_2_WorkOrder_Detail",
    "permissionKey": "ui.Agent_Admin_Portal.WorkOrder.view",
    "apiTag": "order",
    "prototypeSvgUrl": "/prototypes/AA_09_2_WorkOrder_Detail.svg",
    "prototypeMdUrl": "/prototypes/AA_09_2_WorkOrder_Detail.md",
    "apiEndpoints": [
      "GET /orders",
      "GET /orders/{orderId}",
      "POST /orders",
      "POST /orders/{orderId}/cancel",
      "POST /orders/{orderId}/dispatch",
      "GET /orders/{orderId}/timeline"
    ]
  },
  {
    "pageKey": "AA_09_WorkOrders",
    "titleZh": "工单中心",
    "role": "AGENT_ADMIN",
    "module": "WorkOrders",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Agent_Admin_Portal",
    "svgPath": "docs/ui/svg/Agent_Admin_Portal/AA_09_WorkOrders.svg",
    "mdPath": "docs/ui/md/Agent_Admin_Portal/AA_09_WorkOrders.md",
    "implStatus": "implemented",
    "routePath": "/pages/AA_09_WorkOrders",
    "permissionKey": "ui.Agent_Admin_Portal.WorkOrders.view",
    "apiTag": "order",
    "prototypeSvgUrl": "/prototypes/AA_09_WorkOrders.svg",
    "prototypeMdUrl": "/prototypes/AA_09_WorkOrders.md",
    "apiEndpoints": [
      "GET /orders",
      "GET /orders/{orderId}",
      "POST /orders",
      "POST /orders/{orderId}/cancel",
      "POST /orders/{orderId}/dispatch",
      "GET /orders/{orderId}/timeline"
    ]
  },
  {
    "pageKey": "AA_10_1_Report_Filter_Drawer",
    "titleZh": "报表中心",
    "role": "AGENT_ADMIN",
    "module": "Report",
    "parentPage": "AA_10_Reports",
    "entry": "子页面入口",
    "subsystem": "Agent_Admin_Portal",
    "svgPath": "docs/ui/svg/Agent_Admin_Portal/AA_10_1_Report_Filter_Drawer.svg",
    "mdPath": "docs/ui/md/Agent_Admin_Portal/AA_10_1_Report_Filter_Drawer.md",
    "implStatus": "implemented",
    "routePath": "/pages/AA_10_1_Report_Filter_Drawer",
    "permissionKey": "ui.Agent_Admin_Portal.Report.view",
    "apiTag": "reports",
    "prototypeSvgUrl": "/prototypes/AA_10_1_Report_Filter_Drawer.svg",
    "prototypeMdUrl": "/prototypes/AA_10_1_Report_Filter_Drawer.md",
    "apiEndpoints": [
      "GET /reports/summary",
      "POST /reports/export"
    ]
  },
  {
    "pageKey": "AA_10_Reports",
    "titleZh": "报表中心",
    "role": "AGENT_ADMIN",
    "module": "Reports",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Agent_Admin_Portal",
    "svgPath": "docs/ui/svg/Agent_Admin_Portal/AA_10_Reports.svg",
    "mdPath": "docs/ui/md/Agent_Admin_Portal/AA_10_Reports.md",
    "implStatus": "implemented",
    "routePath": "/pages/AA_10_Reports",
    "permissionKey": "ui.Agent_Admin_Portal.Reports.view",
    "apiTag": "reports",
    "prototypeSvgUrl": "/prototypes/AA_10_Reports.svg",
    "prototypeMdUrl": "/prototypes/AA_10_Reports.md",
    "apiEndpoints": [
      "GET /reports/summary",
      "POST /reports/export"
    ]
  },
  {
    "pageKey": "AA_11_1_ChangePassword",
    "titleZh": "修改密码",
    "role": "AGENT_ADMIN",
    "module": "ChangePassword",
    "parentPage": "AA_11_Profile",
    "entry": "子页面入口",
    "subsystem": "Agent_Admin_Portal",
    "svgPath": "docs/ui/svg/Agent_Admin_Portal/AA_11_1_ChangePassword.svg",
    "mdPath": "docs/ui/md/Agent_Admin_Portal/AA_11_1_ChangePassword.md",
    "implStatus": "implemented",
    "routePath": "/pages/AA_11_1_ChangePassword",
    "permissionKey": "ui.Agent_Admin_Portal.ChangePassword.view",
    "apiTag": "ui-static",
    "prototypeSvgUrl": "/prototypes/AA_11_1_ChangePassword.svg",
    "prototypeMdUrl": "/prototypes/AA_11_1_ChangePassword.md",
    "apiEndpoints": []
  },
  {
    "pageKey": "AA_11_Profile",
    "titleZh": "我的账号",
    "role": "AGENT_ADMIN",
    "module": "Profile",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Agent_Admin_Portal",
    "svgPath": "docs/ui/svg/Agent_Admin_Portal/AA_11_Profile.svg",
    "mdPath": "docs/ui/md/Agent_Admin_Portal/AA_11_Profile.md",
    "implStatus": "implemented",
    "routePath": "/pages/AA_11_Profile",
    "permissionKey": "ui.Agent_Admin_Portal.Profile.view",
    "apiTag": "ui-static",
    "prototypeSvgUrl": "/prototypes/AA_11_Profile.svg",
    "prototypeMdUrl": "/prototypes/AA_11_Profile.md",
    "apiEndpoints": []
  },
  {
    "pageKey": "AA_12_1_Binding_Detail",
    "titleZh": "设备健康度",
    "role": "AGENT_ADMIN",
    "module": "Binding",
    "parentPage": "AA_12_CabinetStoreBindings",
    "entry": "进入详情",
    "subsystem": "Agent_Admin_Portal",
    "svgPath": "docs/ui/svg/Agent_Admin_Portal/AA_12_1_Binding_Detail.svg",
    "mdPath": "docs/ui/md/Agent_Admin_Portal/AA_12_1_Binding_Detail.md",
    "implStatus": "implemented",
    "routePath": "/pages/AA_12_1_Binding_Detail",
    "permissionKey": "ui.Agent_Admin_Portal.Binding.manage",
    "apiTag": "asset",
    "prototypeSvgUrl": "/prototypes/AA_12_1_Binding_Detail.svg",
    "prototypeMdUrl": "/prototypes/AA_12_1_Binding_Detail.md",
    "apiEndpoints": [
      "GET /asset/cabinets/{cabinetId}/store-bindings",
      "POST /asset/cabinets/{cabinetId}/store-bindings",
      "PATCH /asset/cabinet-store-bindings/{bindingId}",
      "POST /asset/cabinet-store-bindings/{bindingId}/approve"
    ]
  },
  {
    "pageKey": "AA_12_2_Binding_Request_Create",
    "titleZh": "新建工单",
    "role": "AGENT_ADMIN",
    "module": "Binding",
    "parentPage": "AA_12_CabinetStoreBindings",
    "entry": "新增",
    "subsystem": "Agent_Admin_Portal",
    "svgPath": "docs/ui/svg/Agent_Admin_Portal/AA_12_2_Binding_Request_Create.svg",
    "mdPath": "docs/ui/md/Agent_Admin_Portal/AA_12_2_Binding_Request_Create.md",
    "implStatus": "implemented",
    "routePath": "/pages/AA_12_2_Binding_Request_Create",
    "permissionKey": "ui.Agent_Admin_Portal.Binding.manage",
    "apiTag": "asset",
    "prototypeSvgUrl": "/prototypes/AA_12_2_Binding_Request_Create.svg",
    "prototypeMdUrl": "/prototypes/AA_12_2_Binding_Request_Create.md",
    "apiEndpoints": [
      "GET /asset/cabinets/{cabinetId}/store-bindings",
      "POST /asset/cabinets/{cabinetId}/store-bindings",
      "PATCH /asset/cabinet-store-bindings/{bindingId}",
      "POST /asset/cabinet-store-bindings/{bindingId}/approve"
    ]
  },
  {
    "pageKey": "AA_12_CabinetStoreBindings",
    "titleZh": "共享柜体绑定度",
    "role": "AGENT_ADMIN",
    "module": "CabinetStoreBindings",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Agent_Admin_Portal",
    "svgPath": "docs/ui/svg/Agent_Admin_Portal/AA_12_CabinetStoreBindings.svg",
    "mdPath": "docs/ui/md/Agent_Admin_Portal/AA_12_CabinetStoreBindings.md",
    "implStatus": "implemented",
    "routePath": "/pages/AA_12_CabinetStoreBindings",
    "permissionKey": "ui.Agent_Admin_Portal.CabinetStoreBindings.manage",
    "apiTag": "asset-investment",
    "prototypeSvgUrl": "/prototypes/AA_12_CabinetStoreBindings.svg",
    "prototypeMdUrl": "/prototypes/AA_12_CabinetStoreBindings.md",
    "apiEndpoints": [
      "GET /assets/cabinets",
      "GET /assets/cabinets/{cabinetId}",
      "GET /assets/cabinets/{cabinetId}/slots",
      "POST /iot/cabinets/{cabinetId}/commands/open-slot",
      "GET /iot/cabinets/{cabinetId}/events",
      "GET /asset/cabinets/{cabinetId}/store-bindings",
      "POST /asset/cabinets/{cabinetId}/store-bindings",
      "PATCH /asset/cabinet-store-bindings/{bindingId}",
      "POST /asset/cabinet-store-bindings/{bindingId}/approve"
    ]
  }
]

export const pageMap = new Map<string, PageMeta>(pages.map((page) => [page.pageKey, page]))

export const childMap = pages.reduce((acc, page) => {
  if (!page.parentPage) {
    return acc
  }
  const list = acc.get(page.parentPage) || []
  list.push(page)
  acc.set(page.parentPage, list)
  return acc
}, new Map<string, PageMeta[]>())

export const topLevelPages = pages.filter((page) => !page.parentPage)

export const defaultPageKey = 'AA_02_Region_Dashboard'
