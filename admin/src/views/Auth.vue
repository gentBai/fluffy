<template>
  <div class="page-container">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">认证鉴权</h1>
        <p class="page-subtitle">管理API Key与JWT配置</p>
      </div>
    </div>

    <div class="tabs-card">
      <el-tabs v-model="activeTab" class="custom-tabs">
        <el-tab-pane label="API Key管理" name="apikey">
          <template #label>
            <div class="tab-label">
              <el-icon><Key /></el-icon>
              <span>API Key管理</span>
            </div>
          </template>
          <div class="tab-content">
            <div class="content-header">
              <el-button type="primary" class="add-button" @click="showApiKeyDialog">
                <el-icon><Plus /></el-icon>
                创建API Key
              </el-button>
            </div>
            <el-table :data="apiKeys" border class="custom-table">
              <el-table-column prop="id" label="ID" width="80">
                <template #default="{ row }">
                  <span class="id-cell">{{ row.id }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="name" label="名称" width="150">
                <template #default="{ row }">
                  <div class="name-cell">
                    <el-icon class="name-icon"><Key /></el-icon>
                    {{ row.name }}
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="keyValue" label="Key值">
                <template #default="{ row }">
                  <code class="key-value">{{ row.keyValue }}</code>
                </template>
              </el-table-column>
              <el-table-column prop="rateLimitPerMinute" label="限流(次/分)" width="120" align="center">
                <template #default="{ row }">
                  <span class="number-cell">{{ row.rateLimitPerMinute }}</span>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.deleted ? 'danger' : 'success'" class="status-tag">
                    {{ row.deleted ? '禁用' : '启用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="expiresAt" label="过期时间" width="180">
                <template #default="{ row }">
                  <span class="time-cell">{{ row.expiresAt || '永不过期' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120" align="center">
                <template #default="{ row }">
                  <el-button size="small" class="action-btn delete-btn" @click="deleteApiKey(row)">
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
        <el-tab-pane label="JWT配置" name="jwt">
          <template #label>
            <div class="tab-label">
              <el-icon><Lock /></el-icon>
              <span>JWT配置</span>
            </div>
          </template>
          <div class="tab-content">
            <div class="config-card">
              <el-form :model="jwtConfig" label-width="120px" class="config-form">
                <el-form-item label="发行者">
                  <el-input v-model="jwtConfig.issuer" placeholder="JWT发行者" class="custom-input" />
                </el-form-item>
                <el-form-item label="算法">
                  <el-select v-model="jwtConfig.algorithm" style="width: 100%" class="custom-select">
                    <el-option label="HS256" value="HS256" />
                    <el-option label="HS384" value="HS384" />
                    <el-option label="HS512" value="HS512" />
                  </el-select>
                </el-form-item>
                <el-form-item label="Access Token过期">
                  <div class="input-with-unit">
                    <el-input-number v-model="jwtConfig.accessTokenTtlSec" :min="60" class="custom-number" />
                    <span class="unit-label">秒</span>
                  </div>
                </el-form-item>
                <el-form-item label="Refresh Token过期">
                  <div class="input-with-unit">
                    <el-input-number v-model="jwtConfig.refreshTokenTtlSec" :min="3600" class="custom-number" />
                    <span class="unit-label">秒</span>
                  </div>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="saveJwtConfig" class="save-btn">
                    <el-icon><Check /></el-icon>
                    保存配置
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="apiKeyDialogVisible" title="创建API Key" width="500px" class="custom-dialog">
      <el-form :model="apiKeyForm" label-width="100px" class="dialog-form">
        <el-form-item label="名称">
          <el-input v-model="apiKeyForm.name" placeholder="API Key名称" class="custom-input" />
        </el-form-item>
        <el-form-item label="限流(次/分)">
          <el-input-number v-model="apiKeyForm.rateLimitPerMinute" :min="1" class="custom-number" />
        </el-form-item>
        <el-form-item label="过期时间">
          <el-date-picker
            v-model="apiKeyForm.expiresAt"
            type="datetime"
            placeholder="不设置则永不过期"
            style="width: 100%"
            class="custom-date-picker"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="apiKeyDialogVisible = false" class="cancel-btn">取消</el-button>
          <el-button type="primary" @click="createApiKey" class="save-btn">
            <el-icon><Check /></el-icon>
            创建
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Key, Lock, Plus, Delete, Check } from '@element-plus/icons-vue'
import { authApi } from '../api'

const activeTab = ref('apikey')
const apiKeys = ref([])
const jwtConfig = ref({ issuer: '', algorithm: 'HS256', accessTokenTtlSec: 3600, refreshTokenTtlSec: 86400 })
const apiKeyDialogVisible = ref(false)
const apiKeyForm = ref({ name: '', rateLimitPerMinute: 1000, expiresAt: null })

const loadData = async () => {
  try {
    apiKeys.value = await authApi.apiKeys.list()
    const jwt = await authApi.jwt.get()
    if (jwt) jwtConfig.value = jwt
  } catch (error) {
    ElMessage.error('加载失败')
  }
}

const showApiKeyDialog = () => {
  apiKeyForm.value = { name: '', rateLimitPerMinute: 1000, expiresAt: null }
  apiKeyDialogVisible.value = true
}

const createApiKey = async () => {
  try {
    await authApi.apiKeys.create(apiKeyForm.value)
    ElMessage.success('创建成功')
    apiKeyDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('创建失败')
  }
}

const deleteApiKey = async (row) => {
  try {
    await authApi.apiKeys.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const saveJwtConfig = async () => {
  try {
    await authApi.jwt.update(jwtConfig.value)
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

onMounted(loadData)
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

.page-container {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.page-header {
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page-title {
  font-family: 'Fira Code', monospace;
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.page-subtitle {
  font-family: 'Fira Sans', sans-serif;
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}

.tabs-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 24px;
  box-shadow: var(--shadow-soft);
}

.custom-tabs :deep(.el-tabs__header) {
  margin-bottom: 24px;
}

.custom-tabs :deep(.el-tabs__nav-wrap::after) {
  background: var(--border-color);
  height: 1px;
}

.custom-tabs :deep(.el-tabs__item) {
  font-family: 'Fira Sans', sans-serif;
  font-weight: 500;
  color: var(--text-secondary);
  transition: color var(--transition-fast);
}

.custom-tabs :deep(.el-tabs__item:hover) {
  color: var(--primary);
}

.custom-tabs :deep(.el-tabs__item.is-active) {
  color: var(--primary);
  font-weight: 600;
}

.custom-tabs :deep(.el-tabs__active-bar) {
  background: var(--primary);
  height: 3px;
  border-radius: 3px;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 8px;
}

.content-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

.add-button {
  display: flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
  border: none;
  padding: 10px 18px;
  border-radius: var(--radius-md);
  font-family: 'Fira Sans', sans-serif;
  font-weight: 600;
  color: #FFFFFF;
  cursor: pointer;
  transition: all var(--transition-normal);
}

.add-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(124, 58, 237, 0.3);
}

.custom-table {
  font-family: 'Fira Sans', sans-serif;
}

.custom-table :deep(.el-table__header th) {
  background: #F9FAFB;
  color: var(--text-secondary);
  font-weight: 600;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 14px 16px;
  border-bottom: 1px solid var(--border-color);
}

.custom-table :deep(.el-table__body td) {
  padding: 16px;
  color: var(--text-primary);
  border-bottom: 1px solid var(--border-color);
}

.custom-table :deep(.el-table__row) {
  transition: background var(--transition-fast);
}

.custom-table :deep(.el-table__row:hover > td) {
  background: #FAF5FF;
}

.id-cell {
  font-family: 'Fira Code', monospace;
  font-weight: 600;
  color: var(--text-secondary);
}

.name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.name-icon {
  color: var(--primary);
}

.key-value {
  font-family: 'Fira Code', monospace;
  font-size: 12px;
  background: #F3F4F6;
  padding: 4px 8px;
  border-radius: 4px;
  color: var(--text-primary);
}

.number-cell {
  font-family: 'Fira Code', monospace;
  font-weight: 600;
}

.time-cell {
  font-family: 'Fira Code', monospace;
  font-size: 13px;
  color: var(--text-secondary);
}

.status-tag {
  border-radius: 6px;
  font-weight: 500;
  padding: 4px 10px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  border-radius: 8px;
  font-weight: 500;
  transition: all var(--transition-fast);
  cursor: pointer;
}

.delete-btn {
  color: #EF4444;
}

.delete-btn:hover {
  background: rgba(239, 68, 68, 0.1);
}

.config-card {
  background: #FAFAFA;
  border-radius: var(--radius-md);
  padding: 24px;
  max-width: 600px;
}

.config-form :deep(.el-form-item__label) {
  font-family: 'Fira Sans', sans-serif;
  font-weight: 500;
  color: var(--text-primary);
}

.input-with-unit {
  display: flex;
  align-items: center;
  gap: 12px;
}

.unit-label {
  font-family: 'Fira Sans', sans-serif;
  color: var(--text-secondary);
  font-size: 14px;
}

.save-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
  border: none;
  padding: 10px 20px;
  border-radius: var(--radius-md);
  font-family: 'Fira Sans', sans-serif;
  font-weight: 600;
  color: #FFFFFF;
  cursor: pointer;
  transition: all var(--transition-normal);
}

.save-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(124, 58, 237, 0.3);
}

/* Dialog styles */
.custom-dialog :deep(.el-dialog) {
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.custom-dialog :deep(.el-dialog__header) {
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-color);
  margin: 0;
}

.custom-dialog :deep(.el-dialog__title) {
  font-family: 'Fira Sans', sans-serif;
  font-weight: 600;
  color: var(--text-primary);
}

.custom-dialog :deep(.el-dialog__body) {
  padding: 24px;
}

.custom-dialog :deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid var(--border-color);
}

.dialog-form :deep(.el-form-item__label) {
  font-family: 'Fira Sans', sans-serif;
  font-weight: 500;
  color: var(--text-primary);
}

.custom-input :deep(.el-input__wrapper) {
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

.custom-select :deep(.el-input__wrapper) {
  border-radius: var(--radius-md);
}

.custom-number :deep(.el-input__wrapper) {
  border-radius: var(--radius-md);
}

.custom-date-picker :deep(.el-input__wrapper) {
  border-radius: var(--radius-md);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.cancel-btn {
  border-radius: var(--radius-md);
  font-family: 'Fira Sans', sans-serif;
  cursor: pointer;
}
</style>
