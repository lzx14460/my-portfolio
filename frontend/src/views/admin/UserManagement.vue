<template>
  <div class="user-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-button type="default" @click="goBack" :icon="ArrowLeft" class="back-btn">
          返回主页
        </el-button>
        <div class="header-title">
          <h2>👥 用户管理</h2>
          <p>查看和管理所有用户信息</p>
        </div>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="loadUsers" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 统计卡片（可点击筛选） -->
    <div class="stats-cards">
      <div class="stat-card" :class="{ active: currentFilter === 'all' }" @click="setFilter('all')">
        <div class="stat-icon total">👥</div>
        <div class="stat-info">
          <div class="stat-value">{{ totalUsers }}</div>
          <div class="stat-label">总用户数</div>
        </div>
      </div>
      <div
        class="stat-card"
        :class="{ active: currentFilter === 'admin' }"
        @click="setFilter('admin')"
      >
        <div class="stat-icon admin">⚙️</div>
        <div class="stat-info">
          <div class="stat-value">{{ adminCount }}</div>
          <div class="stat-label">管理员</div>
        </div>
      </div>
      <div
        class="stat-card"
        :class="{ active: currentFilter === 'student' }"
        @click="setFilter('student')"
      >
        <div class="stat-icon student">👨‍🎓</div>
        <div class="stat-info">
          <div class="stat-value">{{ studentCount }}</div>
          <div class="stat-label">学生</div>
        </div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="hover">
      <div class="search-bar">
        <el-input
          v-model="searchParams.keyword"
          placeholder="请输入手机号/姓名/学号"
          prefix-icon="Search"
          clearable
          size="large"
          style="width: 300px"
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        />
        <el-button type="primary" size="large" @click="handleSearch">
          <el-icon><Search /></el-icon>
          查询
        </el-button>
        <el-button size="large" @click="resetSearch">
          <el-icon><RefreshRight /></el-icon>
          重置
        </el-button>
      </div>
    </el-card>

    <!-- 当前筛选状态提示 -->
    <div class="filter-tip" v-if="currentFilter !== 'all'">
      <el-tag
        :type="currentFilter === 'admin' ? 'warning' : 'primary'"
        closable
        @close="setFilter('all')"
      >
        当前筛选：{{ currentFilter === 'admin' ? '管理员' : '学生' }}
      </el-tag>
    </div>

    <!-- 用户列表 -->
    <el-card class="user-list-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>📋 用户列表</span>
          <el-tag type="info" size="small">{{ filteredUsers.length }} 条记录</el-tag>
        </div>
      </template>

      <el-table
        :data="paginatedUsers"
        border
        stripe
        v-loading="loading"
        class="user-table"
        empty-text="暂无用户数据"
      >
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="studentId" label="学号" width="140">
          <template #default="{ row }">
            {{ row.studentId || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="college" label="学院" width="140">
          <template #default="{ row }">
            {{ row.college || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="className" label="班级" width="120">
          <template #default="{ row }">
            {{ row.className || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130">
          <template #default="{ row }">
            {{ row.phone || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="160">
          <template #default="{ row }">
            {{ row.email || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="role" label="角色" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)">
              {{ getRoleText(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 1 || row.status === '1' ? 'success' : 'danger'"
              size="small"
            >
              {{ row.status === 1 || row.status === '1' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="viewUserDetail(row)">
              详情
            </el-button>
            <el-button type="success" size="small" link @click="openEditDialog(row)">
              编辑
            </el-button>
            <el-button
              v-if="row.status === 1 || row.status === '1'"
              type="warning"
              size="small"
              link
              @click="toggleUserStatus(row.id, 0)"
            >
              禁用
            </el-button>
            <el-button v-else type="success" size="small" link @click="toggleUserStatus(row.id, 1)">
              启用
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="filteredUsers.length"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 用户详情对话框 -->
    <el-dialog v-model="detailVisible" title="用户详情" width="550px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="ID">{{ currentUser?.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ currentUser?.username }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ currentUser?.name }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ currentUser?.studentId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="学院">{{ currentUser?.college || '-' }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ currentUser?.className || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentUser?.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentUser?.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ getRoleText(currentUser?.role) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag
            :type="currentUser?.status === 1 || currentUser?.status === '1' ? 'success' : 'danger'"
            size="small"
          >
            {{ currentUser?.status === 1 || currentUser?.status === '1' ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间" :span="2">
          {{ formatDate(currentUser?.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="最后更新" :span="2">
          {{ formatDate(currentUser?.updateTime) }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 编辑用户对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑用户信息" width="550px">
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="学号" prop="studentId">
          <el-input v-model="editForm.studentId" placeholder="请输入学号" />
        </el-form-item>
        <el-form-item label="学院" prop="college">
          <el-input v-model="editForm.college" placeholder="请输入学院" />
        </el-form-item>
        <el-form-item label="班级" prop="className">
          <el-input v-model="editForm.className" placeholder="请输入班级" />
        </el-form-item>

      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit" :loading="editLoading">保存修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Refresh, Search, RefreshRight } from '@element-plus/icons-vue'
import axios from 'axios'

const router = useRouter()

// 数据
const allUsers = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)

// 统计数据
const totalUsers = ref(0)
const adminCount = ref(0)
const studentCount = ref(0)

// 当前筛选类型：'all', 'admin', 'student'
const currentFilter = ref('all')

// 搜索参数
const searchParams = ref({
  keyword: '',
})

// 详情对话框
const detailVisible = ref(false)
const currentUser = ref(null)

// 编辑对话框
const editDialogVisible = ref(false)
const editLoading = ref(false)
const editFormRef = ref(null)
const editForm = ref({
  id: null,
  name: '',
  studentId: '',
  college: '',
  className: '',

})

// 编辑表单验证规则
const editRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],

}

// 返回主页
const goBack = () => {
  router.push('/')
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`
}

// 获取角色文本
const getRoleText = (role) => {
  const r = Number(role)
  switch (r) {
    case 0:
      return '学生'
    case 1:
      return '场馆管理员'
    case 2:
      return '系统管理员'
    default:
      return '未知'
  }
}

// 获取角色标签类型
const getRoleType = (role) => {
  const r = Number(role)
  switch (r) {
    case 0:
      return 'primary'
    case 1:
      return 'warning'
    case 2:
      return 'danger'
    default:
      return 'info'
  }
}

// 设置筛选
const setFilter = (filter) => {
  currentFilter.value = filter
  currentPage.value = 1
}

// 加载用户列表
const loadUsers = async () => {
  loading.value = true
  try {
    const res = await axios.get('http://localhost:8080/api/user/list', {
      withCredentials: true,
    })
    console.log('用户列表返回:', res.data)

    if (res.data.code === 200) {
      allUsers.value = res.data.data || []
      calculateStats()
    } else {
      ElMessage.error(res.data.message || '加载失败')
    }
  } catch (err) {
    console.error('加载用户列表失败:', err)
    ElMessage.error('加载失败，请检查网络')
  } finally {
    loading.value = false
  }
}

// 计算统计数据
const calculateStats = () => {
  totalUsers.value = allUsers.value.length
  adminCount.value = allUsers.value.filter((u) => u.role === 1 || u.role === 2).length
  studentCount.value = allUsers.value.filter((u) => u.role === 0).length
}

// 过滤用户
const filteredUsers = computed(() => {
  let users = [...allUsers.value]

  if (currentFilter.value === 'admin') {
    users = users.filter((u) => u.role === 1 || u.role === 2)
  } else if (currentFilter.value === 'student') {
    users = users.filter((u) => u.role === 0)
  }

  if (searchParams.value.keyword) {
    const keyword = searchParams.value.keyword.toLowerCase()
    users = users.filter(
      (user) =>
        (user.phone && user.phone.includes(keyword)) ||
        (user.name && user.name.toLowerCase().includes(keyword)) ||
        (user.studentId && user.studentId.toLowerCase().includes(keyword)) ||
        (user.username && user.username.toLowerCase().includes(keyword)) ||
        (user.college && user.college.toLowerCase().includes(keyword)) ||
        (user.className && user.className.toLowerCase().includes(keyword))
    )
  }

  return users
})

// 分页后的用户
const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredUsers.value.slice(start, end)
})

// 搜索
const handleSearch = () => {
  currentPage.value = 1
}

// 重置搜索
const resetSearch = () => {
  searchParams.value.keyword = ''
  currentPage.value = 1
}

// 分页变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleCurrentChange = (page) => {
  currentPage.value = page
}

// 查看用户详情
const viewUserDetail = (user) => {
  currentUser.value = user
  detailVisible.value = true
}

// 打开编辑对话框
const openEditDialog = (user) => {
  editForm.value = {
    id: user.id,
    name: user.name || '',
    studentId: user.studentId || '',
    college: user.college || '',
    className: user.className || '',

  }
  editDialogVisible.value = true
}

// 提交编辑
const submitEdit = async () => {
  if (!editFormRef.value) return

  await editFormRef.value.validate(async (valid) => {
    if (!valid) return

    editLoading.value = true
    try {
      const res = await axios.put(
        `http://localhost:8080/api/user/admin/update`,
        editForm.value,
        { withCredentials: true }
      )

      if (res.data.code === 200) {
        ElMessage.success('用户信息更新成功')
        editDialogVisible.value = false
        loadUsers() // 刷新列表
      } else {
        ElMessage.error(res.data.message || '更新失败')
      }
    } catch (err) {
      console.error('更新用户失败:', err)
      ElMessage.error('更新失败，请稍后重试')
    } finally {
      editLoading.value = false
    }
  })
}

// 切换用户状态
const toggleUserStatus = async (userId, status) => {
  const action = status === 1 ? '启用' : '禁用'
  await ElMessageBox.confirm(`确认${action}该用户吗？`, '操作确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })

  try {
    const res = await axios.put(
      `http://localhost:8080/api/user/status/${userId}?status=${status}`,
      {},
      { withCredentials: true }
    )

    if (res.data.code === 200) {
      ElMessage.success(`用户已${action}`)
      loadUsers()
    } else {
      ElMessage.error(res.data.message || '操作失败')
    }
  } catch (err) {
    console.error('操作失败:', err)
    ElMessage.error('操作失败，请稍后重试')
  }
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
/* 样式保持不变，原有样式继续使用 */
.user-management {
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
  transition: all 0.2s;
  cursor: pointer;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.stat-card.active {
  border: 2px solid #409eff;
  background: #ecf5ff;
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

.stat-icon.total {
  background: linear-gradient(135deg, #dbeafe, #bfdbfe);
}
.stat-icon.admin {
  background: linear-gradient(135deg, #fef3c7, #fde68a);
}
.stat-icon.student {
  background: linear-gradient(135deg, #dcfce7, #bbf7d0);
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

.filter-tip {
  margin-bottom: 16px;
}

.search-card {
  border-radius: 16px;
  margin-bottom: 24px;
}

.search-bar {
  display: flex;
  gap: 12px;
  align-items: center;
}

.user-list-card {
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

.user-table {
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

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
