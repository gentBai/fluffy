<template>
  <div class="page-container">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">限流配置</h1>
        <p class="page-subtitle">配置API访问频率限制规则</p>
      </div>
      <el-button type="primary" class="add-button" @click="showDialog('create')">
        <el-icon><Plus /></el-icon>
        创建规则
      </el-button>
    </div>

    <div class="table-card">
      <el-table :data="rules" border class="custom-table">
        <el-table-column prop="id" label="ID" width="80">
          <template #default="{ row }">
            <span class="id-cell">{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" width="150">
          <template #default="{ row }">
            <div class="name-cell">
              <el-icon class="name-icon"><Timer /></el-icon>
              {{ row.name }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="limitType" label="限流类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag class="type-tag" :class="'type-' + row.limitType.toLowerCase()">
              {{ row.limitType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requestsPerMinute" label="次/分钟" width="100" align="center">
          <template #default="{ row }">
            <span class="number-cell">{{ row.requestsPerMinute }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="requestsPerHour" label="次/小时" width="100" align="center">
          <template #default="{ row }">
            <span class="number-cell">{{ row.requestsPerHour }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="requestsPerDay" label="次/天" width="100" align="center">
          <template #default="{ row }">
            <span class="number-cell">{{ row.requestsPerDay }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="burstSize" label="突发容量" width="100" align="center">
          <template #default="{ row }">
            <span class="burst-cell">{{ row.burstSize }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center">
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" class="custom-dialog">
      <el-form :model="form" label-width="120px" class="dialog-form">
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="规则名称" class="custom-input" />
        </el-form-item>
        <el-form-item label="限流类型">
          <el-select v-model="form.limitType" style="width: 100%" class="custom-select">
            <el-option label="IP" value="IP" />
            <el-option label="用户名" value="USERNAME" />
            <el-option label="全局" value="GLOBAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="次/分钟">
          <el-input-number v-model="form.requestsPerMinute" :min="0" class="custom-number" />
        </el-form-item>
        <el-form-item label="次/小时">
          <el-input-number v-model="form.requestsPerHour" :min="0" class="custom-number" />
        </el-form-item>
        <el-form-item label="次/天">
          <el-input-number v-model="form.requestsPerDay" :min="0" class="custom-number" />
        </el-form-item>
        <el-form-item label="突发容量">
          <el-input-number v-model="form.burstSize" :min="0" class="custom-number" />
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Timer, Edit, Delete, Check } from '@element-plus/icons-vue'
import { rateLimitApi } from '../api'

const rules = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('创建规则')
const isEdit = ref(false)
const form = ref({ name: '', limitType: 'GLOBAL', requestsPerMinute: 100, requestsPerHour: 0, requestsPerDay: 0, burstSize: 10 })

const loadData = async () => {
  try {
    rules.value = await rateLimitApi.list()
  } catch (error) {
    ElMessage.error('加载失败')
  }
}

const showDialog = (type, row = null) => {
  if (type === 'create') {
    dialogTitle.value = '创建规则'
    isEdit.value = false
    form.value = { name: '', limitType: 'GLOBAL', requestsPerMinute: 100, requestsPerHour: 0, requestsPerDay: 0, burstSize: 10 }
  } else {
    dialogTitle.value = '编辑规则'
    isEdit.value = true
    form.value = { ...row }
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  try {
    if (isEdit.value) {
      await rateLimitApi.update(form.value.id, form.value)
    } else {
      await rateLimitApi.create(form.value)
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
    await rateLimitApi.delete(row.id)
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
  font-weight: 500;
}

.name-icon {
  color: var(--primary);
}

.type-tag {
  border-radius: 6px;
  font-weight: 600;
  font-size: 11px;
  padding: 4px 10px;
}

.type-ip { background: #EEF2FF; color: #4F46E5; }
.type-username { background: #F0FDF4; color: #16A34A; }
.type-global { background: #FEF3C7; color: #D97706; }

.number-cell {
  font-family: 'Fira Code', monospace;
  font-weight: 600;
}

.burst-cell {
  font-family: 'Fira Code', monospace;
  font-weight: 600;
  color: var(--accent);
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
