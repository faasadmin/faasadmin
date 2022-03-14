import request from '@/utils/request'

// 获取路由
export const getRouters = (ascription) => {
  const data = {
    ascription
  }
  debugger
  return request({
    url: '/list-menus',
    method: 'get',
    params: data
  })
}
