package com.faasadmin.faas.modules.admin.admin.controller.system.user;

import cn.hutool.core.collection.CollUtil;
import com.faasadmin.faas.business.core.module.system.service.dept.SysDeptBussService;
import com.faasadmin.faas.business.core.module.system.service.user.SysUserBussService;
import com.faasadmin.faas.services.lessee.dal.dataobject.lessee.SaasLesseeDO;
import com.faasadmin.faas.services.system.convert.user.SysUserConvert;
import com.faasadmin.faas.services.system.dal.dataobject.dept.SysDeptDO;
import com.faasadmin.faas.services.system.dal.dataobject.user.SysUserDO;
import com.faasadmin.faas.services.system.enums.common.SysSexEnum;
import com.faasadmin.faas.services.system.service.user.SysUserService;
import com.faasadmin.faas.services.system.vo.user.*;
import com.faasadmin.framework.common.enums.CommonStatusEnum;
import com.faasadmin.framework.common.pojo.CommonResult;
import com.faasadmin.framework.common.pojo.PageResult;
import com.faasadmin.framework.common.utils.collection.CollectionUtils;
import com.faasadmin.framework.common.utils.collection.MapUtils;
import com.faasadmin.framework.excel.core.util.ExcelUtils;
import com.faasadmin.framework.operatelog.core.annotations.OperateLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.*;

import static com.faasadmin.framework.common.pojo.CommonResult.success;
import static com.faasadmin.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Api(tags = "用户")
@RestController
@RequestMapping("/system/user")
@Validated
public class SysUserController {

    @Resource
    private SysUserBussService sysUserBussService;
    @Resource
    private SysUserService sysUserService;
    @Resource
    private SysDeptBussService sysDeptBussService;
    @Value("${sys.user.init-password:faasadmin}")
    private String userInitPassword;


    @GetMapping("/get-lessee-id")
    @ApiOperation(value = "使用租户名，获得租户编号", notes = "登录界面，根据用户的租户名，获得租户编号")
    @ApiImplicitParam(name = "name", value = "租户名", required = true, example = "1024", dataTypeClass = Long.class)
    public CommonResult<Long> getLesseeIdByName(@RequestParam("userName") String userName) {
        SaasLesseeDO lesseeDO = sysUserBussService.getLesseeByName(userName);
        return success(lesseeDO != null ? lesseeDO.getId() : null);
    }

    @PostMapping("/create")
    @ApiOperation("新增用户")
    @PreAuthorize("@ss.hasPermission('system:user:create')")
    public CommonResult<Long> createUser(@Valid @RequestBody SysUserCreateReqVO reqVO) {
        Long id = sysUserService.createUser(reqVO);
        return success(id);
    }

    @PutMapping("update")
    @ApiOperation("修改用户")
    @PreAuthorize("@ss.hasPermission('system:user:update')")
    public CommonResult<Boolean> updateUser(@Valid @RequestBody SysUserUpdateReqVO reqVO) {
        sysUserService.updateUser(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除用户")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('system:user:delete')")
    public CommonResult<Boolean> deleteUser(@RequestParam("id") Long id) {
        sysUserService.deleteUser(id);
        return success(true);
    }

    @PutMapping("/update-password")
    @ApiOperation("重置用户密码")
    @PreAuthorize("@ss.hasPermission('system:user:update-password')")
    public CommonResult<Boolean> updateUserPassword(@Valid @RequestBody SysUserUpdatePasswordReqVO reqVO) {
        sysUserService.updateUserPassword(reqVO.getId(), reqVO.getPassword());
        return success(true);
    }

    @PutMapping("/update-status")
    @ApiOperation("修改用户状态")
    @PreAuthorize("@ss.hasPermission('system:user:update')")
    public CommonResult<Boolean> updateUserStatus(@Valid @RequestBody SysUserUpdateStatusReqVO reqVO) {
        sysUserService.updateUserStatus(reqVO.getId(), reqVO.getStatus());
        return success(true);
    }

    @GetMapping("/page")
    @ApiOperation("获得用户分页列表")
    @PreAuthorize("@ss.hasPermission('system:user:list')")
    public CommonResult<PageResult<SysUserPageItemRespVO>> getUserPage(@Valid SysUserPageReqVO reqVO) {
        // 获得用户分页列表
        PageResult<SysUserDO> pageResult = sysUserBussService.getUserPage(reqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(new PageResult<>(pageResult.getTotal())); // 返回空
        }
        // 获得拼接需要的数据
        Collection<Long> deptIds = CollectionUtils.convertList(pageResult.getList(), SysUserDO::getDeptId);
        Map<Long, SysDeptDO> deptMap = sysDeptBussService.getDeptMap(deptIds);
        // 拼接结果返回
        List<SysUserPageItemRespVO> userList = new ArrayList<>(pageResult.getList().size());
        pageResult.getList().forEach(user -> {
            SysUserPageItemRespVO respVO = SysUserConvert.INSTANCE.convert(user);
            respVO.setDept(SysUserConvert.INSTANCE.convert(deptMap.get(user.getDeptId())));
            userList.add(respVO);
        });
        return success(new PageResult<>(userList, pageResult.getTotal()));
    }

    @GetMapping("/get")
    @ApiOperation("获得用户详情")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('system:user:query')")
    public CommonResult<SysUserRespVO> getInfo(@RequestParam("id") Long id) {
        return success(SysUserConvert.INSTANCE.convert(sysUserService.getUser(id)));
    }

    @GetMapping("/export")
    @ApiOperation("导出用户")
    @PreAuthorize("@ss.hasPermission('system:user:export')")
    @OperateLog(type = EXPORT)
    public void exportUsers(@Validated SysUserExportReqVO reqVO,
                            HttpServletResponse response) throws IOException {
        // 获得用户列表
        List<SysUserDO> users = sysUserBussService.getUsers(reqVO);
        // 获得拼接需要的数据
        Collection<Long> deptIds = CollectionUtils.convertList(users, SysUserDO::getDeptId);
        Map<Long, SysDeptDO> deptMap = sysDeptBussService.getDeptMap(deptIds);
        // 拼接数据
        List<SysUserExcelVO> excelUsers = new ArrayList<>(users.size());
        users.forEach(user -> {
            SysUserExcelVO excelVO = SysUserConvert.INSTANCE.convert02(user);
            MapUtils.findAndThen(deptMap, user.getDeptId(), dept -> {
                excelVO.setDeptName(dept.getName());
                excelVO.setDeptLeader(dept.getLeader());
            });
            excelUsers.add(excelVO);
        });
        // 输出
        ExcelUtils.write(response, "用户数据.xls", "用户列表", SysUserExcelVO.class, excelUsers);
    }

    @GetMapping("/get-import-template")
    @ApiOperation("获得导入用户模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        // 手动创建导出 demo
        List<SysUserImportExcelVO> list = Arrays.asList(
                SysUserImportExcelVO.builder().userName("faasadmin").deptId(1L).email("codezhifu@163.com").mobile("100101691300")
                        .userName("faasadmin").status(CommonStatusEnum.ENABLE.getStatus()).sex(SysSexEnum.MALE.getSex()).build(),
                SysUserImportExcelVO.builder().userName("faasadmin").deptId(2L).email("codezhifu@163.com").mobile("100101691300")
                        .userName("源码").status(CommonStatusEnum.DISABLE.getStatus()).sex(SysSexEnum.FEMALE.getSex()).build()
        );
        // 输出
        ExcelUtils.write(response, "用户导入模板.xls", "用户列表", SysUserImportExcelVO.class, list);
    }

    @PostMapping("/import")
    @ApiOperation("导入用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "Excel 文件", required = true, dataTypeClass = MultipartFile.class),
            @ApiImplicitParam(name = "updateSupport", value = "是否支持更新，默认为 false", example = "true", dataTypeClass = Boolean.class)
    })
    @PreAuthorize("@ss.hasPermission('system:user:import')")
    public CommonResult<SysUserImportRespVO> importExcel(@RequestParam("file") MultipartFile file,
                                                         @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) throws Exception {
        List<SysUserImportExcelVO> list = ExcelUtils.raed(file, SysUserImportExcelVO.class);
        return success(sysUserService.importUsers(list, updateSupport, userInitPassword));
    }

}
