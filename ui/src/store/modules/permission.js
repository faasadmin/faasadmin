import { constantRoutes } from '@/router'
import { getRouters } from '@/api/menu'
import Layout from '@/layout/index'
import ParentView from '@/components/ParentView';
import { registerPluginMenu, genDevMenu } from '@/config/plugin/registerPluginApp'
import {treeToAppMenuList} from "@/utils/ruoyi";
import de from "element-ui/src/locale/lang/de";

const permission = {
  state: {
    routes: [],
    addRoutes: [],
    sidebarRouters: []
  },
  mutations: {
    SET_ROUTES: (state, routes) => {
      state.addRoutes = routes
      state.routes = constantRoutes.concat(routes)
    },
    SET_DEFAULT_ROUTES: (state, routes) => {
      state.defaultRoutes = constantRoutes.concat(routes)
    },
    SET_TOPBAR_ROUTES: (state, routes) => {
      // 顶部导航菜单默认添加统计报表栏指向首页
      const index = [{
        path: 'index',
        meta: { title: '统计报表', icon: 'dashboard'}
      }]
      state.topbarRouters = routes.concat(index);
    },
    SET_SIDEBAR_ROUTERS: (state, routers) => {
      state.sidebarRouters = routers
    },
  },
  actions: {
    // 生成路由
    GenerateRoutes({ commit }) {
      return new Promise(resolve => {
        // 向后端请求路由数据
        getRouters("sys_menu_group").then(res => {
          debugger
          var pluginMs = genDevMenu();
          var menuData = res.data.sysMenus.concat(pluginMs);
          const sdata = JSON.parse(JSON.stringify(menuData))
          const rdata = JSON.parse(JSON.stringify(menuData))
          const sidebarRoutes = filterAsyncRouter(sdata)
          const rewriteRoutes = filterAsyncRouter(rdata, true)
          // 注册插件界面
          const mlist = treeToList(res.data.pluginMenus)
          registerPluginMenu(mlist)
          debugger
          rewriteRoutes.push({ path: '*', redirect: '/404', hidden: true })
          commit('SET_ROUTES', rewriteRoutes)
          commit('SET_SIDEBAR_ROUTERS', constantRoutes.concat(sidebarRoutes))
          commit('SET_DEFAULT_ROUTES', sidebarRoutes)
          commit('SET_TOPBAR_ROUTES', sidebarRoutes)
          resolve(rewriteRoutes)
        })
      })
    }
  }
}

/**
 * 树转list
 */
function treeToList(tree){
  for(var i in tree){
    var node = tree[i];
    var list = [];  //结果lsit
    if (node.children.length !== 0) {  //遍历树的第一层,只有一个根结点
      list.push({
        id: node.id,
        menuAlias: node.menuAlias,
        menuPath: node.menuPath,
        rootRouting: node.rootRouting,
        parentId:0
      });
      toList(node.children, list, node.id);  //遍历子树,并加入到list中.
    }
  }
  return list;
}

/**
 * 深度优先遍历树
 * 一个递归方法
 * @params tree:要转换的树结构数据
 * @params list:保存结果的列表结构数据，初始传list = []
 * @params parentId:当前遍历节点的父级节点id，初始为null(因为根节点无parentId)
 **/
function toList (tree, list, parentId) {
  for (var i in tree) { //遍历最上层
    //将当前树放入list中
    var node = tree[i];
    list.push({
      id: node.id,
      menuAlias: node.menuAlias,
      menuPath: node.menuPath,
      rootRouting: node.rootRouting,
      parentId:parentId
    });
    //如果有子结点,再遍历子结点
    debugger
    if (node.children != null) {
      toList(node.children, list, node.id)  //递归
    }
  }
}


// 遍历后台传来的路由字符串，转换为组件对象
function filterAsyncRouter(asyncRouterMap, isRewrite = false) {
  return asyncRouterMap.filter(route => {
    // 将 ruoyi 后端原有耦合前端的逻辑，迁移到此处
    // 处理 meta 属性
    route.meta = {
      title: route.name,
      icon: route.icon,
      noCache: !route.keepAlive,
    }
    //route.hidden = !route.visible
    // 处理 component 属性
    if (route.children) { // 父节点
      // debugger
      if (route.parentId === 0) {
        route.component = Layout
      } else {
        route.component = ParentView
      }
    } else { // 根节点
      route.component = loadView(route.component)
    }

    // filterChildren
    if (isRewrite && route.children) {
      route.children = filterChildren(route.children)
    }
    if (route.children != null && route.children && route.children.length) {
      route.children = filterAsyncRouter(route.children, route, isRewrite)
    }
    return true
  })
}

function filterChildren(childrenMap) {
  var children = []
  childrenMap.forEach((el, index) => {
    if (el.children && el.children.length) {
      if (el.component === 'ParentView') {
        el.children.forEach(c => {
          c.path = el.path + '/' + c.path
          if (c.children && c.children.length) {
            children = children.concat(filterChildren(c.children, c))
            return
          }
          children.push(c)
        })
        return
      }
    }
    children = children.concat(el)
  })
  return children
}

export const loadView = (view) => { // 路由懒加载
  return (resolve) => require([`@/views/${view}`], resolve)
}

export default permission
