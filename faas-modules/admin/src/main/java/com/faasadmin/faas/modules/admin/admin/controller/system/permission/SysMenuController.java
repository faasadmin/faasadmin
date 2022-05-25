package com.faasadmin.faas.modules.admin.admin.controller.system.permission;

import com.faasadmin.faas.business.core.module.system.service.permission.SysMenuBussService;
import com.faasadmin.faas.services.system.convert.permission.SysMenuConvert;
import com.faasadmin.faas.services.system.dal.dataobject.permission.SysMenuDO;
import com.faasadmin.faas.services.system.vo.permission.menu.*;
import com.faasadmin.framework.common.enums.CommonStatusEnum;
import com.faasadmin.framework.common.pojo.CommonResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Comparator;
import java.util.List;

import static com.faasadmin.framework.common.pojo.CommonResult.success;

@Api(tags = "菜单")
@RestController
@RequestMapping("/system/menu")
@Validated
public class SysMenuController {

    @Resource
    private SysMenuBussService sysMenuBussService;

    @PostMapping("/create")
    @ApiOperation("创建菜单")
    @PreAuthorize("@ss.hasPermission('system:menu:create')")
    public CommonResult<Long> createMenu(@Valid @RequestBody SysMenuCreateReqVO reqVO) {
        Long menuId = sysMenuBussService.createMenu(reqVO);
        return success(menuId);
    }

    @PutMapping("/update")
    @ApiOperation("修改菜单")
    @PreAuthorize("@ss.hasPermission('system:menu:update')")
    public CommonResult<Boolean> updateMenu(@Valid @RequestBody SysMenuUpdateReqVO reqVO) {
        sysMenuBussService.updateMenu(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除菜单")
    @ApiImplicitParam(name = "id", value = "角色编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('system:menu:delete')")
    public CommonResult<Boolean> deleteMenu(@RequestParam("id") Long id) {
        sysMenuBussService.deleteMenu(id);
        return success(true);
    }

    @GetMapping("/list")
    @ApiOperation("获取菜单列表")
    @PreAuthorize("@ss.hasPermission('system:menu:query')")
    public CommonResult<List<SysMenuRespVO>> getMenus(SysMenuListReqVO reqVO) {
        List<SysMenuDO> list = sysMenuBussService.getMenus(reqVO);
        list.sort(Comparator.comparing(SysMenuDO::getSort));
        return success(SysMenuConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/list-all-simple")
    @ApiOperation(value = "获取菜单精简信息列表", notes = "只包含被开启的菜单，主要用于前端的下拉选项")
    public CommonResult<List<SysMenuSimpleRespVO>> getSimpleMenus(SysMenuListReqVO reqVO) {
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        List<SysMenuDO> list = sysMenuBussService.getMenus(reqVO);
        // 排序后，返回个诶前端
        list.sort(Comparator.comparing(SysMenuDO::getSort));
        return success(SysMenuConvert.INSTANCE.convertList02(list));
    }

    @GetMapping("/get")
    @ApiOperation("获取菜单信息")
    @PreAuthorize("@ss.hasPermission('system:menu:query')")
    public CommonResult<SysMenuRespVO> getMenu(Long id) {
        SysMenuDO menu = sysMenuBussService.getMenu(id);
        return success(SysMenuConvert.INSTANCE.convert(menu));
    }

}
