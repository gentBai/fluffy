<template>
  <div>
    <h2>黑白名单</h2>
    <el-tabs style="margin-top: 20px">
      <el-tab-pane label="黑名单">
        <div style="display: flex; justify-content: flex-end; margin-bottom: 16px">
          <el-button type="danger" @click="showBlackDialog">添加黑名单</el-button>
        </div>
        <el-table :data="blacklist" border>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="targetType" label="类型" width="100" />
          <el-table-column prop="targetValue" label="目标值" />
          <el-table-column prop="reason" label="原因" />
          <el-table-column prop="expiresAt" label="过期时间" width="180" />
          <el-table-column prop="createdAt" label="创建时间" width="180" />
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button size="small" type="danger" @click="deleteBlack(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="白名单">
        <div style="display: flex; justify-content: flex-end; margin-bottom: 16px">
          <el-button type="success" @click="showWhiteDialog">添加白名单</el-button>
        </div>
        <el-table :data="whitelist" border>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="targetType" label="类型" width="100" />
          <el-table-column prop="targetValue" label="目标值" />
          <el-table-column prop="description" label="描述" />
          <el-table-column prop="createdAt" label="创建时间" width="180" />
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button size="small" type="danger" @click="deleteWhite(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="blackDialogVisible" title="添加黑名单" width="500px">
      <el-form :model="blackForm" label-width="100px">
        <el-form-item label="类型">
          <el-select v-model="blackForm.targetType" style="width: 100%">
            <el-option label="IP" value="IP" />
            <el-option label="用户" value="USER" />
            <el-option label="API Key" value="API_KEY" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标值">
          <el-input v-model="blackForm.targetValue" />
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="blackForm.reason" type="textarea" />
        </el-form-item>
        <el-form-item label="过期时间">
          <el-date-picker v-model="blackForm.expiresAt" type="datetime" placeholder="不设置则永久" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="blackDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="addBlack">添加</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="whiteDialogVisible" title="添加白名单" width="500px">
      <el-form :model="whiteForm" label-width="100px">
        <el-form-item label="类型">
          <el-select v-model="whiteForm.targetType" style="width: 100%">
            <el-option label="IP" value="IP" />
            <el-option label="用户" value="USER" />
            <el-option label="API Key" value="API_KEY" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标值">
          <el-input v-model="whiteForm.targetValue" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="whiteForm.description" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="whiteDialogVisible = false">取消</el-button>
        <el-button type="success" @click="addWhite">添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { blacklistApi, whitelistApi } from '../api'

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
