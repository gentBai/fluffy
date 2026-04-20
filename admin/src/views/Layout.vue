<template>
  <el-container style="height: 100vh" class="layout-container">
    <el-aside width="240px" class="sidebar">
      <div class="sidebar-header">
        <svg class="logo-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M12 2L2 7l10 5 10-5-10-5z"/>
          <path d="M2 17l10 5 10-5"/>
          <path d="M2 12l10 5 10-5"/>
        </svg>
        <span class="logo-text">Fluffy Gateway</span>
      </div>
      <el-menu
        :default-active="$route.path"
        class="sidebar-menu"
        router
      >
        <el-menu-item index="/dashboard" class="menu-item">
          <el-icon><DataAnalysis /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/routes" class="menu-item">
          <el-icon><Guide /></el-icon>
          <span>路由管理</span>
        </el-menu-item>
        <el-menu-item index="/services" class="menu-item">
          <el-icon><Connection /></el-icon>
          <span>服务管理</span>
        </el-menu-item>
        <el-menu-item index="/auth" class="menu-item">
          <el-icon><Key /></el-icon>
          <span>认证鉴权</span>
        </el-menu-item>
        <el-menu-item index="/rate-limit" class="menu-item">
          <el-icon><Timer /></el-icon>
          <span>限流配置</span>
        </el-menu-item>
        <el-menu-item index="/blacklist" class="menu-item">
          <el-icon><CircleClose /></el-icon>
          <span>黑白名单</span>
        </el-menu-item>
        <el-menu-item index="/logs" class="menu-item">
          <el-icon><Document /></el-icon>
          <span>日志监控</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container class="main-container">
      <el-header class="header">
        <div class="header-right">
          <el-dropdown @command="handleCommand" trigger="click">
            <span class="user-dropdown">
              <svg class="user-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="8" r="4"/>
                <path d="M4 20c0-4 4-6 8-6s8 2 8 6"/>
              </svg>
              <span class="user-name">管理员</span>
              <el-icon class="arrow-icon"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout" class="logout-item">
                  <el-icon><SwitchButton /></el-icon>
                  <span>退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { DataAnalysis, Guide, Connection, Key, Timer, CircleClose, Document, ArrowDown, SwitchButton } from '@element-plus/icons-vue'

const router = useRouter()

const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.removeItem('token')
    router.push('/login')
  }
}
</script>

<style scoped>
/* Design System Variables */
:root {
  --primary: #7C3AED;
  --primary-light: #A78BFA;
  --primary-dark: #4C1D95;
  --accent: #F97316;
  --bg-main: #FAF5FF;
  --bg-card: #FFFFFF;
  --text-primary: #4C1D95;
  --text-secondary: #6B7280;
  --border-color: #E5E7EB;
  --shadow-soft: 0 4px 20px rgba(124, 58, 237, 0.08);
  --shadow-hover: 0 8px 30px rgba(124, 58, 237, 0.15);
  --radius-md: 12px;
  --radius-lg: 16px;
  --transition-fast: 150ms ease;
  --transition-normal: 250ms ease;
}

.layout-container {
  background: var(--bg-main);
}

.sidebar {
  background: linear-gradient(180deg, #7C3AED 0%, #5B21B6 100%);
  box-shadow: 4px 0 20px rgba(124, 58, 237, 0.2);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.sidebar-header {
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 0 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-icon {
  width: 32px;
  height: 32px;
  color: #FFFFFF;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

.logo-text {
  font-family: 'Fira Code', monospace;
  font-size: 18px;
  font-weight: 600;
  color: #FFFFFF;
  letter-spacing: -0.5px;
}

.sidebar-menu {
  background: transparent;
  border-right: none;
  padding: 16px 12px;
  flex: 1;
}

.sidebar-menu :deep(.el-menu-item) {
  height: 48px;
  line-height: 48px;
  border-radius: var(--radius-md);
  margin-bottom: 4px;
  color: rgba(255, 255, 255, 0.8);
  transition: all var(--transition-fast);
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.15);
  color: #FFFFFF;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: rgba(255, 255, 255, 0.2);
  color: #FFFFFF;
  font-weight: 600;
}

.sidebar-menu :deep(.el-menu-item .el-icon) {
  margin-right: 12px;
  font-size: 18px;
}

.menu-item {
  cursor: pointer;
}

.main-container {
  display: flex;
  flex-direction: column;
  background: var(--bg-main);
}

.header {
  height: 72px;
  background: var(--bg-card);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 32px;
  box-shadow: var(--shadow-soft);
}

.header-right {
  display: flex;
  align-items: center;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 16px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
  color: var(--text-primary);
}

.user-dropdown:hover {
  background: rgba(124, 58, 237, 0.08);
}

.user-icon {
  width: 20px;
  height: 20px;
  color: var(--primary);
}

.user-name {
  font-family: 'Fira Sans', sans-serif;
  font-weight: 500;
  font-size: 14px;
}

.arrow-icon {
  font-size: 12px;
  color: var(--text-secondary);
  transition: transform var(--transition-fast);
}

.user-dropdown:hover .arrow-icon {
  transform: rotate(180deg);
}

.logout-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #EF4444;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.logout-item:hover {
  background: #FEF2F2;
}

.content {
  padding: 24px 32px;
  overflow-y: auto;
}

/* Element Plus overrides */
:deep(.el-dropdown-menu__item) {
  padding: 12px 20px;
  border-radius: var(--radius-md);
  margin: 4px 8px;
}

:deep(.el-dropdown-menu) {
  padding: 8px;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-hover);
  border: 1px solid var(--border-color);
}
</style>
