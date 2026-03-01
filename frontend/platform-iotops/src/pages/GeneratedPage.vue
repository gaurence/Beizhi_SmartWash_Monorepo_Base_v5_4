<script setup lang="ts">
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
      <div class="brand">{{ 'Platform_IoTOps_Portal' }}</div>
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
