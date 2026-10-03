<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
import { onMounted } from 'vue'

const userStore = useUserStore()

onMounted(async () => {
  console.log('App.vue: 开始获取用户信息')
  // 刷新验证
  if (import.meta.env.DEV) {
    localStorage.removeItem('userInfo')
  }
  await userStore.getCurrentUser()
  console.log('App.vue: 获取完成, userInfo:', userStore.userInfo)
  console.log('App.vue: 获取完成, userRole:', userStore.userRole)
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}
</style>
