import fs from 'node:fs';
import path from 'node:path';
import {
  ensureKnownSubsystems,
  groupPagesBySubsystem,
  loadPageInventory,
  resolveAppDirForSubsystem,
} from '../lib/page-inventory.mjs';
import { loadTraceabilityRows } from '../lib/traceability.mjs';
import { routePathForPage } from '../lib/page-utils.mjs';

const repoRoot = process.cwd();
const pages = loadPageInventory(repoRoot);
ensureKnownSubsystems(pages);
const traceRows = loadTraceabilityRows(repoRoot).rows;
const traceByPage = new Map(traceRows.map((row) => [row.pageKey, row]));

const pagesBySubsystem = groupPagesBySubsystem(pages);

for (const [subsystem, subsystemPages] of pagesBySubsystem.entries()) {
  const appDirName = resolveAppDirForSubsystem(subsystem);
  const appDir = path.join(repoRoot, 'frontend', appDirName);

  const enriched = subsystemPages.map((page) => {
    const trace = traceByPage.get(page.pageKey);
    return {
      ...page,
      routePath: routePathForPage(page.pageKey),
      permissionKey: trace?.permissionKey || '',
      apiTag: trace?.apiTag || '',
      prototypeSvgUrl: `/prototypes/${page.pageKey}.svg`,
      prototypeMdUrl: `/prototypes/${page.pageKey}.md`,
      apiEndpoints: (trace?.apiEndpoints || '')
        .split(';')
        .map((item) => item.trim())
        .filter(Boolean),
    };
  });

  const defaultPage = pickDefaultPage(enriched);

  ensureDir(appDir);
  ensureDir(path.join(appDir, 'src'));
  ensureDir(path.join(appDir, 'src', 'router'));
  ensureDir(path.join(appDir, 'src', 'pages'));
  ensureDir(path.join(appDir, 'src', 'generated'));
  ensureDir(path.join(appDir, 'generated'));
  const prototypeDir = path.join(appDir, 'public', 'prototypes');
  resetDir(prototypeDir);
  for (const page of enriched) {
    copyIfExists(path.join(repoRoot, page.svgPath), path.join(prototypeDir, `${page.pageKey}.svg`));
    copyIfExists(path.join(repoRoot, page.mdPath), path.join(prototypeDir, `${page.pageKey}.md`));
  }

  writeFileIfChanged(path.join(appDir, 'package.json'), JSON.stringify(buildPackageJson(appDirName), null, 2) + '\n');
  writeFileIfChanged(path.join(appDir, 'tsconfig.json'), buildTsConfig());
  writeFileIfChanged(path.join(appDir, 'vite.config.ts'), buildViteConfig());
  writeFileIfChanged(path.join(appDir, 'index.html'), buildIndexHtml(subsystem));

  writeFileIfChanged(path.join(appDir, 'src', 'main.ts'), buildMainTs());
  writeFileIfChanged(path.join(appDir, 'src', 'App.vue'), buildAppVue());
  writeFileIfChanged(path.join(appDir, 'src', 'router', 'index.ts'), buildRouterTs(defaultPage.pageKey));
  writeFileIfChanged(path.join(appDir, 'src', 'pages', 'GeneratedPage.vue'), buildGeneratedPageVue(subsystem));
  writeFileIfChanged(path.join(appDir, 'src', 'styles.css'), buildStylesCss());

  writeFileIfChanged(path.join(appDir, 'src', 'generated', 'pages.ts'), buildGeneratedPagesTs(enriched, defaultPage.pageKey));
  writeFileIfChanged(path.join(appDir, 'generated', 'pages.json'), JSON.stringify(enriched, null, 2) + '\n');
}

console.log(`Generated frontend scaffolds for ${pagesBySubsystem.size} subsystems / ${pages.length} pages.`);

function pickDefaultPage(pagesInSubsystem) {
  const preferred = pagesInSubsystem.find((page) => page.entry.includes('首页'))
    || pagesInSubsystem.find((page) => page.entry.includes('登录'))
    || pagesInSubsystem.find((page) => !page.parentPage)
    || pagesInSubsystem[0];

  if (!preferred) {
    throw new Error('No page found while generating frontend scaffold.');
  }

  return preferred;
}

function buildPackageJson(appDirName) {
  return {
    name: `beizhi-${appDirName}`,
    private: true,
    version: '0.1.0',
    type: 'module',
    scripts: {
      dev: 'vite',
      build: 'vite build',
      preview: 'vite preview',
    },
    dependencies: {
      vue: '^3.4.0',
      'vue-router': '^4.3.0',
    },
    devDependencies: {
      '@vitejs/plugin-vue': '^5.0.0',
      typescript: '^5.3.0',
      vite: '^5.0.0',
    },
  };
}

function buildTsConfig() {
  return `{
  "compilerOptions": {
    "target": "ES2022",
    "module": "ESNext",
    "moduleResolution": "Bundler",
    "strict": true,
    "types": [
      "vite/client"
    ]
  }
}\n`;
}

function buildViteConfig() {
  return `import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
})
`;
}

function buildIndexHtml(subsystem) {
  return `<!doctype html>
<html lang="zh-CN">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>${subsystem}</title>
  </head>
  <body>
    <div id="app"></div>
    <script type="module" src="/src/main.ts"></script>
  </body>
</html>
`;
}

function buildMainTs() {
  return `import { createApp } from 'vue'
import App from './App.vue'
import { router } from './router'
import './styles.css'

createApp(App).use(router).mount('#app')
`;
}

function buildAppVue() {
  return `<template><router-view /></template>
`;
}

function buildRouterTs(defaultPageKey) {
  return `import { createRouter, createWebHistory } from 'vue-router'
import GeneratedPage from '../pages/GeneratedPage.vue'
import { pageMap } from '../generated/pages'

const defaultPath = '/pages/${defaultPageKey}'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: defaultPath },
    {
      path: '/pages/:pageKey',
      component: GeneratedPage,
      beforeEnter: (to) => {
        const pageKey = String(to.params.pageKey || '')
        return pageMap.has(pageKey) ? true : defaultPath
      },
    },
    { path: '/:pathMatch(.*)*', redirect: defaultPath },
  ],
})
`;
}

function buildGeneratedPagesTs(pagesInSubsystem, defaultPageKey) {
  const payload = JSON.stringify(pagesInSubsystem, null, 2);
  return `export interface PageMeta {
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

export const pages: PageMeta[] = ${payload}

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

export const defaultPageKey = '${defaultPageKey}'
`;
}

function buildGeneratedPageVue(subsystem) {
  return `<script setup lang="ts">
import { computed, ref, watch, watchEffect } from 'vue'
import { useRoute } from 'vue-router'
import { childMap, pageMap, topLevelPages } from '../generated/pages'

const route = useRoute()

const rolePresets: Record<string, { username?: string; password?: string; phone?: string; otp?: string }> = {
  PLATFORM_ADMIN: { username: 'pa_admin', password: 'Passw0rd!' },
  PLATFORM_OPS: { username: 'ops_admin', password: 'Passw0rd!' },
  PLATFORM_FINANCE: { username: 'fin_admin', password: 'Passw0rd!' },
  PLATFORM_IOTOPS: { username: 'iot_admin', password: 'Passw0rd!' },
  PLATFORM_CS: { username: 'cs_admin', password: 'Passw0rd!' },
  AGENT_ADMIN: { username: 'agent_admin', password: 'Passw0rd!' },
  STORE_ADMIN: { username: 'store_admin', password: 'Passw0rd!' },
  DEVICE_INVESTOR: { username: 'investor_admin', password: 'Passw0rd!' },
  INVESTOR_ADMIN: { username: 'investor_admin', password: 'Passw0rd!' },
  PROPERTY_FINANCE: { username: 'property_fin', password: 'Passw0rd!' },
  PROPERTY_ADMIN: { username: 'property_admin', password: 'Passw0rd!' },
  TECHNICIAN: { username: 'tech01', password: 'Passw0rd!' },
  VALET: { username: 'valet01', password: 'Passw0rd!' },
  CUSTOMER: { phone: '13800000000', otp: '123456' },
}

const loginForm = ref({ username: '', password: '', phone: '', otp: '' })
const reportPeriod = ref('LAST_30_DAYS')
const reportMetric = ref('orders')
const exportFormat = ref('CSV')
const drawerOpen = ref(false)
const filters = ref({
  store: '全部门店',
  site: '全部站点',
  channel: '全部渠道',
  minAmount: '0',
})
const snapshotAt = ref('2026-02-28T09:00:00Z')
const prototypeMarkdown = ref('')
const prototypeLoading = ref(false)

const periodOptions = [
  { value: 'TODAY', label: '今天' },
  { value: 'LAST_7_DAYS', label: '近 7 天' },
  { value: 'LAST_30_DAYS', label: '近 30 天' },
  { value: 'THIS_MONTH', label: '本月' },
]

const trendSeed = [
  { date: '02-24', orders: 112, amount: 11800 },
  { date: '02-25', orders: 126, amount: 12680 },
  { date: '02-26', orders: 119, amount: 12250 },
  { date: '02-27', orders: 133, amount: 13110 },
  { date: '02-28', orders: 128, amount: 12790 },
]

const rankingSeed = [
  { store: '国贸门店', site: 'CBD A 座', orders: 118, amount: 13260, rate: 0.97, owner: '张三' },
  { store: '望京门店', site: '望京 SOHO', orders: 102, amount: 11240, rate: 0.95, owner: '李四' },
  { store: '中关村门店', site: '软件园二期', orders: 96, amount: 10790, rate: 0.93, owner: '王五' },
  { store: '金融街门店', site: '广场 B1', orders: 84, amount: 9640, rate: 0.92, owner: '赵六' },
]

const currentPage = computed(() => {
  const pageKey = String(route.params.pageKey || '')
  return pageMap.get(pageKey)
})

const parentPage = computed(() => {
  const page = currentPage.value
  if (!page || !page.parentPage) {
    return undefined
  }
  return pageMap.get(page.parentPage)
})

const childPages = computed(() => {
  const page = currentPage.value
  if (!page) {
    return []
  }
  return childMap.get(page.pageKey) || []
})

const isLoginPage = computed(() => {
  const page = currentPage.value
  return !!page && (page.pageKey.endsWith('_Login') || page.module.toLowerCase().includes('login'))
})

const isReportPage = computed(() => {
  const page = currentPage.value
  if (!page) {
    return false
  }
  return page.pageKey === 'PA_02_Global_Dashboard'
    || page.pageKey.includes('_Reports')
    || page.pageKey.includes('Report_Filter_Drawer')
})

const isFilterDrawerPage = computed(() => {
  const page = currentPage.value
  return !!page && page.pageKey.includes('Report_Filter_Drawer')
})

const reportScope = computed(() => {
  const role = currentPage.value?.role || ''
  if (role === 'AGENT_ADMIN') {
    return '代理维度'
  }
  if (role === 'STORE_ADMIN') {
    return '门店维度'
  }
  if (role === 'DEVICE_INVESTOR') {
    return '投资方维度'
  }
  if (role === 'PROPERTY_FINANCE' || role === 'PROPERTY_ADMIN') {
    return '物业维度'
  }
  if (role === 'PLATFORM_FINANCE') {
    return '财务维度'
  }
  return '平台维度'
})

const piiMasked = computed(() => {
  const role = currentPage.value?.role || ''
  return role === 'DEVICE_INVESTOR' || role === 'PROPERTY_FINANCE' || role === 'PROPERTY_ADMIN'
})

const kpiCards = computed(() => {
  const ratio = reportScope.value === '平台维度' ? 1 : 0.74
  return [
    { key: 'orders', label: '订单量', value: Math.round(642 * ratio), delta: '+12.8%' },
    { key: 'gross', label: '交易额(¥)', value: Math.round(268000 * ratio), delta: '+8.4%' },
    { key: 'net', label: '净入账(¥)', value: Math.round(252600 * ratio), delta: '+7.1%' },
    { key: 'onTime', label: '准时率', value: (96.2 * ratio + 24).toFixed(1) + '%', delta: '+1.2pt' },
  ]
})

const trendRows = computed(() => {
  return trendSeed.map((item) => ({
    ...item,
    metric: reportMetric.value === 'amount' ? item.amount : item.orders,
  }))
})

const maxTrendValue = computed(() => {
  const values = trendRows.value.map((row) => row.metric)
  return values.length > 0 ? Math.max(...values) : 1
})

const rankingRows = computed(() => {
  if (!piiMasked.value) {
    return rankingSeed
  }
  return rankingSeed.map((row) => ({
    ...row,
    owner: row.owner.slice(0, 1) + '**',
  }))
})

const periodLabel = computed(() => {
  const matched = periodOptions.find((option) => option.value === reportPeriod.value)
  return matched ? matched.label : reportPeriod.value
})

function openDrawer() {
  drawerOpen.value = true
}

function closeDrawer() {
  if (isFilterDrawerPage.value) {
    return
  }
  drawerOpen.value = false
}

function applyFilters() {
  snapshotAt.value = new Date().toISOString()
  if (!isFilterDrawerPage.value) {
    drawerOpen.value = false
  }
}

function markExport() {
  snapshotAt.value = new Date().toISOString()
}

watchEffect(() => {
  const page = currentPage.value
  if (!page) {
    return
  }
  const preset = rolePresets[page.role]
  loginForm.value = {
    username: preset?.username || '',
    password: preset?.password || '',
    phone: preset?.phone || '',
    otp: preset?.otp || '',
  }
})

watchEffect(() => {
  if (isFilterDrawerPage.value) {
    drawerOpen.value = true
  }
})

watch(currentPage, async (page) => {
  if (!page) {
    prototypeMarkdown.value = ''
    return
  }

  prototypeLoading.value = true
  try {
    const response = await fetch(page.prototypeMdUrl)
    prototypeMarkdown.value = response.ok ? await response.text() : ''
  } catch (_error) {
    prototypeMarkdown.value = ''
  } finally {
    prototypeLoading.value = false
  }
}, { immediate: true })
</script>

<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="brand">{{ '${subsystem}' }}</div>
      <nav class="menu">
        <router-link
          v-for="item in topLevelPages"
          :key="item.pageKey"
          :to="item.routePath"
          class="menu-link"
          :class="{ active: currentPage && currentPage.pageKey === item.pageKey }"
        >
          <span class="menu-key">{{ item.pageKey }}</span>
          <span class="menu-title">{{ item.titleZh || item.pageKey }}</span>
        </router-link>
      </nav>
    </aside>

    <main class="content" v-if="currentPage">
      <header class="page-header">
        <h1>{{ currentPage.titleZh || currentPage.pageKey }}</h1>
        <p>{{ currentPage.pageKey }} · permission: <code>{{ currentPage.permissionKey || 'TBD' }}</code></p>
      </header>

      <section class="cards" v-if="isReportPage">
        <article class="card report-toolbar">
          <div>
            <p class="card-eyebrow">Reporting Workspace</p>
            <h2>{{ reportScope }} · {{ periodLabel }}</h2>
            <p class="muted">最近快照：{{ snapshotAt }}</p>
          </div>
          <div class="toolbar-actions">
            <button type="button" class="btn ghost" @click="openDrawer">筛选条件</button>
            <select v-model="exportFormat" class="select">
              <option value="CSV">CSV</option>
              <option value="XLSX">XLSX</option>
              <option value="PDF">PDF</option>
            </select>
            <button type="button" class="btn primary" @click="markExport">导出报表</button>
          </div>
        </article>

        <article class="card period-switch">
          <button
            v-for="period in periodOptions"
            :key="period.value"
            type="button"
            class="chip"
            :class="{ active: reportPeriod === period.value }"
            @click="reportPeriod = period.value"
          >
            {{ period.label }}
          </button>
          <select v-model="reportMetric" class="select compact">
            <option value="orders">趋势指标：订单量</option>
            <option value="amount">趋势指标：交易额</option>
          </select>
        </article>

        <section class="kpi-grid">
          <article class="card kpi-card" v-for="item in kpiCards" :key="item.key">
            <p class="kpi-label">{{ item.label }}</p>
            <p class="kpi-value">{{ item.value }}</p>
            <p class="kpi-delta">{{ item.delta }}</p>
          </article>
        </section>

        <section class="report-grid">
          <article class="card trend-card">
            <h2>趋势</h2>
            <div class="trend-chart">
              <div class="trend-col" v-for="row in trendRows" :key="row.date">
                <div class="trend-bar-wrap">
                  <span
                    class="trend-bar"
                    :style="{ height: (Math.max(row.metric / maxTrendValue * 150, 20)).toFixed(0) + 'px' }"
                  />
                </div>
                <span class="trend-date">{{ row.date }}</span>
                <strong class="trend-value">{{ row.metric }}</strong>
              </div>
            </div>
          </article>

          <article class="card table-card">
            <h2>门店排名</h2>
            <table class="report-table">
              <thead>
                <tr>
                  <th>门店</th>
                  <th>站点</th>
                  <th>负责人</th>
                  <th>订单量</th>
                  <th>交易额(¥)</th>
                  <th>准时率</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in rankingRows" :key="row.store">
                  <td>{{ row.store }}</td>
                  <td>{{ row.site }}</td>
                  <td>{{ row.owner }}</td>
                  <td>{{ row.orders }}</td>
                  <td>{{ row.amount }}</td>
                  <td>{{ (row.rate * 100).toFixed(1) }}%</td>
                </tr>
              </tbody>
            </table>
          </article>
        </section>

        <article class="card" v-if="currentPage.apiEndpoints.length > 0">
          <h2>API Mapping</h2>
          <ul>
            <li v-for="endpoint in currentPage.apiEndpoints" :key="endpoint">
              <code>{{ endpoint }}</code>
            </li>
          </ul>
        </article>

        <article class="card report-drawer-host" v-if="drawerOpen || isFilterDrawerPage">
          <div class="drawer-mask" @click.self="closeDrawer">
            <aside class="drawer-panel">
              <h3>筛选抽屉</h3>
              <p class="muted">按租户与角色范围执行过滤，不允许跨租户查询。</p>
              <label>
                门店
                <input v-model="filters.store" type="text" />
              </label>
              <label>
                站点
                <input v-model="filters.site" type="text" />
              </label>
              <label>
                渠道
                <input v-model="filters.channel" type="text" />
              </label>
              <label>
                最低交易额
                <input v-model="filters.minAmount" type="number" min="0" />
              </label>
              <div class="drawer-actions">
                <button type="button" class="btn ghost" @click="closeDrawer">取消</button>
                <button type="button" class="btn primary" @click="applyFilters">应用筛选</button>
              </div>
            </aside>
          </div>
        </article>
      </section>

      <section class="cards two-columns" v-else>
        <article class="card">
          <h2>Page Metadata</h2>
          <ul>
            <li><strong>role:</strong> {{ currentPage.role || '-' }}</li>
            <li><strong>module:</strong> {{ currentPage.module || '-' }}</li>
            <li><strong>entry:</strong> {{ currentPage.entry || '-' }}</li>
            <li><strong>impl_status:</strong> {{ currentPage.implStatus }}</li>
            <li><strong>api_tag:</strong> {{ currentPage.apiTag || 'ui-static' }}</li>
            <li><strong>svg:</strong> <code>{{ currentPage.svgPath }}</code></li>
            <li><strong>md:</strong> <code>{{ currentPage.mdPath }}</code></li>
          </ul>
        </article>

        <article class="card">
          <h2>Navigation</h2>
          <div class="link-group" v-if="parentPage">
            <label>Parent</label>
            <router-link :to="parentPage.routePath">{{ parentPage.pageKey }} · {{ parentPage.titleZh }}</router-link>
          </div>
          <div class="link-group" v-else>
            <label>Parent</label>
            <span>-</span>
          </div>
          <div class="link-group">
            <label>Children</label>
            <div class="children">
              <router-link
                v-for="child in childPages"
                :key="child.pageKey"
                :to="child.routePath"
              >
                {{ child.pageKey }} · {{ child.titleZh }}
              </router-link>
              <span v-if="childPages.length === 0">-</span>
            </div>
          </div>
        </article>
      </section>

      <section class="cards two-columns" v-if="isReportPage">
        <article class="card">
          <h2>Page Metadata</h2>
          <ul>
            <li><strong>role:</strong> {{ currentPage.role || '-' }}</li>
            <li><strong>module:</strong> {{ currentPage.module || '-' }}</li>
            <li><strong>entry:</strong> {{ currentPage.entry || '-' }}</li>
            <li><strong>impl_status:</strong> {{ currentPage.implStatus }}</li>
            <li><strong>api_tag:</strong> {{ currentPage.apiTag || 'ui-static' }}</li>
            <li><strong>svg:</strong> <code>{{ currentPage.svgPath }}</code></li>
            <li><strong>md:</strong> <code>{{ currentPage.mdPath }}</code></li>
          </ul>
        </article>

        <article class="card">
          <h2>Navigation</h2>
          <div class="link-group" v-if="parentPage">
            <label>Parent</label>
            <router-link :to="parentPage.routePath">{{ parentPage.pageKey }} · {{ parentPage.titleZh }}</router-link>
          </div>
          <div class="link-group" v-else>
            <label>Parent</label>
            <span>-</span>
          </div>
          <div class="link-group">
            <label>Children</label>
            <div class="children">
              <router-link
                v-for="child in childPages"
                :key="child.pageKey"
                :to="child.routePath"
              >
                {{ child.pageKey }} · {{ child.titleZh }}
              </router-link>
              <span v-if="childPages.length === 0">-</span>
            </div>
          </div>
        </article>
      </section>

      <section class="cards two-columns">
        <article class="card prototype-card">
          <h2>Prototype SVG</h2>
          <div class="prototype-preview">
            <img :src="currentPage.prototypeSvgUrl" :alt="currentPage.pageKey + ' prototype'" loading="lazy" />
          </div>
        </article>

        <article class="card">
          <h2>Page Spec (MD)</h2>
          <p class="muted" v-if="prototypeLoading">Loading markdown...</p>
          <pre class="md-preview" v-else-if="prototypeMarkdown">{{ prototypeMarkdown }}</pre>
          <p class="muted" v-else>Markdown spec unavailable in this runtime.</p>
        </article>
      </section>

      <section class="card" v-if="currentPage.apiEndpoints.length > 0 && !isReportPage">
        <h2>API Mapping</h2>
        <ul>
          <li v-for="endpoint in currentPage.apiEndpoints" :key="endpoint">
            <code>{{ endpoint }}</code>
          </li>
        </ul>
      </section>

      <section class="card" v-if="isLoginPage">
        <h2>Login Mock (Seed Defaults)</h2>
        <div class="form-grid">
          <label>
            Username
            <input v-model="loginForm.username" type="text" autocomplete="username" />
          </label>
          <label>
            Password
            <input v-model="loginForm.password" type="password" autocomplete="current-password" />
          </label>
          <label>
            Phone
            <input v-model="loginForm.phone" type="text" />
          </label>
          <label>
            OTP
            <input v-model="loginForm.otp" type="text" />
          </label>
        </div>
      </section>
    </main>

    <main class="content" v-else>
      <section class="card">
        <h2>Page not found</h2>
      </section>
    </main>
  </div>
</template>
`;
}

function buildStylesCss() {
  return `:root {
  font-family: 'PingFang SC', 'Noto Sans SC', 'Microsoft YaHei', sans-serif;
  color: #0f172a;
  background: #eef2ff;
}

* {
  box-sizing: border-box;
}

body {
  margin: 0;
}

a {
  color: #0f172a;
}

code {
  background: #eef2ff;
  border-radius: 6px;
  padding: 2px 6px;
}

.layout {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
}

.sidebar {
  background: linear-gradient(180deg, #082f49, #0f172a);
  color: #e2e8f0;
  padding: 20px;
  overflow: auto;
}

.brand {
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 16px;
}

.menu {
  display: grid;
  gap: 8px;
}

.menu-link {
  text-decoration: none;
  color: inherit;
  display: grid;
  gap: 4px;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.menu-link.active,
.menu-link:hover {
  background: rgba(255, 255, 255, 0.1);
}

.menu-key {
  font-size: 11px;
  opacity: 0.8;
}

.menu-title {
  font-size: 13px;
}

.content {
  padding: 20px;
  display: grid;
  gap: 16px;
}

.page-header h1 {
  margin: 0;
  font-size: 28px;
}

.page-header p {
  margin: 6px 0 0;
  color: #475569;
}

.cards {
  display: grid;
  gap: 16px;
}

.two-columns {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.card {
  background: #ffffff;
  border-radius: 14px;
  border: 1px solid #dbeafe;
  padding: 16px;
}

.card h2 {
  margin-top: 0;
  font-size: 18px;
}

.card ul {
  margin: 0;
  padding-left: 18px;
  display: grid;
  gap: 8px;
}

.link-group {
  margin-bottom: 10px;
  display: grid;
  gap: 4px;
}

.link-group label {
  font-size: 12px;
  text-transform: uppercase;
  color: #64748b;
  letter-spacing: 0.08em;
}

.children {
  display: grid;
  gap: 6px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.form-grid label {
  display: grid;
  gap: 6px;
  font-size: 13px;
  color: #334155;
}

.form-grid input {
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  padding: 8px 10px;
}

.prototype-card {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
}

.prototype-preview {
  border: 1px solid #dbeafe;
  border-radius: 10px;
  background: #f8fafc;
  padding: 10px;
  min-height: 420px;
  max-height: 620px;
  overflow: auto;
}

.prototype-preview img {
  display: block;
  width: 100%;
  height: auto;
}

.md-preview {
  margin: 0;
  white-space: pre-wrap;
  line-height: 1.45;
  font-size: 12px;
  color: #1e293b;
  background: #f8fafc;
  border: 1px solid #dbeafe;
  border-radius: 10px;
  padding: 10px;
  min-height: 420px;
  max-height: 620px;
  overflow: auto;
}

.card-eyebrow {
  margin: 0 0 6px;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: #64748b;
}

.muted {
  color: #64748b;
  font-size: 13px;
}

.toolbar-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.btn {
  border: 1px solid transparent;
  border-radius: 9px;
  font-size: 13px;
  font-weight: 600;
  padding: 8px 12px;
  cursor: pointer;
}

.btn.ghost {
  background: #f8fafc;
  border-color: #cbd5e1;
  color: #334155;
}

.btn.primary {
  background: #ff7a00;
  color: #ffffff;
}

.select {
  border: 1px solid #cbd5e1;
  border-radius: 9px;
  padding: 8px 10px;
  background: #ffffff;
  color: #0f172a;
}

.select.compact {
  min-width: 220px;
}

.period-switch {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.chip {
  border: 1px solid #cbd5e1;
  border-radius: 999px;
  padding: 6px 12px;
  background: #ffffff;
  color: #334155;
  font-size: 12px;
  cursor: pointer;
}

.chip.active {
  border-color: #fb923c;
  background: #fff7ed;
  color: #9a3412;
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.kpi-card {
  border-color: #fed7aa;
  background: linear-gradient(180deg, #fff7ed, #ffffff);
}

.kpi-label {
  margin: 0;
  color: #64748b;
  font-size: 12px;
}

.kpi-value {
  margin: 10px 0 6px;
  font-size: 28px;
  font-weight: 700;
  color: #111827;
}

.kpi-delta {
  margin: 0;
  color: #16a34a;
  font-size: 12px;
}

.report-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
}

.trend-card,
.table-card {
  min-height: 320px;
}

.trend-chart {
  margin-top: 10px;
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 10px;
  align-items: end;
}

.trend-col {
  display: grid;
  justify-items: center;
  gap: 6px;
}

.trend-bar-wrap {
  height: 180px;
  width: 100%;
  border-radius: 10px;
  background: #f1f5f9;
  display: flex;
  align-items: flex-end;
  padding: 6px;
}

.trend-bar {
  width: 100%;
  border-radius: 8px 8px 0 0;
  background: linear-gradient(180deg, #fdba74, #f97316);
}

.trend-date {
  font-size: 12px;
  color: #64748b;
}

.trend-value {
  font-size: 12px;
  color: #1f2937;
}

.report-table {
  width: 100%;
  border-collapse: collapse;
}

.report-table th,
.report-table td {
  border-bottom: 1px solid #e2e8f0;
  text-align: left;
  padding: 10px 8px;
  font-size: 13px;
}

.report-table th {
  background: #f8fafc;
  color: #475569;
  font-weight: 600;
}

.report-drawer-host {
  border: 0;
  padding: 0;
  background: transparent;
}

.drawer-mask {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  justify-content: flex-end;
  z-index: 40;
}

.drawer-panel {
  width: min(420px, 92vw);
  height: 100%;
  background: #ffffff;
  padding: 18px;
  display: grid;
  gap: 12px;
}

.drawer-panel h3 {
  margin: 0;
  font-size: 20px;
}

.drawer-panel label {
  display: grid;
  gap: 6px;
  font-size: 13px;
  color: #334155;
}

.drawer-panel input {
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  padding: 8px 10px;
}

.drawer-actions {
  margin-top: auto;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

@media (max-width: 1024px) {
  .layout {
    grid-template-columns: 1fr;
  }

  .sidebar {
    max-height: 35vh;
  }

  .two-columns {
    grid-template-columns: 1fr;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .kpi-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .report-grid {
    grid-template-columns: 1fr;
  }

  .toolbar-actions {
    width: 100%;
  }

  .select.compact {
    min-width: 100%;
  }
}
`;
}

function ensureDir(dirPath) {
  fs.mkdirSync(dirPath, { recursive: true });
}

function resetDir(dirPath) {
  fs.rmSync(dirPath, { recursive: true, force: true });
  fs.mkdirSync(dirPath, { recursive: true });
}

function copyIfExists(fromPath, toPath) {
  if (!fs.existsSync(fromPath)) {
    return;
  }
  fs.copyFileSync(fromPath, toPath);
}

function writeFileIfChanged(filePath, next) {
  const current = fs.existsSync(filePath) ? fs.readFileSync(filePath, 'utf8') : null;
  if (current === next) {
    return;
  }
  fs.writeFileSync(filePath, next, 'utf8');
}
