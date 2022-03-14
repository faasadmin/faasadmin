import request from '@/utils/request'

// 创建行政区域
export function createRegion(data) {
  return request({
    url: '/system/region/create',
    method: 'post',
    data: data
  })
}

// 更新行政区域
export function updateRegion(data) {
  return request({
    url: '/system/region/update',
    method: 'put',
    data: data
  })
}

// 删除行政区域
export function deleteRegion(id) {
  return request({
    url: '/system/region/delete?id=' + id,
    method: 'delete'
  })
}

// 获得行政区域
export function getRegion(id) {
  return request({
    url: '/system/region/get?id=' + id,
    method: 'get'
  })
}

// 获得行政区域分页
export function getRegionPage(query) {
  return request({
    url: '/system/region/page',
    method: 'get',
    params: query
  })
}

// 导出行政区域 Excel
export function exportRegionExcel(query) {
  return request({
    url: '/system/region/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
