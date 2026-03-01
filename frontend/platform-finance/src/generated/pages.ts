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
    "pageKey": "PF_01_Login",
    "titleZh": "备至·智洗平台",
    "role": "PLATFORM_FINANCE",
    "module": "Login",
    "parentPage": "",
    "entry": "入口-登录",
    "subsystem": "Platform_Finance_Portal",
    "svgPath": "docs/ui/svg/Platform_Finance_Portal/PF_01_Login.svg",
    "mdPath": "docs/ui/md/Platform_Finance_Portal/PF_01_Login.md",
    "implStatus": "implemented",
    "routePath": "/pages/PF_01_Login",
    "permissionKey": "ui.Platform_Finance_Portal.Login.view",
    "apiTag": "identity",
    "prototypeSvgUrl": "/prototypes/PF_01_Login.svg",
    "prototypeMdUrl": "/prototypes/PF_01_Login.md",
    "apiEndpoints": [
      "POST /auth/login",
      "POST /auth/mfa/verify",
      "POST /auth/logout"
    ]
  },
  {
    "pageKey": "PF_02_Finance_Dashboard",
    "titleZh": "运营大盘",
    "role": "PLATFORM_FINANCE",
    "module": "Finance",
    "parentPage": "",
    "entry": "入口-首页",
    "subsystem": "Platform_Finance_Portal",
    "svgPath": "docs/ui/svg/Platform_Finance_Portal/PF_02_Finance_Dashboard.svg",
    "mdPath": "docs/ui/md/Platform_Finance_Portal/PF_02_Finance_Dashboard.md",
    "implStatus": "implemented",
    "routePath": "/pages/PF_02_Finance_Dashboard",
    "permissionKey": "ui.Platform_Finance_Portal.Finance.view",
    "apiTag": "ui-static",
    "prototypeSvgUrl": "/prototypes/PF_02_Finance_Dashboard.svg",
    "prototypeMdUrl": "/prototypes/PF_02_Finance_Dashboard.md",
    "apiEndpoints": []
  },
  {
    "pageKey": "PF_03_Commercial_Agreements",
    "titleZh": "商业协议（财务视图）",
    "role": "PLATFORM_FINANCE",
    "module": "Commercial",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Platform_Finance_Portal",
    "svgPath": "docs/ui/svg/Platform_Finance_Portal/PF_03_Commercial_Agreements.svg",
    "mdPath": "docs/ui/md/Platform_Finance_Portal/PF_03_Commercial_Agreements.md",
    "implStatus": "implemented",
    "routePath": "/pages/PF_03_Commercial_Agreements",
    "permissionKey": "ui.Platform_Finance_Portal.Commercial.view",
    "apiTag": "ui-static",
    "prototypeSvgUrl": "/prototypes/PF_03_Commercial_Agreements.svg",
    "prototypeMdUrl": "/prototypes/PF_03_Commercial_Agreements.md",
    "apiEndpoints": []
  },
  {
    "pageKey": "PF_04_Collector_Accounts",
    "titleZh": "收款账户（代收/入账）",
    "role": "PLATFORM_FINANCE",
    "module": "Collector",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Platform_Finance_Portal",
    "svgPath": "docs/ui/svg/Platform_Finance_Portal/PF_04_Collector_Accounts.svg",
    "mdPath": "docs/ui/md/Platform_Finance_Portal/PF_04_Collector_Accounts.md",
    "implStatus": "implemented",
    "routePath": "/pages/PF_04_Collector_Accounts",
    "permissionKey": "ui.Platform_Finance_Portal.Collector.view",
    "apiTag": "finance",
    "prototypeSvgUrl": "/prototypes/PF_04_Collector_Accounts.svg",
    "prototypeMdUrl": "/prototypes/PF_04_Collector_Accounts.md",
    "apiEndpoints": [
      "GET /finance/payout-accounts",
      "POST /finance/payout-accounts",
      "PATCH /finance/payout-accounts/{accountId}",
      "POST /finance/payout-accounts/{accountId}/submit-review"
    ]
  },
  {
    "pageKey": "PF_05_Settlements_List",
    "titleZh": "结算批次",
    "role": "PLATFORM_FINANCE",
    "module": "Settlements",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Platform_Finance_Portal",
    "svgPath": "docs/ui/svg/Platform_Finance_Portal/PF_05_Settlements_List.svg",
    "mdPath": "docs/ui/md/Platform_Finance_Portal/PF_05_Settlements_List.md",
    "implStatus": "implemented",
    "routePath": "/pages/PF_05_Settlements_List",
    "permissionKey": "ui.Platform_Finance_Portal.Settlements.manage",
    "apiTag": "finance",
    "prototypeSvgUrl": "/prototypes/PF_05_Settlements_List.svg",
    "prototypeMdUrl": "/prototypes/PF_05_Settlements_List.md",
    "apiEndpoints": [
      "GET /finance/settlements",
      "GET /finance/settlements/{settlementId}",
      "GET /finance/settlements/{settlementId}/lines",
      "POST /finance/settlements/{settlementId}/export"
    ]
  },
  {
    "pageKey": "PF_06_Settlement_Detail",
    "titleZh": "结算批次详情与分账明细",
    "role": "PLATFORM_FINANCE",
    "module": "Settlement",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Platform_Finance_Portal",
    "svgPath": "docs/ui/svg/Platform_Finance_Portal/PF_06_Settlement_Detail.svg",
    "mdPath": "docs/ui/md/Platform_Finance_Portal/PF_06_Settlement_Detail.md",
    "implStatus": "implemented",
    "routePath": "/pages/PF_06_Settlement_Detail",
    "permissionKey": "ui.Platform_Finance_Portal.Settlement.manage",
    "apiTag": "finance",
    "prototypeSvgUrl": "/prototypes/PF_06_Settlement_Detail.svg",
    "prototypeMdUrl": "/prototypes/PF_06_Settlement_Detail.md",
    "apiEndpoints": [
      "GET /finance/settlements",
      "GET /finance/settlements/{settlementId}",
      "GET /finance/settlements/{settlementId}/lines",
      "POST /finance/settlements/{settlementId}/export"
    ]
  },
  {
    "pageKey": "PF_07_Payout_Remittance",
    "titleZh": "打款：生成打款指令渠道账单与跑批",
    "role": "PLATFORM_FINANCE",
    "module": "Payout",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Platform_Finance_Portal",
    "svgPath": "docs/ui/svg/Platform_Finance_Portal/PF_07_Payout_Remittance.svg",
    "mdPath": "docs/ui/md/Platform_Finance_Portal/PF_07_Payout_Remittance.md",
    "implStatus": "implemented",
    "routePath": "/pages/PF_07_Payout_Remittance",
    "permissionKey": "ui.Platform_Finance_Portal.Payout.view",
    "apiTag": "finance",
    "prototypeSvgUrl": "/prototypes/PF_07_Payout_Remittance.svg",
    "prototypeMdUrl": "/prototypes/PF_07_Payout_Remittance.md",
    "apiEndpoints": [
      "GET /finance/payout-accounts",
      "POST /finance/payout-accounts",
      "PATCH /finance/payout-accounts/{accountId}",
      "POST /finance/payout-accounts/{accountId}/submit-review"
    ]
  },
  {
    "pageKey": "PF_08_Refunds_List",
    "titleZh": "退款审批中心",
    "role": "PLATFORM_FINANCE",
    "module": "Refunds",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Platform_Finance_Portal",
    "svgPath": "docs/ui/svg/Platform_Finance_Portal/PF_08_Refunds_List.svg",
    "mdPath": "docs/ui/md/Platform_Finance_Portal/PF_08_Refunds_List.md",
    "implStatus": "implemented",
    "routePath": "/pages/PF_08_Refunds_List",
    "permissionKey": "ui.Platform_Finance_Portal.Refunds.manage",
    "apiTag": "finance",
    "prototypeSvgUrl": "/prototypes/PF_08_Refunds_List.svg",
    "prototypeMdUrl": "/prototypes/PF_08_Refunds_List.md",
    "apiEndpoints": [
      "GET /finance/refunds",
      "POST /finance/refunds",
      "POST /finance/refunds/{refundId}/approve"
    ]
  },
  {
    "pageKey": "PF_09_Refund_Detail",
    "titleZh": "退款单详情（审批/拒绝）",
    "role": "PLATFORM_FINANCE",
    "module": "Refund",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Platform_Finance_Portal",
    "svgPath": "docs/ui/svg/Platform_Finance_Portal/PF_09_Refund_Detail.svg",
    "mdPath": "docs/ui/md/Platform_Finance_Portal/PF_09_Refund_Detail.md",
    "implStatus": "implemented",
    "routePath": "/pages/PF_09_Refund_Detail",
    "permissionKey": "ui.Platform_Finance_Portal.Refund.manage",
    "apiTag": "finance",
    "prototypeSvgUrl": "/prototypes/PF_09_Refund_Detail.svg",
    "prototypeMdUrl": "/prototypes/PF_09_Refund_Detail.md",
    "apiEndpoints": [
      "GET /finance/refunds",
      "POST /finance/refunds",
      "POST /finance/refunds/{refundId}/approve"
    ]
  },
  {
    "pageKey": "PF_10_Reconciliation_Run",
    "titleZh": "对账：导入渠道账单与跑批",
    "role": "PLATFORM_FINANCE",
    "module": "Reconciliation",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Platform_Finance_Portal",
    "svgPath": "docs/ui/svg/Platform_Finance_Portal/PF_10_Reconciliation_Run.svg",
    "mdPath": "docs/ui/md/Platform_Finance_Portal/PF_10_Reconciliation_Run.md",
    "implStatus": "implemented",
    "routePath": "/pages/PF_10_Reconciliation_Run",
    "permissionKey": "ui.Platform_Finance_Portal.Reconciliation.view",
    "apiTag": "finance",
    "prototypeSvgUrl": "/prototypes/PF_10_Reconciliation_Run.svg",
    "prototypeMdUrl": "/prototypes/PF_10_Reconciliation_Run.md",
    "apiEndpoints": [
      "GET /finance/reconciliation/tickets",
      "POST /finance/reconciliation/tickets",
      "GET /finance/reconciliation/tickets/{ticketId}"
    ]
  },
  {
    "pageKey": "PF_11_Reconciliation_Tickets",
    "titleZh": "对账差异工单（财务）",
    "role": "PLATFORM_FINANCE",
    "module": "Reconciliation",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Platform_Finance_Portal",
    "svgPath": "docs/ui/svg/Platform_Finance_Portal/PF_11_Reconciliation_Tickets.svg",
    "mdPath": "docs/ui/md/Platform_Finance_Portal/PF_11_Reconciliation_Tickets.md",
    "implStatus": "implemented",
    "routePath": "/pages/PF_11_Reconciliation_Tickets",
    "permissionKey": "ui.Platform_Finance_Portal.Reconciliation.view",
    "apiTag": "finance",
    "prototypeSvgUrl": "/prototypes/PF_11_Reconciliation_Tickets.svg",
    "prototypeMdUrl": "/prototypes/PF_11_Reconciliation_Tickets.md",
    "apiEndpoints": [
      "GET /finance/reconciliation/tickets",
      "POST /finance/reconciliation/tickets",
      "GET /finance/reconciliation/tickets/{ticketId}"
    ]
  },
  {
    "pageKey": "PF_12_Reconciliation_Ticket_Detail",
    "titleZh": "对账工单详情（财务）",
    "role": "PLATFORM_FINANCE",
    "module": "Reconciliation",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Platform_Finance_Portal",
    "svgPath": "docs/ui/svg/Platform_Finance_Portal/PF_12_Reconciliation_Ticket_Detail.svg",
    "mdPath": "docs/ui/md/Platform_Finance_Portal/PF_12_Reconciliation_Ticket_Detail.md",
    "implStatus": "implemented",
    "routePath": "/pages/PF_12_Reconciliation_Ticket_Detail",
    "permissionKey": "ui.Platform_Finance_Portal.Reconciliation.view",
    "apiTag": "finance",
    "prototypeSvgUrl": "/prototypes/PF_12_Reconciliation_Ticket_Detail.svg",
    "prototypeMdUrl": "/prototypes/PF_12_Reconciliation_Ticket_Detail.md",
    "apiEndpoints": [
      "GET /finance/reconciliation/tickets",
      "POST /finance/reconciliation/tickets",
      "GET /finance/reconciliation/tickets/{ticketId}"
    ]
  },
  {
    "pageKey": "PF_13_Invoices_List",
    "titleZh": "发票审批中心",
    "role": "PLATFORM_FINANCE",
    "module": "Invoices",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Platform_Finance_Portal",
    "svgPath": "docs/ui/svg/Platform_Finance_Portal/PF_13_Invoices_List.svg",
    "mdPath": "docs/ui/md/Platform_Finance_Portal/PF_13_Invoices_List.md",
    "implStatus": "implemented",
    "routePath": "/pages/PF_13_Invoices_List",
    "permissionKey": "ui.Platform_Finance_Portal.Invoices.view",
    "apiTag": "ui-static",
    "prototypeSvgUrl": "/prototypes/PF_13_Invoices_List.svg",
    "prototypeMdUrl": "/prototypes/PF_13_Invoices_List.md",
    "apiEndpoints": []
  },
  {
    "pageKey": "PF_14_Invoice_Detail",
    "titleZh": "发票单详情（审批/拒绝）",
    "role": "PLATFORM_FINANCE",
    "module": "Invoice",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Platform_Finance_Portal",
    "svgPath": "docs/ui/svg/Platform_Finance_Portal/PF_14_Invoice_Detail.svg",
    "mdPath": "docs/ui/md/Platform_Finance_Portal/PF_14_Invoice_Detail.md",
    "implStatus": "implemented",
    "routePath": "/pages/PF_14_Invoice_Detail",
    "permissionKey": "ui.Platform_Finance_Portal.Invoice.view",
    "apiTag": "ui-static",
    "prototypeSvgUrl": "/prototypes/PF_14_Invoice_Detail.svg",
    "prototypeMdUrl": "/prototypes/PF_14_Invoice_Detail.md",
    "apiEndpoints": []
  },
  {
    "pageKey": "PF_15_Fee_Plans",
    "titleZh": "费率策略（Service Fee Policy）",
    "role": "PLATFORM_FINANCE",
    "module": "Fee",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Platform_Finance_Portal",
    "svgPath": "docs/ui/svg/Platform_Finance_Portal/PF_15_Fee_Plans.svg",
    "mdPath": "docs/ui/md/Platform_Finance_Portal/PF_15_Fee_Plans.md",
    "implStatus": "implemented",
    "routePath": "/pages/PF_15_Fee_Plans",
    "permissionKey": "ui.Platform_Finance_Portal.Fee.view",
    "apiTag": "ui-static",
    "prototypeSvgUrl": "/prototypes/PF_15_Fee_Plans.svg",
    "prototypeMdUrl": "/prototypes/PF_15_Fee_Plans.md",
    "apiEndpoints": []
  },
  {
    "pageKey": "PF_16_Finance_Reports",
    "titleZh": "财务报表",
    "role": "PLATFORM_FINANCE",
    "module": "Finance",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Platform_Finance_Portal",
    "svgPath": "docs/ui/svg/Platform_Finance_Portal/PF_16_Finance_Reports.svg",
    "mdPath": "docs/ui/md/Platform_Finance_Portal/PF_16_Finance_Reports.md",
    "implStatus": "implemented",
    "routePath": "/pages/PF_16_Finance_Reports",
    "permissionKey": "ui.Platform_Finance_Portal.Finance.view",
    "apiTag": "reports",
    "prototypeSvgUrl": "/prototypes/PF_16_Finance_Reports.svg",
    "prototypeMdUrl": "/prototypes/PF_16_Finance_Reports.md",
    "apiEndpoints": [
      "GET /reports/summary",
      "POST /reports/export"
    ]
  },
  {
    "pageKey": "PF_17_Audit_Export",
    "titleZh": "审计日志导出（不可变审计）",
    "role": "PLATFORM_FINANCE",
    "module": "Audit",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Platform_Finance_Portal",
    "svgPath": "docs/ui/svg/Platform_Finance_Portal/PF_17_Audit_Export.svg",
    "mdPath": "docs/ui/md/Platform_Finance_Portal/PF_17_Audit_Export.md",
    "implStatus": "implemented",
    "routePath": "/pages/PF_17_Audit_Export",
    "permissionKey": "ui.Platform_Finance_Portal.Audit.view",
    "apiTag": "audit",
    "prototypeSvgUrl": "/prototypes/PF_17_Audit_Export.svg",
    "prototypeMdUrl": "/prototypes/PF_17_Audit_Export.md",
    "apiEndpoints": [
      "GET /audit/logs",
      "GET /audit/logs/{logId}",
      "POST /audit/exports"
    ]
  },
  {
    "pageKey": "PF_18_Profile",
    "titleZh": "我的账号",
    "role": "PLATFORM_FINANCE",
    "module": "Profile",
    "parentPage": "",
    "entry": "菜单入口",
    "subsystem": "Platform_Finance_Portal",
    "svgPath": "docs/ui/svg/Platform_Finance_Portal/PF_18_Profile.svg",
    "mdPath": "docs/ui/md/Platform_Finance_Portal/PF_18_Profile.md",
    "implStatus": "implemented",
    "routePath": "/pages/PF_18_Profile",
    "permissionKey": "ui.Platform_Finance_Portal.Profile.view",
    "apiTag": "ui-static",
    "prototypeSvgUrl": "/prototypes/PF_18_Profile.svg",
    "prototypeMdUrl": "/prototypes/PF_18_Profile.md",
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

export const defaultPageKey = 'PF_02_Finance_Dashboard'
