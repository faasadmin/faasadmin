import store from '@/store'
import {registerMicroApps} from 'qiankun'
import config from '@/utils/config'
import {getDevApp, getDevMenu} from './devPluginConfig'

const genActiveRule = hash => location => location.hash.startsWith(hash)

export const registerPluginMenu = (pluginWebInfos) => {
  debugger
  if (config.isDev()) {
    // 在开发环境下获取开发环境配置的microApp
    pluginWebInfos = getDevApp()
  }
  if (pluginWebInfos) {
    const registerMicroApp = []
    const props = getProps()
    console.info('微应用传参数：' + props)
    debugger
    pluginWebInfos.forEach(pluginWebInfo => {
      registerMicroApp.push({
        name: pluginWebInfo.menuAlias, // app name registered
        entry: pluginWebInfo.menuPath,
        container: '#micro-view',
        activeRule: pluginWebInfo.rootRouting,
        //activeRule: genActiveRule(`#${pluginWebInfo.rootRouting}`),
        props: props
      })
    })
    registerMicroApp.map(app => {
      // 将监听的路由下发到子应用的base
      const routerBase = app.activeRule
      console.info('qiankun 路由：' + routerBase)
      app.props = {
        routerBase
      }
      return app
    });
    registerMicroApps(registerMicroApp, {
      beforeLoad: app => console.log('qiankun beforeLoad ===> ', app.name),
      beforeMount: app => console.log('qiankun beforeMount ===> ', app.name),
      afterMount: app => console.log('qiankun afterMount ===> ', app.name),
      beforeUnmount: app => console.log('qiankun beforeUnmount ===> ', app.name),
      afterUnmount: app => console.log('qiankun afterUnmount ===> ', app.name)
    })
  }
}

export const genDevMenu = () => {
  if (config.isDev()) {
    return getDevMenu()
  } else {
    return []
  }
}

const getProps = () => {
  const props = {}
  props.token = store.getters.token
  return props
}
