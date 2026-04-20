<template>
  <div class="page-container">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">路由管理</h1>
        <p class="page-subtitle">配置API路由与转发规则</p>
      </div>
      <el-button type="primary" class="add-button" @click="showDialog('create')">
        <el-icon><Plus /></el-icon>
        创建路由
      </el-button>
    </div>

    <div class="table-card">
      <el-table :data="routes" border class="custom-table">
        <el-table-column prop="id" label="ID" width="80">
          <template #default="{ row }">
            <span class="id-cell">{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" width="150">
          <template #default="{ row }">
            <div class="name-cell">
              <el-icon class="name-icon"><Guide /></el-icon>
              {{ row.name }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="pathPattern" label="路径模式" width="200">
          <template #default="{ row }">
            <span class="path-cell">{{ row.pathPattern }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="httpMethod" label="方法" width="100" align="center">
          <template #default="{ row }">
            <el-tag class="method-tag" :class="'method-' + row.httpMethod.toLowerCase()">
              {{ row.httpMethod }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="serviceId" label="服务ID" width="100" align="center">
          <template #default="{ row }">
            <span class="service-id">{{ row.serviceId }}</span>
          </template>
        </el-table-column>
        <el-table-column label="认证" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.authRequired ? 'warning' : 'info'" class="status-tag">
              {{ row.authRequired ? '需要' : '无需' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="限流" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.rateLimitEnabled ? 'warning' : 'info'" class="status-tag">
              {{ row.rateLimitEnabled ? '启用' : '关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100" align="center">
          <template #default="{ row }">
            <span class="priority-cell">{{ row.priority }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.deleted ? 'danger' : 'success'" class="status-tag">
              {{ row.deleted ? '禁用' : '启用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="160" align="center">
          <template #default="{ row }">
            <div class="action-buttons">
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
          <el-input v-model="form.name" placeholder="路由名称" class="custom-input" />
        </el-form-item>
        <el-form-item label="路径模式">
          <el-input v-model="form.pathPattern" placeholder="/api/users/*" class="custom-input">
            <template #prefix>
              <el-icon class="input-icon"><Guide /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="HTTP方法">
          <el-select v-model="form.httpMethod" style="width: 100%" class="custom-select">
            <el-option label="*" value="*" />
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联服务">
          <el-select v-model="form.serviceId" style="width: 100%" class="custom-select">
            <el-option v-for="s in services" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="需要认证">
          <el-switch v-model="form.authRequired" class="custom-switch" />
        </el-form-item>
        <el-form-item label="启用限流">
          <el-switch v-model="form.rateLimitEnabled" class="custom-switch" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model="form.priority" :min="0" class="custom-number" />
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Guide, Edit, Delete, Check } from '@element-plus/icons-vue'
import { routesApi, servicesApi } from '../api'

const routes = ref([])
const services = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('创建路由')
const isEdit = ref(false)

const form = ref({
  id: null,
  name: '',
  pathPattern: '',
  httpMethod: '*',
  serviceId: null,
  authRequired: false,
  rateLimitEnabled: false,
  priority: 0
})

const loadData = async () => {
  try {
    routes.value = await routesApi.list()
    services.value = await servicesApi.list()
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

const showDialog = (type, row = null) => {
  if (type === 'create') {
    dialogTitle.value = '创建路由'
    isEdit.value = false
    form.value = { id: null, name: '', pathPattern: '', httpMethod: '*', serviceId: null, authRequired: false, rateLimitEnabled: false, priority: 0 }
  } else {
    dialogTitle.value = '编辑路由'
    isEdit.value = true
    form.value = { ...row }
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  try {
    if (isEdit.value) {
      await routesApi.update(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await routesApi.create(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除此路由?', '警告', { type: 'warning' })
    await routesApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('删除失败')
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

.path-cell {
  font-family: 'Fira Code', monospace;
  font-size: 13px;
  color: var(--primary-light);
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
.method-* { background: #F3F4F6; color: #6B7280; }

.service-id {
  font-family: 'Fira Code', monospace;
  color: var(--text-secondary);
}

.priority-cell {
  font-family: 'Fira Code', monospace;
  font-weight: 600;
  color: var(--primary);
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
  color: #EF4444;
}

.delete-btn:hover {
  background: rgba(239, 68, 68, 0.1);
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

.custom-switch :deep(.el-switch.is-checked .el-switch__core) {
  background: var(--primary);
  border-color: var(--primary);
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
