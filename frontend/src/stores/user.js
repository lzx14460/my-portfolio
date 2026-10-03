import { defineStore } from 'pinia'
import { login, logout, getCurrentUser } from '@/api/user'

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: null,
    isLogin: false,
  }),

  getters: {
    userId: (state) => state.userInfo?.id || null,
    userName: (state) => state.userInfo?.name || '',
    userRole: (state) => state.userInfo?.role || null,
  },

  actions: {
    async login(loginForm) {
      try {
        const res = await login(loginForm)
        console.log('登录返回完整数据:', res.data)
        console.log('role 字段值:', res.data.role)

        if (res.code === 200) {
          this.userInfo = res.data
          this.isLogin = true
          localStorage.setItem('userInfo', JSON.stringify(res.data))
          console.log('保存到 store 的 userInfo:', this.userInfo)
          return { success: true, data: res.data }
        }
        return { success: false, message: res.message }
      } catch (error) {
        console.error('登录错误:', error)
        return { success: false, message: '登录失败，请稍后重试' }
      }
    },

    async logout() {
      try {
        await logout()
      } catch (error) {
        console.error('登出错误:', error)
      } finally {
        this.userInfo = null
        this.isLogin = false
        localStorage.removeItem('userInfo')
      }
    },

    async getCurrentUser() {
  try {
    console.log('1. getCurrentUser 开始执行')
    const res = await getCurrentUser()
    console.log('2. getCurrentUser 接口返回:', res)
    console.log('3. res.code:', res?.code)
    console.log('4. res.data:', res?.data)

    if (res && res.code === 200) {
      this.userInfo = res.data
      this.isLogin = true
      localStorage.setItem('userInfo', JSON.stringify(res.data))
      console.log('5. 设置 userInfo 成功:', this.userInfo)
      console.log('6. userInfo.role:', this.userInfo?.role)
    } else {
      console.log('7. 接口返回非200，尝试从 localStorage 恢复')
      const storedUser = localStorage.getItem('userInfo')
      if (storedUser) {
        this.userInfo = JSON.parse(storedUser)
        this.isLogin = true
        console.log('8. 从 localStorage 恢复:', this.userInfo)
      }
    }
  } catch (error) {
    console.error('9. getCurrentUser 请求失败:', error)
    const storedUser = localStorage.getItem('userInfo')
    if (storedUser) {
      this.userInfo = JSON.parse(storedUser)
      this.isLogin = true
      console.log('10. 异常后从 localStorage 恢复:', this.userInfo)
    }
  }
},
  },
})
