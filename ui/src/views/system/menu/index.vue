<template>
  <div class="app-container">

    <template>
      <el-tabs v-model="activeName" @tab-click="handleTabClick">
        <el-tab-pane v-for="dict in menuAscription" :key="dict.value" :label="dict.label"
                     :name="dict.value"></el-tab-pane>
      </el-tabs>
    </template>

    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams">
      <el-form-item label="菜单名称" prop="name">
        <el-input v-model="queryParams.name" clearable placeholder="请输入菜单名称" size="small"
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" clearable placeholder="菜单状态" size="small">
          <el-option v-for="dict in statusDictDatas" :key="parseInt(dict.value)" :label="dict.label"
                     :value="parseInt(dict.value)"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button icon="el-icon-search" size="mini" type="cyan" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button v-hasPermi="['system:menu:create']" icon="el-icon-plus" size="mini" type="primary"
                   @click="handleAdd">新增
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="menuList" :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
              row-key="id">
      <el-table-column :show-overflow-tooltip="true" label="菜单名称" prop="name" width="200"></el-table-column>
      <el-table-column :show-overflow-tooltip="true" :formatter="typeFormat" label="菜单类型" prop="type" width="200"></el-table-column>
      <el-table-column align="center" label="图标" prop="icon" width="100">
        <template slot-scope="scope">
          <svg-icon :icon-class="scope.row.icon"/>
        </template>
      </el-table-column>
      <el-table-column label="排序" prop="sort" width="60"></el-table-column>
      <el-table-column :show-overflow-tooltip="true" label="权限标识" prop="permission"></el-table-column>
      <el-table-column :show-overflow-tooltip="true" label="组件路径" prop="component"></el-table-column>
      <el-table-column :formatter="statusFormat" label="菜单状态" prop="status" width="80"></el-table-column>
      <el-table-column :formatter="hiddenFormat" label="是否显示" prop="hidden" width="80"></el-table-column>
      <el-table-column align="center" label="创建时间" prop="createTime">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template slot-scope="scope">
          <el-button v-hasPermi="['system:menu:update']" icon="el-icon-edit" size="mini" type="text"
                     @click="handleUpdate(scope.row)">修改
          </el-button>
          <el-button v-hasPermi="['system:menu:create']" icon="el-icon-plus" size="mini" type="text"
                     @click="handleAdd(scope.row)">新增
          </el-button>
          <el-button v-hasPermi="['system:menu:delete']" icon="el-icon-delete" size="mini" type="text"
                     @click="handleDelete(scope.row)">删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加或修改菜单对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="600px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="上级菜单">
              <treeselect v-model="form.parentId" :normalizer="normalizer" :options="menuOptions" :show-count="true"
                          placeholder="选择上级菜单"/>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="应用系统" prop="type">
              <el-select v-model="form.ascription" placeholder="请选择应用系统" clearable size="small">
                <el-option v-for="dict in this.getDictDatas(DICT_TYPE.SYS_MENU_ASCRIPTION)"
                           :key="dict.value" :label="dict.label" :value="dict.value"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="菜单类型" prop="type">
              <el-radio-group v-model="form.type">
                <el-radio v-for="dict in menuTypeDictDatas" :key="parseInt(dict.value)" :label="parseInt(dict.value)">
                  {{ dict.label }}
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item v-if="form.type != '3'" label="菜单图标">
              <el-popover placement="bottom-start" trigger="click" width="460" @show="$refs['iconSelect'].reset()">
                <IconSelect ref="iconSelect" @selected="selected"/>
                <el-input slot="reference" v-model="form.icon" placeholder="点击选择图标" readonly>
                  <svg-icon v-if="form.icon" slot="prefix" :icon-class="form.icon" class="el-input__icon"
                            style="height: 32px;width: 16px;"/>
                  <i v-else slot="prefix" class="el-icon-search el-input__icon"/>
                </el-input>
              </el-popover>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜单名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入菜单名称"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="显示排序" prop="sort">
              <el-input-number v-model="form.sort" :min="0" controls-position="right"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item v-if="form.type != '3'" label="路由地址" prop="path">
              <el-input v-model="form.path" placeholder="请输入路由地址"/>
            </el-form-item>
          </el-col>
          <el-col v-if="form.type == '2'" :span="12">
            <el-form-item label="组件路径" prop="component">
              <el-input v-model="form.component" placeholder="请输入组件路径"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item v-if="form.type != '1'" label="权限标识">
              <el-input v-model="form.permission" maxlength="50" placeholder="请权限标识"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜单状态">
              <el-radio-group v-model="form.status">
                <el-radio v-for="dict in this.getDictDatas(DICT_TYPE.SYS_COMMON_STATUS)"
                          :key="dict.value" :label="parseInt(dict.value)">{{ dict.label }}
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否显示">
              <el-radio-group v-model="form.hidden">
                <el-radio v-for="dict in this.getDictDatas(DICT_TYPE.SYS_COMMON_VISIBLE)"
                          :key="dict.value" :label="parseInt(dict.value)">{{ dict.label }}
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {addMenu, delMenu, getMenu, listMenu, updateMenu} from "@/api/system/menu";
import Treeselect from "@riophae/vue-treeselect";
import "@riophae/vue-treeselect/dist/vue-treeselect.css";
import IconSelect from "@/components/IconSelect";

import {SysCommonStatusEnum, SysCommonVisibleEnum, SysMenuTypeEnum} from '@/utils/constants'
import {DICT_TYPE, getDictDataLabel, getDictDatas} from '@/utils/dict'

export default {
  name: "Menu",
  components: {Treeselect, IconSelect},
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 菜单表格树数据
      menuList: [],
      // 菜单树选项
      menuOptions: [],
      // 弹出层标题
      title: "",
      activeName: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        name: undefined,
        ascription: 'sys_menu_group',
        visible: undefined
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        name: [
          {required: true, message: "菜单名称不能为空", trigger: "blur"}
        ],
        sort: [
          {required: true, message: "菜单顺序不能为空", trigger: "blur"}
        ],
        path: [
          {required: true, message: "路由地址不能为空", trigger: "blur"}
        ],
        group: [
          {required: true, message: "应用系统不能为空", trigger: "blur"}
        ],
        status: [
          {required: true, message: "状态不能为空", trigger: "blur"}
        ]
      },
      // 枚举
      MenuTypeEnum: SysMenuTypeEnum,
      CommonStatusEnum: SysCommonStatusEnum,
      // 数据字典
      menuTypeDictDatas: getDictDatas(DICT_TYPE.SYS_MENU_TYPE),
      statusDictDatas: getDictDatas(DICT_TYPE.SYS_COMMON_STATUS),
      menuAscription: getDictDatas(DICT_TYPE.SYS_MENU_ASCRIPTION)
    };
  },
  created() {
    this.getList();
    this.activeName = this.menuAscription[0].value;
  },
  methods: {
    // 选择图标
    selected(name) {
      this.form.icon = name;
    },
    /** 查询菜单列表 */
    getList() {
      this.loading = true;
      listMenu(this.queryParams).then(response => {
        this.menuList = this.handleTree(response.data, "id");
        this.loading = false;
      });
    },
    /** 转换菜单数据结构 */
    normalizer(node) {
      if (node.children && !node.children.length) {
        delete node.children;
      }
      return {
        id: node.id,
        label: node.name,
        children: node.children
      };
    },
    /** 查询菜单下拉树结构 */
    getTreeselect() {
      listMenu({ascription: ''}).then(response => {
        this.menuOptions = [];
        const menu = {id: 0, name: '主类目', children: []};
        menu.children = this.handleTree(response.data, "id");
        this.menuOptions.push(menu);
      });
    },
    // 菜单状态字典翻译
    statusFormat(row, column) {
      return getDictDataLabel(DICT_TYPE.SYS_COMMON_STATUS, row.status)
    },
    hiddenFormat(row, column){
      return getDictDataLabel(DICT_TYPE.SYS_COMMON_VISIBLE, row.hidden)
    },
    typeFormat(row, column) {
      return getDictDataLabel(DICT_TYPE.SYS_MENU_TYPE, row.type)
    },
    ascriptionFormat(row, column) {
      return getDictDataLabel(DICT_TYPE.SYS_MENU_ASCRIPTION, row.ascription)
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        id: undefined,
        parentId: 0,
        name: undefined,
        icon: undefined,
        type: SysMenuTypeEnum.DIR,
        sort: undefined,
        status: SysCommonStatusEnum.ENABLE,
        hidden: SysCommonVisibleEnum.SHOW
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 新增按钮操作 */
    handleAdd(row) {
      this.reset();
      this.getTreeselect();
      if (row != null && row.id) {
        this.form.parentId = row.id;
      } else {
        this.form.parentId = 0;
      }
      this.open = true;
      this.title = "添加菜单";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      this.getTreeselect();
      getMenu(row.id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改菜单";
      });
    },
    /** 提交按钮 */
    submitForm: function () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          // 若权限类型为目录或者菜单时，进行 path 的校验，避免后续拼接出来的路由无法跳转
          if (this.form.type === SysMenuTypeEnum.DIR
            || this.form.type === SysMenuTypeEnum.MENU) {
            // 如果是外链，则不进行校验
            const path = this.form.path
            if (path.indexOf('http://') === -1 || path.indexOf('https://') === -1) {
              // 父权限为根节点，path 必须以 / 开头
              if (this.form.parentId === 0 && path.charAt(0) !== '/') {
                this.msgSuccess('前端必须以 / 开头')
                return
              } else if (this.form.parentId !== 0 && path.charAt(0) === '/') {
                this.msgSuccess('前端不能以 / 开头')
                return
              }
            }
          }

          // 提交
          if (this.form.id !== undefined) {
            updateMenu(this.form).then(response => {
              this.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addMenu(this.form).then(response => {
              this.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      this.$confirm('是否确认删除名称为"' + row.name + '"的数据项?', "警告", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(function () {
        return delMenu(row.id);
      }).then(() => {
        this.getList();
        this.msgSuccess("删除成功");
      })
    },
    handleTabClick(tab, event) {
      debugger
      console.log(tab, event);
      this.queryParams.ascription = tab.name;
      this.form.ascription = tab.name;
      this.handleQuery();
    }
  }
};
</script>
