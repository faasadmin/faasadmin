import {
  isEmpty,
  isNotEmpty
} from "@/utils/ruoyi";
const Config = {
  getConfig () {
    return window.config
  },
  getPasswordEncryption () {
    if (window.config.passwordEncryption) {
      return window.config.passwordEncryption
    } else {
      return false
    }
  },
  getEnv () {
    debugger
    if (isNotEmpty(process.env.NODE_ENV)) {
      return process.env.NODE_ENV;
    }else {
      return window.config.env
    }
  },
  isDev () {
    return this.getEnv() === 'development'
  },
  isProd () {
    return this.getEnv() === 'production'
  },
  getServerUrl () {
    return window.config.serverUrl
  },
  getApiUrl (suffix) {
    if (suffix && !suffix.startsWith('/')) {
      suffix = '/' + suffix
    }
    return `${window.config.serverUrl}/api${suffix}`
  }
}
export default Config
