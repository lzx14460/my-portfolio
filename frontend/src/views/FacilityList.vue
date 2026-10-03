<template>
  <div class="facility-list">
    <!-- 页面头部 -->
    <div class="page-header">
      <el-button type="default" @click="goBack" :icon="ArrowLeft">返回首页</el-button>
      <h2> 体育设施预约</h2>
      <div></div>
    </div>

    <!-- 设施卡片网格 -->
    <div class="facility-grid">
      <el-row :gutter="20">
        <el-col v-for="item in facilityList" :key="item.id" :xs="24" :sm="12" :md="8" :lg="6">
          <el-card class="facility-card" shadow="hover">
            <img :src="item.imageUrl || defaultImage" class="facility-img" />
            <div class="card-info">
              <h3>{{ item.name }}</h3>
              <div class="info-item">
                <el-icon><Location /></el-icon>
                <span>{{ item.location }}</span>
              </div>
              <div class="info-item">
                <el-icon><Money /></el-icon>
                <span>¥{{ item.pricePerHour }}/小时</span>
              </div>
              <div class="info-item">
                <el-icon><Clock /></el-icon>
                <span>{{ item.openTime }} - {{ item.closeTime }}</span>
              </div>
              <div class="info-item">
                <el-icon><Timer /></el-icon>
                <span>最长预约 {{ item.maxDuration || 4 }} 小时</span>
              </div>
              <div class="card-actions">
                <el-button
                  type="primary"
                  size="small"
                  @click="openReserveDialog(item)"
                  :icon="Edit"
                >
                  立即预约
                </el-button>
                <el-button
                  type="info"
                  size="small"
                  plain
                  @click="viewSchedule(item)"
                  :icon="View"
                >
                  查看预约表
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-if="!loading && facilityList.length === 0" description="暂无体育设施" />
    </div>

    <!-- 预约对话框 -->
    <el-dialog v-model="dialogVisible" :title="`预约 ${selectedFacility?.name}`" width="550px">
      <el-form :model="reserveForm" label-width="100px">
        <el-form-item label="预约日期">
          <el-date-picker
            v-model="reserveForm.date"
            type="date"
            placeholder="选择日期"
            :disabled-date="disabledDate"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-time-picker
            v-model="reserveForm.startTime"
            format="HH:mm"
            value-format="HH:mm:ss"
            :step="30"
            placeholder="选择开始时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-time-picker
            v-model="reserveForm.endTime"
            format="HH:mm"
            value-format="HH:mm:ss"
            :step="30"
            placeholder="选择结束时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="预约时长">
          <span>{{ computedDuration }} 小时</span>
        </el-form-item>
        <el-form-item label="总金额">
          <span style="color: #f97316; font-size: 20px; font-weight: bold;">
            ¥{{ computedTotalPrice }}
          </span>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="reserveForm.remark" type="textarea" rows="2" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReservation" :loading="submitting">
          确认预约并支付
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看预约时间表对话框 -->
    <el-dialog
      v-model="scheduleDialogVisible"
      :title="`${selectedFacilityForSchedule?.name} - 预约时间表`"
      width="700px"
    >
      <div class="schedule-filter">
        <el-date-picker
          v-model="scheduleDate"
          type="date"
          placeholder="选择日期"
          value-format="YYYY-MM-DD"
          :disabled-date="disabledPastDate"
          @change="loadFacilitySchedule"
        />
      </div>

      <div class="schedule-list">
        <div class="schedule-header">
          <div class="header-time">时间段</div>
          <div class="header-status">状态</div>
          <div class="header-info">预约信息</div>
        </div>
        <div class="schedule-items">
          <div
            v-for="slot in timeSlots"
            :key="slot.hour"
            class="schedule-item"
            :class="{ 'booked': slot.isBooked, 'available': !slot.isBooked }"
          >
            <div class="item-time">{{ slot.timeRange }}</div>
            <div class="item-status">
              <el-tag :type="slot.isBooked ? 'danger' : 'success'" size="small">
                {{ slot.isBooked ? '已预约' : '空闲' }}
              </el-tag>
            </div>
            <div class="item-info">
              <span v-if="slot.isBooked" class="booked-info">
                <el-icon><Lock /></el-icon>
                该时段已被预约
              </span>
              <span v-else class="available-info">
                <el-icon><Check /></el-icon>
                可预约
              </span>
            </div>
          </div>
        </div>
      </div>

      <div class="schedule-tip" v-if="noReservations">
        <el-alert title="该日期暂无预约记录" type="info" :closable="false" show-icon />
      </div>

      <template #footer>
        <el-button @click="scheduleDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 支付模拟对话框（简化版） -->
    <el-dialog v-model="payDialogVisible" title="支付" width="400px">
      <div class="pay-content">
        <p>订单号：{{ currentOrderNo }}</p>
        <p>应付金额：¥{{ currentAmount }}</p>
        <el-radio-group v-model="payMethod">
          <el-radio label="wechat">微信支付</el-radio>
          <el-radio label="alipay">支付宝</el-radio>
          <el-radio label="campus">校园卡</el-radio>
        </el-radio-group>
      </div>
      <template #footer>
        <el-button @click="payDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmPay">确认支付</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Location, Money, Clock, Timer, Edit, View, Lock, Check } from '@element-plus/icons-vue'
import axios from 'axios'

const router = useRouter()
const loading = ref(false)
const facilityList = ref([])
const dialogVisible = ref(false)
const selectedFacility = ref(null)
const submitting = ref(false)
const payDialogVisible = ref(false)
const currentOrderNo = ref('')
const currentAmount = ref(0)
const payMethod = ref('wechat')

// 预约时间表相关
const scheduleDialogVisible = ref(false)
const selectedFacilityForSchedule = ref(null)
const scheduleDate = ref(new Date().toISOString().slice(0, 10))
const facilitySchedule = ref([])

const defaultImage = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 300 200"%3E%3Crect width="300" height="200" fill="%23f5f7fa"%3E%3C/rect%3E%3Ctext x="50%25" y="50%25" text-anchor="middle" dy=".3em" fill="%23999"%3E🏟️ 暂无图片%3C/text%3E%3C/svg%3E'

const reserveForm = reactive({
  date: '',
  startTime: '',
  endTime: '',
  remark: ''
})

// 计算预约时长（小时）
const computedDuration = computed(() => {
  if (!reserveForm.startTime || !reserveForm.endTime) return 0
  const start = reserveForm.startTime.split(':')
  const end = reserveForm.endTime.split(':')
  const startMinutes = parseInt(start[0]) * 60 + parseInt(start[1])
  const endMinutes = parseInt(end[0]) * 60 + parseInt(end[1])
  if (endMinutes <= startMinutes) return 0
  return (endMinutes - startMinutes) / 60
})

// 计算总价
const computedTotalPrice = computed(() => {
  if (!selectedFacility.value) return 0
  return (computedDuration.value * selectedFacility.value.pricePerHour).toFixed(2)
})

// 禁止选择过去的日期
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7
}

// 禁止选择过去日期（用于预约表）
const disabledPastDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7
}

// 判断是否有预约记录
const noReservations = computed(() => {
  return facilitySchedule.value.length === 0
})

// 生成时间段列表（8:00 - 22:00，按小时分割）
const timeSlots = computed(() => {
  const slots = []
  const openHour = 8
  const closeHour = 22

  for (let hour = openHour; hour < closeHour; hour++) {
    const timeRange = `${hour.toString().padStart(2, '0')}:00 - ${(hour + 1).toString().padStart(2, '0')}:00`

    // 检查该时间段是否有预约
    const booking = facilitySchedule.value.find(r => {
      const startHour = parseInt(r.startTime.split(':')[0])
      const endHour = parseInt(r.endTime.split(':')[0])
      return hour >= startHour && hour < endHour
    })

    slots.push({
      hour: hour,
      timeRange: timeRange,
      isBooked: !!booking
    })
  }
  return slots
})

// 加载设施列表
const loadFacilities = async () => {
  loading.value = true
  try {
    const res = await axios.get('http://localhost:8080/api/facility/list', {
      withCredentials: true
    })
    if (res.data.code === 200) {
      facilityList.value = (res.data.data || []).map(item => {
        if (item.imageUrl && item.imageUrl.startsWith('/') && !item.imageUrl.startsWith('/api/')) {
          item.imageUrl = 'http://localhost:8080/api' + item.imageUrl;
        } else if (item.imageUrl && item.imageUrl.startsWith('/api/')) {
          item.imageUrl = 'http://localhost:8080' + item.imageUrl;
        }
        return item
      })
    } else {
      ElMessage.error(res.data.message || '加载失败')
    }
  } catch (err) {
    console.error('加载设施列表失败:', err)
    ElMessage.error('加载失败，请检查网络')
  } finally {
    loading.value = false
  }
}

// 加载设施预约时间表
const loadFacilitySchedule = async () => {
  if (!selectedFacilityForSchedule.value) return

  try {
    const res = await axios.get('http://localhost:8080/api/reservation/facility', {
      params: {
        facilityId: selectedFacilityForSchedule.value.id,
        date: scheduleDate.value
      },
      withCredentials: true
    })
    if (res.data.code === 200) {
      facilitySchedule.value = res.data.data || []
    } else {
      facilitySchedule.value = []
    }
  } catch (err) {
    console.error('加载预约时间表失败:', err)
    ElMessage.error('加载失败')
    facilitySchedule.value = []
  }
}

// 查看预约时间表
const viewSchedule = async (facility) => {
  selectedFacilityForSchedule.value = facility
  scheduleDate.value = new Date().toISOString().slice(0, 10)
  scheduleDialogVisible.value = true
  await loadFacilitySchedule()
}

// 打开预约对话框
const openReserveDialog = async (facility) => {
  selectedFacility.value = facility
  reserveForm.date = ''
  reserveForm.startTime = ''
  reserveForm.endTime = ''
  reserveForm.remark = ''
  dialogVisible.value = true
}

// 提交预约
const submitReservation = async () => {
  if (!reserveForm.date) {
    ElMessage.warning('请选择预约日期')
    return
  }
  if (!reserveForm.startTime || !reserveForm.endTime) {
    ElMessage.warning('请选择开始和结束时间')
    return
  }
  const duration = computedDuration.value
  if (duration <= 0) {
    ElMessage.warning('结束时间必须晚于开始时间')
    return
  }

  // 检查最长预约时间
  const maxDuration = selectedFacility.value.maxDuration || 4
  if (duration > maxDuration) {
    ElMessage.warning(`该设施最长预约时间为 ${maxDuration} 小时，请重新选择时间`)
    return
  }

  submitting.value = true
  try {
    // 1. 检测时间段是否已被预约
    const checkRes = await axios.post('http://localhost:8080/api/reservation/check', {
      facilityId: selectedFacility.value.id,
      reservationDate: reserveForm.date,
      startTime: reserveForm.startTime,
      endTime: reserveForm.endTime
    }, { withCredentials: true })

    if (checkRes.data.code !== 200) {
      ElMessage.error(checkRes.data.message || '该时间段已被预约，请选择其他时间')
      return
    }

    // 2. 创建预约订单
    const orderRes = await axios.post('http://localhost:8080/api/reservation/create', {
      facilityId: selectedFacility.value.id,
      reservationDate: reserveForm.date,
      startTime: reserveForm.startTime,
      endTime: reserveForm.endTime,
      remark: reserveForm.remark
    }, { withCredentials: true })

    if (orderRes.data.code !== 200) {
      ElMessage.error(orderRes.data.message || '创建订单失败')
      return
    }

    const { orderNo, totalPrice } = orderRes.data.data
    currentOrderNo.value = orderNo
    currentAmount.value = totalPrice
    dialogVisible.value = false
    payDialogVisible.value = true
  } catch (err) {
    console.error('预约失败:', err)
    ElMessage.error('预约失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 确认支付
const confirmPay = async () => {
  try {
    const payRes = await axios.post('http://localhost:8080/api/reservation/pay-by-order', {
      orderNo: currentOrderNo.value,
      paymentMethod: payMethod.value === 'wechat' ? 1 : (payMethod.value === 'alipay' ? 2 : 3)
    }, { withCredentials: true })

    if (payRes.data.code === 200) {
      ElMessage.success('支付成功！预约已生效')
      payDialogVisible.value = false
      router.push('/reservation-list')
    } else {
      ElMessage.error(payRes.data.message || '支付失败')
    }
  } catch (err) {
    console.error('支付失败:', err)
    ElMessage.error('支付接口异常')
  }
}

const goBack = () => {
  router.push('/')
}

onMounted(() => {
  loadFacilities()
})
</script>

<style scoped>
.facility-list {
  min-height: 100vh;
  background: linear-gradient(145deg, #f8fafc 0%, #eef2ff 100%),
              url('/images/1120.png') center/cover repeat;
  background-blend-mode: overlay;
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  background: white;
  border-radius: 48px;
  padding: 8px 24px 8px 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03), 0 1px 2px rgba(0, 0, 0, 0.05);
  border: 1px solid #e2e8f0;
}

.page-header h2 {
  font-size: 1.8rem;
  font-weight: 700;
  background: linear-gradient(135deg, #1e293b 0%, #f97316 100%);
  background-clip: text;
  -webkit-background-clip: text;
  color: transparent;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-header h2::before {
  content: "⚡";
  font-size: 1.8rem;
  background: none;
  -webkit-background-clip: unset;
  color: #f97316;
}

.page-header .el-button {
  border-radius: 40px;
  padding: 10px 20px;
  font-weight: 500;
  border: 1px solid #e2e8f0;
  background: white;
  transition: all 0.2s;
}

.page-header .el-button:hover {
  background: #f97316;
  border-color: #f97316;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(249, 115, 22, 0.2);
}

/* 设施网格 */
.facility-grid {
  margin-top: 10px;
}

/* 卡片 */
.facility-card {
  background: white;
  border-radius: 28px;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.2, 0.9, 0.4, 1.1);
  margin-bottom: 24px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.05), 0 8px 10px -6px rgba(0, 0, 0, 0.02);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.facility-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 35px -10px rgba(0, 0, 0, 0.15);
  border-color: #f97316;
}

.facility-img {
  width: 100%;
  height: 180px;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.facility-card:hover .facility-img {
  transform: scale(1.03);
}

.card-info {
  padding: 18px 18px 22px;
}

.card-info h3 {
  font-size: 1.4rem;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 12px 0;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.9rem;
  color: #334155;
  margin-bottom: 10px;
  background: #f1f5f9;
  padding: 6px 14px;
  border-radius: 40px;
  width: fit-content;
  transition: all 0.2s;
}

.info-item .el-icon {
  color: #f97316;
  font-size: 1rem;
}

.card-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.card-actions .el-button {
  flex: 1;
  border-radius: 40px;
}

/* 预约时间表样式 */
.schedule-filter {
  margin-bottom: 20px;
  text-align: center;
}

.schedule-list {
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  overflow: hidden;
}

.schedule-header {
  display: flex;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
  font-weight: 600;
  color: #1e293b;
}

.schedule-header > div {
  padding: 12px 16px;
}

.header-time {
  width: 140px;
  border-right: 1px solid #e2e8f0;
}

.header-status {
  width: 100px;
  border-right: 1px solid #e2e8f0;
}

.header-info {
  flex: 1;
}

.schedule-items {
  max-height: 400px;
  overflow-y: auto;
}

.schedule-item {
  display: flex;
  border-bottom: 1px solid #f1f5f9;
  transition: background 0.2s;
}

.schedule-item:last-child {
  border-bottom: none;
}

.schedule-item > div {
  padding: 10px 16px;
}

.schedule-item .item-time {
  width: 140px;
  border-right: 1px solid #e2e8f0;
  font-size: 14px;
}

.schedule-item .item-status {
  width: 100px;
  border-right: 1px solid #e2e8f0;
}

.schedule-item .item-info {
  flex: 1;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.booked-info {
  color: #f56c6c;
  display: flex;
  align-items: center;
  gap: 4px;
}

.available-info {
  color: #67c23a;
  display: flex;
  align-items: center;
  gap: 4px;
}

.schedule-item.booked {
  background: #fef2f2;
}

.schedule-item.available:hover {
  background: #f0fdf4;
}

.schedule-tip {
  margin-top: 16px;
}

/* 空状态 */
.el-empty {
  background: transparent;
}
.el-empty__description p {
  color: #64748b;
}

/* 对话框样式 */
:deep(.el-dialog) {
  background: white;
  border-radius: 32px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
}

:deep(.el-dialog__title) {
  color: #f97316;
  font-weight: 700;
  font-size: 1.5rem;
}

:deep(.el-dialog__header) {
  border-bottom: 2px solid #fee2e2;
  margin-bottom: 16px;
}

:deep(.el-dialog__body) {
  padding: 20px 28px;
}

:deep(.el-form-item__label) {
  color: #1e293b;
  font-weight: 600;
}

:deep(.el-input__wrapper), :deep(.el-textarea__inner), :deep(.el-select .el-input__wrapper) {
  background: #f8fafc;
  border-radius: 20px;
  box-shadow: none;
  border: 1px solid #e2e8f0;
  transition: all 0.2s;
}

:deep(.el-input__wrapper:hover), :deep(.el-textarea__inner:hover) {
  border-color: #f97316;
}

:deep(.el-input__wrapper.is-focus), :deep(.el-textarea__inner:focus) {
  border-color: #f97316;
  box-shadow: 0 0 0 2px rgba(249, 115, 22, 0.2);
}

:deep(.el-button--primary) {
  background: #f97316;
  border: none;
  border-radius: 40px;
  font-weight: 600;
  padding: 10px 24px;
}

:deep(.el-button--primary:hover) {
  background: #ea580c;
  transform: scale(1.02);
  box-shadow: 0 8px 20px rgba(249, 115, 22, 0.3);
}

:deep(.el-button--default) {
  border-radius: 40px;
}

/* 支付弹窗 */
.pay-content {
  background: #f8fafc;
  padding: 20px;
  border-radius: 24px;
  color: #0f172a;
  text-align: center;
}

.pay-content p {
  margin: 8px 0;
  font-weight: 500;
}

:deep(.el-radio-group) {
  display: flex;
  gap: 24px;
  justify-content: center;
  margin-top: 20px;
}

:deep(.el-radio__label) {
  color: #1e293b;
}

/* 响应式 */
@media (max-width: 768px) {
  .facility-list {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
    text-align: center;
    border-radius: 32px;
    padding: 16px;
  }

  .card-info h3 {
    font-size: 1.2rem;
  }

  .info-item {
    font-size: 0.8rem;
  }

  .card-actions {
    flex-direction: column;
  }

  .header-time, .item-time {
    width: 100px;
  }

  .header-status, .item-status {
    width: 70px;
  }
}
</style>
