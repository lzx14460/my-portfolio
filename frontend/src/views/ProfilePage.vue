<template>
  <div class="profile-page">
    <!-- 动态运动背景 -->
    <div class="hero-bg">
      <div class="overlay"></div>
      <div class="sport-pattern"></div>
    </div>

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
          <h2>👤 个人中心</h2>
          <p>查看和修改个人信息</p>
        </div>
      </div>
    </div>

    <div class="profile-container">
      <!-- 左侧：头像和基本信息 -->
      <div class="profile-sidebar">
        <div class="avatar-section">
          <div class="avatar-wrapper">
            <div class="avatar-ring"></div>
            <el-avatar :size="100" :icon="UserFilled" class="user-avatar" />
            <div class="sport-badge">🏀</div>
          </div>
          <h3>{{ userInfo?.name || userInfo?.username }}</h3>
          <el-tag :type="getRoleType(userInfo?.role)" size="large" effect="dark">
            {{ getRoleText(userInfo?.role) }}
          </el-tag>
        </div>

        

        <!-- 权限提示 -->
        <div class="info-tips">
          <el-alert
            title="温馨提示"
            type="info"
            description="姓名，学号，学院，班级等信息请联系系统管理员进入用户管理中心进行更改"
            show-icon
            :closable="false"
          />
        </div>
      </div>

      <!-- 右侧：表单 -->
      <div class="profile-main">
        <el-tabs v-model="activeTab">
          <!-- 基本信息 Tab -->
          <el-tab-pane label="基本信息" name="basic">
            <el-form
              :model="basicForm"
              :rules="basicRules"
              ref="basicFormRef"
              label-width="100px"
              class="profile-form"
            >
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="姓名">
                    <el-input v-model="basicForm.name" disabled size="large" />

                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="用户名" prop="username">
                    <el-input
                      v-model="basicForm.username"
                      placeholder="请输入用户名"
                      size="large"
                      prefix-icon="User"
                    />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="手机号" prop="phone">
                    <el-input
                      v-model="basicForm.phone"
                      placeholder="请输入手机号"
                      maxlength="11"
                      show-word-limit
                      size="large"
                      prefix-icon="Phone"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="邮箱" prop="email">
                    <el-input
                      v-model="basicForm.email"
                      placeholder="请输入邮箱"
                      size="large"
                      prefix-icon="Message"
                    />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="学号">
                    <el-input
                      v-model="basicForm.studentId"
                      placeholder="学号"
                      size="large"
                      prefix-icon="School"
                      disabled
                    />

                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="学院">
                    <el-input
                      v-model="basicForm.college"
                      placeholder="学院"
                      size="large"
                      disabled
                    />

                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="班级">
                    <el-input
                      v-model="basicForm.className"
                      placeholder="班级"
                      size="large"
                      disabled
                    />

                  </el-form-item>
                </el-col>

              </el-row>

              <el-form-item>
                <el-button type="primary" size="large" @click="updateProfile" :loading="updateLoading" class="save-btn">
                  保存修改
                </el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <!-- 修改密码 Tab -->
          <el-tab-pane label="修改密码" name="password">
            <el-form
              :model="passwordForm"
              :rules="passwordRules"
              ref="passwordFormRef"
              label-width="100px"
              class="profile-form"
            >
              <el-form-item label="原密码" prop="oldPassword">
                <el-input
                  v-model="passwordForm.oldPassword"
                  type="password"
                  placeholder="请输入原密码"
                  show-password
                  size="large"
                  prefix-icon="Lock"
                />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input
                  v-model="passwordForm.newPassword"
                  type="password"
                  placeholder="请输入新密码（6-20位）"
                  show-password
                  size="large"
                  prefix-icon="Key"
                />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  placeholder="请再次输入新密码"
                  show-password
                  size="large"
                  prefix-icon="Key"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" size="large" @click="changePassword" :loading="passwordLoading" class="save-btn">
                  修改密码
                </el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, UserFilled } from '@element-plus/icons-vue'
import axios from 'axios'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const activeTab = ref('basic')

// 用户信息
const userInfo = ref(null)

// 基本信息表单
const basicForm = reactive({
  username: '',
  name: '',
  phone: '',
  email: '',
  studentId: '',
  college: '',
  className: ''
})

// 手机号验证
const validatePhone = async (rule, value, callback) => {
  if (!value) {
    callback()
    return
  }
  if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的11位手机号'))
    return
  }
  // 检查手机号是否已被其他用户使用
  if (value !== userInfo.value?.phone) {
    try {
      const res = await axios.get(`http://localhost:8080/api/user/check-phone?phone=${value}`, {
        withCredentials: true
      })
      if (res.data.code === 200 && res.data.data === true) {
        callback(new Error('该手机号已被其他用户绑定'))
        return
      }
      callback()
    } catch {
      callback()
    }
  } else {
    callback()
  }
}

// 用户名验证
const validateUsername = async (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入用户名'))
    return
  }
  if (value.length < 3 || value.length > 20) {
    callback(new Error('用户名长度在3-20位之间'))
    return
  }
  // 检查用户名是否已被其他用户使用
  if (value !== userInfo.value?.username) {
    try {
      const res = await axios.get(`http://localhost:8080/api/user/check-username?username=${value}`, {
        withCredentials: true
      })
      if (res.data.code === 200 && res.data.data === true) {
        callback(new Error('该用户名已被使用'))
        return
      }
      callback()
    } catch {
      callback()
    }
  } else {
    callback()
  }
}

// 邮箱验证
const validateEmail = (rule, value, callback) => {
  if (value && !/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(value)) {
    callback(new Error('请输入正确的邮箱地址'))
  } else {
    callback()
  }
}

// 基本信息验证规则
const basicRules = {
  username: [
    { validator: validateUsername, trigger: 'blur' }
  ],
  phone: [
    { validator: validatePhone, trigger: 'blur' }
  ],
  email: [
    { validator: validateEmail, trigger: 'blur' }
  ]
}

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码验证规则
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6到20位之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const basicFormRef = ref(null)
const passwordFormRef = ref(null)
const updateLoading = ref(false)
const passwordLoading = ref(false)

// 返回主页
const goBack = () => {
  router.push('/')
}

// 获取角色文本
const getRoleText = (role) => {
  const r = Number(role)
  switch (r) {
    case 0: return '学生'
    case 1: return '场馆管理员'
    case 2: return '系统管理员'
    default: return '未知'
  }
}

// 获取角色标签类型
const getRoleType = (role) => {
  const r = Number(role)
  switch (r) {
    case 0: return 'primary'
    case 1: return 'warning'
    case 2: return 'danger'
    default: return 'info'
  }
}

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/user/current', {
      withCredentials: true
    })
    if (res.data.code === 200) {
      userInfo.value = res.data.data
      basicForm.username = userInfo.value.username || ''
      basicForm.name = userInfo.value.name || ''
      basicForm.phone = userInfo.value.phone || ''
      basicForm.email = userInfo.value.email || ''
      basicForm.studentId = userInfo.value.studentId || ''
      basicForm.college = userInfo.value.college || ''
      basicForm.className = userInfo.value.className || ''
    }
  } catch (err) {
    console.error('加载用户信息失败:', err)
    ElMessage.error('加载用户信息失败')
  }
}

// 更新个人信息
const updateProfile = async () => {
  if (!basicFormRef.value) return

  await basicFormRef.value.validate(async (valid) => {
    if (!valid) return

    updateLoading.value = true
    try {
      const res = await axios.put('http://localhost:8080/api/user/update', {
        id: userInfo.value.id,
        username: basicForm.username,
        phone: basicForm.phone,
        email: basicForm.email
      }, { withCredentials: true })

      if (res.data.code === 200) {
        ElMessage.success('个人信息更新成功')
        // 如果用户名修改了，需要更新本地存储和重新登录
        if (basicForm.username !== userInfo.value.username) {
          ElMessage.warning('用户名已修改，请重新登录')
          setTimeout(() => {
            forceLogout()
          }, 1500)
        } else {
          await loadUserInfo()
          // 更新 store 中的用户信息
          userStore.userInfo = userInfo.value
          localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
        }
      } else {
        ElMessage.error(res.data.message || '更新失败')
      }
    } catch (err) {
      console.error('更新失败:', err)
      ElMessage.error(err.response?.data?.message || '更新失败，请稍后重试')
    } finally {
      updateLoading.value = false
    }
  })
}

// 强制退出登录
const forceLogout = () => {
  localStorage.removeItem('userInfo')
  userStore.userInfo = null
  userStore.isLogin = false
  ElMessage.success('请重新登录')
  setTimeout(() => {
    router.push('/login')
  }, 1500)
}

// 修改密码
const changePassword = async () => {
  if (!passwordFormRef.value) return

  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return

    passwordLoading.value = true
    try {
      const res = await axios.put('http://localhost:8080/api/user/change-password', null, {
        params: {
          id: userInfo.value.id,
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        },
        withCredentials: true
      })

      if (res.data.code === 200) {
        forceLogout()
      } else {
        ElMessage.error(res.data.message || '密码修改失败')
      }
    } catch (err) {
      console.error('密码修改失败:', err)
      ElMessage.error(err.response?.data?.message || '密码修改失败，请稍后重试')
    } finally {
      passwordLoading.value = false
    }
  })
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
}

/* 运动背景 */
.hero-bg {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  z-index: -2;
}

.overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle at 20% 30%, rgba(255,255,255,0.15) 0%, transparent 60%);
  pointer-events: none;
}

/* 运动图案 */
.sport-pattern {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'%3E%3Ccircle cx='20' cy='20' r='8' fill='rgba(255,255,255,0.1)'/%3E%3Ccircle cx='80' cy='30' r='12' fill='rgba(255,255,255,0.08)'/%3E%3Ccircle cx='50' cy='70' r='10' fill='rgba(255,255,255,0.06)'/%3E%3Ccircle cx='90' cy='80' r='6' fill='rgba(255,255,255,0.1)'/%3E%3Cpath d='M10 50 Q30 40 50 50 T90 50' stroke='rgba(255,255,255,0.08)' fill='none' stroke-width='2'/%3E%3C/svg%3E");
  background-repeat: repeat;
  opacity: 0.5;
  pointer-events: none;
}

/* 页面头部 */
.page-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 12px 24px;
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.back-btn {
  border-radius: 8px;
  transition: all 0.2s;
  font-size: 14px;
  background: rgba(255, 255, 255, 0.8);
}

.back-btn:hover {
  background: white;
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

/* 主容器 */
.profile-container {
  display: flex;
  gap: 20px;
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
  position: relative;
  z-index: 10;
}

/* 左侧边栏 */
.profile-sidebar {
  width: 300px;
  flex-shrink: 0;
}

.avatar-section {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 24px;
  padding: 30px 20px;
  text-align: center;
  margin-bottom: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.avatar-wrapper {
  position: relative;
  display: inline-block;
}

.avatar-ring {
  position: absolute;
  top: -5px;
  left: -5px;
  width: 110px;
  height: 110px;
  border-radius: 50%;
  background: linear-gradient(135deg, #f97316, #ef4444, #f97316);
  animation: rotate 3s linear infinite;
}

@keyframes rotate {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.user-avatar {
  position: relative;
  z-index: 2;
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.sport-badge {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 30px;
  height: 30px;
  background: #f97316;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  border: 2px solid white;
  z-index: 3;
}

.avatar-section h3 {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin: 16px 0 8px;
}

/* 运动数据卡片 */
.sport-stats {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 20px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-around;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.stat-item {
  text-align: center;
}

.stat-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.stat-content .stat-value {
  font-size: 20px;
  font-weight: 700;
  color: #f97316;
}

.stat-content .stat-label {
  font-size: 11px;
  color: #64748b;
}

.info-tips {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);
}

/* 右侧主内容 */
.profile-main {
  flex: 1;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 24px;
  padding: 24px 28px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.profile-form {
  margin-top: 16px;
}

.field-tip {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #1e293b;
  font-size: 14px;
}

:deep(.el-input__wrapper) {
  border-radius: 12px;
  background: #f8fafc;
  transition: all 0.2s;
}

:deep(.el-input__wrapper:hover) {
  background: white;
  border-color: #f97316;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #f97316;
  box-shadow: 0 0 0 2px rgba(249, 115, 22, 0.2);
}

:deep(.el-tabs__header) {
  margin-bottom: 20px;
}

:deep(.el-tabs__item) {
  font-size: 16px;
  font-weight: 500;
}

:deep(.el-tabs__item.is-active) {
  color: #f97316;
}

:deep(.el-tabs__active-bar) {
  background-color: #f97316;
}

.save-btn {
  background: linear-gradient(135deg, #f97316, #ef4444);
  border: none;
  padding: 12px 32px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 40px;
  transition: all 0.3s;
}

.save-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(249, 115, 22, 0.3);
}

@media (max-width: 768px) {
  .profile-container {
    flex-direction: column;
  }

  .profile-sidebar {
    width: 100%;
  }
}
</style>
