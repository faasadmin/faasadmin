import request from '@/utils/request'


// 使用租户名，获得租户编号
export function getLesseeIdByName(name) {
  return request({
    url: '/saas/lessee/get-id-by-name',
    method: 'get',
    params: {
      name
    }
  })
}

// 创建租户
export function createLessee(data) {
  return request({
    url: '/saas/lessee/create',
    method: 'post',
    data: data
  })
}

// 更新租户
export function updateLessee(data) {
  return request({
    url: '/saas/lessee/update',
    method: 'put',
    data: data
  })
}

// 删除租户
export function deleteLessee(id) {
  return request({
    url: '/saas/lessee/delete?id=' + id,
    method: 'delete'
  })
}

// 获得租户
export function getLessee(id) {
  return request({
    url: '/saas/lessee/get?id=' + id,
    method: 'get'
  })
}

// 获得租户分页
export function getLesseePage(query) {
  return request({
    url: '/saas/lessee/page',
    method: 'get',
    params: query
  })
}

// 导出租户 Excel
export function exportLesseeExcel(query) {
  return request({
    url: '/saas/lessee/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}

//获取租户初始化数据
export function getLesseeInitData() {
  return request({
    url: '/saas/lessee/init-data',
    method: 'get',
  })
}
