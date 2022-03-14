import request from '@/utils/request'

// 获取系统证书信息
export function getLicenseInfo(query) {
  return request({
    url: '/system/common/license-info',
    method: 'get',
    params: query
  })
}


//校验验证码
export function checkVerifySn(data){
  return request({
    url: '/system/license/verify-sn',
    method: 'post',
    params: data
  })
}

// 创建产品许可
export function createSysLicense(data) {
  return request({
    url: '/system/license/create',
    method: 'post',
    data: data
  })
}

// 更新产品许可
export function updateSysLicense(data) {
  return request({
    url: '/system/license/update',
    method: 'put',
    data: data
  })
}


// 下载产品许可
export function downloadSysLicense(id) {
  return request({
    url: '/system/license/download-id?id=' +id,
    method: 'post',
    responseType: 'blob'
  })
}


// 删除产品许可
export function deleteSysLicense(id) {
  return request({
    url: '/system/license/delete?id=' + id,
    method: 'delete'
  })
}

// 获得产品许可
export function getSysLicense(id) {
  return request({
    url: '/system/license/get?id=' + id,
    method: 'get'
  })
}

// 获得产品许可分页
export function getSysLicensePage(query) {
  return request({
    url: '/system/license/page',
    method: 'get',
    params: query
  })
}

// 导出产品许可 Excel
export function exportSysLicenseExcel(query) {
  return request({
    url: '/system/license/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
