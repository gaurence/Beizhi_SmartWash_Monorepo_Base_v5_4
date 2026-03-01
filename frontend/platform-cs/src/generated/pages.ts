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
    "pageKey": "PC_01_Login",
    "titleZh": "备至·智洗平台",
    "role": "PLATFORM_CS",
    "module": "Login",
    "parentPage": "",
    "entry": "入口-登录",
    "subsystem": "Platform_CS_Portal",
    "svgPath": "docs/ui/svg/Platform_CS_Portal/PC_01_Login.svg",
    "mdPath": "docs/ui/md/Platform_CS_Portal/PC_01_Login.md",
    "implStatus": "implemented",
    "routePath": "/pages/PC_01_Login",
    "permissionKey": "ui.Platform_CS_Portal.Login.view",
    "apiTag": "identity",
    "prototypeSvgUrl": "/prototypes/PC_01_Login.svg",
    "prototypeMdUrl": "/prototypes/PC_01_Login.md",
    "apiEndpoints": [
      "POST /auth/login",
      "POST /auth/mfa/verify",
      "POST /auth/logout"
    ]
  },
  {
    "pageKey": "PC_02_CS_Dashboard",
    "titleZh": "运营大盘",
    "role": "PLATFORM_CS",
    "module": "CS",
    "parentPage": "",
    "entry": "入口-首页",
    "subsystem": "Platform_CS_Portal",
    "svgPath": "docs/ui/svg/Platform_CS_Portal/PC_02_CS_Dashboard.svg",
    "mdPath": "docs/ui/md/Platform_CS_Portal/PC_02_CS_Dashboard.md",
    "implStatus": "implemented",
    "routePath": "/pages/PC_02_CS_Dashboard",
    "permissionKey": "ui.Platform_CS_Portal.CS.view",
    "apiTag": "ui-static",
    "prototypeSvgUrl": "/prototypes/PC_02_CS_Dashboard.svg",
    "prototypeMdUrl": "/prototypes/PC_02_CS_Dashboard.md",
    "apiEndpoints": []
  },
  {
    "pageKey": "PC_03_Tickets_List",
    "titleZh": "客服工单中心",
    "role": "PLATFORM_CS",
    "module": "Tickets",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Platform_CS_Portal",
    "svgPath": "docs/ui/svg/Platform_CS_Portal/PC_03_Tickets_List.svg",
    "mdPath": "docs/ui/md/Platform_CS_Portal/PC_03_Tickets_List.md",
    "implStatus": "implemented",
    "routePath": "/pages/PC_03_Tickets_List",
    "permissionKey": "ui.Platform_CS_Portal.Tickets.view",
    "apiTag": "finance",
    "prototypeSvgUrl": "/prototypes/PC_03_Tickets_List.svg",
    "prototypeMdUrl": "/prototypes/PC_03_Tickets_List.md",
    "apiEndpoints": [
      "GET /finance/reconciliation/tickets",
      "POST /finance/reconciliation/tickets",
      "GET /finance/reconciliation/tickets/{ticketId}"
    ]
  },
  {
    "pageKey": "PC_04_Ticket_Detail",
    "titleZh": "投诉/纠纷工单详情（客服）",
    "role": "PLATFORM_CS",
    "module": "Ticket",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Platform_CS_Portal",
    "svgPath": "docs/ui/svg/Platform_CS_Portal/PC_04_Ticket_Detail.svg",
    "mdPath": "docs/ui/md/Platform_CS_Portal/PC_04_Ticket_Detail.md",
    "implStatus": "implemented",
    "routePath": "/pages/PC_04_Ticket_Detail",
    "permissionKey": "ui.Platform_CS_Portal.Ticket.view",
    "apiTag": "finance",
    "prototypeSvgUrl": "/prototypes/PC_04_Ticket_Detail.svg",
    "prototypeMdUrl": "/prototypes/PC_04_Ticket_Detail.md",
    "apiEndpoints": [
      "GET /finance/reconciliation/tickets",
      "POST /finance/reconciliation/tickets",
      "GET /finance/reconciliation/tickets/{ticketId}"
    ]
  },
  {
    "pageKey": "PC_05_Order_Search",
    "titleZh": "订单查询",
    "role": "PLATFORM_CS",
    "module": "Order",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Platform_CS_Portal",
    "svgPath": "docs/ui/svg/Platform_CS_Portal/PC_05_Order_Search.svg",
    "mdPath": "docs/ui/md/Platform_CS_Portal/PC_05_Order_Search.md",
    "implStatus": "implemented",
    "routePath": "/pages/PC_05_Order_Search",
    "permissionKey": "ui.Platform_CS_Portal.Order.view",
    "apiTag": "order",
    "prototypeSvgUrl": "/prototypes/PC_05_Order_Search.svg",
    "prototypeMdUrl": "/prototypes/PC_05_Order_Search.md",
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
    "pageKey": "PC_06_Order_Detail",
    "titleZh": "订单详情（客服）",
    "role": "PLATFORM_CS",
    "module": "Order",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Platform_CS_Portal",
    "svgPath": "docs/ui/svg/Platform_CS_Portal/PC_06_Order_Detail.svg",
    "mdPath": "docs/ui/md/Platform_CS_Portal/PC_06_Order_Detail.md",
    "implStatus": "implemented",
    "routePath": "/pages/PC_06_Order_Detail",
    "permissionKey": "ui.Platform_CS_Portal.Order.view",
    "apiTag": "order",
    "prototypeSvgUrl": "/prototypes/PC_06_Order_Detail.svg",
    "prototypeMdUrl": "/prototypes/PC_06_Order_Detail.md",
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
    "pageKey": "PC_07_Evidence_Export",
    "titleZh": "审计日志导出（不可变审计）",
    "role": "PLATFORM_CS",
    "module": "Evidence",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Platform_CS_Portal",
    "svgPath": "docs/ui/svg/Platform_CS_Portal/PC_07_Evidence_Export.svg",
    "mdPath": "docs/ui/md/Platform_CS_Portal/PC_07_Evidence_Export.md",
    "implStatus": "implemented",
    "routePath": "/pages/PC_07_Evidence_Export",
    "permissionKey": "ui.Platform_CS_Portal.Evidence.view",
    "apiTag": "evidence",
    "prototypeSvgUrl": "/prototypes/PC_07_Evidence_Export.svg",
    "prototypeMdUrl": "/prototypes/PC_07_Evidence_Export.md",
    "apiEndpoints": [
      "POST /evidence/upload-url",
      "POST /evidence/records",
      "GET /orders/{orderId}/evidence"
    ]
  },
  {
    "pageKey": "PC_08_Insurance_Claims",
    "titleZh": "理赔中心",
    "role": "PLATFORM_CS",
    "module": "Insurance",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Platform_CS_Portal",
    "svgPath": "docs/ui/svg/Platform_CS_Portal/PC_08_Insurance_Claims.svg",
    "mdPath": "docs/ui/md/Platform_CS_Portal/PC_08_Insurance_Claims.md",
    "implStatus": "implemented",
    "routePath": "/pages/PC_08_Insurance_Claims",
    "permissionKey": "ui.Platform_CS_Portal.Insurance.view",
    "apiTag": "ui-static",
    "prototypeSvgUrl": "/prototypes/PC_08_Insurance_Claims.svg",
    "prototypeMdUrl": "/prototypes/PC_08_Insurance_Claims.md",
    "apiEndpoints": []
  },
  {
    "pageKey": "PC_09_Claim_Detail",
    "titleZh": "理赔详情（客服）",
    "role": "PLATFORM_CS",
    "module": "Claim",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Platform_CS_Portal",
    "svgPath": "docs/ui/svg/Platform_CS_Portal/PC_09_Claim_Detail.svg",
    "mdPath": "docs/ui/md/Platform_CS_Portal/PC_09_Claim_Detail.md",
    "implStatus": "implemented",
    "routePath": "/pages/PC_09_Claim_Detail",
    "permissionKey": "ui.Platform_CS_Portal.Claim.view",
    "apiTag": "ui-static",
    "prototypeSvgUrl": "/prototypes/PC_09_Claim_Detail.svg",
    "prototypeMdUrl": "/prototypes/PC_09_Claim_Detail.md",
    "apiEndpoints": []
  },
  {
    "pageKey": "PC_10_Refund_Assist",
    "titleZh": "退款审批中心",
    "role": "PLATFORM_CS",
    "module": "Refund",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Platform_CS_Portal",
    "svgPath": "docs/ui/svg/Platform_CS_Portal/PC_10_Refund_Assist.svg",
    "mdPath": "docs/ui/md/Platform_CS_Portal/PC_10_Refund_Assist.md",
    "implStatus": "implemented",
    "routePath": "/pages/PC_10_Refund_Assist",
    "permissionKey": "ui.Platform_CS_Portal.Refund.manage",
    "apiTag": "finance",
    "prototypeSvgUrl": "/prototypes/PC_10_Refund_Assist.svg",
    "prototypeMdUrl": "/prototypes/PC_10_Refund_Assist.md",
    "apiEndpoints": [
      "GET /finance/refunds",
      "POST /finance/refunds",
      "POST /finance/refunds/{refundId}/approve"
    ]
  },
  {
    "pageKey": "PC_11_Profile",
    "titleZh": "我的账号",
    "role": "PLATFORM_CS",
    "module": "Profile",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Platform_CS_Portal",
    "svgPath": "docs/ui/svg/Platform_CS_Portal/PC_11_Profile.svg",
    "mdPath": "docs/ui/md/Platform_CS_Portal/PC_11_Profile.md",
    "implStatus": "implemented",
    "routePath": "/pages/PC_11_Profile",
    "permissionKey": "ui.Platform_CS_Portal.Profile.view",
    "apiTag": "ui-static",
    "prototypeSvgUrl": "/prototypes/PC_11_Profile.svg",
    "prototypeMdUrl": "/prototypes/PC_11_Profile.md",
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

export const defaultPageKey = 'PC_02_CS_Dashboard'
