<template>
  <div class="layout" :class="{ compact: isCompact }">
    <aside v-show="!isCompact" class="aside">
      <div class="brand">
        <img class="logo" src="/logo.png" alt="高校FOR-GET舞室" />
        <div>
          <div class="title">高校FOR-GET舞室</div>
          <div class="sub">管理后台</div>
        </div>
      </div>
      <div class="menu-scroll">
        <AdminMenu />
      </div>
    </aside>

    <el-drawer
      v-model="menuOpen"
      direction="ltr"
      size="220px"
      :with-header="false"
      append-to-body
      class="admin-nav-drawer"
    >
      <div class="aside drawer-aside">
        <div class="brand">
          <img class="logo" src="/logo.png" alt="高校FOR-GET舞室" />
          <div>
            <div class="title">高校FOR-GET舞室</div>
            <div class="sub">管理后台</div>
          </div>
        </div>
        <div class="menu-scroll">
          <AdminMenu @select="menuOpen = false" />
        </div>
      </div>
    </el-drawer>

    <div class="shell">
      <header class="header" :class="{ compact: isCompact, mobile: isMobile }">
        <template v-if="isMobile">
          <div class="mobile-header-top">
            <button v-if="isCompact" type="button" class="menu-btn" aria-label="打开菜单" @click="menuOpen = true">
              <el-icon :size="20"><Menu /></el-icon>
            </button>
            <div class="crumb">{{ route.meta.title || '管理后台' }}</div>
            <p class="header-hint">{{ mobileHeaderHint }}</p>
          </div>
          <div class="mobile-header-bottom">
            <el-select
              v-if="campusOptions.length > 1"
              v-model="campusId"
              placeholder="全部校区"
              clearable
              class="campus-select"
              @change="onCampusChange"
            >
              <el-option v-for="item in campusOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
            <el-button text type="primary" class="mobile-logout" @click="logout">退出</el-button>
          </div>
        </template>
        <template v-else>
          <div class="header-left">
            <div class="crumb">{{ route.meta.title || '管理后台' }}</div>
          </div>
          <div class="right">
            <template v-if="campusOptions.length > 1">
              <span class="campus-label">校区</span>
              <el-select
                v-model="campusId"
                placeholder="全部校区"
                clearable
                class="campus-select"
                @change="onCampusChange"
              >
                <el-option v-for="item in campusOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </template>
            <span class="role-tag">{{ roleLabel(auth.profile?.role) }}</span>
            <span class="user-name">{{ auth.profile?.name || auth.profile?.username || '管理员' }}</span>
            <el-button text type="primary" @click="logout">退出</el-button>
          </div>
        </template>
      </header>
      <main class="main" :class="{ compact: isCompact, mobile: isMobile }">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { Menu } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'
import { useCampusStore } from '../stores/campus'
import { allowedCampuses, roleLabel } from '../common/adminAccess'
import { loadCampuses } from '../common/campuses'
import { useBreakpoint } from '../composables/useBreakpoint'
import AdminMenu from './AdminMenu.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const campusStore = useCampusStore()
const { campusId } = storeToRefs(campusStore)
const campusOptions = computed(() => allowedCampuses(auth.profile))
const { isMobile, isCompact } = useBreakpoint()
const menuOpen = ref(false)

const mobileHeaderHint = computed(() => {
  const name = auth.profile?.name || auth.profile?.username
  const role = roleLabel(auth.profile?.role)
  if (name && role) return `你好，${name} · ${role}`
  if (name) return `你好，${name}`
  return role || '欢迎使用管理后台'
})

function onCampusChange(value) {
  campusStore.setCampus(value || '')
}

watch(
  () => route.path,
  () => {
    menuOpen.value = false
  },
)

watch(isCompact, (compact) => {
  if (!compact) menuOpen.value = false
})

onMounted(async () => {
  if (auth.token) {
    try {
      await loadCampuses()
      await auth.fetchMe()
      campusStore.syncWithProfile(auth.profile)
    } catch {
      auth.logout()
      router.push('/login')
    }
  }
})

watch(
  () => auth.profile,
  (profile) => {
    if (profile) campusStore.syncWithProfile(profile)
  },
)

function logout() {
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  display: flex;
  width: 100%;
  height: 100%;
  overflow: hidden;
  background: #f4f5f8;
}

.aside {
  width: 220px;
  flex-shrink: 0;
  background: #16161c;
  color: #fff;
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.drawer-aside {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.menu-scroll {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}

.brand {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 20px 16px 16px;
}

.logo {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  object-fit: contain;
  background: #2a1818;
  flex-shrink: 0;
}

.title {
  font-weight: 700;
  font-size: 13px;
  line-height: 1.3;
}

.sub {
  font-size: 12px;
  color: #9a98a8;
  margin-top: 2px;
}

.shell {
  flex: 1;
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  background: #fff;
  border-bottom: 1px solid #eee;
  min-height: 60px;
  padding: 10px 20px;
  box-sizing: border-box;
  flex-shrink: 0;
  flex-wrap: wrap;
}

.header.compact {
  padding: 10px 12px;
  padding-top: calc(10px + env(safe-area-inset-top, 0px));
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.menu-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 8px;
  background: #f4f4f6;
  color: #303038;
  cursor: pointer;
  flex-shrink: 0;
}

.menu-btn:hover {
  background: #ebebef;
}

.crumb {
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.right {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #555;
  flex-shrink: 0;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.campus-label {
  font-size: 13px;
  color: #6b6b76;
}

.campus-select {
  width: 200px;
}

.role-tag {
  font-size: 12px;
  color: #8a8a96;
  padding: 2px 8px;
  background: #f4f4f6;
  border-radius: 999px;
  white-space: nowrap;
}

.user-name {
  max-width: 96px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.main {
  flex: 1 1 0;
  min-height: 0;
  overflow-x: hidden;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  padding: 20px 12px calc(48px + env(safe-area-inset-bottom, 0px)) 20px;
  box-sizing: border-box;
}

.main.compact {
  padding: 16px 8px calc(48px + env(safe-area-inset-bottom, 0px)) 16px;
}

.main.mobile {
  padding: 10px 8px calc(40px + env(safe-area-inset-bottom, 0px)) 10px;
}

.aside :deep(.el-sub-menu__title) {
  margin: 4px 10px;
  border-radius: 8px;
}

.aside :deep(.el-sub-menu .el-menu-item) {
  min-width: auto;
  padding-left: 44px !important;
}

.aside :deep(.el-menu-item),
.aside :deep(.el-sub-menu__title) {
  box-sizing: border-box;
}

@media (max-width: 1280px) {
  .campus-select {
    width: 160px;
  }
}

@media (max-width: 768px) {
  .header.mobile {
    flex-direction: column;
    align-items: stretch;
    gap: 6px;
    min-height: auto;
    padding: 8px 12px;
    padding-top: calc(8px + env(safe-area-inset-top, 0px));
  }

  .header.mobile.compact {
    padding: 8px 12px;
    padding-top: calc(8px + env(safe-area-inset-top, 0px));
  }

  .mobile-header-top {
    display: flex;
    align-items: center;
    gap: 8px;
    min-width: 0;
  }

  .mobile-header-top .crumb {
    flex-shrink: 0;
    font-size: 15px;
  }

  .header-hint {
    flex: 1;
    min-width: 0;
    margin: 0;
    font-size: 12px;
    line-height: 1.3;
    color: #8a8a96;
    text-align: right;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .mobile-header-bottom {
    display: flex;
    align-items: center;
    gap: 8px;
    min-width: 0;
  }

  .mobile-header-bottom .campus-select {
    flex: 1;
    min-width: 0;
    width: auto !important;
  }

  .mobile-header-bottom .mobile-logout {
    flex-shrink: 0;
    padding: 0 4px;
    font-size: 14px;
    margin-left: auto;
  }

  .menu-btn {
    width: 32px;
    height: 32px;
  }
}
</style>

<style>
.admin-nav-drawer.el-drawer {
  background: #16161c;
}
.admin-nav-drawer .el-drawer__body {
  padding: 0 !important;
  height: 100%;
  background: #16161c;
  overflow: hidden;
}
</style>
