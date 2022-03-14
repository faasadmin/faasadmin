// 开发环境下配置插件菜单和app信息
const systemToolsRootRoute = '/mszf-applets-addons'
const pluginWebAppInfos = [
  {
    menuAlias: 'mszf-applets-addons',
    rootRouting:'/mszf-applets-addons'
  }
]

const navigationInfos = []

export const getDevApp = () => {
  return pluginWebAppInfos
}

export const getDevMenu = () => {
  return navigationInfos
}

const getProps = () => {
  const props = {}
  props.token = store.getters.token
  return props
}
