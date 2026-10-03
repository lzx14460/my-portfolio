import request from '@/utils/request'

// 用户登录
export const login = (data) => {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

// 用户登出
export const logout = () => {
  return request({
    url: '/user/logout',
    method: 'post'
  })
}

// 获取当前用户信息
export const getCurrentUser = () => {
  return request({
    url: '/user/current',
    method: 'get'
  })
}
