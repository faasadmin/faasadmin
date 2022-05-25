package com.faasadmin.faas.modules.admin.admin.controller.system.permission;

import com.faasadmin.faas.business.core.module.system.service.permission.SysPermissionBussService;
import com.faasadmin.faas.services.system.vo.permission.permission.SysPermissionAssignRoleDataScopeReqVO;
import com.faasadmin.faas.services.system.vo.permission.permission.SysPermissionAssignRoleMenuReqVO;
import com.faasadmin.faas.services.system.vo.permission.permission.SysPermissionAssignUserRoleReqVO;
import com.faasadmin.framework.common.pojo.CommonResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Set;

import static com.faasadmin.framework.common.pojo.CommonResult.success;

/**
 * 权限 Controller，提供赋予用户、角色的权限的 API 接口
 *
 * @author faasadmin
 */
@Api(tags = "权限")
@RestController
@RequestMapping("/system/permission")
public class SysPermissionController {

    @Resource
    private SysPermissionBussService sysPermissionBussService;

    @ApiOperation("获得角色拥有的菜单编号")
    @ApiImplicitParam(name = "roleId", value = "角色编号", required = true, dataTypeClass = Long.class)
    @GetMapping("/list-role-resources")
//    @RequiresPermissions("system:permission:assign-role-menu")
    public CommonResult<Set<Long>> listRoleMenus(Long roleId) {
        return success(sysPermissionBussService.listRoleMenuIds(roleId));
    }

    @PostMapping("/assign-role-menu")
    @ApiOperation("赋予角色菜单")
//    @RequiresPermissions("system:permission:assign-role-resource")
    public CommonResult<Boolean> assignRoleMenu(@Validated @RequestBody SysPermissionAssignRoleMenuReqVO reqVO) {
        sysPermissionBussService.assignRoleMenu(reqVO.getRoleId(), reqVO.getMenuIds());
        return success(true);
    }

    @PostMapping("/assign-role-data-scope")
    @ApiOperation("赋予角色数据权限")
//    @RequiresPermissions("system:permission:assign-role-data-scope")
    public CommonResult<Boolean> assignRoleDataScope(
            @Validated @RequestBody SysPermissionAssignRoleDataScopeReqVO reqVO) {
        sysPermissionBussService.assignRoleDataScope(reqVO.getRoleId(), reqVO.getDataScope(), reqVO.getDataScopeDeptIds());
        return success(true);
    }

    @ApiOperation("获得管理员拥有的角色编号列表")
    @ApiImplicitParam(name = "userId", value = "用户编号", required = true, dataTypeClass = Long.class)
    @GetMapping("/list-user-roles")
//    @RequiresPermissions("system:permission:assign-user-role")
    public CommonResult<Set<Long>> listAdminRoles(@RequestParam("userId") Long userId) {
        return success(sysPermissionBussService.listUserRoleIs(userId));
    }

    @ApiOperation("赋予用户角色")
    @PostMapping("/assign-user-role")
//    @RequiresPermissions("system:permission:assign-user-role")
    public CommonResult<Boolean> assignUserRole(@Validated @RequestBody SysPermissionAssignUserRoleReqVO reqVO) {
        sysPermissionBussService.assignUserRole(reqVO.getUserId(), reqVO.getRoleIds());
        return success(true);
    }

}
