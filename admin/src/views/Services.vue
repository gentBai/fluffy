<template>
  <div class="page-container">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">服务管理</h1>
        <p class="page-subtitle">管理系统微服务节点</p>
      </div>
      <el-button type="primary" class="add-button" @click="showDialog('create')">
        <el-icon><Plus /></el-icon>
        创建服务
      </el-button>
    </div>

    <div class="table-card">
      <el-table :data="services" border class="custom-table" row-hover-class="table-row-hover">
        <el-table-column prop="id" label="ID" width="80">
          <template #default="{ row }">
            <span class="id-cell">{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" width="150">
          <template #default="{ row }">
            <div class="name-cell">
              <el-icon class="name-icon"><Connection /></el-icon>
              {{ row.name }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="baseUrl" label="基础URL">
          <template #default="{ row }">
            <span class="url-cell">{{ row.baseUrl }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="healthCheckUrl" label="健康检查URL" width="200">
          <template #default="{ row }">
            <span class="path-cell">{{ row.healthCheckUrl }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="timeoutMs" label="超时(ms)" width="100" align="center">
          <template #default="{ row }">
            <span class="number-cell">{{ row.timeoutMs }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.deleted ? 'danger' : 'success'" class="status-tag">
              {{ row.deleted ? '禁用' : '启用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="200" align="center">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button size="small" class="action-btn" @click="showInstances(row)">
                <el-icon><Grid /></el-icon>
                实例
              </el-button>
              <el-button size="small" class="action-btn edit-btn" @click="showDialog('edit', row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button size="small" class="action-btn delete-btn" @click="handleDelete(row)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" class="custom-dialog">
      <el-form :model="form" label-width="100px" class="dialog-form">
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="服务名称" class="custom-input" />
        </el-form-item>
        <el-form-item label="基础URL">
          <el-input v-model="form.baseUrl" placeholder="http://localhost:8080" class="custom-input" />
        </el-form-item>
        <el-form-item label="健康检查URL">
          <el-input v-model="form.healthCheckUrl" placeholder="/health" class="custom-input" />
        </el-form-item>
        <el-form-item label="检查间隔(秒)">
          <el-input-number v-model="form.healthCheckInterval" :min="10" class="custom-number" />
        </el-form-item>
        <el-form-item label="超时(ms)">
          <el-input-number v-model="form.timeoutMs" :min="1000" :step="1000" class="custom-number" />
        </el-form-item>
        <el-form-item label="最大连接数">
          <el-input-number v-model="form.maxConnections" :min="1" class="custom-number" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false" class="cancel-btn">取消</el-button>
          <el-button type="primary" @click="handleSave" class="save-btn">
            <el-icon><Check /></el-icon>
            保存
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="instanceDialogVisible" title="服务实例" width="800px" class="custom-dialog">
      <div class="instance-header">
        <el-button type="primary" size="small" @click="showInstanceDialog('create')" class="add-instance-btn">
          <el-icon><Plus /></el-icon>
          添加实例
        </el-button>
      </div>
      <el-table :data="instances" border class="custom-table">
        <el-table-column prop="id" label="ID" width="80">
          <template #default="{ row }">
            <span class="id-cell">{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="host" label="主机" width="150">
          <template #default="{ row }">
            <span class="host-cell">{{ row.host }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="port" label="端口" width="100" align="center">
          <template #default="{ row }">
            <span class="number-cell">{{ row.port }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="weight" label="权重" width="100" align="center">
          <template #default="{ row }">
            <span class="number-cell">{{ row.weight }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'HEALTHY' ? 'success' : row.status === 'DOWN' ? 'danger' : 'warning'" class="status-tag">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="活跃" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.active ? 'success' : 'info'" class="status-tag">
              {{ row.active ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button size="small" class="action-btn delete-btn" @click="deleteInstance(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Connection, Grid, Edit, Delete, Check } from '@element-plus/icons-vue'
import { servicesApi } from '../api'

const services = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('创建服务')
const isEdit = ref(false)
const instanceDialogVisible = ref(false)
const currentServiceId = ref(null)
const instances = ref([])

const form = ref({
  name: '', baseUrl: '', healthCheckUrl: '', healthCheckInterval: 30, timeoutMs: 5000, maxConnections: 200
})

const instanceForm = ref({ host: '', port: 80, weight: 100 })

const loadData = async () => {
  try {
    services.value = await servicesApi.list()
  } catch (error) {
    ElMessage.error('加载失败')
  }
}

const showDialog = (type, row = null) => {
  if (type === 'create') {
    dialogTitle.value = '创建服务'
    isEdit.value = false
    form.value = { name: '', baseUrl: '', healthCheckUrl: '', healthCheckInterval: 30, timeoutMs: 5000, maxConnections: 200 }
  } else {
    dialogTitle.value = '编辑服务'
    isEdit.value = true
    form.value = { ...row }
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  try {
    if (isEdit.value) {
      await servicesApi.update(form.value.id, form.value)
    } else {
      await servicesApi.create(form.value)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除?', '警告', { type: 'warning' })
    await servicesApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}

const showInstances = async (row) => {
  currentServiceId.value = row.id
  try {
    instances.value = await servicesApi.instances.list(row.id)
    instanceDialogVisible.value = true
  } catch (error) {
    ElMessage.error('加载实例失败')
  }
}

const showInstanceDialog = (type) => {
  instanceForm.value = { host: '', port: 80, weight: 100 }
}

const deleteInstance = async (row) => {
  try {
    await servicesApi.instances.delete(currentServiceId.value, row.id)
    ElMessage.success('删除成功')
    showInstances({ id: currentServiceId.value })
  } catch (error) {
    ElMessage.error('删除失败')
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
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
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

.add-button {
  display: flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
  border: none;
  padding: 12px 20px;
  border-radius: var(--radius-md);
  font-family: 'Fira Sans', sans-serif;
  font-weight: 600;
  color: #FFFFFF;
  cursor: pointer;
  transition: all var(--transition-normal);
}

.add-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(124, 58, 237, 0.3);
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
  font-weight: 600;
}

.name-icon {
  color: var(--primary);
}

.url-cell {
  font-family: 'Fira Code', monospace;
  font-size: 13px;
  color: var(--text-secondary);
}

.path-cell {
  font-family: 'Fira Code', monospace;
  font-size: 13px;
  color: var(--primary-light);
}

.number-cell {
  font-family: 'Fira Code', monospace;
  font-weight: 600;
}

.status-tag {
  border-radius: 6px;
  font-weight: 500;
  padding: 4px 10px;
}

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
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

.edit-btn:hover {
  color: var(--primary);
  background: rgba(124, 58, 237, 0.1);
}

.delete-btn {
  color: var(--danger, #EF4444);
}

.delete-btn:hover {
  background: rgba(239, 68, 68, 0.1);
}

.instance-header {
  margin-bottom: 16px;
}

.add-instance-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background: var(--primary);
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
}

.host-cell {
  font-family: 'Fira Code', monospace;
  font-size: 13px;
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

.custom-number :deep(.el-input__wrapper) {
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

.save-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
  border: none;
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
</style>
