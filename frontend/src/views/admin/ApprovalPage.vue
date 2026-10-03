<template>
  <div class="approval-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-button
          type="default"
          @click="goBack"
          :icon="ArrowLeft"
          class="back-btn"
        >
          返回主页
        </el-button>
        <div class="header-title">
          <h2>👥 管理员申请审批</h2>
          <p>查看并处理用户的管理员角色申请</p>
        </div>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="loadPendingList" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon pending">⏳</div>
        <div class="stat-info">
          <div class="stat-value">{{ pendingList.length }}</div>
          <div class="stat-label">待审批申请</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon total">👥</div>
        <div class="stat-info">
          <div class="stat-value">{{ totalUsers }}</div>
          <div class="stat-label">总用户数</div>
        </div>
      </div>
    </div>

    <!-- 待审批列表 -->
    <el-card class="list-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>📋 待审批申请列表</span>
          <el-tag type="warning" size="small">{{ pendingList.length }} 条待处理</el-tag>
        </div>
      </template>

      <el-table
        :data="pendingList"
        border
        stripe
        v-loading="loading"
        class="approval-table"
        empty-text="暂无待审批申请"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="150">
          <template #default="{ row }">
            {{ row.phone || '未填写' }}
          </template>
        </el-table-column>
        <el-table-column prop="appliedRole" label="申请角色" width="150" align="center">
          <template #default="{ row }">
            <el-tag :type="row.appliedRole === 1 ? 'warning' : 'danger'" effect="dark">
              {{ row.appliedRole === 1 ? '🏟️ 场馆管理员' : '⚙️ 系统管理员' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.applyTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
              type="success"
              size="small"
              @click="approve(row.id, 2)"
              :icon="Check"
            >
              通过
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="approve(row.id, 3)"
              :icon="Close"
            >
              拒绝
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 已审批历史 -->
    <el-card class="history-card" shadow="hover" v-if="historyList.length > 0">
      <template #header>
        <div class="card-header">
          <span>📜 已审批记录</span>
          <el-tag type="info" size="small">{{ historyList.length }} 条记录</el-tag>
        </div>
      </template>
      <el-table :data="historyList" border stripe size="small">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <!-- 添加手机号列 -->
        <el-table-column prop="phone" label="手机号" width="140">
          <template #default="{ row }">
            {{ row.phone || '未填写' }}
          </template>
        </el-table-column>
        <el-table-column prop="appliedRole" label="申请角色" width="120">
          <template #default="{ row }">
            {{ row.appliedRole === 1 ? '场馆管理员' : '系统管理员' }}
          </template>
        </el-table-column>
        <el-table-column prop="applyStatus" label="审批结果" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.applyStatus === 2 ? 'success' : 'danger'" size="small">
              {{ row.applyStatus === 2 ? '已通过' : '已拒绝' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="approveTime" label="审批时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.approveTime) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 无历史记录时的提示 -->
    <el-empty
      v-if="!loading && historyList.length === 0 && pendingList.length === 0"
      description="暂无审批记录"
      :image-size="120"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Close, Refresh, ArrowLeft } from '@element-plus/icons-vue'
import axios from 'axios'

const router = useRouter()
const pendingList = ref([])
const historyList = ref([])
const loading = ref(false)
const totalUsers = ref(0)

// 返回主页
const goBack = () => {
  router.push('/')
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 加载待审批列表
const loadPendingList = async () => {
  loading.value = true
  try {
    const res = await axios.get('http://localhost:8080/api/user/pending-approvals', {
      withCredentials: true
    })
    console.log('待审批列表返回:', res.data)

    if (res.data.code === 200) {
      pendingList.value = res.data.data || []
      console.log('待审批数量:', pendingList.value.length)
    } else {
      ElMessage.error(res.data.message || '加载失败')
    }
  } catch (err) {
    console.error('加载失败:', err)
    ElMessage.error('加载失败，请检查网络')
  } finally {
    loading.value = false
  }
}

// 加载已审批历史
const loadHistory = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/user/approved-history', {
      withCredentials: true
    })
    if (res.data.code === 200) {
      historyList.value = res.data.data || []
      console.log('历史记录数量:', historyList.value.length)
    }
  } catch (err) {
    console.error('加载历史失败:', err)
  }
}

// 加载总用户数
const loadTotalUsers = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/user/count', {
      withCredentials: true
    })
    if (res.data.code === 200) {
      totalUsers.value = res.data.data || 0
    }
  } catch (err) {
    console.error('加载用户数失败:', err)
  }
}

// 审批
const approve = async (userId, status) => {
  console.log('审批参数 - userId:', userId, 'status:', status)
  const action = status === 2 ? '通过' : '拒绝'
  await ElMessageBox.confirm(
    `确认${action}该申请吗？${status === 2 ? '通过后用户将获得管理员权限。' : '拒绝后用户将保持普通学生身份。'}`,
    '审批确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )

  try {
    const res = await axios.post('http://localhost:8080/api/user/approve-apply', {
      userId: userId,
      status: status,
      remark: ''
    }, { withCredentials: true })
    console.log('审批返回结果:', res.data)
    if (res.data.code === 200) {
      ElMessage.success(res.data.message)
      loadPendingList()  // 刷新列表
      loadHistory()      // 刷新历史
      loadTotalUsers()   // 刷新统计
    } else {
      ElMessage.error(res.data.message || '操作失败')
    }
  } catch (err) {
    console.error('操作失败:', err)
    ElMessage.error('操作失败，请稍后重试')
  }
}

onMounted(() => {
  loadPendingList()
  loadHistory()
  loadTotalUsers()
})
</script>

<style scoped>
.approval-page {
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #eef2f6 100%);
  min-height: 100vh;
}

/* 页面头部 */
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

/* 统计卡片 */
.stats-cards {
  display: flex;
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  flex: 1;
  background: white;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  font-size: 40px;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
}

.stat-icon.pending {
  background: linear-gradient(135deg, #fef3c7, #fde68a);
}

.stat-icon.total {
  background: linear-gradient(135deg, #dbeafe, #bfdbfe);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
}

.stat-label {
  font-size: 13px;
  color: #64748b;
  margin-top: 4px;
}

/* 卡片样式 */
.list-card, .history-card {
  border-radius: 16px;
  margin-bottom: 24px;
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #1e293b;
}

/* 表格样式 */
.approval-table {
  width: 100%;
}

:deep(.el-table th) {
  background-color: #f8fafc;
  color: #1e293b;
  font-weight: 600;
}

:deep(.el-table .el-button) {
  margin: 0 4px;
}

/* 空状态样式 */
:deep(.el-empty) {
  padding: 60px 0;
}
</style>
