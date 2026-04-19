<template>
  <el-container style="height: 100vh">
    <el-aside width="200px" style="background: #304156">
      <div style="height: 60px; line-height: 60px; text-align: center; color: #fff; font-size: 18px; font-weight: bold">
        Fluffy Gateway
      </div>
      <el-menu
        :default-active="$route.path"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        router
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/routes">
          <el-icon><Guide /></el-icon>
          <span>路由管理</span>
        </el-menu-item>
        <el-menu-item index="/services">
          <el-icon><Connection /></el-icon>
          <span>服务管理</span>
        </el-menu-item>
        <el-menu-item index="/auth">
          <el-icon><Key /></el-icon>
          <span>认证鉴权</span>
        </el-menu-item>
        <el-menu-item index="/rate-limit">
          <el-icon><Timer /></el-icon>
          <span>限流配置</span>
        </el-menu-item>
        <el-menu-item index="/blacklist">
          <el-icon><CircleClose /></el-icon>
          <span>黑白名单</span>
        </el-menu-item>
        <el-menu-item index="/logs">
          <el-icon><Document /></el-icon>
          <span>日志监控</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="display: flex; justify-content: flex-end; align-items: center; border-bottom: 1px solid #e6e6e6">
        <el-dropdown @command="handleCommand">
          <span class="el-dropdown-link">
            管理员<i class="el-icon-arrow-down el-icon--right"></i>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { DataAnalysis, Guide, Connection, Key, Timer, CircleClose, Document } from '@element-plus/icons-vue'

const router = useRouter()

const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.removeItem('token')
    router.push('/login')
  }
}
</script>

<style scoped>
.el-dropdown-link {
  cursor: pointer;
  color: #409eff;
}
</style>
