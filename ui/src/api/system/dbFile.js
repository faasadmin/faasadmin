import request from '@/utils/request'

// 删除文件
export function deleteFile(id) {
  return request({
    url: '/system/db/file/delete?id=' + id,
    method: 'delete'
  })
}

// 获得文件分页
export function getFilePage(query) {
  return request({
    url: '/system/db/file/page',
    method: 'get',
    params: query
  })
}
