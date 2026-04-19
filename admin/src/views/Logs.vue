<template>
  <div>
    <h2>日志监控</h2>
    <el-form :inline="true" style="margin-top: 20px">
      <el-form-item label="路径">
        <el-input v-model="query.path" placeholder="/api/*" style="width: 200px" />
      </el-form-item>
      <el-form-item label="方法">
        <el-select v-model="query.method" style="width: 120px">
          <el-option label="全部" value="" />
          <el-option label="GET" value="GET" />
          <el-option label="POST" value="POST" />
          <el-option label="PUT" value="PUT" />
          <el-option label="DELETE" value="DELETE" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态码">
        <el-input v-model="query.status" placeholder="200" style="width: 100px" />
      </el-form-item>
      <el-form-item label="时间范围">
        <el-date-picker v-model="query.startTime" type="datetime" placeholder="开始时间" />
        <span style="margin: 0 10px">-</span>
        <el-date-picker v-model="query.endTime" type="datetime" placeholder="结束时间" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button type="success" @click="handleExport">导出</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="logs" border style="margin-top: 16px" height="500">
      <el-table-column prop="traceId" label="追踪ID" width="150" show-overflow-tooltip />
      <el-table-column prop="requestMethod" label="方法" width="80" />
      <el-table-column prop="requestPath" label="路径" width="200" show-overflow-tooltip />
      <el-table-column prop="responseStatus" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.responseStatus >= 400 ? 'danger' : row.responseStatus >= 200 ? 'success' : 'info'">
            {{ row.responseStatus }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="responseTimeMs" label="响应时间(ms)" width="120" />
      <el-table-column prop="clientIp" label="客户端IP" width="130" />
      <el-table-column prop="routeId" label="路由ID" width="80" />
      <el-table-column prop="errorMessage" label="错误信息" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="时间" width="180" />
    </el-table>

    <el-pagination
      v-model:current-page="pagination.page"
      v-model:page-size="pagination.size"
      :total="pagination.total"
      :page-sizes="[20, 50, 100, 200]"
      layout="total, sizes, prev, pager, next, jumper"
      style="margin-top: 16px; text-align: right"
      @size-change="loadData"
      @current-change="loadData"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
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
