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
    "pageKey": "PP_01_Login",
    "titleZh": "备至·智洗平台",
    "role": "PROPERTY_FINANCE",
    "module": "Login",
    "parentPage": "",
    "entry": "入口-登录",
    "subsystem": "Property_Portal",
    "svgPath": "docs/ui/svg/Property_Portal/PP_01_Login.svg",
    "mdPath": "docs/ui/md/Property_Portal/PP_01_Login.md",
    "implStatus": "implemented",
    "routePath": "/pages/PP_01_Login",
    "permissionKey": "ui.Property_Portal.Login.view",
    "apiTag": "identity",
    "prototypeSvgUrl": "/prototypes/PP_01_Login.svg",
    "prototypeMdUrl": "/prototypes/PP_01_Login.md",
    "apiEndpoints": [
      "POST /auth/login",
      "POST /auth/mfa/verify",
      "POST /auth/logout"
    ]
  },
  {
    "pageKey": "PP_02_Property_Dashboard",
    "titleZh": "运营大盘",
    "role": "PROPERTY_FINANCE",
    "module": "Property",
    "parentPage": "",
    "entry": "入口-首页",
    "subsystem": "Property_Portal",
    "svgPath": "docs/ui/svg/Property_Portal/PP_02_Property_Dashboard.svg",
    "mdPath": "docs/ui/md/Property_Portal/PP_02_Property_Dashboard.md",
    "implStatus": "implemented",
    "routePath": "/pages/PP_02_Property_Dashboard",
    "permissionKey": "ui.Property_Portal.Property.view",
    "apiTag": "ui-static",
    "prototypeSvgUrl": "/prototypes/PP_02_Property_Dashboard.svg",
    "prototypeMdUrl": "/prototypes/PP_02_Property_Dashboard.md",
    "apiEndpoints": []
  },
  {
    "pageKey": "PP_03_Settlements_List",
    "titleZh": "物业结算批次",
    "role": "PROPERTY_FINANCE",
    "module": "Settlements",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Property_Portal",
    "svgPath": "docs/ui/svg/Property_Portal/PP_03_Settlements_List.svg",
    "mdPath": "docs/ui/md/Property_Portal/PP_03_Settlements_List.md",
    "implStatus": "implemented",
    "routePath": "/pages/PP_03_Settlements_List",
    "permissionKey": "ui.Property_Portal.Settlements.manage",
    "apiTag": "finance",
    "prototypeSvgUrl": "/prototypes/PP_03_Settlements_List.svg",
    "prototypeMdUrl": "/prototypes/PP_03_Settlements_List.md",
    "apiEndpoints": [
      "GET /finance/settlements",
      "GET /finance/settlements/{settlementId}",
      "GET /finance/settlements/{settlementId}/lines",
      "POST /finance/settlements/{settlementId}/export"
    ]
  },
  {
    "pageKey": "PP_04_Settlement_Detail",
    "titleZh": "物业结算批次详情与分账明细",
    "role": "PROPERTY_FINANCE",
    "module": "Settlement",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Property_Portal",
    "svgPath": "docs/ui/svg/Property_Portal/PP_04_Settlement_Detail.svg",
    "mdPath": "docs/ui/md/Property_Portal/PP_04_Settlement_Detail.md",
    "implStatus": "implemented",
    "routePath": "/pages/PP_04_Settlement_Detail",
    "permissionKey": "ui.Property_Portal.Settlement.manage",
    "apiTag": "finance",
    "prototypeSvgUrl": "/prototypes/PP_04_Settlement_Detail.svg",
    "prototypeMdUrl": "/prototypes/PP_04_Settlement_Detail.md",
    "apiEndpoints": [
      "GET /finance/settlements",
      "GET /finance/settlements/{settlementId}",
      "GET /finance/settlements/{settlementId}/lines",
      "POST /finance/settlements/{settlementId}/export"
    ]
  },
  {
    "pageKey": "PP_05_Remittance_Tasks",
    "titleZh": "打款：生成打款指令渠道账单与跑批",
    "role": "PROPERTY_FINANCE",
    "module": "Remittance",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Property_Portal",
    "svgPath": "docs/ui/svg/Property_Portal/PP_05_Remittance_Tasks.svg",
    "mdPath": "docs/ui/md/Property_Portal/PP_05_Remittance_Tasks.md",
    "implStatus": "implemented",
    "routePath": "/pages/PP_05_Remittance_Tasks",
    "permissionKey": "ui.Property_Portal.Remittance.view",
    "apiTag": "task",
    "prototypeSvgUrl": "/prototypes/PP_05_Remittance_Tasks.svg",
    "prototypeMdUrl": "/prototypes/PP_05_Remittance_Tasks.md",
    "apiEndpoints": [
      "GET /tasks",
      "POST /tasks/{taskId}/assign",
      "POST /tasks/{taskId}/accept",
      "POST /tasks/{taskId}/complete"
    ]
  },
  {
    "pageKey": "PP_06_Reconciliation_Tickets",
    "titleZh": "对账差异工单（物业）",
    "role": "PROPERTY_FINANCE",
    "module": "Reconciliation",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Property_Portal",
    "svgPath": "docs/ui/svg/Property_Portal/PP_06_Reconciliation_Tickets.svg",
    "mdPath": "docs/ui/md/Property_Portal/PP_06_Reconciliation_Tickets.md",
    "implStatus": "implemented",
    "routePath": "/pages/PP_06_Reconciliation_Tickets",
    "permissionKey": "ui.Property_Portal.Reconciliation.view",
    "apiTag": "finance",
    "prototypeSvgUrl": "/prototypes/PP_06_Reconciliation_Tickets.svg",
    "prototypeMdUrl": "/prototypes/PP_06_Reconciliation_Tickets.md",
    "apiEndpoints": [
      "GET /finance/reconciliation/tickets",
      "POST /finance/reconciliation/tickets",
      "GET /finance/reconciliation/tickets/{ticketId}"
    ]
  },
  {
    "pageKey": "PP_07_Ticket_Detail",
    "titleZh": "对账工单详情（物业）",
    "role": "PROPERTY_FINANCE",
    "module": "Ticket",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Property_Portal",
    "svgPath": "docs/ui/svg/Property_Portal/PP_07_Ticket_Detail.svg",
    "mdPath": "docs/ui/md/Property_Portal/PP_07_Ticket_Detail.md",
    "implStatus": "implemented",
    "routePath": "/pages/PP_07_Ticket_Detail",
    "permissionKey": "ui.Property_Portal.Ticket.view",
    "apiTag": "finance",
    "prototypeSvgUrl": "/prototypes/PP_07_Ticket_Detail.svg",
    "prototypeMdUrl": "/prototypes/PP_07_Ticket_Detail.md",
    "apiEndpoints": [
      "GET /finance/reconciliation/tickets",
      "POST /finance/reconciliation/tickets",
      "GET /finance/reconciliation/tickets/{ticketId}"
    ]
  },
  {
    "pageKey": "PP_08_Reports",
    "titleZh": "物业报表",
    "role": "PROPERTY_FINANCE",
    "module": "Reports",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Property_Portal",
    "svgPath": "docs/ui/svg/Property_Portal/PP_08_Reports.svg",
    "mdPath": "docs/ui/md/Property_Portal/PP_08_Reports.md",
    "implStatus": "implemented",
    "routePath": "/pages/PP_08_Reports",
    "permissionKey": "ui.Property_Portal.Reports.view",
    "apiTag": "reports",
    "prototypeSvgUrl": "/prototypes/PP_08_Reports.svg",
    "prototypeMdUrl": "/prototypes/PP_08_Reports.md",
    "apiEndpoints": [
      "GET /reports/summary",
      "POST /reports/export"
    ]
  },
  {
    "pageKey": "PP_09_Profile",
    "titleZh": "我的账号",
    "role": "PROPERTY_FINANCE",
    "module": "Profile",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Property_Portal",
    "svgPath": "docs/ui/svg/Property_Portal/PP_09_Profile.svg",
    "mdPath": "docs/ui/md/Property_Portal/PP_09_Profile.md",
    "implStatus": "implemented",
    "routePath": "/pages/PP_09_Profile",
    "permissionKey": "ui.Property_Portal.Profile.view",
    "apiTag": "ui-static",
    "prototypeSvgUrl": "/prototypes/PP_09_Profile.svg",
    "prototypeMdUrl": "/prototypes/PP_09_Profile.md",
    "apiEndpoints": []
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

export const defaultPageKey = 'PP_02_Property_Dashboard'
