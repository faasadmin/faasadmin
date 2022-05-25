import request from '@/utils/request'

// 创建文件
export function createFile(data) {
  return request({
    url: '/system/file-upload/create',
    method: 'post',
    data: data
  })
}

// 更新文件
export function updateFile(data) {
  return request({
    url: '/system/file-upload/update',
    method: 'put',
    data: data
  })
}

// 删除文件
export function deleteFile(id) {
  return request({
    url: '/system/file-upload/delete?id=' + id,
    method: 'delete'
  })
}

// 获得文件
export function getFile(id) {
  return request({
    url: '/system/file-upload/get?id=' + id,
    method: 'get'
  })
}

// 获得文件分页
export function getFilePage(query) {
  return request({
    url: '/system/file-upload/page',
    method: 'get',
    params: query
  })
}

// 导出文件 Excel
export function exportFileExcel(query) {
  return request({
    url: '/system/file-upload/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
