<template>
  <div class="facility-statistics">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-button type="default" @click="goBack" :icon="ArrowLeft" class="back-btn">
          返回设施管理
        </el-button>
        <div class="header-title">
          <h2>📊 设施预约趋势分析</h2>
          <p>查看预约订单变化趋势及各场馆利用率</p>
        </div>
      </div>
      <div class="header-right">
        <el-button @click="loadData" :loading="loading" :icon="Refresh">
          刷新
        </el-button>
      </div>
    </div>

    <!-- 时间范围选择 -->
    <el-card class="filter-card" shadow="hover">
      <div class="filter-bar">
        <el-radio-group v-model="dateRange" @change="onDateRangeChange">
          <el-radio-button label="week">本周</el-radio-button>
          <el-radio-button label="month">本月</el-radio-button>
          <el-radio-button label="year">本年</el-radio-button>
          <el-radio-button label="custom">自定义</el-radio-button>
        </el-radio-group>

        <div v-if="dateRange === 'custom'" class="custom-date">
          <el-date-picker
            v-model="customStartDate"
            type="date"
            placeholder="开始日期"
            value-format="YYYY-MM-DD"
          />
          <span>至</span>
          <el-date-picker
            v-model="customEndDate"
            type="date"
            placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
          <el-button type="primary" size="small" @click="loadData">查询</el-button>
        </div>
      </div>
    </el-card>

    <el-row :gutter="24">
      <!-- 左侧：预约趋势图表 -->
      <el-col :xs="24" :md="14">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>📈 预约订单趋势</span>
              <el-radio-group v-model="chartType" size="small" @change="updateTrendChart">
                <el-radio-button label="line">折线图</el-radio-button>
                <el-radio-button label="bar">柱状图</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>

        <!-- 时段利用率 -->
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>⏰ 各时段设施利用率</span>
            </div>
          </template>
          <div ref="hourChartRef" class="chart-container"></div>
        </el-card>
      </el-col>

      <!-- 右侧：场馆利用率统计 -->
      <el-col :xs="24" :md="10">
        <el-card class="stats-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>🏟️ 场馆利用率排行</span>
            </div>
          </template>
          <div class="facility-ranking">
            <div
              v-for="(item, index) in facilityRanking"
              :key="item.facilityId"
              class="ranking-item"
            >
              <div class="ranking-number" :class="{ 'top1': index === 0, 'top2': index === 1, 'top3': index === 2 }">
                {{ index + 1 }}
              </div>
              <div class="ranking-info">
                <div class="ranking-name">{{ item.facilityName }}</div>
                <div class="ranking-stats">
                  <span>预约次数: {{ item.reservationCount }} 次</span>
                  <span>使用率: {{ item.utilizationRate }}%</span>
                </div>
              </div>
              <div class="ranking-bar">
                <el-progress
                  :percentage="item.utilizationRate"
                  :stroke-width="8"
                  :color="getProgressColor(item.utilizationRate)"
                />
              </div>
            </div>
          </div>
          <el-empty v-if="facilityRanking.length === 0" description="暂无数据" />
        </el-card>


      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Refresh } from '@element-plus/icons-vue'
import axios from 'axios'
import * as echarts from 'echarts'

const router = useRouter()

// 数据
const loading = ref(false)
const dateRange = ref('week')
const customStartDate = ref('')
const customEndDate = ref('')
const chartType = ref('line')

// 图表实例
const trendChartRef = ref(null)
let trendChart = null
const hourChartRef = ref(null)
let hourChart = null

// 统计数据
const trendData = ref([])
const hourUtilization = ref([])
const facilityRanking = ref([])


// 获取日期范围
const getDateRange = () => {
  const now = new Date()
  let startDate = ''
  let endDate = ''
  const type = dateRange.value

  if (type === 'week') {
    const weekStart = new Date(now)
    weekStart.setDate(now.getDate() - now.getDay())
    startDate = weekStart.toISOString().slice(0, 10)
    endDate = now.toISOString().slice(0, 10)
  } else if (type === 'month') {
    startDate = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-01`
    endDate = now.toISOString().slice(0, 10)
  } else if (type === 'year') {
    startDate = `${now.getFullYear()}-01-01`
    endDate = now.toISOString().slice(0, 10)
  } else if (type === 'custom') {
    startDate = customStartDate.value
    endDate = customEndDate.value
  }

  return { startDate, endDate }
}

// 加载数据
const loadData = async () => {
  const { startDate, endDate } = getDateRange()
  if (!startDate || !endDate) {
    ElMessage.warning('请选择完整的日期范围')
    return
  }

  loading.value = true
  try {
    // 获取趋势数据
    const trendRes = await axios.get('http://localhost:8080/api/reservation/statistics/trend', {
      params: { startDate, endDate },
      withCredentials: true
    })
    if (trendRes.data.code === 200) {
      trendData.value = trendRes.data.data || []
      updateTrendChart()
    }

    // 获取时段利用率
    const hourRes = await axios.get('http://localhost:8080/api/reservation/statistics/hour-utilization', {
      params: { startDate, endDate },
      withCredentials: true
    })
    if (hourRes.data.code === 200) {
      hourUtilization.value = hourRes.data.data || []
      updateHourChart()
    }

    // 获取场馆排行
    const rankRes = await axios.get('http://localhost:8080/api/reservation/statistics/facility-ranking', {
      params: { startDate, endDate },
      withCredentials: true
    })
    if (rankRes.data.code === 200) {
      facilityRanking.value = rankRes.data.data || []
    }


  } catch (err) {
    console.error('加载统计数据失败:', err)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

// 更新趋势图表
const updateTrendChart = () => {
  if (!trendChartRef.value) return
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }

  const dates = trendData.value.map(item => item.date)
  const counts = trendData.value.map(item => item.count)

  const option = {
    title: {
      text: '每日预约订单趋势',
      left: 'center',
      top: 0,
      textStyle: { fontSize: 14, fontWeight: 'normal' }
    },
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        if (!params || params.length === 0) return ''
        return `${params[0].axisValue}<br/>预约订单数: ${params[0].value} 次`
      }
    },
    xAxis: {
      type: 'category',
      data: dates,
      name: '日期',
      axisLabel: { rotate: 30, interval: 0 }
    },
    yAxis: {
      type: 'value',
      name: '预约数量'
    },
    series: [{
      name: '预约订单数',
      type: chartType.value,
      data: counts,
      smooth: true,
      lineStyle: { width: 3, color: '#f97316' },
      areaStyle: chartType.value === 'line' ? { opacity: 0.2, color: '#f97316' } : undefined,
      itemStyle: { color: '#f97316', borderRadius: [8, 8, 0, 0] }
    }],
    grid: { containLabel: true, top: 50, bottom: 20 }
  }
  trendChart.setOption(option, true)
}

// 更新时段图表（利用率）
const updateHourChart = () => {
  if (!hourChartRef.value) return
  if (!hourChart) {
    hourChart = echarts.init(hourChartRef.value)
  }

  const hours = hourUtilization.value.map(item => `${item.hour}:00`)
  const rates = hourUtilization.value.map(item => item.utilization_rate || 0)

  const option = {
    title: {
      text: '各时段设施利用率',
      left: 'center',
      top: 0,
      textStyle: { fontSize: 14, fontWeight: 'normal' }
    },
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        if (!params || params.length === 0) return ''
        const val = params[0].value
        return `${params[0].axisValue}<br/>利用率: ${val}%`
      }
    },
    xAxis: {
      type: 'category',
      data: hours,
      name: '时间段',
      axisLabel: { rotate: 30 }
    },
    yAxis: {
      type: 'value',
      name: '利用率 (%)',
      min: 0,
      max: 100,
      axisLabel: { formatter: '{value}%' }
    },
    series: [{
      type: 'bar',
      data: rates,
      itemStyle: {
        color: (params) => {
          const rate = params.value
          if (rate >= 70) return '#f56c6c'
          if (rate >= 40) return '#e6a23c'
          return '#67c23a'
        },
        borderRadius: [8, 8, 0, 0]
      },
      label: {
        show: true,
        position: 'top',
        formatter: '{c}%'
      }
    }],
    grid: { containLabel: true, top: 50, bottom: 20 }
  }
  hourChart.setOption(option, true)
}

// 获取进度条颜色
const getProgressColor = (rate) => {
  if (rate >= 70) return '#f56c6c'
  if (rate >= 40) return '#e6a23c'
  return '#67c23a'
}

// 日期范围变化
const onDateRangeChange = () => {
  if (dateRange.value !== 'custom') {
    loadData()
  }
}

// 返回
const goBack = () => {
  router.push('/facility-management')
}

// 窗口大小变化处理
const handleResize = () => {
  if (trendChart) trendChart.resize()
  if (hourChart) hourChart.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (trendChart) trendChart.dispose()
  if (hourChart) hourChart.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.facility-statistics {
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #eef2f6 100%);
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  background: white;
  border-radius: 48px;
  padding: 8px 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.back-btn {
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

.filter-card {
  border-radius: 20px;
  margin-bottom: 24px;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.custom-date {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chart-card, .stats-card {
  border-radius: 20px;
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.chart-container {
  width: 100%;
  height: 350px;
}

.facility-ranking {
  max-height: 400px;
  overflow-y: auto;
}

.ranking-item {
  padding: 12px 0;
  border-bottom: 1px solid #e2e8f0;
}

.ranking-number {
  display: inline-block;
  width: 28px;
  height: 28px;
  line-height: 28px;
  text-align: center;
  background: #f1f5f9;
  border-radius: 50%;
  margin-right: 12px;
  font-size: 12px;
  font-weight: 600;
}

.ranking-number.top1 {
  background: #fef3c7;
  color: #d97706;
}

.ranking-number.top2 {
  background: #e5e7eb;
  color: #6b7280;
}

.ranking-number.top3 {
  background: #fef3c7;
  color: #b45309;
}

.ranking-info {
  display: inline-block;
  width: calc(100% - 40px);
}

.ranking-name {
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 4px;
}

.ranking-stats {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #64748b;
  margin-bottom: 8px;
}

.idle-times {
  max-height: 300px;
  overflow-y: auto;
}

.idle-item {
  padding: 12px;
  border-bottom: 1px solid #e2e8f0;
}

.idle-name {
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 8px;
}

.idle-periods {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .facility-statistics {
    padding: 16px;
  }

  .filter-bar {
    flex-direction: column;
    align-items: flex-start;
  }

  .custom-date {
    flex-wrap: wrap;
  }

  .chart-container {
    height: 250px;
  }
}
</style>
