<template>
  <div class="reservation-management">
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
          <h2>📋 预约订单管理</h2>
          <p>查看和管理所有用户的预约订单</p>
        </div>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="loadReservations" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card" @click="setStatusFilter(null)">
        <div class="stat-icon total">📅</div>
        <div class="stat-info">
          <div class="stat-value">{{ totalCount }}</div>
          <div class="stat-label">总订单数</div>
        </div>
      </div>
      <div class="stat-card" @click="setStatusFilter(1)">
        <div class="stat-icon paid">✅</div>
        <div class="stat-info">
          <div class="stat-value">{{ statusCounts[1] || 0 }}</div>
          <div class="stat-label">已支付</div>
        </div>
      </div>
      <div class="stat-card" @click="setStatusFilter(2)">
        <div class="stat-icon used">🏃</div>
        <div class="stat-info">
          <div class="stat-value">{{ statusCounts[2] || 0 }}</div>
          <div class="stat-label">已使用</div>
        </div>
      </div>
      <div class="stat-card" @click="setStatusFilter(3)">
        <div class="stat-icon cancelled">❌</div>
        <div class="stat-info">
          <div class="stat-value">{{ statusCounts[3] || 0 }}</div>
          <div class="stat-label">已取消</div>
        </div>
      </div>
    </div>

    <!-- 当前筛选状态提示 -->
    <div class="filter-tip" v-if="statusFilter !== null">
      <el-tag closable @close="setStatusFilter(null)">
        当前筛选：{{ getStatusText(statusFilter) }}
      </el-tag>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="hover">
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="请输入用户名/手机号/设施名称"
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

    <!-- 预约列表 -->
    <el-card class="list-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>📋 预约订单列表</span>
          <el-tag type="info" size="small">{{ filteredReservations.length }} 条记录</el-tag>
        </div>
      </template>

      <el-table
        :data="paginatedReservations"
        border
        stripe
        v-loading="loading"
        class="reservation-table"
        empty-text="暂无预约记录"
      >
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="userName" label="用户名" width="100" />
        <el-table-column prop="userPhone" label="手机号" width="120">
          <template #default="{ row }">
            {{ row.userPhone || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="facilityName" label="设施名称" min-width="140" />
        <el-table-column prop="reservationDate" label="预约日期" width="110" />
        <el-table-column label="时间段" width="140">
          <template #default="{ row }">
            {{ row.startTime }} - {{ row.endTime }}
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="时长(小时)" width="90" align="center" />
        <el-table-column prop="totalPrice" label="总金额(元)" width="100" align="center">
          <template #default="{ row }">
            ¥{{ row.totalPrice }}
          </template>
        </el-table-column>
        <el-table-column prop="statusText" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.statusColor" size="small">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <!-- 签到按钮：仅已支付状态可签到 -->
            <el-button
              v-if="row.status === 1"
              type="success"
              size="small"
              @click="checkIn(row)"
              :loading="checkInLoading === row.id"
            >
              签到
            </el-button>
            <span v-else-if="row.status === 2" class="checked-in-text">已签到</span>
            <!-- 删除按钮 -->
            <el-button
              type="danger"
              size="small"
              plain
              @click="deleteReservation(row)"
              :loading="deleteLoading === row.id"
            >
              删除
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
          :total="filteredReservations.length"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Refresh, Search, RefreshRight } from '@element-plus/icons-vue'
import axios from 'axios'

const router = useRouter()

const loading = ref(false)
const allReservations = ref([])
const checkInLoading = ref(null)
const deleteLoading = ref(null)

// 分页
const currentPage = ref(1)
const pageSize = ref(10)

// 状态筛选 (null 表示全部)
const statusFilter = ref(null)

// 搜索关键词
const searchKeyword = ref('')

// 状态映射
const statusMap = {
  0: { text: '待支付', color: 'warning' },
  1: { text: '已支付', color: 'success' },
  2: { text: '已使用', color: 'info' },
  3: { text: '已取消', color: 'danger' },
  4: { text: '已过期', color: 'info' }
}

// 获取状态文本
const getStatusText = (status) => {
  return statusMap[status]?.text || '未知'
}

// 统计各状态数量
const statusCounts = computed(() => {
  const counts = { 0: 0, 1: 0, 2: 0, 3: 0, 4: 0 }
  allReservations.value.forEach(r => {
    if (counts[r.status] !== undefined) counts[r.status]++
  })
  return counts
})

// 总订单数
const totalCount = computed(() => allReservations.value.length)

// 筛选后的预约列表（状态筛选 + 搜索）
const filteredReservations = computed(() => {
  let list = [...allReservations.value]

  // 状态筛选
  if (statusFilter.value !== null) {
    list = list.filter(r => r.status === statusFilter.value)
  }

  // 搜索筛选
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.toLowerCase()
    list = list.filter(r =>
      (r.userName && r.userName.toLowerCase().includes(keyword)) ||
      (r.userPhone && r.userPhone.includes(keyword)) ||
      (r.facilityName && r.facilityName.toLowerCase().includes(keyword)) ||
      (r.orderNo && r.orderNo.toLowerCase().includes(keyword))
    )
  }

  return list
})

// 分页数据
const paginatedReservations = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredReservations.value.slice(start, start + pageSize.value)
})

// 设置状态筛选
const setStatusFilter = (status) => {
  statusFilter.value = status
  currentPage.value = 1
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
}

const resetSearch = () => {
  searchKeyword.value = ''
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

// 加载预约列表
const loadReservations = async () => {
  loading.value = true
  try {
    // 调用管理端接口获取所有预约（需要后端支持）
    const res = await axios.get('http://localhost:8080/api/reservation/admin/list', {
      withCredentials: true
    })
    if (res.data.code === 200) {
      allReservations.value = (res.data.data || []).map(item => ({
        ...item,
        status: item.status !== undefined ? item.status : (item.statusText === '已支付' ? 1 : (item.statusText === '已使用' ? 2 : (item.statusText === '已取消' ? 3 : 0))),
        statusText: item.statusText,
        statusColor: item.statusColor
      }))
    } else {
      ElMessage.error(res.data.message || '加载失败')
    }
  } catch (err) {
    console.error('加载预约列表失败:', err)
    ElMessage.error('加载失败，请检查网络')
  } finally {
    loading.value = false
  }
}

// 签到
const checkIn = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认用户「${row.userName}」已在「${row.facilityName}」签到吗？`,
      '签到确认',
      { confirmButtonText: '确认签到', cancelButtonText: '取消', type: 'info' }
    )

    checkInLoading.value = row.id
    const res = await axios.put(`http://localhost:8080/api/reservation/checkin/${row.id}`, null, {
      withCredentials: true
    })

    if (res.data.code === 200) {
      ElMessage.success('签到成功')
      await loadReservations()
    } else {
      ElMessage.error(res.data.message || '签到失败')
    }
  } catch (err) {
    if (err !== 'cancel') {
      console.error('签到失败:', err)
      ElMessage.error('签到失败，请稍后重试')
    }
  } finally {
    checkInLoading.value = null
  }
}

// 删除预约
const deleteReservation = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认删除「${row.facilityName}」的预约订单吗？删除后不可恢复。`,
      '删除确认',
      { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning' }
    )

    deleteLoading.value = row.id
    const res = await axios.delete(`http://localhost:8080/api/reservation/admin/${row.id}`, {
      withCredentials: true
    })

    if (res.data.code === 200) {
      ElMessage.success('删除成功')
      await loadReservations()
    } else {
      ElMessage.error(res.data.message || '删除失败')
    }
  } catch (err) {
    if (err !== 'cancel') {
      console.error('删除失败:', err)
      ElMessage.error('删除失败，请稍后重试')
    }
  } finally {
    deleteLoading.value = null
  }
}

const goBack = () => {
  router.push('/')
}

onMounted(() => {
  loadReservations()
})
</script>

<style scoped>
.reservation-management {
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
  flex-wrap: wrap;
}

.stat-card {
  flex: 1;
  min-width: 140px;
  background: white;
  border-radius: 16px;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.2s;
  cursor: pointer;
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  font-size: 32px;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
}
.stat-icon.total { background: linear-gradient(135deg, #dbeafe, #bfdbfe); }
.stat-icon.paid { background: linear-gradient(135deg, #dcfce7, #bbf7d0); }
.stat-icon.used { background: linear-gradient(135deg, #e0e7ff, #c7d2fe); }
.stat-icon.cancelled { background: linear-gradient(135deg, #ffe4e4, #fecaca); }

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
  margin-top: 2px;
}

/* 筛选提示 */
.filter-tip {
  margin-bottom: 16px;
}

/* 搜索卡片 */
.search-card {
  border-radius: 16px;
  margin-bottom: 24px;
}
.search-bar {
  display: flex;
  gap: 12px;
  align-items: center;
}

/* 列表卡片 */
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
.reservation-table {
  width: 100%;
}
:deep(.el-table th) {
  background-color: #f8fafc;
  color: #1e293b;
  font-weight: 600;
}
.checked-in-text {
  color: #67c23a;
  font-size: 12px;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
