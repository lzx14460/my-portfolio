<template>
  <div class="reservation-list">
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
          <h2>📋 我的预约</h2>
          <p>查看和管理您的预约订单</p>
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
          <div class="stat-label">总预约次数</div>
        </div>
      </div>
      <div class="stat-card" @click="setStatusFilter(0)">
        <div class="stat-icon pending">⏳</div>
        <div class="stat-info">
          <div class="stat-value">{{ statusCounts[0] || 0 }}</div>
          <div class="stat-label">待支付</div>
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
      <div class="stat-card" @click="setStatusFilter(4)">
        <div class="stat-icon expired">⏰</div>
        <div class="stat-info">
          <div class="stat-value">{{ statusCounts[4] || 0 }}</div>
          <div class="stat-label">已过期</div>
        </div>
      </div>
    </div>

    <!-- 当前筛选状态提示 -->
    <div class="filter-tip" v-if="statusFilter !== null">
      <el-tag closable @close="setStatusFilter(null)">
        当前筛选：{{ getStatusText(statusFilter) }}
      </el-tag>
    </div>

    <!-- 预约列表 -->
    <el-card class="list-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>📋 预约订单</span>
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
        <el-table-column prop="facilityName" label="设施名称" min-width="150" />
        <el-table-column prop="reservationDate" label="预约日期" width="120" />
        <el-table-column label="时间段" width="150">
          <template #default="{ row }">
            {{ row.startTime }} - {{ row.endTime }}
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="时长(小时)" width="100" />
        <el-table-column prop="totalPrice" label="总金额(元)" width="110">
          <template #default="{ row }">
            ¥{{ row.totalPrice }}
          </template>
        </el-table-column>
        <el-table-column prop="statusText" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.statusColor">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
              v-if="canCancel(row)"
              type="danger"
              size="small"
              @click="cancelReservation(row)"
              :loading="cancelLoading === row.id"
            >
              取消预约
            </el-button>
            <el-button
              v-else-if="row.status === 0 || row.status === 1"
              size="small"
              disabled
              plain
            >
              不可取消
            </el-button>
            <span v-else class="disabled-action">-</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[5, 10, 20, 50]"
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
import { ArrowLeft, Refresh } from '@element-plus/icons-vue'
import axios from 'axios'

const router = useRouter()

const loading = ref(false)
const allReservations = ref([])
const cancelLoading = ref(null)

// 分页
const currentPage = ref(1)
const pageSize = ref(10)

// 状态筛选 (null 表示全部)
const statusFilter = ref(null)

// 统计各状态数量
const statusCounts = computed(() => {
  const counts = { 0: 0, 1: 0, 2: 0, 3: 0, 4: 0 }
  allReservations.value.forEach(r => {
    if (counts[r.status] !== undefined) counts[r.status]++
  })
  return counts
})

// 总预约次数
const totalCount = computed(() => allReservations.value.length)

// 获取状态文本
const getStatusText = (status) => {
  const map = { 0: '待支付', 1: '已支付', 2: '已使用', 3: '已取消', 4: '已过期' }
  return map[status] || '未知'
}

// 判断是否可取消（状态为0或1，且距离开始时间大于6小时）
const canCancel = (reservation) => {
  if (reservation.status !== 0 && reservation.status !== 1) return false
  const startDateTime = new Date(`${reservation.reservationDate}T${reservation.startTime}`)
  const now = new Date()
  const diffHours = (startDateTime - now) / (1000 * 60 * 60)
  return diffHours > 6
}

// 加载预约列表
const loadReservations = async () => {
  loading.value = true
  try {
    const res = await axios.get('http://localhost:8080/api/reservation/my-list', {
      withCredentials: true
    })
    if (res.data.code === 200) {
      allReservations.value = res.data.data || []
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

// 根据状态筛选
const filteredReservations = computed(() => {
  if (statusFilter.value === null) return allReservations.value
  return allReservations.value.filter(r => r.status === statusFilter.value)
})

// 分页数据
const paginatedReservations = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredReservations.value.slice(start, end)
})

// 设置状态筛选
const setStatusFilter = (status) => {
  statusFilter.value = status
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

// 取消预约
const cancelReservation = async (row) => {
  // 再次检查可取消性（防止前端状态滞后）
  if (!canCancel(row)) {
    ElMessage.warning('距离预约开始不足6小时，无法取消')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认取消「${row.facilityName}」于 ${row.reservationDate} ${row.startTime} 的预约吗？`,
      '取消预约',
      { confirmButtonText: '确认取消', cancelButtonText: '再想想', type: 'warning' }
    )
  } catch {
    return
  }

  cancelLoading.value = row.id
  try {
    const res = await axios.put(`http://localhost:8080/api/reservation/cancel/${row.id}`, null, {
      withCredentials: true
    })
    if (res.data.code === 200) {
      ElMessage.success('取消成功')
      await loadReservations()
    } else {
      ElMessage.error(res.data.message || '取消失败')
    }
  } catch (err) {
    console.error('取消预约失败:', err)
    ElMessage.error('取消失败，请稍后重试')
  } finally {
    cancelLoading.value = null
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
.reservation-list {
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
  min-width: 120px;
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
.stat-icon.pending { background: linear-gradient(135deg, #fef3c7, #fde68a); }
.stat-icon.paid { background: linear-gradient(135deg, #dcfce7, #bbf7d0); }
.stat-icon.used { background: linear-gradient(135deg, #e0e7ff, #c7d2fe); }
.stat-icon.cancelled { background: linear-gradient(135deg, #ffe4e4, #fecaca); }
.stat-icon.expired { background: linear-gradient(135deg, #f1f5f9, #e2e8f0); }

.stat-info {
  flex: 1;
}
.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
}
.stat-label {
  font-size: 12px;
  color: #64748b;
  margin-top: 2px;
}

/* 筛选提示 */
.filter-tip {
  margin-bottom: 16px;
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
.disabled-action {
  color: #94a3b8;
  font-size: 12px;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
