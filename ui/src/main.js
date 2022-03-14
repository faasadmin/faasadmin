import Vue from 'vue'

import Cookies from 'js-cookie'
import pinyin from 'js-pinyin'
import Element from 'element-ui'
import './assets/styles/element-variables.scss'

import '@/assets/styles/index.scss' // global css
import '@/assets/styles/ruoyi.scss' // ruoyi css
import App from './App'
import store from './store'
import router from './router'
import permission from './directive/permission'

import './assets/icons' // icon
import './permission' // permission control
import { getDicts } from "@/api/system/dict/data";
import { getConfigKey } from "@/api/infra/config";
import {
  parseTime,
  resetForm,
  addDateRange,
  addBeginAndEndTime,
  selectDictLabel,
  download,
  handleTree,
  treeToAppMenuList,
  isEmpty,
  isNotEmpty,
  downloadExcel,
  downloadWord,
  downloadZip,
  downloadHtml,
  downloadMarkdown, storageUnit, getOssUrl, downloadOctet
} from "@/utils/ruoyi";
import Pagination from "@/components/Pagination";
// 自定义表格工具扩展
import RightToolbar from "@/components/RightToolbar"
// 富文本组件
import Editor from "@/components/Editor"
// 文件上传组件
import FileUpload from "@/components/FileUpload"
// 图片上传组件
import ImageUpload from "@/components/ImageUpload"
// 代码高亮插件
// import hljs from 'highlight.js'
// import 'highlight.js/styles/github-gist.css'
import {DICT_TYPE, getDictDataLabel, getDictDatas} from "@/utils/dict";

// 全局方法挂载
Vue.prototype.getDicts = getDicts
Vue.prototype.pinyin = pinyin
Vue.prototype.getConfigKey = getConfigKey
Vue.prototype.parseTime = parseTime
Vue.prototype.resetForm = resetForm
Vue.prototype.addDateRange = addDateRange
Vue.prototype.addBeginAndEndTime = addBeginAndEndTime
Vue.prototype.selectDictLabel = selectDictLabel
Vue.prototype.getDictDatas = getDictDatas
Vue.prototype.getDictDataLabel = getDictDataLabel
Vue.prototype.DICT_TYPE = DICT_TYPE
Vue.prototype.download = download
Vue.prototype.downloadExcel = downloadExcel
Vue.prototype.downloadOctet = downloadOctet
Vue.prototype.downloadWord = downloadWord
Vue.prototype.downloadHtml = downloadHtml
Vue.prototype.downloadMarkdown = downloadMarkdown
Vue.prototype.downloadZip = downloadZip
Vue.prototype.handleTree = handleTree
Vue.prototype.treeToAppMenuList = treeToAppMenuList
Vue.prototype.isEmpty = isEmpty
Vue.prototype.isNotEmpty = isNotEmpty
Vue.prototype.storageUnit = storageUnit
Vue.prototype.getOssUrl = getOssUrl

Vue.prototype.msgSuccess = function (msg) {
  this.$message({ showClose: true, message: msg, type: "success" });
}

Vue.prototype.msgError = function (msg) {
  this.$message({ showClose: true, message: msg, type: "error" });
}

Vue.prototype.msgInfo = function (msg) {
  this.$message.info(msg);
}

// 全局组件挂载
Vue.component('Pagination', Pagination)
Vue.component('RightToolbar', RightToolbar)
Vue.component('Editor', Editor)
Vue.component('FileUpload', FileUpload)
Vue.component('ImageUpload', ImageUpload)

Vue.use(permission)
// Vue.use(hljs.vuePlugin);

/**
 * If you don't want to use mock-server
 * you want to use MockJs for mock api
 * you can execute: mockXHR()
 *
 * Currently MockJs will be used in the production environment,
 * please remove it before going online! ! !
 */

Vue.use(Element, {
  size: Cookies.get('size') || 'medium' // set element-ui default size
})

Vue.config.productionTip = false
debugger
console.info('路由；' + router);
new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
