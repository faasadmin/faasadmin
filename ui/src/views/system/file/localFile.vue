<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!--文件夹-->
      <el-col :span="4" :xs="24">
        <div class="head-container">
          <el-input v-model="folderName" placeholder="请输入文件夹名称" clearable size="small" prefix-icon="el-icon-search" style="margin-bottom: 20px"/>
        </div>
        <div class="head-container">
          <el-tree :data="folderOptions" :props="defaultProps" :expand-on-click-node="false" :filter-node-method="filterNode"
                   ref="tree" default-expand-all @node-click="handleNodeClick"/>
        </div>
      </el-col>
      <!--文件数据-->
      <el-col :span="20" :xs="24">
        <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
          <el-form-item label="名称" prop="userName">
            <el-input v-model="queryParams.fileName" placeholder="请输入名称" clearable size="small" style="width: 240px"
                      @keyup.enter.native="handleQuery"/>
          </el-form-item>
          <el-form-item>
            <el-button type="cyan" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
            <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" icon="el-icon-plus" size="mini" @click="handleAddFolder"
                       v-hasPermi="['system:user:create']">新增文件夹</el-button>
            <el-button type="primary" icon="el-icon-plus" size="mini" @click="handleUploadFile"
                       v-hasPermi="['system:user:create']">上传文件</el-button>
          </el-col>
          <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <!-- 列表 -->
        <el-table v-loading="loading" :data="list">
          <el-table-column label="主键" align="center" prop="id" />
          <el-table-column label="文件名称" align="center" prop="fileName" />
          <el-table-column label="文件大小" align="center" prop="fileSize" :formatter="fileSizeFormatter"/>
          <el-table-column label="存储类型" align="center" prop="ossType" :formatter="ossTypeFormatter"/>
          <el-table-column label="文件内容" align="center" prop="content">
            <template slot-scope="scope">
              <img v-if="scope.row.fileType === '2'"
                   width="200px" :src="getFileUrl + scope.row.id">
              <i v-else>非图片，无法预览</i>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" align="center" prop="createTime" width="180">
            <template slot-scope="scope">
              <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
            <template slot-scope="scope">
              <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
                         v-hasPermi="['system:file:update']">修改</el-button>
              <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
                         v-hasPermi="['system:file:delete']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                    @pagination="getList"/>
      </el-col>
    </el-row>


    <!-- 对话框(添加 / 修改) -->
    <el-dialog :title="upload.title" :visible.sync="upload.open" width="500px" append-to-body>
      <el-form ref="form" :model="form"  label-width="100px">
        <el-form-item label="归属文件夹">
          <treeselect v-model="upload.data.fid" :options="folderOptions" :show-count="true"
                      placeholder="请选择文件夹" :normalizer="normalizer"/>
        </el-form-item>
        <el-form-item label="文件">
          <el-upload ref="upload" :limit="1" accept=".jpg, .png, .gif" :auto-upload="false" drag
                     :headers="upload.headers" :action="upload.url" :data="upload.data" :disabled="upload.isUploading"
                     :on-change="handleFileChange"
                     :on-progress="handleFileUploadProgress"
                     :on-success="handleFileSuccess">
            <i class="el-icon-upload"></i>
            <div class="el-upload__text">
              将文件拖到此处，或 <em>点击上传</em>
            </div>
          </el-upload>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitFileForm">确 定</el-button>
        <el-button @click="upload.open = false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 新增文件夹 -->
    <el-dialog :title="folder.title" :visible.sync="folder.open" width="500px" append-to-body>
      <el-form ref="folder" :model="folderForm" :rules="rules" label-width="100px">
        <el-form-item label="上级文件夹" prop="parentId">
          <treeselect v-model="folderForm.parentId" :options="folderOptions" :show-count="true"
                      placeholder="请选择上级文件夹" :normalizer="normalizer"/>
        </el-form-item>
        <el-form-item label="文件夹名称" prop="fileName">
          <el-input v-model="folderForm.folderName" placeholder="请输入文件夹名称" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input v-model="folderForm.sort" placeholder="请输入排序" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input type="textarea" :rows="3" v-model="folderForm.remark"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitFolderForm">确 定</el-button>
        <el-button @click="folder.open = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {listSimpleFolders, createFileFolder, updateFileFolder, deleteFileFolder, getFileFolder, getFileFolderPage, exportFileFolderExcel } from "@/api/system/fileRecordFolder";
import { createFile, updateFile, deleteFile, getFile, getFilePage, exportFileExcel } from "@/api/system/fileRecord";

import {getToken} from "@/utils/auth";
import Treeselect from "@riophae/vue-treeselect";
import "@riophae/vue-treeselect/dist/vue-treeselect.css";

import {SysCommonStatusEnum} from "@/utils/constants";
import {DICT_TYPE, getDictDataLabel, getDictDatas} from "@/utils/dict";
import {assignUserRole, listUserRoles} from "@/api/system/permission";
import {listSimpleRoles} from "@/api/system/role";

export default {
  name: "User",
  components: { Treeselect },
  computed: {
    // 上传是带的参数
    fileParam() {
      return {
        ...this.form
      }
    }
  },
  data() {
    return {
      getFileUrl: process.env.VUE_APP_BASE_API + '/api/system/file/record/download/',
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 文件表格数据
      list: null,
      // 弹出层标题
      title: "",
      // 文件夹树选项
      folderOptions: undefined,
      // 是否显示弹出层
      open: false,
      // 文件夹名称
      folderName: undefined,
      // 日期范围
      dateRange: [],
      // 状态数据字典
      statusOptions: [],
      // 表单参数
      form: {},
      // 文件夹参数
      folderForm: {},
      defaultProps: {
        children: "children",
        label: "name"
      },
      folder:{
        // 是否显示弹出层
        open: false,
        // 弹出层标题
        title: "",
      },
      upload: {
        // 是否显示弹出层
        open: false,
        // 弹出层标题
        title: "",
        // 是否禁用上传
        isUploading: false,
        // 是否更新已经存在的用户数据
        updateSupport: 0,
        // 设置上传的请求头部
        headers: { Authorization: "Bearer " + getToken() },
        // 上传的地址
        url: process.env.VUE_APP_BASE_API + '/api/' + "/system/file/record/uploadLocal",
        data:{
          fid:undefined,
        },
      },
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        fileName: undefined,
        ossType: 0
      },
      // 表单校验
      rules: {
        folderName: [
          { required: true, message: "文件夹名称不能为空", trigger: "blur" }
        ],
        sort: [
          { required: true, message: "排序不能为空", trigger: "blur" }
        ],
      },
      // 是否显示弹出层（角色权限）
      openRole: false,
      // 枚举
      SysCommonStatusEnum: SysCommonStatusEnum,
      // 数据字典
      statusDictDatas: getDictDatas(DICT_TYPE.SYS_COMMON_STATUS),
      ossTypeDictDatas: getDictDatas(DICT_TYPE.SYS_OSS_TYPE),
    };
  },
  watch: {
    // 根据名称筛选部门树
    deptName(val) {
      this.$refs.tree.filter(val);
    }
  },
  created() {
    this.getList();
    this.getTreeselect();
  },
  methods: {
    /** 查询文件列表 */
    getList() {
      this.loading = true;
      // 处理查询参数
      let params = {...this.queryParams};
      this.addBeginAndEndTime(params, this.dateRangeCreateTime, 'createTime');
      // 执行查询
      getFilePage(params).then(response => {
        this.list = response.data.list;
        this.total = response.data.total;
        this.loading = false;
      });
    },
    /** 查询文件夹下拉树结构  */
    getTreeselect() {
      listSimpleFolders().then(response => {
        debugger
        // 处理 folderOptions 参数
        this.folderOptions = [];
        this.folderOptions.push(...this.handleTree(response.data, "id"));
      });
    },
    // 筛选节点
    filterNode(value, data) {
      if (!value) return true;
      return data.name.indexOf(value) !== -1;
    },
    // 节点单击事件
    handleNodeClick(data) {
      this.queryParams.folderId = data.id;
      this.getList();
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 取消按钮（角色权限）
    cancelRole() {
      this.openRole = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        id: undefined,
        deptId: undefined,
        userName: undefined,
        nickname: undefined,
        password: undefined,
        mobile: undefined,
        email: undefined,
        sex: undefined,
        status: "0",
        remark: undefined,
        postIds: [],
        roleIds: []
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNo = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.dateRange = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 新建文件夹 */
    handleAddFolder(){
      // 获得下拉数据
      this.getTreeselect();
      this.folder.open = true;
      this.folder.title = "新增文件夹";
    },
    /** 上传文件操作 */
    handleUploadFile() {
      this.reset();
      // 获得下拉数据
      this.getTreeselect();
      this.upload.open = true;
      this.upload.title = "上传文件";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      this.getTreeselect();
      const id = row.id;
      getUser(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改用户";
        this.form.password = "";
      });
    },
    /** 重置密码按钮操作 */
    handleResetPwd(row) {
      this.$prompt('请输入"' + row.userName + '"的新密码', "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消"
      }).then(({ value }) => {
        resetUserPwd(row.id, value).then(response => {
          this.msgSuccess("修改成功，新密码是：" + value);
        });
      }).catch(() => {});
    },
    /** 分配用户角色操作 */
    handleRole(row) {
      this.reset();
      const id = row.id
      // 处理了 form 的用户 username 和 nickname 的展示
      this.form.id = id;
      this.form.userName = row.userName;
      this.form.nickname = row.nickname;
      // 打开弹窗
      this.openRole = true;
      // 获得角色列表
      listSimpleRoles().then(response => {
        // 处理 roleOptions 参数
        this.roleOptions = [];
        this.roleOptions.push(...response.data);
      });
      // 获得角色拥有的菜单集合
      listUserRoles(id).then(response => {
        // 设置选中
        this.form.roleIds = response.data;
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$confirm('是否确认删除用户编号为"' + ids + '"的数据项?', "警告", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(function() {
        return delUser(ids);
      }).then(() => {
        this.getList();
        this.msgSuccess("删除成功");
      })
    },
    /** 处理上传的文件发生变化 */
    handleFileChange(file, fileList) {
      this.upload.data.name = file.name;
    },
    /** 处理文件上传中 */
    handleFileUploadProgress(event, file, fileList) {
      this.upload.isUploading = true; // 禁止修改
    },
    /** 文件上传成功处理 */
    handleFileSuccess(response, file, fileList) {
      // 清理
      this.upload.open = false;
      this.upload.isUploading = false;
      this.$refs.upload.clearFiles();
      // 提示成功，并刷新
      this.msgSuccess("上传成功");
      this.getList();
    },
    // 提交上传文件
    submitFileForm() {
      this.$refs.upload.submit();
    },
    submitFolderForm(){
      this.$refs["folder"].validate(valid => {
        if (valid) {
          if (this.folderForm.id !== undefined) {
            updateFileFolder(this.folderForm).then(response => {
              this.msgSuccess("修改成功");
              this.folder.open = false;
              this.getTreeselect();
            });
          } else {
            createFileFolder(this.folderForm).then(response => {
              this.msgSuccess("新增成功");
              this.folder.open = false;
              this.getTreeselect();
            });
          }
        }
      });
    },
    // 格式化部门的下拉框
    normalizer(node) {
      return {
        id: node.id,
        label: node.name,
        children: node.children
      }
    },
    // 角色类型字典翻译
    ossTypeFormatter(row, column) {
      return getDictDataLabel(DICT_TYPE.SYS_OSS_TYPE, row.ossType)
    },
    fileSizeFormatter(row, column){
      return this.storageUnit(row.fileSize)
    }
  }
};
</script>
<style>
.el-dropdown-link {
  cursor: pointer;
  color: #1890ff;
  margin-left: 5px;
}
.el-icon-arrow-down {
  font-size: 14px;
}
</style>
