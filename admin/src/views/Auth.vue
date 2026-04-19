<template>
  <div>
    <h2>认证鉴权</h2>
    <el-tabs style="margin-top: 20px">
      <el-tab-pane label="API Key管理">
        <div style="display: flex; justify-content: flex-end; margin-bottom: 16px">
          <el-button type="primary" @click="showApiKeyDialog">创建API Key</el-button>
        </div>
        <el-table :data="apiKeys" border>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="name" label="名称" width="150" />
          <el-table-column prop="keyValue" label="Key值" />
          <el-table-column prop="rateLimitPerMinute" label="限流(次/分)" width="120" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.deleted ? 'danger' : 'success'">{{ row.deleted ? '禁用' : '启用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="expiresAt" label="过期时间" width="180" />
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button size="small" type="danger" @click="deleteApiKey(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="JWT配置">
        <el-form :model="jwtConfig" label-width="120px" style="max-width: 600px">
          <el-form-item label="发行者">
            <el-input v-model="jwtConfig.issuer" />
          </el-form-item>
          <el-form-item label="算法">
            <el-select v-model="jwtConfig.algorithm" style="width: 100%">
              <el-option label="HS256" value="HS256" />
              <el-option label="HS384" value="HS384" />
              <el-option label="HS512" value="HS512" />
            </el-select>
          </el-form-item>
          <el-form-item label="Access Token过期">
            <el-input-number v-model="jwtConfig.accessTokenTtlSec" :min="60" />
            秒
          </el-form-item>
          <el-form-item label="Refresh Token过期">
            <el-input-number v-model="jwtConfig.refreshTokenTtlSec" :min="3600" />
            秒
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveJwtConfig">保存</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="apiKeyDialogVisible" title="创建API Key" width="500px">
      <el-form :model="apiKeyForm" label-width="100px">
        <el-form-item label="名称">
          <el-input v-model="apiKeyForm.name" />
        </el-form-item>
        <el-form-item label="限流(次/分)">
          <el-input-number v-model="apiKeyForm.rateLimitPerMinute" :min="1" />
        </el-form-item>
        <el-form-item label="过期时间">
          <el-date-picker v-model="apiKeyForm.expiresAt" type="datetime" placeholder="不设置则永不过期" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="apiKeyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createApiKey">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { authApi } from '../api'

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
