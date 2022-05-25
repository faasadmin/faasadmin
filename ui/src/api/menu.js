import request from '@/utils/request'

// 获取路由
export const getRouters = (ascription) => {
  const data = {
    ascription
  }
  return request({
    url: '/system/auth/list-menus',
    method: 'get',
    params: data
  })
}
