<script setup lang="ts">
import { computed, ref, watchEffect } from 'vue'
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
  PROPERTY_ADMIN: { username: 'property_admin', password: 'Passw0rd!' },
  TECHNICIAN: { username: 'tech01', password: 'Passw0rd!' },
  VALET: { username: 'valet01', password: 'Passw0rd!' },
  CUSTOMER: { phone: '13800000000', otp: '123456' },
}

const loginForm = ref({ username: '', password: '', phone: '', otp: '' })

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
</script>

<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="brand">{{ 'Platform_Admin_Portal' }}</div>
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

      <section class="cards two-columns">
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

      <section class="card" v-if="currentPage.apiEndpoints.length > 0">
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
