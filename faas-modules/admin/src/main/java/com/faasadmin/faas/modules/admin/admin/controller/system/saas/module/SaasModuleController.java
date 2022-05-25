package com.faasadmin.faas.modules.admin.admin.controller.system.saas.module;


import com.faasadmin.faas.business.core.module.saas.service.module.SaasModuleBussService;
import com.faasadmin.faas.services.lessee.convert.module.SaasModuleConvert;
import com.faasadmin.faas.services.lessee.dal.dataobject.module.SaasModuleDO;
import com.faasadmin.faas.services.lessee.vo.module.*;
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

@Api(tags = "模块")
@RestController
@RequestMapping("/saas/module")
@Validated
public class SaasModuleController {

    @Resource
    private SaasModuleBussService moduleService;

    @PostMapping("/create")
    @ApiOperation("创建模块")
    @PreAuthorize("@ss.hasPermission('saas:module:create')")
    public CommonResult<Long> createModule(@Valid @RequestBody SaasModuleCreateReqVO createReqVO) {
        return success(moduleService.createModule(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新模块")
    @PreAuthorize("@ss.hasPermission('saas:module:update')")
    public CommonResult<Boolean> updateModule(@Valid @RequestBody SaasModuleUpdateReqVO updateReqVO) {
        moduleService.updateModule(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除模块")
    @ApiImplicitParam(name = "id", value = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('saas:module:delete')")
    public CommonResult<Boolean> deleteModule(@RequestParam("id") Long id) {
        moduleService.deleteModule(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得模块")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('saas:module:query')")
    public CommonResult<SaasModuleRespVO> getModule(@RequestParam("id") Long id) {
        SaasModuleDO module = moduleService.getModule(id);
        return success(SaasModuleConvert.INSTANCE.convert(module));
    }

    @GetMapping("/list")
    @ApiOperation("获得模块列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('saas:module:query')")
    public CommonResult<List<SaasModuleRespVO>> getModuleList(@RequestParam("ids") Collection<Long> ids) {
        List<SaasModuleDO> list = moduleService.getModuleList(ids);
        return success(SaasModuleConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/all")
    @ApiOperation("获得所有模块")
    @PreAuthorize("@ss.hasPermission('saas:module:query')")
    public CommonResult<List<SaasModuleRespVO>> all() {
        List<SaasModuleDO> list = moduleService.getAllModule();
        return success(SaasModuleConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得模块分页")
    @PreAuthorize("@ss.hasPermission('saas:module:query')")
    public CommonResult<PageResult<SaasModuleRespVO>> getModulePage(@Valid SaasModulePageReqVO pageVO) {
        PageResult<SaasModuleDO> pageResult = moduleService.getModulePage(pageVO);
        return success(SaasModuleConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出模块 Excel")
    @PreAuthorize("@ss.hasPermission('saas:module:export')")
    @OperateLog(type = EXPORT)
    public void exportModuleExcel(@Valid SaasModuleExportReqVO exportReqVO,
                                  HttpServletResponse response) throws IOException {
        List<SaasModuleDO> list = moduleService.getModuleList(exportReqVO);
        // 导出 Excel
        List<SaasModuleExcelVO> datas = SaasModuleConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "模块.xls", "数据", SaasModuleExcelVO.class, datas);
    }

}
