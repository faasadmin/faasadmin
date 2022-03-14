const state = {
  configData: {},
  firstFlag:true
}

const mutations = {
  SET_CONFIG: (state, data) => {
    state.configData = data
  },
  SET_FIRST_FLAG: (state, data) => {
    state.firstFlag = data
  }
}

const actions = {
  setConfig({ commit }, data) {
    commit('SET_CONFIG', data)
  },
  setFirstFlag({ commit }, data) {
    commit('SET_FIRST_FLAG', data)
  },
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
