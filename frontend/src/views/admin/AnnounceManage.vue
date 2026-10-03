<template>
  <div class="announce-manage">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-button type="default" @click="goBack" :icon="ArrowLeft">返回主页</el-button>
        <div class="header-title">
          <h2>📢 公告管理</h2>
          <p>发布、编辑和管理平台公告</p>
        </div>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="openAddDialog" :icon="Plus">发布公告</el-button>
        <el-button @click="loadAnnouncements" :loading="loading" :icon="Refresh">刷新</el-button>
      </div>
    </div>

    <!-- 公告列表 -->
    <el-card class="list-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>📋 公告列表</span>
          <el-tag type="info" size="small">{{ announcements.length }} 条记录</el-tag>
        </div>
      </template>

      <el-table :data="announcements" border stripe v-loading="loading" class="announce-table">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="typeText" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.typeColor" size="small">{{ row.typeText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priorityText" label="优先级" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.priorityColor" size="small">{{ row.priorityText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="statusText" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.statusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布日期" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.publishTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="publisherName" label="发布人" width="100" />
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openEditDialog(row)">编辑</el-button>
            <el-button
              v-if="row.status === 1"
              type="warning"
              size="small"
              link
              @click="toggleStatus(row.id, 0)"
            >
              下架
            </el-button>
            <el-button
              v-else
              type="success"
              size="small"
              link
              @click="toggleStatus(row.id, 1)"
            >
              发布
            </el-button>
            <el-button type="danger" size="small" link @click="deleteAnnouncement(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑公告对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="announcementForm" :rules="formRules" ref="formRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="announcementForm.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="announcementForm.content"
            type="textarea"
            :rows="5"
            placeholder="请输入公告内容"
          />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="announcementForm.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="系统公告" :value="1" />
            <el-option label="场馆通知" :value="2" />
            <el-option label="活动通知" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-radio-group v-model="announcementForm.priority">
            <el-radio :value="0">普通</el-radio>
            <el-radio :value="1">重要</el-radio>
            <el-radio :value="2">紧急</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="发布日期" prop="publishTime">
          <el-date-picker
            v-model="announcementForm.publishTime"
            type="datetime"
            placeholder="选择发布日期"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Plus, Refresh } from '@element-plus/icons-vue'
import axios from 'axios'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const announcements = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

// 表单数据
const announcementForm = reactive({
  id: null,
  title: '',
  content: '',
  type: 1,
  priority: 0,
  publishTime: '',
  status: 1
})

// 表单验证规则
const formRules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }],
  type: [{ required: true, message: '请选择公告类型', trigger: 'change' }],
  publishTime: [{ required: true, message: '请选择发布日期', trigger: 'change' }]
}

// 对话框标题
const dialogTitle = computed(() => isEdit.value ? '编辑公告' : '发布公告')

// 格式化日期时间
const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 加载公告列表
const loadAnnouncements = async () => {
  loading.value = true
  try {
    const res = await axios.get('http://localhost:8080/api/announcement/list', {
      withCredentials: true
    })
    if (res.data.code === 200) {
      announcements.value = res.data.data || []
    } else {
      ElMessage.error(res.data.message || '加载失败')
    }
  } catch (err) {
    console.error('加载公告列表失败:', err)
    ElMessage.error('加载失败，请检查网络')
  } finally {
    loading.value = false
  }
}

// 打开添加对话框
const openAddDialog = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 打开编辑对话框
const openEditDialog = (row) => {
  isEdit.value = true
  announcementForm.id = row.id
  announcementForm.title = row.title
  announcementForm.content = row.content
  announcementForm.type = row.type
  announcementForm.priority = row.priority
  announcementForm.publishTime = row.publishTime
  announcementForm.status = row.status
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  announcementForm.id = null
  announcementForm.title = ''
  announcementForm.content = ''
  announcementForm.type = 1
  announcementForm.priority = 0
  announcementForm.publishTime = new Date().toISOString().slice(0, 19).replace('T', ' ')
  announcementForm.status = 1
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      const url = isEdit.value
        ? 'http://localhost:8080/api/announcement/update'
        : 'http://localhost:8080/api/announcement/add'

      const res = await axios.put(url, announcementForm, {
        withCredentials: true
      })

      if (res.data.code === 200) {
        ElMessage.success(isEdit.value ? '公告更新成功' : '公告发布成功')
        dialogVisible.value = false
        loadAnnouncements()
      } else {
        ElMessage.error(res.data.message || '操作失败')
      }
    } catch (err) {
      console.error('操作失败:', err)
      ElMessage.error('操作失败，请稍后重试')
    } finally {
      submitting.value = false
    }
  })
}

// 切换状态（发布/下架）
const toggleStatus = async (id, status) => {
  const action = status === 1 ? '发布' : '下架'
  try {
    await ElMessageBox.confirm(`确认${action}该公告吗？`, '操作确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await axios.put(`http://localhost:8080/api/announcement/status/${id}?status=${status}`, {}, {
      withCredentials: true
    })

    if (res.data.code === 200) {
      ElMessage.success(`公告已${action}`)
      loadAnnouncements()
    } else {
      ElMessage.error(res.data.message || '操作失败')
    }
  } catch (err) {
    if (err !== 'cancel') {
      console.error('操作失败:', err)
      ElMessage.error('操作失败')
    }
  }
}

// 删除公告
const deleteAnnouncement = async (id) => {
  try {
    await ElMessageBox.confirm('确认删除该公告吗？删除后无法恢复。', '删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await axios.delete(`http://localhost:8080/api/announcement/${id}`, {
      withCredentials: true
    })

    if (res.data.code === 200) {
      ElMessage.success('删除成功')
      loadAnnouncements()
    } else {
      ElMessage.error(res.data.message || '删除失败')
    }
  } catch (err) {
    if (err !== 'cancel') {
      console.error('删除失败:', err)
      ElMessage.error('删除失败')
    }
  }
}

const goBack = () => {
  router.push('/')
}

onMounted(() => {
  loadAnnouncements()
})
</script>

<style scoped>
.announce-manage {
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #eef2f6 100%);
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 0 8px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.back-btn {
  background: white;
  border-radius: 8px;
  transition: all 0.2s;
}
.back-btn:hover {
  background: #f0f2f5;
  transform: translateX(-2px);
}

.header-title h2 {
  font-size: 24px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 4px;
}
.header-title p {
  color: #64748b;
  font-size: 14px;
}

.list-card {
  border-radius: 16px;
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #1e293b;
}

.announce-table {
  width: 100%;
}

:deep(.el-table th) {
  background-color: #f8fafc;
  color: #1e293b;
  font-weight: 600;
}
</style>
