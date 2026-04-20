<template>
  <div class="page-container">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">日志监控</h1>
        <p class="page-subtitle">查询与分析API访问日志</p>
      </div>
    </div>

    <div class="filter-card">
      <el-form :inline="true" class="filter-form">
        <el-form-item label="路径" class="filter-item">
          <el-input v-model="query.path" placeholder="/api/*" style="width: 200px" class="custom-input" />
        </el-form-item>
        <el-form-item label="方法" class="filter-item">
          <el-select v-model="query.method" style="width: 120px" class="custom-select">
            <el-option label="全部" value="" />
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态码" class="filter-item">
          <el-input v-model="query.status" placeholder="200" style="width: 100px" class="custom-input" />
        </el-form-item>
        <el-form-item label="时间范围" class="filter-item filter-item-wide">
          <el-date-picker
            v-model="query.startTime"
            type="datetime"
            placeholder="开始时间"
            class="custom-date-picker"
          />
          <span class="date-separator">-</span>
          <el-date-picker
            v-model="query.endTime"
            type="datetime"
            placeholder="结束时间"
            class="custom-date-picker"
          />
        </el-form-item>
        <el-form-item class="filter-actions">
          <el-button type="primary" class="action-btn query-btn" @click="handleQuery">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button class="action-btn reset-btn" @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
          <el-button type="success" class="action-btn export-btn" @click="handleExport">
            <el-icon><Download /></el-icon>
            导出
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-card">
      <el-table :data="logs" border class="custom-table" height="500">
        <el-table-column prop="traceId" label="追踪ID" width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <code class="trace-id">{{ row.traceId }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="requestMethod" label="方法" width="80" align="center">
          <template #default="{ row }">
            <el-tag class="method-tag" :class="'method-' + row.requestMethod.toLowerCase()">
              {{ row.requestMethod }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requestPath" label="路径" width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="path-cell">{{ row.requestPath }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="responseStatus" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag
              :type="row.responseStatus >= 400 ? 'danger' : row.responseStatus >= 200 ? 'success' : 'info'"
              class="status-tag"
            >
              {{ row.responseStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="responseTimeMs" label="响应时间(ms)" width="120" align="center">
          <template #default="{ row }">
            <span class="time-cell" :class="{ 'time-slow': row.responseTimeMs > 1000 }">
              {{ row.responseTimeMs }}ms
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="clientIp" label="客户端IP" width="130">
          <template #default="{ row }">
            <span class="ip-cell">{{ row.clientIp }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="routeId" label="路由ID" width="80" align="center">
          <template #default="{ row }">
            <span class="route-id">{{ row.routeId || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="errorMessage" label="错误信息" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.errorMessage" class="error-message">
              <el-icon class="error-icon"><WarningFilled /></el-icon>
              {{ row.errorMessage }}
            </span>
            <span v-else class="no-error">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间" width="180">
          <template #default="{ row }">
            <span class="time-value">{{ row.createdAt }}</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[20, 50, 100, 200]"
          layout="total, sizes, prev, pager, next, jumper"
          class="custom-pagination"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Download, WarningFilled } from '@element-plus/icons-vue'
import { logsApi } from '../api'

const logs = ref([])
const query = ref({ path: '', method: '', status: '', startTime: null, endTime: null })
const pagination = ref({ page: 1, size: 20, total: 0 })

const loadData = async () => {
  try {
    const params = {
      page: pagination.value.page,
      size: pagination.value.size,
      ...query.value
    }
    const res = await logsApi.list(params)
    logs.value = res.data || []
    pagination.value.total = res.total || 0
  } catch (error) {
    ElMessage.error('加载失败')
  }
}

const handleQuery = () => {
  pagination.value.page = 1
  loadData()
}

const handleReset = () => {
  query.value = { path: '', method: '', status: '', startTime: null, endTime: null }
  handleQuery()
}

const handleExport = async () => {
  try {
    const res = await logsApi.export(query.value)
    const blob = new Blob([res], { type: 'application/vnd.ms-excel' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `access_logs_${Date.now()}.xlsx`
    a.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
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
  --success: #10B981;
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

.filter-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 20px 24px;
  box-shadow: var(--shadow-soft);
  margin-bottom: 20px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  gap: 16px;
}

.filter-item {
  margin-bottom: 0;
}

.filter-item-wide {
  flex-basis: auto;
}

.filter-actions {
  margin-bottom: 0;
  margin-left: auto;
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

.custom-date-picker :deep(.el-input__wrapper) {
  border-radius: var(--radius-md);
}

.date-separator {
  margin: 0 10px;
  color: var(--text-secondary);
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  border-radius: var(--radius-md);
  font-family: 'Fira Sans', sans-serif;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-normal);
}

.query-btn {
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
  border: none;
  color: #FFFFFF;
}

.query-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(124, 58, 237, 0.3);
}

.reset-btn {
  background: #F3F4F6;
  border: none;
  color: var(--text-primary);
}

.reset-btn:hover {
  background: #E5E7EB;
}

.export-btn {
  background: linear-gradient(135deg, var(--success) 0%, #059669 100%);
  border: none;
  color: #FFFFFF;
}

.export-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

.table-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 24px;
  box-shadow: var(--shadow-soft);
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
  padding: 14px 16px;
  color: var(--text-primary);
  border-bottom: 1px solid var(--border-color);
}

.custom-table :deep(.el-table__row) {
  transition: background var(--transition-fast);
}

.custom-table :deep(.el-table__row:hover > td) {
  background: #FAF5FF;
}

.trace-id {
  font-family: 'Fira Code', monospace;
  font-size: 12px;
  background: #F3F4F6;
  padding: 4px 8px;
  border-radius: 4px;
  color: var(--text-secondary);
}

.method-tag {
  border-radius: 6px;
  font-family: 'Fira Code', monospace;
  font-weight: 600;
  font-size: 11px;
  padding: 4px 8px;
}

.method-get { background: #ECFDF5; color: #059669; }
.method-post { background: #EFF6FF; color: #2563EB; }
.method-put { background: #FFFBEB; color: #D97706; }
.method-delete { background: #FEF2F2; color: #DC2626; }

.path-cell {
  font-family: 'Fira Code', monospace;
  font-size: 13px;
  color: var(--primary-light);
}

.status-tag {
  border-radius: 6px;
  font-weight: 600;
  padding: 4px 10px;
}

.time-cell {
  font-family: 'Fira Code', monospace;
  font-weight: 600;
}

.time-slow {
  color: #EF4444;
}

.ip-cell {
  font-family: 'Fira Code', monospace;
  font-size: 13px;
  color: var(--text-secondary);
}

.route-id {
  font-family: 'Fira Code', monospace;
  font-weight: 600;
  color: var(--primary);
}

.error-message {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #EF4444;
  font-size: 13px;
}

.error-icon {
  flex-shrink: 0;
}

.no-error {
  color: var(--text-secondary);
}

.time-value {
  font-family: 'Fira Code', monospace;
  font-size: 13px;
  color: var(--text-secondary);
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.custom-pagination {
  font-family: 'Fira Sans', sans-serif;
}

.custom-pagination :deep(.el-pager li) {
  border-radius: var(--radius-md);
  font-weight: 500;
}

.custom-pagination :deep(.el-pager li.is-active) {
  background: var(--primary);
  color: #FFFFFF;
}

.custom-pagination :deep(.el-pager li:hover) {
  color: var(--primary);
}
</style>
