<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <svg class="login-logo" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M12 2L2 7l10 5 10-5-10-5z"/>
          <path d="M2 17l10 5 10-5"/>
          <path d="M2 12l10 5 10-5"/>
        </svg>
        <h1 class="login-title">Fluffy Gateway</h1>
        <p class="login-subtitle">API Gateway Management Console</p>
      </div>

      <el-form :model="form" class="login-form" @submit.prevent="handleLogin">
        <el-form-item class="form-item">
          <label class="form-label">用户名</label>
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            size="large"
            class="custom-input"
          >
            <template #prefix>
              <el-icon class="input-icon"><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item class="form-item">
          <label class="form-label">密码</label>
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            class="custom-input"
            show-password
          >
            <template #prefix>
              <el-icon class="input-icon"><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item class="form-item">
          <el-button
            type="primary"
            size="large"
            class="login-button"
            :loading="loading"
            @click="handleLogin"
          >
            <span v-if="!loading">登 录</span>
            <span v-else>登录中...</span>
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <p class="footer-text">Vert.x 5.0 Gateway Admin</p>
      </div>
    </div>

    <div class="background-decoration">
      <div class="decoration-circle circle-1"></div>
      <div class="decoration-circle circle-2"></div>
      <div class="decoration-circle circle-3"></div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { authApi } from '../api'

const router = useRouter()
const loading = ref(false)
const form = reactive({
  username: '',
  password: ''
})

const handleLogin = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  loading.value = true
  try {
    const res = await authApi.login(form)
    localStorage.setItem('token', res.token)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (error) {
    ElMessage.error('登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
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

.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #FAF5FF 0%, #EDE9FE 50%, #DDD6FE 100%);
  position: relative;
  overflow: hidden;
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 420px;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 48px 40px;
  box-shadow: var(--shadow-hover);
  position: relative;
  z-index: 10;
  border: 1px solid rgba(124, 58, 237, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-logo {
  width: 56px;
  height: 56px;
  color: var(--primary);
  margin-bottom: 16px;
}

.login-title {
  font-family: 'Fira Code', monospace;
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 8px 0;
  letter-spacing: -0.5px;
}

.login-subtitle {
  font-family: 'Fira Sans', sans-serif;
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-item {
  margin-bottom: 20px;
}

.form-item :deep(.el-form-item__content) {
  display: block;
}

.form-label {
  font-family: 'Fira Sans', sans-serif;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 8px;
  display: block;
}

.custom-input {
  width: 100%;
}

.custom-input :deep(.el-input__wrapper) {
  padding: 4px 16px;
  border-radius: var(--radius-md);
  box-shadow: 0 0 0 1px var(--border-color);
  transition: all var(--transition-fast);
}

.custom-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--primary-light);
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px var(--primary);
}

.custom-input :deep(.el-input__inner) {
  height: 44px;
  font-family: 'Fira Sans', sans-serif;
}

.input-icon {
  color: var(--primary-light);
  font-size: 16px;
}

.login-button {
  width: 100%;
  height: 48px;
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
  border: none;
  border-radius: var(--radius-md);
  font-family: 'Fira Sans', sans-serif;
  font-size: 16px;
  font-weight: 600;
  color: #FFFFFF;
  cursor: pointer;
  transition: all var(--transition-normal);
  margin-top: 8px;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(124, 58, 237, 0.3);
}

.login-button:active {
  transform: translateY(0);
}

.login-button :deep(.el-icon) {
  margin-right: 8px;
}

.login-footer {
  text-align: center;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--border-color);
}

.footer-text {
  font-family: 'Fira Sans', sans-serif;
  font-size: 12px;
  color: var(--text-secondary);
  margin: 0;
}

/* Background decoration */
.background-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.5;
}

.circle-1 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(124, 58, 237, 0.1) 0%, transparent 70%);
  top: -100px;
  right: -100px;
}

.circle-2 {
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(167, 139, 250, 0.1) 0%, transparent 70%);
  bottom: -200px;
  left: -200px;
}

.circle-3 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(249, 115, 22, 0.05) 0%, transparent 70%);
  top: 50%;
  left: 10%;
}
</style>
