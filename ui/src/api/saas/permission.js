import request from '@/utils/request'

// 查询模块拥有的菜单数组
export function listModuleMenus(moduleId) {
  return request({
    url: '/saas/module-authorize/list-module-menu?moduleId=' + moduleId,
    method: 'get'
  })
}

// 赋予模块菜单
export function assignModuleMenu(data) {
  return request({
    url: '/saas/module-authorize/assign-module-menu',
    method: 'post',
    data: data
  })
}

// 查询租户拥有的模块数组
export function listLesseeModule(lesseeId) {
  return request({
    url: '/saas/module-authorize/list-lessee-module?lesseeId=' + lesseeId,
    method: 'get'
  })
}

// 赋予用户模块
export function assignLesseeModule(data) {
  return request({
    url: '/saas/module-authorize/assign-lessee-module',
    method: 'post',
    data: data
  })
}
