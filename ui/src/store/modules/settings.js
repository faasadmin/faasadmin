import variables from '@/assets/styles/element-variables.scss'
import defaultSettings from '@/settings'

const { sideTheme, showSettings, topNav,tagsView, fixedHeader, sidebarLogo } = defaultSettings
console.info('配置：' + localStorage.getItem('layout-setting'))
const storageSetting = JSON.parse(localStorage.getItem('layout-setting')) || ''
debugger
const state = {
  theme: variables.theme,
  sideTheme: sideTheme,
  showSettings: showSettings,
  tagsView: tagsView,
  fixedHeader: fixedHeader,
  sidebarLogo: sidebarLogo,
  topNav:  storageSetting.topNav === undefined ? topNav : storageSetting.topNav
}

const mutations = {
  CHANGE_SETTING: (state, { key, value }) => {
    if (state.hasOwnProperty(key)) {
      state[key] = value
    }
  }
}

const actions = {
  changeSetting({ commit }, data) {
    commit('CHANGE_SETTING', data)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}

