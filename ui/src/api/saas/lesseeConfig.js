import request from '@/utils/request'

// 查询租户系统配置
export function systemConfig(query) {
  return request({
    url: '/saas/lessee-config/system',
    method: 'get',
    params: query
  })
}

// 创建租户配置
export function createLesseeConfig(data) {
  return request({
    url: '/saas/lessee-config/create',
    method: 'post',
    data: data
  })
}

// 更新租户配置
export function updateLesseeConfig(data) {
  return request({
    url: '/saas/lessee-config/update',
    method: 'put',
    data: data
  })
}

// 删除租户配置
export function deleteLesseeConfig(id) {
  return request({
    url: '/saas/lessee-config/delete?id=' + id,
    method: 'delete'
  })
}

// 获得租户配置
export function getLesseeConfig(id) {
  return request({
    url: '/saas/lessee-config/get?id=' + id,
    method: 'get'
  })
}

// 获得租户配置分页
export function getLesseeConfigPage(query) {
  return request({
    url: '/saas/lessee-config/page',
    method: 'get',
    params: query
  })
}

// 导出租户配置 Excel
export function exportLesseeConfigExcel(query) {
  return request({
    url: '/saas/lessee-config/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
