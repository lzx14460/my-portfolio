<template>
  <div class="sport-record">
    <!-- 页面头部 -->
    <div class="page-header">
      <el-button type="default" @click="goBack" :icon="ArrowLeft">返回首页</el-button>
      <h2>🏃 运动记录与健康数据</h2>
      <div></div>
    </div>

    <el-row :gutter="24">
      <!-- 左侧：运动记录 -->
      <el-col :xs="24" :md="12">
        <el-card class="record-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>📋 运动记录</span>
              <div>
                <el-button type="primary" size="small" @click="openAddSportDialog" :icon="Plus">
                  添加记录
                </el-button>
                <el-tag type="success" size="large" style="margin-left: 10px;">
                  总运动时长: {{ formatDuration(totalSportDuration) }}
                </el-tag>
              </div>
            </div>
          </template>

          <div class="sport-tip" v-if="totalSportDuration > 0">
            <el-alert
              :title="`您已经运动了 ${formatDuration(totalSportDuration)}，继续加油！💪`"
              type="success"
              :closable="false"
              show-icon
            />
          </div>
          <div class="sport-tip" v-else>
            <el-alert
              title="暂无运动记录，点击上方按钮添加运动记录吧！🏀"
              type="info"
              :closable="false"
              show-icon
            />
          </div>

          <el-timeline>
            <el-timeline-item
              v-for="record in sportRecords"
              :key="record.id"
              :timestamp="record.sportDate"
              placement="top"
              :type="record.duration >= 60 ? 'success' : 'primary'"
            >
              <el-card shadow="hover" class="record-item">
                <div class="record-info">
                  <div class="record-title">
                    <strong>{{ record.sportTypeText }}</strong>
                    <el-tag :type="record.duration >= 60 ? 'success' : 'info'" size="small">
                      {{ record.durationText }}
                    </el-tag>
                  </div>
                  <div class="record-time" v-if="record.facilityName">
                    <el-icon><Location /></el-icon>
                    {{ record.facilityName }}
                  </div>
                  <!-- 卡路里 -->
                  <div class="record-time" v-if="record.calories">
                    <span style="margin-right: 4px;">🔥</span>
                    消耗 {{ record.calories }} 千卡
                  </div>
                  <!-- 心率 -->
                  <div class="record-time" v-if="record.heartRateAvg">
                    <span style="margin-right: 4px;">💓</span>
                    平均心率 {{ record.heartRateAvg }} 次/分
                  </div>
                  <div class="record-date">
                    <el-icon><Calendar /></el-icon>
                    {{ record.sportDate }}
                  </div>
                  <!-- 备注 -->
                  <div class="record-remark" v-if="record.remark">
                    <span style="margin-right: 4px;">💬</span>
                    {{ record.remark }}
                  </div>
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>

          <el-empty v-if="sportRecords.length === 0" description="暂无运动记录" />
        </el-card>
      </el-col>

      <!-- 右侧：健康数据 -->
      <el-col :xs="24" :md="12">
        <el-card class="health-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>📊 健康数据</span>
            </div>
          </template>

          <!-- 身高体重输入（带日期） -->
          <div class="health-input">
            <el-form :model="healthForm" label-width="80px">
              <el-form-item label="记录日期">
                <el-date-picker
                  v-model="healthForm.recordDate"
                  type="date"
                  placeholder="选择记录日期"
                  value-format="YYYY-MM-DD"
                  :disabled-date="disabledFutureDate"
                  style="width: 100%"
                />
              </el-form-item>
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="身高(cm)">
                    <el-input-number
                      v-model="healthForm.height"
                      :min="50"
                      :max="250"
                      :precision="1"
                      placeholder="输入身高"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="体重(kg)">
                    <el-input-number
                      v-model="healthForm.weight"
                      :min="10"
                      :max="300"
                      :precision="1"
                      placeholder="输入体重"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item>
                <el-button type="primary" @click="saveHealthData" :loading="savingHealth" style="width: 100%">
                  保存记录
                </el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- BMI 显示 -->
          <div class="bmi-display" v-if="currentBMI">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="当前身高">{{ currentHeight }} cm</el-descriptions-item>
              <el-descriptions-item label="当前体重">{{ currentWeight }} kg</el-descriptions-item>
              <el-descriptions-item label="BMI 值" :span="2">
                <span class="bmi-value" :class="getBMIClass(currentBMI)">
                  {{ currentBMI.toFixed(1) }}
                </span>
                <span class="bmi-status">（{{ getBMIStatus(currentBMI) }}）</span>
              </el-descriptions-item>
            </el-descriptions>
          </div>

          <!-- 图表类型切换 -->
          <div class="chart-tabs">
            <el-radio-group v-model="chartType" size="large" @change="updateChart">
              <el-radio-button label="weight">体重</el-radio-button>
              <el-radio-button label="height">身高</el-radio-button>
              <el-radio-button label="bmi">BMI</el-radio-button>
            </el-radio-group>
          </div>

          <!-- ECharts 折线图 -->
          <div ref="chartRef" class="chart-container"></div>

          <!-- 生成健康报告按钮 -->
          <div class="report-btn-container">
            <el-button
              type="primary"
              size="large"
              @click="generateHealthReport"
              :loading="reportLoading"
              class="report-btn"
            >
              <el-icon><Document /></el-icon>
              生成健康报告
            </el-button>
          </div>

          <!-- 健康报告弹窗 -->
          <el-dialog v-model="reportDialogVisible" title="📋 健康评估报告" width="600px" class="report-dialog">
            <div class="report-content" v-if="healthReport">
              <div class="report-section">
                <h4>📊 数据概况</h4>
                <p>记录天数: {{ healthReport.dataDays }} 天</p>
                <p>身高范围: {{ healthReport.heightRange }}</p>
                <p>体重范围: {{ healthReport.weightRange }}</p>
                <p>BMI 范围: {{ healthReport.bmiRange }}</p>
              </div>
              <div class="report-section">
                <h4>🏃 运动概况</h4>
                <p>总运动时长: {{ formatDuration(totalSportDuration) }}</p>
                <p>运动次数: {{ sportRecords.length }} 次</p>
              </div>
              <div class="report-section">
                <h4>💡 AI 健康建议</h4>
                <div class="ai-advice">{{ healthReport.aiAdvice }}</div>
              </div>
            </div>
            <div v-else class="report-loading">
              <el-icon class="is-loading"><Loading /></el-icon>
              <span>正在生成报告...</span>
            </div>
            <template #footer>
              <el-button @click="reportDialogVisible = false">关闭</el-button>
            </template>
          </el-dialog>
        </el-card>
      </el-col>
    </el-row>

    <!-- 添加运动记录对话框 -->
    <el-dialog v-model="addSportDialogVisible" title="🏃 添加运动记录" width="500px">
      <el-form :model="sportForm" :rules="sportRules" ref="sportFormRef" label-width="100px">
        <el-form-item label="运动类型" prop="sportType">
          <el-select v-model="sportForm.sportType" placeholder="请选择运动类型" style="width: 100%">
            <el-option label="篮球" :value="1" />
            <el-option label="足球" :value="2" />
            <el-option label="羽毛球" :value="3" />
            <el-option label="跑步" :value="4" />
            <el-option label="健身" :value="5" />
            <el-option label="游泳" :value="6" />
          </el-select>
        </el-form-item>
        <el-form-item label="运动日期" prop="sportDate">
          <el-date-picker
            v-model="sportForm.sportDate"
            type="date"
            placeholder="选择运动日期"
            value-format="YYYY-MM-DD"
            :disabled-date="disabledFutureDate"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="运动时长" prop="duration">
          <el-input-number
            v-model="sportForm.duration"
            :min="1"
            :max="480"
            placeholder="输入时长"
            style="width: 100%"
          />
          <span style="margin-left: 8px;">分钟</span>
        </el-form-item>
        <el-form-item label="消耗卡路里">
          <el-input-number
            v-model="sportForm.calories"
            :min="0"
            :max="5000"
            placeholder="自动估算"
            style="width: 100%"
          />
          <span style="margin-left: 8px;">千卡</span>
        </el-form-item>
        <el-form-item label="平均心率">
          <el-input-number
            v-model="sportForm.heartRateAvg"
            :min="40"
            :max="220"
            placeholder="选填"
            style="width: 100%"
          />
          <span style="margin-left: 8px;">次/分</span>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="sportForm.remark" type="textarea" rows="2" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addSportDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAddSport" :loading="addSportLoading">确认添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft,  Calendar, Document, Loading, Plus, Location } from '@element-plus/icons-vue'
import axios from 'axios'
import * as echarts from 'echarts'

const router = useRouter()

// 数据
const sportRecords = ref([])
const healthRecords = ref([])
const savingHealth = ref(false)
const reportLoading = ref(false)
const reportDialogVisible = ref(false)
const healthReport = ref(null)

// 手动添加运动记录
const addSportDialogVisible = ref(false)
const addSportLoading = ref(false)
const sportFormRef = ref(null)
const sportForm = ref({
  sportType: null,
  sportDate: new Date().toISOString().slice(0, 10),
  duration: null,
  calories: null,
  heartRateAvg: null,
  remark: ''
})

// 健康表单
const healthForm = ref({
  recordDate: new Date().toISOString().slice(0, 10),
  height: null,
  weight: null
})

// 图表相关
const chartRef = ref(null)
let chartInstance = null
const chartType = ref('weight')

// 表单验证规则
const sportRules = {
  sportType: [{ required: true, message: '请选择运动类型', trigger: 'change' }],
  sportDate: [{ required: true, message: '请选择运动日期', trigger: 'change' }],
  duration: [{ required: true, message: '请输入运动时长', trigger: 'blur' }]
}

// 禁止选择未来日期
const disabledFutureDate = (time) => {
  return time.getTime() > Date.now()
}

// 计算总运动时长（分钟）
const totalSportDuration = computed(() => {
  let totalMinutes = 0
  sportRecords.value.forEach(record => {
    if (record.duration) {
      totalMinutes += record.duration
    }
  })
  return totalMinutes
})

// 格式化时长显示
const formatDuration = (minutes) => {
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (hours === 0) return `${mins}分钟`
  if (mins === 0) return `${hours}小时`
  return `${hours}小时${mins}分钟`
}

// 当前最新的身高体重
const currentHeight = computed(() => {
  if (healthRecords.value.length === 0) return null
  return healthRecords.value[healthRecords.value.length - 1].height
})

const currentWeight = computed(() => {
  if (healthRecords.value.length === 0) return null
  return healthRecords.value[healthRecords.value.length - 1].weight
})

const currentBMI = computed(() => {
  if (currentHeight.value && currentWeight.value) {
    const heightM = currentHeight.value / 100
    return currentWeight.value / (heightM * heightM)
  }
  return null
})

// 获取 BMI 等级
const getBMIStatus = (bmi) => {
  if (bmi < 18.5) return '偏瘦'
  if (bmi < 24) return '正常'
  if (bmi < 28) return '超重'
  return '肥胖'
}

const getBMIClass = (bmi) => {
  if (bmi < 18.5) return 'bmi-thin'
  if (bmi < 24) return 'bmi-normal'
  if (bmi < 28) return 'bmi-overweight'
  return 'bmi-obese'
}

// 加载运动记录（从 sport_record 表）
const loadSportRecords = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/sport-record/my-list', {
      withCredentials: true
    })
    if (res.data.code === 200) {
      sportRecords.value = res.data.data || []
    }
  } catch (err) {
    console.error('加载运动记录失败:', err)
  }
}

// 加载健康数据
const loadHealthRecords = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/health/records', {
      withCredentials: true
    })
    if (res.data.code === 200) {
      healthRecords.value = (res.data.data || []).sort((a, b) => new Date(a.recordDate) - new Date(b.recordDate))
      if (healthRecords.value.length > 0) {
        const latest = healthRecords.value[healthRecords.value.length - 1]
        healthForm.value.height = latest.height
        healthForm.value.weight = latest.weight
      }
      updateChart()
    }
  } catch (err) {
    console.error('加载健康数据失败:', err)
  }
}

// 保存健康数据
const saveHealthData = async () => {
  if (!healthForm.value.height || !healthForm.value.weight) {
    ElMessage.warning('请填写完整的身高和体重')
    return
  }
  if (!healthForm.value.recordDate) {
    ElMessage.warning('请选择记录日期')
    return
  }

  savingHealth.value = true
  try {
    const res = await axios.post('http://localhost:8080/api/health/record', {
      recordDate: healthForm.value.recordDate,
      height: healthForm.value.height,
      weight: healthForm.value.weight
    }, { withCredentials: true })

    if (res.data.code === 200) {
      ElMessage.success('健康数据保存成功')
      await loadHealthRecords()
    } else {
      ElMessage.error(res.data.message || '保存失败')
    }
  } catch (err) {
    console.error('保存健康数据失败:', err)
    ElMessage.error('保存失败，请稍后重试')
  } finally {
    savingHealth.value = false
  }
}

// 打开添加运动记录对话框
const openAddSportDialog = () => {
  sportForm.value = {
    sportType: null,
    sportDate: new Date().toISOString().slice(0, 10),
    duration: null,
    calories: null,
    heartRateAvg: null,
    remark: ''
  }
  addSportDialogVisible.value = true
}

// 提交添加运动记录
const submitAddSport = async () => {
  if (!sportFormRef.value) return
  await sportFormRef.value.validate(async (valid) => {
    if (!valid) return

    addSportLoading.value = true
    try {
      const res = await axios.post('http://localhost:8080/api/sport-record/add', sportForm.value, {
        withCredentials: true
      })
      if (res.data.code === 200) {
        ElMessage.success('运动记录添加成功')
        addSportDialogVisible.value = false
        loadSportRecords()
      } else {
        ElMessage.error(res.data.message || '添加失败')
      }
    } catch (err) {
      console.error('添加运动记录失败:', err)
      ElMessage.error('添加失败，请稍后重试')
    } finally {
      addSportLoading.value = false
    }
  })
}

// 更新图表
const updateChart = () => {
  if (!chartRef.value) return
  if (healthRecords.value.length === 0) {
    if (chartInstance) {
      chartInstance.clear()
    }
    return
  }

  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }

  const xAxisData = healthRecords.value.map(r => r.recordDate)
  let seriesData = []
  let yAxisName = ''
  let seriesName = ''

  if (chartType.value === 'weight') {
    seriesData = healthRecords.value.map(r => r.weight)
    yAxisName = '体重 (kg)'
    seriesName = '体重变化'
  } else if (chartType.value === 'height') {
    seriesData = healthRecords.value.map(r => r.height)
    yAxisName = '身高 (cm)'
    seriesName = '身高变化'
  } else {
    seriesData = healthRecords.value.map(r => {
      const heightM = r.height / 100
      return r.weight / (heightM * heightM)
    })
    yAxisName = 'BMI 值'
    seriesName = 'BMI 变化'
  }

  // 计算Y轴范围
  const yRange = (() => {
    if (!seriesData || seriesData.length === 0) return { min: null, max: null }

    const min = Math.min(...seriesData)
    const max = Math.max(...seriesData)
    const range = max - min

    if (range === 0) {
      if (chartType.value === 'bmi') {
        return { min: min - 1, max: max + 1 }
      }
      return { min: min - 2, max: max + 2 }
    }

    let paddingRate = 0.15
    if (chartType.value === 'height') {
      paddingRate = 0.2
    } else if (chartType.value === 'bmi') {
      paddingRate = 0.25
    }

    const relativeChange = range / max
    if (relativeChange < 0.05) {
      const expandRate = 0.15
      return {
        min: Math.floor((min - max * expandRate) * 10) / 10,
        max: Math.ceil((max + max * expandRate) * 10) / 10
      }
    }

    return {
      min: Math.floor((min - range * paddingRate) * 10) / 10,
      max: Math.ceil((max + range * paddingRate) * 10) / 10
    }
  })()

  const option = {
    title: {
      text: seriesName,
      left: 'center',
      top: 0,
      textStyle: { fontSize: 14, fontWeight: 'normal' }
    },
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        if (!params || params.length === 0) return ''
        const val = params[0].value
        if (chartType.value === 'bmi') {
          return `${params[0].axisValue}<br/>${seriesName}: ${val.toFixed(2)}`
        }
        return `${params[0].axisValue}<br/>${seriesName}: ${val.toFixed(1)} ${yAxisName === '体重 (kg)' ? 'kg' : 'cm'}`
      }
    },
    xAxis: {
      type: 'category',
      data: xAxisData,
      name: '日期',
      axisLabel: {
        rotate: 30,
        interval: 0
      }
    },
    yAxis: {
      type: 'value',
      name: yAxisName,
      min: yRange.min,
      max: yRange.max,
      splitNumber: 8,
      axisLabel: {
        formatter: (value) => {
          if (chartType.value === 'bmi') {
            return value.toFixed(1)
          }
          return value.toFixed(1)
        }
      }
    },
    series: [{
      data: seriesData,
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: { width: 3, color: '#f97316' },
      areaStyle: { opacity: 0.2, color: '#f97316' },
      itemStyle: { color: '#f97316' },
      label: {
        show: true,
        position: 'top',
        formatter: (params) => {
          if (chartType.value === 'bmi') {
            return params.value.toFixed(1)
          }
          return params.value.toFixed(1)
        },
        fontSize: 11,
        offset: [0, -8]
      }
    }],
    grid: {
      containLabel: true,
      top: 60,
      bottom: 30,
      left: 60,
      right: 30
    }
  }

  chartInstance.setOption(option, true)
}

// 监听图表类型变化
watch(chartType, () => {
  updateChart()
})

// 监听健康数据变化
watch(healthRecords, () => {
  updateChart()
}, { deep: true })

// 生成健康报告
const generateHealthReport = async () => {
  if (healthRecords.value.length === 0) {
    ElMessage.warning('暂无健康数据，请先录入身高体重')
    return
  }

  reportLoading.value = true
  reportDialogVisible.value = true
  healthReport.value = null

  try {
    const heights = healthRecords.value.map(r => r.height)
    const weights = healthRecords.value.map(r => r.weight)
    const bmis = healthRecords.value.map(r => {
      const heightM = r.height / 100
      return r.weight / (heightM * heightM)
    })

    const dataSummary = {
      dataDays: healthRecords.value.length,
      heightRange: heights.length > 0 ? `${Math.min(...heights)} - ${Math.max(...heights)} cm` : '暂无数据',
      weightRange: weights.length > 0 ? `${Math.min(...weights)} - ${Math.max(...weights)} kg` : '暂无数据',
      bmiRange: bmis.length > 0 ? `${Math.min(...bmis).toFixed(1)} - ${Math.max(...bmis).toFixed(1)}` : '暂无数据',
      currentBMI: currentBMI.value ? currentBMI.value.toFixed(1) : '暂无',
      bmiStatus: currentBMI.value ? getBMIStatus(currentBMI.value) : '暂无',
      sportCount: sportRecords.value.length,
      sportDuration: totalSportDuration.value
    }

    const prompt = `请根据以下用户健康数据，生成一份简短的健康评估和建议报告（200字以内）：

用户健康数据：
- 共记录${dataSummary.dataDays}天健康数据
- 身高范围：${dataSummary.heightRange}
- 体重范围：${dataSummary.weightRange}
- BMI范围：${dataSummary.bmiRange}
- 当前BMI：${dataSummary.currentBMI}（${dataSummary.bmiStatus}）
- 运动次数：${dataSummary.sportCount}次
- 总运动时长：${formatDuration(dataSummary.sportDuration)}

请给出：
1. 简短的健康状况评估
2. 针对性的运动建议
3. 饮食或生活习惯建议`

    const res = await axios.post('http://localhost:8080/api/ai/health-report', {
      prompt: prompt
    }, { withCredentials: true })

    if (res.data.code === 200) {
      healthReport.value = {
        ...dataSummary,
        aiAdvice: res.data.data
      }
    } else {
      healthReport.value = dataSummary
      ElMessage.warning('AI 服务暂时不可用，已生成基础报告')
    }
  } catch (err) {
    console.error('生成报告失败:', err)
    const heights = healthRecords.value.map(r => r.height)
    const weights = healthRecords.value.map(r => r.weight)
    const bmis = healthRecords.value.map(r => {
      const heightM = r.height / 100
      return r.weight / (heightM * heightM)
    })

    healthReport.value = {
      dataDays: healthRecords.value.length,
      heightRange: heights.length > 0 ? `${Math.min(...heights)} - ${Math.max(...heights)} cm` : '暂无数据',
      weightRange: weights.length > 0 ? `${Math.min(...weights)} - ${Math.max(...weights)} kg` : '暂无数据',
      bmiRange: bmis.length > 0 ? `${Math.min(...bmis).toFixed(1)} - ${Math.max(...bmis).toFixed(1)}` : '暂无数据',
      aiAdvice: '请坚持规律运动，保持健康饮食，定期监测身体数据。如需更详细的建议，请咨询专业医生或运动教练。'
    }
    ElMessage.warning('AI 服务连接失败，已生成基础报告')
  } finally {
    reportLoading.value = false
  }
}

// 监听窗口大小变化
const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}

const goBack = () => {
  router.push('/')
}

onMounted(() => {
  loadSportRecords()
  loadHealthRecords()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.sport-record {
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

.page-header h2 {
  font-size: 1.5rem;
  font-weight: 700;
  background: linear-gradient(135deg, #1e293b 0%, #f97316 100%);
  background-clip: text;
  -webkit-background-clip: text;
  color: transparent;
  margin: 0;
}

.record-card, .health-card {
  border-radius: 20px;
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.sport-tip {
  margin-bottom: 20px;
}

.record-item {
  margin-bottom: 12px;
  border-radius: 12px;
}

.record-info {
  padding: 8px 0;
}

.record-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 16px;
}

.record-time, .record-date, .record-remark {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #64748b;
  margin-top: 6px;
}

.health-input {
  margin-bottom: 24px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 16px;
}

.bmi-display {
  margin-bottom: 24px;
}

.bmi-value {
  font-size: 24px;
  font-weight: 700;
  margin-right: 12px;
}

.bmi-thin { color: #409eff; }
.bmi-normal { color: #67c23a; }
.bmi-overweight { color: #e6a23c; }
.bmi-obese { color: #f56c6c; }

.chart-tabs {
  margin: 20px 0;
  text-align: center;
}

.chart-container {
  width: 100%;
  height: 380px;
  margin: 16px 0;
}

.report-btn-container {
  margin-top: 20px;
  text-align: center;
}

.report-btn {
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 40px;
  font-size: 16px;
  font-weight: 600;
}

.report-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
}

.report-dialog :deep(.el-dialog__body) {
  padding: 20px;
}

.report-content {
  max-height: 500px;
  overflow-y: auto;
}

.report-section {
  margin-bottom: 20px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
}

.report-section h4 {
  margin: 0 0 12px 0;
  color: #1e293b;
  font-size: 16px;
}

.report-section p {
  margin: 6px 0;
  color: #475569;
  font-size: 14px;
}

.ai-advice {
  line-height: 1.8;
  color: #334155;
  white-space: pre-wrap;
}

.report-loading {
  text-align: center;
  padding: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #64748b;
}
</style>
