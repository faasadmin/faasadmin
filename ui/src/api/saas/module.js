import request from '@/utils/request'

// 创建模块
export function createModule(data) {
  return request({
    url: '/saas/module/create',
    method: 'post',
    data: data
  })
}

// 更新模块
export function updateModule(data) {
  return request({
    url: '/saas/module/update',
    method: 'put',
    data: data
  })
}

// 删除模块
export function deleteModule(id) {
  return request({
    url: '/saas/module/delete?id=' + id,
    method: 'delete'
  })
}

// 获得模块
export function getModule(id) {
  return request({
    url: '/saas/module/get?id=' + id,
    method: 'get'
  })
}

// 获得所有模块
export function getAllModule() {
  return request({
    url: '/saas/module/all',
    method: 'get'
  })
}

// 获得模块分页
export function getModulePage(query) {
  return request({
    url: '/saas/module/page',
    method: 'get',
    params: query
  })
}

// 导出模块 Excel
export function exportModuleExcel(query) {
  return request({
    url: '/saas/module/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
