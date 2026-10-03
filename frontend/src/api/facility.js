import request from '@/utils/request'

// 获取所有设施
export const getFacilityList = () => {
  return request({
    url: '/facility/list',
    method: 'get'
  })
}
