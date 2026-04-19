<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2>路由管理</h2>
      <el-button type="primary" @click="showDialog('create')">创建路由</el-button>
    </div>

    <el-table :data="routes" border style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" width="150" />
      <el-table-column prop="pathPattern" label="路径模式" width="200" />
      <el-table-column prop="httpMethod" label="方法" width="100" />
      <el-table-column prop="serviceId" label="服务ID" width="100" />
      <el-table-column label="认证" width="80">
        <template #default="{ row }">
          <el-tag :type="row.authRequired ? 'warning' : 'info'">
            {{ row.authRequired ? '需要' : '无需' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="限流" width="80">
        <template #default="{ row }">
          <el-tag :type="row.rateLimitEnabled ? 'warning' : 'info'">
            {{ row.rateLimitEnabled ? '启用' : '关闭' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="priority" label="优先级" width="100" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.deleted ? 'danger' : 'success'">
            {{ row.deleted ? '禁用' : '启用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="180">
        <template #default="{ row }">
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
        <el-form-item label="路径模式">
          <el-input v-model="form.pathPattern" placeholder="/api/users/*" />
        </el-form-item>
        <el-form-item label="HTTP方法">
          <el-select v-model="form.httpMethod" style="width: 100%">
            <el-option label="*" value="*" />
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联服务">
          <el-select v-model="form.serviceId" style="width: 100%">
            <el-option v-for="s in services" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="需要认证">
          <el-switch v-model="form.authRequired" />
        </el-form-item>
        <el-form-item label="启用限流">
          <el-switch v-model="form.rateLimitEnabled" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model="form.priority" :min="0" />
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
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
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
