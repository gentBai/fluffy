<template>
  <div class="dashboard">
    <div class="page-header">
      <h1 class="page-title">仪表盘</h1>
      <p class="page-subtitle">实时监控网关运行状态</p>
    </div>

    <el-row :gutter="24" class="stats-row">
      <el-col :span="6">
        <div class="stat-card stat-card-primary">
          <div class="stat-icon-wrapper">
            <el-icon class="stat-icon"><DataLine /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalRequests.toLocaleString() }}</div>
            <div class="stat-label">总请求数</div>
          </div>
          <div class="stat-trend trend-up">
            <el-icon><Top /></el-icon>
            <span>+12.5%</span>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-card-success">
          <div class="stat-icon-wrapper">
            <el-icon class="stat-icon"><Guide /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.activeRoutes }}</div>
            <div class="stat-label">活跃路由</div>
          </div>
          <div class="stat-trend trend-stable">
            <span>Active</span>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-card-warning">
          <div class="stat-icon-wrapper">
            <el-icon class="stat-icon"><Connection /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.activeServices }}</div>
            <div class="stat-label">活跃服务</div>
          </div>
          <div class="stat-trend trend-stable">
            <span>Online</span>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-card-danger">
          <div class="stat-icon-wrapper">
            <el-icon class="stat-icon"><Warning /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.errorRate }}%</div>
            <div class="stat-label">错误率</div>
          </div>
          <div class="stat-trend trend-down">
            <el-icon><Bottom /></el-icon>
            <span>-0.3%</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="24" class="tables-row">
      <el-col :span="12">
        <div class="table-card">
          <div class="table-card-header">
            <h3 class="table-card-title">
              <el-icon class="title-icon"><Monitor /></el-icon>
              服务健康状态
            </h3>
          </div>
          <el-table :data="serviceHealth" class="custom-table">
            <el-table-column prop="name" label="服务名">
              <template #default="{ row }">
                <div class="service-name">
                  <span class="service-dot" :class="row.status === 'HEALTHY' ? 'dot-success' : 'dot-danger'"></span>
                  {{ row.name }}
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="row.status === 'HEALTHY' ? 'success' : 'danger'" class="custom-tag">
                  {{ row.status === 'HEALTHY' ? '健康' : '异常' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="instanceCount" label="实例数" width="100" align="center">
              <template #default="{ row }">
                <span class="instance-count">{{ row.instanceCount }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="table-card">
          <div class="table-card-header">
            <h3 class="table-card-title">
              <el-icon class="title-icon"><Bell /></el-icon>
              最近告警
            </h3>
            <el-tag type="warning" class="alert-count">{{ alerts.length }} 条</el-tag>
          </div>
          <el-table :data="alerts" class="custom-table" :show-header="alerts.length > 0">
            <el-table-column prop="time" label="时间" width="160">
              <template #default="{ row }">
                <span class="time-value">{{ row.time }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="message" label="告警信息">
              <template #default="{ row }">
                <div class="alert-message">
                  <el-icon class="alert-icon"><WarningFilled /></el-icon>
                  {{ row.message }}
                </div>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="alerts.length === 0" class="empty-state">
            <el-icon class="empty-icon"><SuccessFilled /></el-icon>
            <p>暂无告警信息</p>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import {
  DataLine, Guide, Connection, Warning, Top, Bottom,
  Monitor, Bell, WarningFilled, SuccessFilled
} from '@element-plus/icons-vue'

const stats = ref({
  totalRequests: 0,
  activeRoutes: 0,
  activeServices: 0,
  errorRate: 0
})

const serviceHealth = ref([])
const alerts = ref([])

onMounted(() => {
  stats.value = {
    totalRequests: 1234567,
    activeRoutes: 15,
    activeServices: 8,
    errorRate: 0.5
  }
  serviceHealth.value = [
    { name: 'user-service', status: 'HEALTHY', instanceCount: 3 },
    { name: 'order-service', status: 'HEALTHY', instanceCount: 2 },
    { name: 'product-service', status: 'UNHEALTHY', instanceCount: 1 }
  ]
  alerts.value = [
    { time: '2026-04-19 20:00:00', message: 'product-service 响应超时' }
  ]
})
</script>

<style scoped>
:root {
  --primary: #7C3AED;
  --primary-light: #A78BFA;
  --primary-dark: #4C1D95;
  --accent: #F97316;
  --success: #10B981;
  --warning: #F59E0B;
  --danger: #EF4444;
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

.dashboard {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.page-header {
  margin-bottom: 32px;
}

.page-title {
  font-family: 'Fira Code', monospace;
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-family: 'Fira Sans', sans-serif;
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}

.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 24px;
  box-shadow: var(--shadow-soft);
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: relative;
  overflow: hidden;
  transition: all var(--transition-normal);
  cursor: default;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-hover);
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
}

.stat-card-primary::before { background: linear-gradient(90deg, var(--primary), var(--primary-light)); }
.stat-card-success::before { background: linear-gradient(90deg, var(--success), #34D399); }
.stat-card-warning::before { background: linear-gradient(90deg, var(--warning), #FBBF24); }
.stat-card-danger::before { background: linear-gradient(90deg, var(--danger), #F87171); }

.stat-icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-card-primary .stat-icon-wrapper { background: rgba(124, 58, 237, 0.1); }
.stat-card-success .stat-icon-wrapper { background: rgba(16, 185, 129, 0.1); }
.stat-card-warning .stat-icon-wrapper { background: rgba(245, 158, 11, 0.1); }
.stat-card-danger .stat-icon-wrapper { background: rgba(239, 68, 68, 0.1); }

.stat-icon {
  font-size: 24px;
}

.stat-card-primary .stat-icon { color: var(--primary); }
.stat-card-success .stat-icon { color: var(--success); }
.stat-card-warning .stat-icon { color: var(--warning); }
.stat-card-danger .stat-icon { color: var(--danger); }

.stat-content {
  flex: 1;
}

.stat-value {
  font-family: 'Fira Code', monospace;
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-label {
  font-family: 'Fira Sans', sans-serif;
  font-size: 14px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-family: 'Fira Sans', sans-serif;
  font-size: 12px;
  font-weight: 500;
  padding: 4px 10px;
  border-radius: 20px;
}

.trend-up { background: rgba(16, 185, 129, 0.1); color: var(--success); }
.trend-down { background: rgba(239, 68, 68, 0.1); color: var(--danger); }
.trend-stable { background: rgba(124, 58, 237, 0.1); color: var(--primary); }

.tables-row {
  margin-bottom: 24px;
}

.table-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 24px;
  box-shadow: var(--shadow-soft);
  height: 100%;
}

.table-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.table-card-title {
  font-family: 'Fira Sans', sans-serif;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon {
  color: var(--primary);
}

.alert-count {
  cursor: pointer;
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
  padding: 12px 16px;
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

.service-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.service-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.dot-success { background: var(--success); }
.dot-danger { background: var(--danger); }

.custom-tag {
  border-radius: 6px;
  font-weight: 500;
}

.instance-count {
  font-family: 'Fira Code', monospace;
  font-weight: 600;
  color: var(--primary);
}

.time-value {
  font-family: 'Fira Code', monospace;
  font-size: 13px;
  color: var(--text-secondary);
}

.alert-message {
  display: flex;
  align-items: center;
  gap: 8px;
}

.alert-icon {
  color: var(--warning);
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: var(--text-secondary);
}

.empty-icon {
  font-size: 48px;
  color: var(--success);
  margin-bottom: 12px;
}

.empty-state p {
  margin: 0;
  font-family: 'Fira Sans', sans-serif;
}
</style>
