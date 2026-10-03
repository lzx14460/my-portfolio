<template>
  <div class="auth-page">
    <!-- 背景动态层：运动明星 + 光效 -->
    <div class="hero-bg">
      <div class="overlay"></div>
      <div class="athlete-cutout"></div>
    </div>

    <!-- 主容器：登录 / 注册卡片切换 -->
    <div class="glass-card">
      <!-- 品牌区 -->
      <div class="brand">
        <div class="logo-icon">🏆</div>
        <h1>智慧校园体育</h1>
        <p>预约 · 记录 · 突破</p>
      </div>

      <!-- Tab 切换 -->
      <div class="tabs">
        <button
          :class="['tab', { active: activeTab === 'login' }]"
          @click="activeTab = 'login'"
        >
          登录
        </button>
        <button
          :class="['tab', { active: activeTab === 'register' }]"
          @click="activeTab = 'register'"
        >
          注册
        </button>
      </div>

      <!-- 登录表单 -->
      <el-form
        v-if="activeTab === 'login'"
        :model="loginForm"
        :rules="loginRules"
        ref="loginFormRef"
        class="auth-form"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名 / 学号"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-button
          type="primary"
          size="large"
          :loading="loginLoading"
          @click="handleLogin"
          class="submit-btn"
        >
          {{ loginLoading ? '登录中...' : '登录' }}
        </el-button>
      </el-form>

      <!-- 注册表单 -->
      <el-form
        v-if="activeTab === 'register'"
        :model="registerForm"
        :rules="registerRules"
        ref="registerFormRef"
        class="auth-form"
      >
        <!-- 第一行：用户名 + 真实姓名 -->
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item prop="username">
              <el-input
                v-model="registerForm.username"
                placeholder="用户名"
                prefix-icon="User"
                size="large"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item prop="name">
              <el-input
                v-model="registerForm.name"
                placeholder="真实姓名"
                prefix-icon="Edit"
                size="large"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 第二行：手机号 + 邮箱 -->
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item prop="phone">
              <el-input
                v-model="registerForm.phone"
                placeholder="手机号（11位数字）"
                prefix-icon="Phone"
                size="large"
                maxlength="11"
                show-word-limit
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item prop="email">
              <el-input
                v-model="registerForm.email"
                placeholder="邮箱（选填）"
                prefix-icon="Message"
                size="large"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 第三行：学号 + 学院 -->
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item prop="studentId">
              <el-input
                v-model="registerForm.studentId"
                placeholder="学号"
                prefix-icon="School"
                size="large"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item prop="college">
              <el-input
                v-model="registerForm.college"
                placeholder="学院"
                prefix-icon="OfficeBuilding"
                size="large"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 第四行：班级 + 密码 -->
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item prop="className">
              <el-input
                v-model="registerForm.className"
                placeholder="班级"
                prefix-icon="Grid"
                size="large"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="密码"
                prefix-icon="Lock"
                size="large"
                show-password
              />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 第五行：确认密码 -->
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="确认密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>

        <!-- 角色选择 -->
        <el-form-item prop="role">
          <el-radio-group v-model="registerForm.role" class="role-group">
            <el-radio :value="0" class="role-item">
              <span>👨‍🎓 普通学生</span>
            </el-radio>
            <el-radio :value="1" class="role-item">
              <span>🏟️ 场馆管理员</span>
            </el-radio>
            <el-radio :value="2" class="role-item">
              <span>⚙️ 系统管理员</span>
            </el-radio>
          </el-radio-group>
          <div class="role-tip" v-if="registerForm.role > 0">
            <el-icon><InfoFilled /></el-icon>
            <span>管理员角色需要超级管理员审批后才能生效</span>
          </div>
        </el-form-item>

        <el-button
          type="primary"
          size="large"
          :loading="registerLoading"
          @click="handleRegister"
          class="submit-btn"
        >
          {{ registerLoading ? '注册中...' : '立即注册' }}
        </el-button>
      </el-form>

      <div class="footer-note">
        <span>🏀 挑战自己 · 永不止步</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { InfoFilled } from '@element-plus/icons-vue'
import axios from 'axios'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const activeTab = ref('login')

// ----- 登录相关 -----
const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const loginFormRef = ref(null)
const loginLoading = ref(false)

const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    loginLoading.value = true
    try {
      const result = await userStore.login(loginForm)
      console.log('登录结果:', result)

      if (result.success) {
        ElMessage.success('登录成功，欢迎回来！')
        // 设置登录标记，用于首页弹窗
        sessionStorage.setItem('justLoggedIn', 'true')
        router.push('/')
      } else {
        ElMessage.error(result.message || '登录失败')
      }
    } catch (err) {
      console.error('登录失败:', err)
      ElMessage.error('网络错误，请稍后重试')
    } finally {
      loginLoading.value = false
    }
  })
}

// 手机号验证
const validatePhone = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入手机号'))
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的11位手机号'))
  } else {
    callback()
  }
}

// 邮箱验证（选填，有值时验证格式）
const validateEmail = (rule, value, callback) => {
  if (value && !/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(value)) {
    callback(new Error('请输入正确的邮箱地址'))
  } else {
    callback()
  }
}

// 学号验证
const validateStudentId = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入学号'))
  } else if (value.length > 20) {
    callback(new Error('学号长度不能超过20位'))
  } else {
    callback()
  }
}

// 学院验证
const validateCollege = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入学院'))
  } else if (value.length > 50) {
    callback(new Error('学院名称长度不能超过50位'))
  } else {
    callback()
  }
}

// 班级验证
const validateClassName = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入班级'))
  } else if (value.length > 50) {
    callback(new Error('班级名称长度不能超过50位'))
  } else {
    callback()
  }
}

// ----- 注册相关 -----
const registerForm = reactive({
  username: '',
  name: '',
  phone: '',
  email: '',
  studentId: '',
  college: '',
  className: '',
  password: '',
  confirmPassword: '',
  role: 0
})

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { validator: validatePhone, trigger: 'blur' }
  ],
  email: [
    { validator: validateEmail, trigger: 'blur' }
  ],
  studentId: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    { validator: validateStudentId, trigger: 'blur' }
  ],
  college: [
    { required: true, message: '请输入学院', trigger: 'blur' },
    { validator: validateCollege, trigger: 'blur' }
  ],
  className: [
    { required: true, message: '请输入班级', trigger: 'blur' },
    { validator: validateClassName, trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度 6-20 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  role: [
    { required: true, message: '请选择注册身份', trigger: 'change' }
  ]
}

const registerFormRef = ref(null)
const registerLoading = ref(false)

const handleRegister = async () => {
  if (!registerFormRef.value) return
  await registerFormRef.value.validate(async (valid) => {
    if (!valid) return
    registerLoading.value = true
    try {
      const res = await axios.post('http://localhost:8080/api/user/register', {
        username: registerForm.username,
        name: registerForm.name,
        phone: registerForm.phone,
        email: registerForm.email,
        studentId: registerForm.studentId,
        college: registerForm.college,
        className: registerForm.className,
        password: registerForm.password,
        role: registerForm.role
      }, { withCredentials: true })

      if (res.data.code === 200) {
        const roleName = registerForm.role === 0 ? '普通用户' :
                        (registerForm.role === 1 ? '场馆管理员' : '系统管理员')

        if (registerForm.role > 0) {
          ElMessage.success(`注册成功，已提交${roleName}申请，请等待管理员审批`)
        } else {
          ElMessage.success('注册成功，请登录')
        }

        activeTab.value = 'login'
        // 清空注册表单
        registerForm.username = ''
        registerForm.name = ''
        registerForm.phone = ''
        registerForm.email = ''
        registerForm.studentId = ''
        registerForm.college = ''
        registerForm.className = ''
        registerForm.password = ''
        registerForm.confirmPassword = ''
        registerForm.role = 0
      } else {
        ElMessage.error(res.data.message || '注册失败，用户名或手机号可能已存在')
      }
    } catch (err) {
      console.error('注册失败:', err)
      ElMessage.error('注册失败，请稍后重试')
    } finally {
      registerLoading.value = false
    }
  })
}

</script>

<style scoped>
/* 全屏运动氛围 */
.auth-page {
  height: 100vh;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

/* 动态背景层（融合明星素材 + 炫光） */
.hero-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(145deg, #0a0f2c 0%, #0a1f3a 100%);
  z-index: 0;
}

.overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle at 20% 40%, rgba(255, 100, 40, 0.2), transparent 60%);
  pointer-events: none;
}

/* 运动员剪影区域（潮流明星风格） */
.athlete-cutout {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 55%;
  height: 90%;
  background: url('https://images.unsplash.com/photo-1546519638-68e109498ffc?q=80&w=2780&auto=format') center/cover no-repeat;
  opacity: 0.3;
  mix-blend-mode: overlay;
  pointer-events: none;
}

/* 毛玻璃卡片 */
.glass-card {
  position: relative;
  z-index: 2;
  width: 650px;
  max-width: 92%;
  padding: 32px 28px 40px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  border-radius: 48px;
  box-shadow: 0 30px 45px rgba(0, 0, 0, 0.3), 0 0 0 1px rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
}

.brand {
  text-align: center;
  margin-bottom: 20px;
}

.logo-icon {
  font-size: 48px;
  background: linear-gradient(135deg, #f97316, #ef4444);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  margin-bottom: 8px;
}

.brand h1 {
  font-size: 28px;
  font-weight: 700;
  letter-spacing: -0.5px;
  background: linear-gradient(135deg, #1e293b, #334155);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  margin: 0;
}

.brand p {
  font-size: 14px;
  color: #f97316;
  font-weight: 500;
  margin-top: 6px;
}

/* Tab 切换 */
.tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 28px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  padding-bottom: 6px;
}

.tab {
  flex: 1;
  background: none;
  border: none;
  font-size: 18px;
  font-weight: 600;
  padding: 8px 0;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}

.tab.active {
  color: #f97316;
}

.tab.active::after {
  content: '';
  position: absolute;
  bottom: -7px;
  left: 0;
  width: 100%;
  height: 3px;
  background: #f97316;
  border-radius: 4px;
}

/* 表单样式 */
.auth-form {
  margin-top: 12px;
}

:deep(.el-input__wrapper) {
  background-color: #f8fafc;
  border-radius: 40px;
  box-shadow: none;
  border: 1px solid #e2e8f0;
  transition: all 0.2s;
}

:deep(.el-input__wrapper:hover) {
  border-color: #f97316;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #f97316;
  box-shadow: 0 0 0 2px rgba(249, 115, 22, 0.2);
}

/* 角色选择样式 */
.role-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.role-item {
  width: 100%;
  margin: 0;
  padding: 12px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  transition: all 0.2s;
}

.role-item:hover {
  border-color: #f97316;
  background-color: rgba(249, 115, 22, 0.05);
}

:deep(.role-item .el-radio__label) {
  width: 100%;
  font-size: 14px;
  font-weight: 500;
  color: #334155;
}

.role-tip {
  margin-top: 8px;
  padding: 8px 12px;
  background-color: #fef3c7;
  border-radius: 8px;
  font-size: 12px;
  color: #d97706;
  display: flex;
  align-items: center;
  gap: 6px;
}

.submit-btn {
  width: 100%;
  margin-top: 18px;
  background: linear-gradient(95deg, #f97316, #ef4444);
  border: none;
  border-radius: 60px;
  font-weight: 600;
  font-size: 16px;
  transition: 0.2s;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 18px -8px rgba(249, 115, 22, 0.5);
}

.footer-note {
  text-align: center;
  margin-top: 28px;
  font-size: 12px;
  color: #94a3b8;
  font-weight: 500;
  letter-spacing: 0.5px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .glass-card {
    width: 95%;
    padding: 24px 20px;
  }

  .glass-card .el-row {
    flex-direction: column;
  }

  .glass-card .el-col {
    width: 100%;
    margin-bottom: 0;
  }
}
</style>
