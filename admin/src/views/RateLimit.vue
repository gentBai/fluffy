<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2>限流配置</h2>
      <el-button type="primary" @click="showDialog('create')">创建规则</el-button>
    </div>

    <el-table :data="rules" border style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" width="150" />
      <el-table-column prop="limitType" label="限流类型" width="120">
        <template #default="{ row }">
          <el-tag>{{ row.limitType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="requestsPerMinute" label="次/分钟" width="100" />
      <el-table-column prop="requestsPerHour" label="次/小时" width="100" />
      <el-table-column prop="requestsPerDay" label="次/天" width="100" />
      <el-table-column prop="burstSize" label="突发容量" width="100" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="showDialog('edit', row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="限流类型">
          <el-select v-model="form.limitType" style="width: 100%">
            <el-option label="IP" value="IP" />
            <el-option label="用户名" value="USERNAME" />
            <el-option label="全局" value="GLOBAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="次/分钟">
          <el-input-number v-model="form.requestsPerMinute" :min="0" />
        </el-form-item>
        <el-form-item label="次/小时">
          <el-input-number v-model="form.requestsPerHour" :min="0" />
        </el-form-item>
        <el-form-item label="次/天">
          <el-input-number v-model="form.requestsPerDay" :min="0" />
        </el-form-item>
        <el-form-item label="突发容量">
          <el-input-number v-model="form.burstSize" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
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
