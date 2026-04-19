<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2>服务管理</h2>
      <el-button type="primary" @click="showDialog('create')">创建服务</el-button>
    </div>

    <el-table :data="services" border style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" width="150" />
      <el-table-column prop="baseUrl" label="基础URL" />
      <el-table-column prop="healthCheckUrl" label="健康检查URL" width="200" />
      <el-table-column prop="timeoutMs" label="超时(ms)" width="100" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.deleted ? 'danger' : 'success'">
            {{ row.deleted ? '禁用' : '启用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="showInstances(row)">实例</el-button>
          <el-button size="small" @click="showDialog('edit', row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="基础URL">
          <el-input v-model="form.baseUrl" placeholder="http://localhost:8080" />
        </el-form-item>
        <el-form-item label="健康检查URL">
          <el-input v-model="form.healthCheckUrl" placeholder="/health" />
        </el-form-item>
        <el-form-item label="检查间隔(秒)">
          <el-input-number v-model="form.healthCheckInterval" :min="10" />
        </el-form-item>
        <el-form-item label="超时(ms)">
          <el-input-number v-model="form.timeoutMs" :min="1000" :step="1000" />
        </el-form-item>
        <el-form-item label="最大连接数">
          <el-input-number v-model="form.maxConnections" :min="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="instanceDialogVisible" title="服务实例" width="800px">
      <div style="margin-bottom: 16px">
        <el-button type="primary" size="small" @click="showInstanceDialog('create')">添加实例</el-button>
      </div>
      <el-table :data="instances" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="host" label="主机" width="150" />
        <el-table-column prop="port" label="端口" width="100" />
        <el-table-column prop="weight" label="权重" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'HEALTHY' ? 'success' : row.status === 'DOWN' ? 'danger' : 'warning'">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="活跃" width="80">
          <template #default="{ row }">
            <el-tag :type="row.active ? 'success' : 'info'">{{ row.active ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button size="small" type="danger" @click="deleteInstance(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
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
