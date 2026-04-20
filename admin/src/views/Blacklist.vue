<template>
  <div class="page-container">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">黑白名单</h1>
        <p class="page-subtitle">管理访问控制列表</p>
      </div>
    </div>

    <div class="tabs-card">
      <el-tabs v-model="activeTab" class="custom-tabs">
        <el-tab-pane label="黑名单" name="black">
          <template #label>
            <div class="tab-label tab-label-danger">
              <el-icon><CircleClose /></el-icon>
              <span>黑名单</span>
            </div>
          </template>
          <div class="tab-content">
            <div class="content-header">
              <el-button type="danger" class="add-button add-button-danger" @click="showBlackDialog">
                <el-icon><Plus /></el-icon>
                添加黑名单
              </el-button>
            </div>
            <el-table :data="blacklist" border class="custom-table">
              <el-table-column prop="id" label="ID" width="80">
                <template #default="{ row }">
                  <span class="id-cell">{{ row.id }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="targetType" label="类型" width="100" align="center">
                <template #default="{ row }">
                  <el-tag class="type-tag type-danger">{{ row.targetType }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="targetValue" label="目标值">
                <template #default="{ row }">
                  <code class="target-value">{{ row.targetValue }}</code>
                </template>
              </el-table-column>
              <el-table-column prop="reason" label="原因">
                <template #default="{ row }">
                  <span class="reason-text">{{ row.reason || '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="expiresAt" label="过期时间" width="180">
                <template #default="{ row }">
                  <span class="time-cell">{{ row.expiresAt || '永久' }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" label="创建时间" width="180">
                <template #default="{ row }">
                  <span class="time-cell">{{ row.createdAt }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120" align="center">
                <template #default="{ row }">
                  <el-button size="small" class="action-btn delete-btn" @click="deleteBlack(row)">
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
        <el-tab-pane label="白名单" name="white">
          <template #label>
            <div class="tab-label tab-label-success">
              <el-icon><CircleCheck /></el-icon>
              <span>白名单</span>
            </div>
          </template>
          <div class="tab-content">
            <div class="content-header">
              <el-button type="success" class="add-button add-button-success" @click="showWhiteDialog">
                <el-icon><Plus /></el-icon>
                添加白名单
              </el-button>
            </div>
            <el-table :data="whitelist" border class="custom-table">
              <el-table-column prop="id" label="ID" width="80">
                <template #default="{ row }">
                  <span class="id-cell">{{ row.id }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="targetType" label="类型" width="100" align="center">
                <template #default="{ row }">
                  <el-tag class="type-tag type-success">{{ row.targetType }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="targetValue" label="目标值">
                <template #default="{ row }">
                  <code class="target-value">{{ row.targetValue }}</code>
                </template>
              </el-table-column>
              <el-table-column prop="description" label="描述">
                <template #default="{ row }">
                  <span class="reason-text">{{ row.description || '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" label="创建时间" width="180">
                <template #default="{ row }">
                  <span class="time-cell">{{ row.createdAt }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120" align="center">
                <template #default="{ row }">
                  <el-button size="small" class="action-btn delete-btn" @click="deleteWhite(row)">
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="blackDialogVisible" title="添加黑名单" width="500px" class="custom-dialog">
      <el-form :model="blackForm" label-width="100px" class="dialog-form">
        <el-form-item label="类型">
          <el-select v-model="blackForm.targetType" style="width: 100%" class="custom-select">
            <el-option label="IP" value="IP" />
            <el-option label="用户" value="USER" />
            <el-option label="API Key" value="API_KEY" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标值">
          <el-input v-model="blackForm.targetValue" placeholder="IP、用户ID或API Key" class="custom-input" />
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="blackForm.reason" type="textarea" rows="3" placeholder="封禁原因" class="custom-textarea" />
        </el-form-item>
        <el-form-item label="过期时间">
          <el-date-picker
            v-model="blackForm.expiresAt"
            type="datetime"
            placeholder="不设置则永久"
            style="width: 100%"
            class="custom-date-picker"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="blackDialogVisible = false" class="cancel-btn">取消</el-button>
          <el-button type="danger" @click="addBlack" class="save-btn save-btn-danger">
            <el-icon><Plus /></el-icon>
            添加
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="whiteDialogVisible" title="添加白名单" width="500px" class="custom-dialog">
      <el-form :model="whiteForm" label-width="100px" class="dialog-form">
        <el-form-item label="类型">
          <el-select v-model="whiteForm.targetType" style="width: 100%" class="custom-select">
            <el-option label="IP" value="IP" />
            <el-option label="用户" value="USER" />
            <el-option label="API Key" value="API_KEY" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标值">
          <el-input v-model="whiteForm.targetValue" placeholder="IP、用户ID或API Key" class="custom-input" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="whiteForm.description" type="textarea" rows="3" placeholder="白名单描述" class="custom-textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="whiteDialogVisible = false" class="cancel-btn">取消</el-button>
          <el-button type="success" @click="addWhite" class="save-btn save-btn-success">
            <el-icon><Plus /></el-icon>
            添加
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { CircleClose, CircleCheck, Plus, Delete } from '@element-plus/icons-vue'
import { blacklistApi, whitelistApi } from '../api'

const activeTab = ref('black')
const blacklist = ref([])
const whitelist = ref([])
const blackDialogVisible = ref(false)
const whiteDialogVisible = ref(false)
const blackForm = ref({ targetType: 'IP', targetValue: '', reason: '', expiresAt: null })
const whiteForm = ref({ targetType: 'IP', targetValue: '', description: '' })

const loadData = async () => {
  try {
    blacklist.value = await blacklistApi.list()
    whitelist.value = await whitelistApi.list()
  } catch (error) {
    ElMessage.error('加载失败')
  }
}

const showBlackDialog = () => {
  blackForm.value = { targetType: 'IP', targetValue: '', reason: '', expiresAt: null }
  blackDialogVisible.value = true
}

const showWhiteDialog = () => {
  whiteForm.value = { targetType: 'IP', targetValue: '', description: '' }
  whiteDialogVisible.value = true
}

const addBlack = async () => {
  try {
    await blacklistApi.create(blackForm.value)
    ElMessage.success('添加成功')
    blackDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

const deleteBlack = async (row) => {
  try {
    await blacklistApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const addWhite = async () => {
  try {
    await whitelistApi.create(whiteForm.value)
    ElMessage.success('添加成功')
    whiteDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

const deleteWhite = async (row) => {
  try {
    await whitelistApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
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
  --success: #10B981;
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

.tab-label-danger { color: var(--danger); }
.tab-label-success { color: var(--success); }

.content-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

.add-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  border-radius: var(--radius-md);
  font-family: 'Fira Sans', sans-serif;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-normal);
}

.add-button-danger {
  background: linear-gradient(135deg, var(--danger) 0%, #DC2626 100%);
  border: none;
  color: #FFFFFF;
}

.add-button-danger:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(239, 68, 68, 0.3);
}

.add-button-success {
  background: linear-gradient(135deg, var(--success) 0%, #059669 100%);
  border: none;
  color: #FFFFFF;
}

.add-button-success:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(16, 185, 129, 0.3);
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

.type-tag {
  border-radius: 6px;
  font-weight: 600;
  font-size: 11px;
  padding: 4px 10px;
}

.type-danger { background: #FEF2F2; color: #DC2626; }
.type-success { background: #F0FDF4; color: #059669; }

.target-value {
  font-family: 'Fira Code', monospace;
  font-size: 12px;
  background: #F3F4F6;
  padding: 4px 8px;
  border-radius: 4px;
  color: var(--text-primary);
}

.reason-text {
  color: var(--text-secondary);
  font-size: 14px;
}

.time-cell {
  font-family: 'Fira Code', monospace;
  font-size: 13px;
  color: var(--text-secondary);
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

.custom-input :deep(.el-input__wrapper),
.custom-textarea :deep(.el-textarea__inner) {
  border-radius: var(--radius-md);
  box-shadow: 0 0 0 1px var(--border-color);
  transition: all var(--transition-fast);
}

.custom-input :deep(.el-input__wrapper:hover),
.custom-textarea :deep(.el-textarea__inner:hover) {
  box-shadow: 0 0 0 1px var(--primary-light);
}

.custom-input :deep(.el-input__wrapper.is-focus),
.custom-textarea :deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 2px var(--primary);
}

.custom-select :deep(.el-input__wrapper) {
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

.save-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border-radius: var(--radius-md);
  font-family: 'Fira Sans', sans-serif;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-normal);
}

.save-btn-danger {
  background: linear-gradient(135deg, var(--danger) 0%, #DC2626 100%);
  border: none;
  color: #FFFFFF;
}

.save-btn-danger:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

.save-btn-success {
  background: linear-gradient(135deg, var(--success) 0%, #059669 100%);
  border: none;
  color: #FFFFFF;
}

.save-btn-success:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}
</style>
