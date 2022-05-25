package com.faasadmin.faas.modules.admin.admin.controller.system.saas.module;


import com.faasadmin.faas.business.core.module.saas.service.permission.SaasPermissionBussService;
import com.faasadmin.faas.services.lessee.vo.moduleMenu.SaasAssignLesseeModuleReqVO;
import com.faasadmin.faas.services.lessee.vo.moduleMenu.SaasAssignModuleMenuReqVO;
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
 * @version: V1.0
 * @author: faasadmin
 * @description: 模块授权
 * @data: 2021-09-03 19:36
 **/
@Api(tags = "模块与菜单关联")
@RestController
@RequestMapping("/saas/module-authorize")
@Validated
public class SaasModuleAuthorizeController {
    
    @Resource
    private SaasPermissionBussService saasPermissionService;

    @PostMapping("/assign-module-menu")
    @ApiOperation("赋予模块菜单")
    public CommonResult<Boolean> assignRoleMenu(@Validated @RequestBody SaasAssignModuleMenuReqVO reqVO) {
        saasPermissionService.assignModuleMenu(reqVO.getModuleId(), reqVO.getMenuIds());
        return success(true);
    }

    @ApiOperation("赋予租户模块")
    @PostMapping("/assign-lessee-module")
    public CommonResult<Boolean> assignUserRole(@Validated @RequestBody SaasAssignLesseeModuleReqVO reqVO) {
        saasPermissionService.assignLesseeModules(reqVO.getLesseeId(), reqVO.getModuleIds());
        return success(true);
    }

    @ApiOperation("获得模块拥有的菜单编号")
    @ApiImplicitParam(name = "lesseeId", value = "模块编号", required = true, dataTypeClass = Long.class)
    @GetMapping("/list-module-menu")
    public CommonResult<Set<Long>> listRoleMenus(Long moduleId) {
        return success(saasPermissionService.listModuleMenuIds(moduleId));
    }

    @ApiOperation("获得租户拥有的模块列表")
    @ApiImplicitParam(name = "lesseeId", value = "租户编号", required = true, dataTypeClass = Long.class)
    @GetMapping("/list-lessee-module")
    public CommonResult<Set<Long>> listAdminRoles(@RequestParam("lesseeId") Long lesseeId) {
        return success(saasPermissionService.listLesseeModuleIds(lesseeId));
    }
}
