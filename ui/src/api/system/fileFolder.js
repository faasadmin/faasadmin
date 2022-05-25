import request from '@/utils/request'


// 获取文件夹精简信息列表
export function listSimpleFolders() {
  return request({
    url: '/system/file-folder/list-all-simple',
    method: 'get'
  })
}

// 创建文件夹
export function createFileFolder(data) {
  return request({
    url: '/system/file-folder/create',
    method: 'post',
    data: data
  })
}

// 更新文件夹
export function updateFileFolder(data) {
  return request({
    url: '/system/file-folder/update',
    method: 'put',
    data: data
  })
}

// 删除文件夹
export function deleteFileFolder(id) {
  return request({
    url: '/system/file-folder/delete?id=' + id,
    method: 'delete'
  })
}

// 获得文件夹
export function getFileFolder(id) {
  return request({
    url: '/system/file-folder/get?id=' + id,
    method: 'get'
  })
}

// 获得文件夹分页
export function getFileFolderPage(query) {
  return request({
    url: '/system/file-folder/page',
    method: 'get',
    params: query
  })
}

// 导出文件夹 Excel
export function exportFileFolderExcel(query) {
  return request({
    url: '/system/file-folder/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
