<template>
  <div class="facility-management">
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
          <h2>🏟️ 体育设施管理</h2>
          <p>添加、修改和管理体育设施</p>
        </div>
      </div>
      <div class="header-right">
        <el-button type="success" @click="goToStatistics" :icon="DataAnalysis">
          查看设施使用情况
        </el-button>
        <el-button type="primary" @click="openAddDialog" :icon="Plus">
          添加设施
        </el-button>
        <el-button @click="loadFacilities" :loading="loading" :icon="Refresh">
          刷新
        </el-button>
      </div>
    </div>

    <!-- 设施列表 -->
    <div class="facility-grid">
      <el-row :gutter="20">
        <el-col
          v-for="item in facilityList"
          :key="item.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
        >
          <el-card class="facility-card" shadow="hover" :body-style="{ padding: '0px' }">
            <!-- 设施图片 -->
            <div class="facility-image-container">
              <img
                :src="item.imageUrl || defaultImage"
                class="facility-image"
                @error="handleImageError"
              />
              <div class="facility-type-tag">
                <el-tag :type="getFacilityTypeColor(item.type)" size="large">
                  {{ getFacilityTypeText(item.type) }}
                </el-tag>
              </div>
            </div>

            <!-- 设施信息 -->
            <div class="facility-info">
              <h3>{{ item.name }}</h3>
              <div class="info-row">
                <el-icon><Location /></el-icon>
                <span>{{ item.location }}</span>
              </div>
              <div class="info-row">
                <el-icon><User /></el-icon>
                <span>容纳 {{ item.capacity }} 人</span>
              </div>
              <div class="info-row">
                <el-icon><Money /></el-icon>
                <span>¥{{ item.pricePerHour }}/小时</span>
              </div>
              <div class="info-row">
                <el-icon><Timer /></el-icon>
                <span>最长预约 {{ item.maxDuration || 4 }} 小时</span>
              </div>
              <div class="info-row">
                <el-icon><Clock /></el-icon>
                <span>{{ item.openTime }} - {{ item.closeTime }}</span>
              </div>
              <div class="info-row description">
                <el-icon><Document /></el-icon>
                <span>{{ item.description || '暂无描述' }}</span>
              </div>
              <div class="facility-actions">
                <el-button
                  type="info"
                  size="small"
                  @click="viewReservations(item)"
                  :icon="View"
                  plain
                >
                  查看预约
                </el-button>
                <el-button
                  type="primary"
                  size="small"
                  @click="openEditDialog(item)"
                  :icon="Edit"
                >
                  编辑
                </el-button>
                <el-button
                  type="danger"
                  size="small"
                  @click="deleteFacility(item.id, item.name)"
                  :icon="Delete"
                >
                  删除
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 空状态 -->
    <el-empty v-if="!loading && facilityList.length === 0" description="暂无体育设施" />

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="650px"
      :close-on-click-modal="false"
    >
      <el-form
        :model="facilityForm"
        :rules="facilityRules"
        ref="facilityFormRef"
        label-width="100px"
      >
        <el-form-item label="设施名称" prop="name">
          <el-input v-model="facilityForm.name" placeholder="请输入设施名称" />
        </el-form-item>

        <el-form-item label="设施类型" prop="type">
          <el-select v-model="facilityForm.type" placeholder="请选择设施类型" style="width: 100%">
            <el-option label="篮球场" :value="1" />
            <el-option label="足球场" :value="2" />
            <el-option label="羽毛球场" :value="3" />
            <el-option label="乒乓球场" :value="4" />
            <el-option label="健身房" :value="5" />
            <el-option label="游泳馆" :value="6" />
          </el-select>
        </el-form-item>

        <el-form-item label="所在位置" prop="location">
          <el-input v-model="facilityForm.location" placeholder="请输入所在位置" />
        </el-form-item>

        <el-form-item label="容纳人数" prop="capacity">
          <el-input-number v-model="facilityForm.capacity" :min="1" :max="500" style="width: 100%" />
        </el-form-item>

        <el-form-item label="价格(元/小时)" prop="pricePerHour">
          <el-input-number v-model="facilityForm.pricePerHour" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>

        <el-form-item label="最长预约时间" prop="maxDuration">
          <el-input-number
            v-model="facilityForm.maxDuration"
            :min="1"
            :max="12"
            :precision="0"
            style="width: 100%"
          />
          <span style="margin-left: 8px; color: #64748b;">小时</span>
        </el-form-item>

        <el-form-item label="开放时间" prop="openTime">
          <el-time-picker
            v-model="facilityForm.openTime"
            format="HH:mm"
            value-format="HH:mm:ss"
            placeholder="选择开放时间"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="关闭时间" prop="closeTime">
          <el-time-picker
            v-model="facilityForm.closeTime"
            format="HH:mm"
            value-format="HH:mm:ss"
            placeholder="选择关闭时间"
            style="width: 100%"
          />
        </el-form-item>

        <!-- 图片上传组件 -->
        <el-form-item label="设施图片">
          <div class="upload-container">
            <el-upload
              class="facility-uploader"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
              :before-upload="beforeUpload"
              accept="image/jpeg,image/png,image/jpg,image/gif"
              :with-credentials="true"
            >
              <img v-if="facilityForm.imageUrl" :src="facilityForm.imageUrl" class="uploaded-image" />
              <div v-else class="upload-placeholder">
                <el-icon class="uploader-icon"><Plus /></el-icon>
                <div class="upload-text">点击上传图片</div>
              </div>
            </el-upload>

            <!-- 图片操作按钮 -->
            <div v-if="facilityForm.imageUrl" class="image-actions">
              <el-button type="danger" size="small" @click="removeImage" :icon="Delete">
                删除图片
              </el-button>
            </div>

            <div class="upload-tip">
              <el-icon><Picture /></el-icon>
              <span>点击上传图片，支持 JPG、PNG 格式，大小不超过 5MB</span>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="设施描述" prop="description">
          <el-input
            v-model="facilityForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入设施描述"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">
          {{ isEdit ? '保存修改' : '添加设施' }}
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft, Plus, Refresh, Edit, Delete, Location, User,
  Money, Clock, Document, Picture, Timer, View, Lock, Check, DataAnalysis
} from '@element-plus/icons-vue'
import axios from 'axios'

const router = useRouter()
const loading = ref(false)
const submitLoading = ref(false)
const facilityList = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const facilityFormRef = ref(null)

// 预约时间表相关
const scheduleDialogVisible = ref(false)
const selectedFacilityForSchedule = ref(null)
const scheduleDate = ref(new Date().toISOString().slice(0, 10))
const facilitySchedule = ref([])

// 图片上传配置
const uploadUrl = 'http://localhost:8080/api/upload/image'
const uploadHeaders = {}

// 本地默认图片
const defaultImage = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 300 200"%3E%3Crect width="300" height="200" fill="%23f5f7fa"%3E%3C/rect%3E%3Ctext x="50%25" y="50%25" text-anchor="middle" dy=".3em" fill="%23999"%3E🏟️ 暂无图片%3C/text%3E%3C/svg%3E'

// 设施类型映射
const getFacilityTypeText = (type) => {
  const types = {
    1: '篮球场', 2: '足球场', 3: '羽毛球场',
    4: '乒乓球场', 5: '健身房', 6: '游泳馆'
  }
  return types[type] || '其他'
}

const getFacilityTypeColor = (type) => {
  const colors = {
    1: 'warning', 2: 'success', 3: 'primary',
    4: 'info', 5: '', 6: 'danger'
  }
  return colors[type] || 'info'
}

// 表单数据
const facilityForm = reactive({
  id: null,
  name: '',
  type: 1,
  location: '',
  capacity: 20,
  pricePerHour: 50,
  maxDuration: 4,
  openTime: '08:00:00',
  closeTime: '22:00:00',
  imageUrl: '',
  description: ''
})

// 表单验证规则
const facilityRules = {
  name: [
    { required: true, message: '请输入设施名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在2-50个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择设施类型', trigger: 'change' }
  ],
  location: [
    { required: true, message: '请输入所在位置', trigger: 'blur' }
  ],
  capacity: [
    { required: true, message: '请输入容纳人数', trigger: 'blur' }
  ],
  pricePerHour: [
    { required: true, message: '请输入价格', trigger: 'blur' }
  ],
  maxDuration: [
    { required: true, message: '请输入最长预约时间', trigger: 'blur' },
    { type: 'number', min: 1, max: 12, message: '最长预约时间在1-12小时之间', trigger: 'blur' }
  ],
  openTime: [
    { required: true, message: '请选择开放时间', trigger: 'change' }
  ],
  closeTime: [
    { required: true, message: '请选择关闭时间', trigger: 'change' }
  ]
}

// 对话框标题
const dialogTitle = computed(() => isEdit.value ? '编辑设施' : '添加设施')

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

// 图片上传前的校验
const beforeUpload = (file) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/jpg' || file.type === 'image/gif'
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传 JPG、PNG 格式的图片！')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB！')
    return false
  }
  return true
}

// 图片上传成功回调
const handleUploadSuccess = (response) => {
  if (response.code === 200 && response.data && response.data.url) {
    const fullUrl = 'http://localhost:8080/api' + response.data.url;
    facilityForm.imageUrl = fullUrl;
    console.log('图片上传成功:', fullUrl);
    ElMessage.success('图片上传成功');
  } else {
    ElMessage.error(response.message || '图片上传失败');
  }
}

// 图片上传失败回调
const handleUploadError = (error) => {
  console.error('上传失败:', error)
  ElMessage.error('图片上传失败，请稍后重试')
}

// 删除图片
const removeImage = () => {
  facilityForm.imageUrl = ''
  ElMessage.info('已删除图片')
}

// 返回主页
const goBack = () => {
  router.push('/')
}

// 跳转到统计页面
const goToStatistics = () => {
  router.push('/facility-statistics')
}

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
const viewReservations = async (facility) => {
  selectedFacilityForSchedule.value = facility
  scheduleDate.value = new Date().toISOString().slice(0, 10)
  scheduleDialogVisible.value = true
  await loadFacilitySchedule()
}

// 打开添加对话框
const openAddDialog = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 打开编辑对话框
const openEditDialog = (facility) => {
  isEdit.value = true
  facilityForm.id = facility.id
  facilityForm.name = facility.name
  facilityForm.type = facility.type
  facilityForm.location = facility.location
  facilityForm.capacity = facility.capacity
  facilityForm.pricePerHour = facility.pricePerHour
  facilityForm.maxDuration = facility.maxDuration || 4
  facilityForm.openTime = facility.openTime
  facilityForm.closeTime = facility.closeTime
  let imageUrl = facility.imageUrl || ''
  if (imageUrl && imageUrl.startsWith('/')) {
    imageUrl = 'http://localhost:8080/' + imageUrl
  }
  facilityForm.imageUrl = imageUrl
  facilityForm.description = facility.description || ''
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  facilityForm.id = null
  facilityForm.name = ''
  facilityForm.type = 1
  facilityForm.location = ''
  facilityForm.capacity = 20
  facilityForm.pricePerHour = 50
  facilityForm.maxDuration = 4
  facilityForm.openTime = '08:00:00'
  facilityForm.closeTime = '22:00:00'
  facilityForm.imageUrl = ''
  facilityForm.description = ''
  if (facilityFormRef.value) {
    facilityFormRef.value.resetFields()
  }
}

// 提交表单
const submitForm = async () => {
  if (!facilityFormRef.value) return

  await facilityFormRef.value.validate(async (valid) => {
    if (!valid) return

    submitLoading.value = true
    try {
      const submitData = { ...facilityForm }
      if (submitData.imageUrl && submitData.imageUrl.startsWith('http://localhost:8080/api')) {
        submitData.imageUrl = submitData.imageUrl.replace('http://localhost:8080/api', '')
      }

      const url = isEdit.value
        ? 'http://localhost:8080/api/facility/update'
        : 'http://localhost:8080/api/facility/add'

      const res = await axios.post(url, submitData, {
        withCredentials: true
      })

      if (res.data.code === 200) {
        ElMessage.success(isEdit.value ? '设施更新成功' : '设施添加成功')
        dialogVisible.value = false
        loadFacilities()
      } else {
        ElMessage.error(res.data.message || '操作失败')
      }
    } catch (err) {
      console.error('操作失败:', err)
      ElMessage.error('操作失败，请稍后重试')
    } finally {
      submitLoading.value = false
    }
  })
}

// 删除设施
const deleteFacility = async (id, name) => {
  try {
    await ElMessageBox.confirm(
      `确认删除设施「${name}」吗？删除后无法恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const res = await axios.delete(`http://localhost:8080/api/facility/${id}`, {
      withCredentials: true
    })
    if (res.data.code === 200) {
      ElMessage.success('删除成功')
      loadFacilities()
    } else {
      ElMessage.error(res.data.message || '删除失败')
    }
  } catch (err) {
    if (err !== 'cancel') {
      console.error('删除失败:', err)
      ElMessage.error('删除失败，请稍后重试')
    }
  }
}

// 图片加载错误处理
const handleImageError = (event) => {
  console.warn('图片加载失败:', event.target.src)
  event.target.src = defaultImage
}

onMounted(() => {
  loadFacilities()
})
</script>

<style scoped>
.facility-management {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #eef2f6 100%);
}

.page-header {
  background: white;
  padding: 16px 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: sticky;
  top: 0;
  z-index: 100;
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
  font-size: 22px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.header-title p {
  color: #64748b;
  font-size: 13px;
  margin: 2px 0 0 0;
}

.header-right {
  display: flex;
  gap: 12px;
}

.facility-grid {
  padding: 24px;
}

.facility-card {
  margin-bottom: 20px;
  border-radius: 16px;
  overflow: hidden;
  transition: transform 0.3s, box-shadow 0.3s;
}

.facility-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
}

.facility-image-container {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.facility-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.facility-card:hover .facility-image {
  transform: scale(1.05);
}

.facility-type-tag {
  position: absolute;
  top: 12px;
  right: 12px;
}

.facility-info {
  padding: 16px;
}

.facility-info h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 12px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 14px;
  color: #475569;
}

.info-row .el-icon {
  color: #f97316;
  font-size: 16px;
}

.info-row.description {
  align-items: flex-start;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #e2e8f0;
}

.info-row.description span {
  flex: 1;
  line-height: 1.4;
  word-break: break-all;
}

.facility-actions {
  display: flex;
  gap: 8px;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #e2e8f0;
}

.facility-actions .el-button {
  flex: 1;
  font-size: 12px;
  padding: 8px 0;
}

.upload-container {
  width: 100%;
}

.facility-uploader {
  border: 2px dashed #d9d9d9;
  border-radius: 12px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  width: 100%;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fafafa;
}

.facility-uploader:hover {
  border-color: #f97316;
  background-color: #fef3e8;
}

.uploaded-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.upload-placeholder {
  text-align: center;
  color: #8c939d;
}

.uploader-icon {
  font-size: 48px;
  color: #c0c4cc;
}

.upload-text {
  margin-top: 8px;
  font-size: 14px;
  color: #909399;
}

.image-actions {
  margin-top: 12px;
  text-align: center;
}

.upload-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #94a3b8;
  display: flex;
  align-items: center;
  gap: 4px;
  justify-content: center;
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

@media (max-width: 768px) {
  .facility-grid {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .header-left {
    width: 100%;
  }

  .header-right {
    width: 100%;
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  .facility-actions {
    flex-wrap: wrap;
  }

  .header-time, .item-time {
    width: 100px;
  }

  .header-status, .item-status {
    width: 70px;
  }
}
</style>
