<template>
  <div>
    <h2>仪表盘</h2>
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="6">
        <el-card>
          <div style="text-align: center">
            <div style="font-size: 32px; font-weight: bold; color: #409eff">{{ stats.totalRequests }}</div>
            <div>总请求数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div style="text-align: center">
            <div style="font-size: 32px; font-weight: bold; color: #67c23a">{{ stats.activeRoutes }}</div>
            <div>活跃路由</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div style="text-align: center">
            <div style="font-size: 32px; font-weight: bold; color: #e6a23c">{{ stats.activeServices }}</div>
            <div>活跃服务</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div style="text-align: center">
            <div style="font-size: 32px; font-weight: bold; color: #f56c6c">{{ stats.errorRate }}%</div>
            <div>错误率</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card title="服务健康状态">
          <el-table :data="serviceHealth" style="width: 100%">
            <el-table-column prop="name" label="服务名" />
            <el-table-column prop="status" label="状态">
              <template #default="{ row }">
                <el-tag :type="row.status === 'HEALTHY' ? 'success' : 'danger'">
                  {{ row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="instanceCount" label="实例数" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card title="最近告警">
          <el-table :data="alerts" style="width: 100%">
            <el-table-column prop="time" label="时间" width="160" />
            <el-table-column prop="message" label="告警信息" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

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
