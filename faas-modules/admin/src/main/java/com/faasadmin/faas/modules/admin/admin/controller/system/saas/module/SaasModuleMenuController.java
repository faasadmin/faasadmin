package com.faasadmin.faas.modules.admin.admin.controller.system.saas.module;


import com.faasadmin.faas.business.core.module.saas.service.module.SaasModuleMenuBussService;
import com.faasadmin.faas.services.lessee.convert.module.SaasModuleMenuConvert;
import com.faasadmin.faas.services.lessee.dal.dataobject.module.SaasModuleMenuDO;
import com.faasadmin.faas.services.lessee.vo.moduleMenu.*;
import com.faasadmin.framework.common.pojo.CommonResult;
import com.faasadmin.framework.common.pojo.PageResult;
import com.faasadmin.framework.excel.core.util.ExcelUtils;
import com.faasadmin.framework.operatelog.core.annotations.OperateLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static com.faasadmin.framework.common.pojo.CommonResult.success;
import static com.faasadmin.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Api(tags = "模块与菜单关联")
@RestController
@RequestMapping("/saas/module-menu")
@Validated
public class SaasModuleMenuController {

    @Resource
    private SaasModuleMenuBussService moduleMenuService;

    @PostMapping("/create")
    @ApiOperation("创建模块与菜单关联")
    @PreAuthorize("@ss.hasPermission('saas:module-menu:create')")
    public CommonResult<Long> createModuleMenu(@Valid @RequestBody SaasModuleMenuCreateReqVO createReqVO) {
        return success(moduleMenuService.createModuleMenu(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新模块与菜单关联")
    @PreAuthorize("@ss.hasPermission('saas:module-menu:update')")
    public CommonResult<Boolean> updateModuleMenu(@Valid @RequestBody SaasModuleMenuUpdateReqVO updateReqVO) {
        moduleMenuService.updateModuleMenu(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除模块与菜单关联")
    @ApiImplicitParam(name = "id", value = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('saas:module-menu:delete')")
    public CommonResult<Boolean> deleteModuleMenu(@RequestParam("id") Long id) {
        moduleMenuService.deleteModuleMenu(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得模块与菜单关联")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('saas:module-menu:query')")
    public CommonResult<SaasModuleMenuRespVO> getModuleMenu(@RequestParam("id") Long id) {
        SaasModuleMenuDO moduleMenu = moduleMenuService.getModuleMenu(id);
        return success(SaasModuleMenuConvert.INSTANCE.convert(moduleMenu));
    }

    @GetMapping("/list")
    @ApiOperation("获得模块与菜单关联列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('saas:module-menu:query')")
    public CommonResult<List<SaasModuleMenuRespVO>> getModuleMenuList(@RequestParam("ids") Collection<Long> ids) {
        List<SaasModuleMenuDO> list = moduleMenuService.getModuleMenuList(ids);
        return success(SaasModuleMenuConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得模块与菜单关联分页")
    @PreAuthorize("@ss.hasPermission('saas:module-menu:query')")
    public CommonResult<PageResult<SaasModuleMenuRespVO>> getModuleMenuPage(@Valid SaasModuleMenuPageReqVO pageVO) {
        PageResult<SaasModuleMenuDO> pageResult = moduleMenuService.getModuleMenuPage(pageVO);
        return success(SaasModuleMenuConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出模块与菜单关联 Excel")
    @PreAuthorize("@ss.hasPermission('saas:module-menu:export')")
    @OperateLog(type = EXPORT)
    public void exportModuleMenuExcel(@Valid SaasModuleMenuExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<SaasModuleMenuDO> list = moduleMenuService.getModuleMenuList(exportReqVO);
        // 导出 Excel
        List<SaasModuleMenuExcelVO> datas = SaasModuleMenuConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "模块与菜单关联.xls", "数据", SaasModuleMenuExcelVO.class, datas);
    }

}
