import request from '@/utils/request'

// 创建security认证地址配置
export function createAuthorizeUrl(data) {
  return request({
    url: '/system/authorizeUrl/create',
    method: 'post',
    data: data
  })
}

// 更新security认证地址配置
export function updateAuthorizeUrl(data) {
  return request({
    url: '/system/authorizeUrl/update',
    method: 'put',
    data: data
  })
}

// 删除security认证地址配置
export function deleteAuthorizeUrl(id) {
  return request({
    url: '/system/authorizeUrl/delete?id=' + id,
    method: 'delete'
  })
}

// 获得security认证地址配置
export function getAuthorizeUrl(id) {
  return request({
    url: '/system/authorizeUrl/get?id=' + id,
    method: 'get'
  })
}

// 获得security认证地址配置分页
export function getAuthorizeUrlPage(query) {
  return request({
    url: '/system/authorizeUrl/page',
    method: 'get',
    params: query
  })
}

// 导出security认证地址配置 Excel
export function exportAuthorizeUrlExcel(query) {
  return request({
    url: '/system/authorizeUrl/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
