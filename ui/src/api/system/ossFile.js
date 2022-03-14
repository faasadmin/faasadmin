import request from '@/utils/request'

// 下载oss文件
export function downloadOssFile(id) {
  return request({
    url: '/system/file/record/download/fileId/' + id,
    method: 'post',
    responseType: 'blob'
  })
}
