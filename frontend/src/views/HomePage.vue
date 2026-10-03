<template>
  <div class="home">
    <el-container>
      <el-header>
        <div class="header-content">
          <div class="logo-area">
            <span class="logo-icon">🏆</span>
            <h2>智慧校园体育平台</h2>
          </div>

          <!-- 公告图标 -->
          <div class="announcement-icon" @click="showAnnouncementDialog">
            <el-badge :value="unreadCount" :hidden="unreadCount === 0">
              <el-icon :size="24"><Bell /></el-icon>
            </el-badge>
            <span>公告</span>
          </div>

          <div class="user-info">
            <el-button
              v-if="userStore.userRole === 2"
              type="warning"
              size="small"
              @click="goToApproval"
              class="approval-btn"
            >
              <el-icon><Setting /></el-icon>
              管理员审批
            </el-button>
            <span class="welcome-text">欢迎，{{ userStore.userName }}</span>
            <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
          </div>
        </div>
      </el-header>

      <el-container>
        <el-aside width="220px">
          <el-menu router :default-active="activeMenu" class="side-menu">
            <el-menu-item index="/facility">
              <el-icon><Basketball /></el-icon>
              <span>体育设施</span>
            </el-menu-item>
            <el-menu-item index="/reservation-list">
              <el-icon><Calendar /></el-icon>
              <span>我的预约</span>
            </el-menu-item>
            <el-menu-item index="/sport-recordandhealth-report">
              <el-icon><DataLine /></el-icon>
              <span>运动记录与健康数据</span>
            </el-menu-item>
            <el-menu-item index="/profile">
              <el-icon><User /></el-icon>
              <span>个人中心</span>
            </el-menu-item>

            <el-menu-item
              v-if="userStore.userRole === 1 || userStore.userRole === 2"
              index="/facility-management"
            >
              <el-icon><Management /></el-icon>
              <span>体育设施管理</span>
            </el-menu-item>

            <el-menu-item
              v-if="userStore.userRole === 1 || userStore.userRole === 2"
              index="/announce-manage"
            >
              <el-icon><Bell /></el-icon>
              <span>公告管理</span>
            </el-menu-item>

            <el-menu-item v-if="userStore.userRole === 2" index="/user-management">
              <el-icon><UserFilled /></el-icon>
              <span>用户管理</span>
            </el-menu-item>

            <el-menu-item
              v-if="userStore.userRole === 1"
              index="/reservation-management"
            >
              <el-icon><List /></el-icon>
              <span>预约订单管理</span>
            </el-menu-item>
          </el-menu>
        </el-aside>

        <el-main>
          <el-card class="welcome-card" shadow="hover">
            <div class="welcome-header">
              <div class="welcome-icon">🎉</div>
              <div>
                <h3>欢迎回来，{{ userStore.userName }}！</h3>
                <p>今天是 {{ currentDate }}，保持运动，保持健康 💪</p>
              </div>
            </div>
          </el-card>

          <el-row v-if="userStore.userRole >= 1" :gutter="20" class="stats-row">
            <el-col :span="8">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-content">
                  <div class="stat-icon blue">📅</div>
                  <div class="stat-info">
                    <div class="stat-value">{{ stats.todayReservations }}</div>
                    <div class="stat-label">今日预约</div>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-content">
                  <div class="stat-icon green">👥</div>
                  <div class="stat-info">
                    <div class="stat-value">{{ stats.totalUsers }}</div>
                    <div class="stat-label">总用户数</div>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-content">
                  <div class="stat-icon orange">🏟️</div>
                  <div class="stat-info">
                    <div class="stat-value">{{ stats.totalFacilities }}</div>
                    <div class="stat-label">体育设施</div>
                  </div>
                </div>
              </el-card>
            </el-col>

          </el-row>

          <el-card class="quick-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span>🚀 快捷入口</span>
              </div>
            </template>
            <div class="quick-links">
              <div class="quick-item" @click="goToPage('/facility')">
                <div class="quick-icon">🏀</div>
                <span>预约场地</span>
              </div>
              <div class="quick-item" @click="goToPage('/reservation-list')">
                <div class="quick-icon">📋</div>
                <span>我的预约</span>
              </div>
              <div class="quick-item" @click="goToPage('/sport-recordandhealth-report')">
                <div class="quick-icon">📊</div>
                <span>运动记录与健康数据</span>
              </div>
              <div class="quick-item" @click="goToPage('/profile')">
                <div class="quick-icon">👤</div>
                <span>个人中心</span>
              </div>
            </div>
          </el-card>

          <el-card class="quote-card" shadow="never">
            <div class="quote-content">
              <span class="quote-mark">“</span>
              <p class="quote-text">{{ currentQuote }}</p>
              <span class="quote-author">—— {{ quoteAuthor }}</span>
            </div>
          </el-card>
        </el-main>
      </el-container>
    </el-container>

    <!-- 公告弹窗 -->
    <el-dialog v-model="announcementDialogVisible" title="📢 平台公告" width="500px" class="announcement-dialog">
      <div class="announcement-list">
        <div v-for="item in announcements" :key="item.id" class="announcement-item">
          <div class="announcement-header">
            <el-tag :type="item.typeColor" size="small">{{ item.typeText }}</el-tag>
            <el-tag :type="item.priorityColor" size="small" v-if="item.priority === 2">紧急</el-tag>
            <span class="announcement-time">{{ formatDate(item.publishTime) }}</span>
          </div>
          <div class="announcement-title">{{ item.title }}</div>
          <div class="announcement-content">{{ item.content }}</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="announcementDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { Management, List } from '@element-plus/icons-vue'
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Setting, Basketball, Calendar, DataLine, User, UserFilled, Bell
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import axios from 'axios'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 当前激活的菜单
const activeMenu = computed(() => route.path)

// 当前日期
const currentDate = ref('')
const updateDate = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  const weekday = weekdays[now.getDay()]
  currentDate.value = `${year}年${month}月${day}日 ${weekday}`
}

// 统计数据
const stats = ref({
  todayReservations: 0,
  totalUsers: 0,
  totalFacilities: 0,
  totalSports: 0
})

const loadStats = async () => {
  if (userStore.userRole < 1) return

  try {
    const todayRes = await axios.get('http://localhost:8080/api/reservation/today-count', {
      withCredentials: true
    })
    stats.value.todayReservations = todayRes.data.data || 0
  } catch (err) {
    console.error('加载统计数据失败', err)
  }

  try {
    const usersRes = await axios.get('http://localhost:8080/api/user/count', {
      withCredentials: true
    })
    stats.value.totalUsers = usersRes.data.data || 0
  } catch (err) {
    console.error('加载用户数失败', err)
  }

  try {
    const facilityRes = await axios.get('http://localhost:8080/api/facility/count', {
      withCredentials: true
    })
    stats.value.totalFacilities = facilityRes.data.data || 0
  } catch (err) {
    console.error('加载设施数失败', err)
  }

  stats.value.totalSports = 128
}

// 运动名言
const quotes = [
  { text: '生命在于运动', author: '伏尔泰' },
  { text: '运动是健康的源泉', author: '亚里士多德' },
  { text: '坚持运动，遇见更好的自己', author: '佚名' },
  { text: '汗水不会欺骗你', author: '运动格言' },
  { text: '每一次挥汗，都是对生活的热爱', author: '佚名' },
  { text: '运动，是治愈一切的良药', author: '柏拉图' }
]

const quoteIndex = ref(Math.floor(Math.random() * quotes.length))
const currentQuote = computed(() => quotes[quoteIndex.value].text)
const quoteAuthor = computed(() => quotes[quoteIndex.value].author)

const goToApproval = () => {
  router.push('/approval')
}

const goToPage = (path) => {
  router.push(path)
}

const handleLogout = async () => {
  await userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}

// ==================== 公告相关 ====================
const announcements = ref([])
const announcementDialogVisible = ref(false)
const unreadCount = ref(0)
const hasShownInCurrentSession = ref(false)

// 加载公告
const loadAnnouncements = async (shouldShowDialog = false) => {
  try {
    const res = await axios.get('http://localhost:8080/api/announcement/current', {
      withCredentials: true
    })
    if (res.data.code === 200) {
      announcements.value = res.data.data || []

      const lastReadTime = localStorage.getItem('lastReadAnnouncementTime')
      const hasNew = announcements.value.some(item =>
        !lastReadTime || new Date(item.publishTime) > new Date(lastReadTime)
      )
      unreadCount.value = hasNew ? announcements.value.length : 0

      // 如果需要弹窗、当前会话未弹过、且有公告
      if (shouldShowDialog && !hasShownInCurrentSession.value && announcements.value.length > 0) {
        setTimeout(() => {
          announcementDialogVisible.value = true
          hasShownInCurrentSession.value = true
        }, 500)
      }
    }
  } catch (err) {
    console.error('加载公告失败:', err)
  }
}

// 显示公告弹窗（点击图标时）
const showAnnouncementDialog = () => {
  announcementDialogVisible.value = true
  localStorage.setItem('lastReadAnnouncementTime', new Date().toISOString())
  unreadCount.value = 0
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
}

onMounted(() => {
  updateDate()
  loadStats()

  // 判断是否应该显示公告弹窗
  // 方式1：检查 sessionStorage 中的登录标记
  const justLoggedIn = sessionStorage.getItem('justLoggedIn')
  // 方式2：检查路由参数
  const fromLogin = route.query.from === 'login'

  if (justLoggedIn === 'true' || fromLogin) {
    // 清除标记，防止刷新后再次弹出
    sessionStorage.removeItem('justLoggedIn')
    // 加载公告并弹窗
    loadAnnouncements(true)
  } else {
    // 正常加载但不弹窗
    loadAnnouncements(false)
  }

  setInterval(updateDate, 60000)
})
</script>

<style scoped>
.home {
  height: 100vh;
  overflow: hidden;
}

.el-header {
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  color: white;
  display: flex;
  align-items: center;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  font-size: 28px;
}

.logo-area h2 {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  background: linear-gradient(135deg, #fff, #94a3b8);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.announcement-icon {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.1);
  transition: all 0.2s;
  margin-left: auto;
  margin-right: 20px;
}

.announcement-icon:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-1px);
}

.announcement-icon span {
  font-size: 14px;
  color: #e2e8f0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.welcome-text {
  font-size: 14px;
  color: #e2e8f0;
}

.approval-btn {
  background: rgba(245, 158, 11, 0.2);
  border-color: #f59e0b;
  color: #fbbf24;
}

.approval-btn:hover {
  background: #f59e0b;
  color: white;
}

.el-aside {
  background-color: #ffffff;
  border-right: 1px solid #e2e8f0;
  height: calc(100vh - 60px);
}

.side-menu {
  border-right: none;
  height: 100%;
}

.side-menu .el-menu-item {
  height: 50px;
  line-height: 50px;
  margin: 4px 8px;
  border-radius: 12px;
}

.side-menu .el-menu-item:hover {
  background-color: #f1f5f9;
}

.side-menu .el-menu-item.is-active {
  background: linear-gradient(135deg, #f97316, #ef4444);
  color: white;
}

.el-main {
  background-color: #f8fafc;
  padding: 24px;
  overflow-y: auto;
  height: calc(100vh - 60px);
}

.welcome-card {
  margin-bottom: 24px;
  border-radius: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
}

.welcome-card :deep(.el-card__body) {
  padding: 24px;
}

.welcome-header {
  display: flex;
  align-items: center;
  gap: 16px;
}

.welcome-icon {
  font-size: 48px;
}

.welcome-header h3 {
  font-size: 20px;
  margin: 0 0 8px 0;
}

.welcome-header p {
  margin: 0;
  opacity: 0.9;
}

.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 16px;
  border: none;
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  font-size: 36px;
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
}

.stat-icon.blue {
  background: rgba(59, 130, 246, 0.1);
}
.stat-icon.green {
  background: rgba(34, 197, 94, 0.1);
}
.stat-icon.orange {
  background: rgba(249, 115, 22, 0.1);
}
.stat-icon.purple {
  background: rgba(168, 85, 247, 0.1);
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

.quick-card {
  margin-bottom: 24px;
  border-radius: 16px;
}

.card-header {
  font-weight: 600;
  color: #1e293b;
}

.quick-links {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.quick-item {
  flex: 1;
  min-width: 100px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.2s;
}

.quick-item:hover {
  background: linear-gradient(135deg, #f97316, #ef4444);
  transform: translateY(-4px);
  color: white;
}

.quick-icon {
  font-size: 32px;
}

.quick-item span {
  font-size: 14px;
  font-weight: 500;
}

.quote-card {
  border-radius: 16px;
  background: linear-gradient(135deg, #1e293b, #0f172a);
  color: white;
  border: none;
}

.quote-card :deep(.el-card__body) {
  padding: 24px;
}

.quote-content {
  text-align: center;
}

.quote-mark {
  font-size: 48px;
  color: #f97316;
  font-family: serif;
}

.quote-text {
  font-size: 18px;
  margin: 16px 0;
  line-height: 1.6;
}

.quote-author {
  font-size: 14px;
  color: #94a3b8;
  display: block;
  margin-top: 12px;
}

.announcement-dialog :deep(.el-dialog__body) {
  padding: 20px;
  max-height: 400px;
  overflow-y: auto;
}

.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.announcement-item {
  padding: 12px;
  background: #f8fafc;
  border-radius: 12px;
  border-left: 4px solid #f97316;
}

.announcement-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.announcement-time {
  font-size: 12px;
  color: #94a3b8;
  margin-left: auto;
}

.announcement-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 6px;
}

.announcement-content {
  font-size: 13px;
  color: #475569;
  line-height: 1.5;
}
</style>
