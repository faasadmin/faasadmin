package com.faasadmin.faas.modules.admin.admin.controller.system.saas.lessee;

import com.faasadmin.faas.business.core.module.saas.service.lessee.SaasLesseeModuleBussService;
import com.faasadmin.faas.services.lessee.convert.lessee.SaasLesseeModuleConvert;
import com.faasadmin.faas.services.lessee.dal.dataobject.lessee.SaasLesseeModuleDO;
import com.faasadmin.faas.services.lessee.vo.lesseeModule.*;
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

@Api(tags = "租户拥有模块")
@RestController
@RequestMapping("/saas/lessee-module")
@Validated
public class SaasLesseeModuleController {

    @Resource
    private SaasLesseeModuleBussService lesseeModuleService;

    @PostMapping("/create")
    @ApiOperation("创建租户拥有模块")
    @PreAuthorize("@ss.hasPermission('saas:lessee-module:create')")
    public CommonResult<Long> createLesseeModule(@Valid @RequestBody SaasLesseeModuleCreateReqVO createReqVO) {
        return success(lesseeModuleService.createLesseeModule(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新租户拥有模块")
    @PreAuthorize("@ss.hasPermission('saas:lessee-module:update')")
    public CommonResult<Boolean> updateLesseeModule(@Valid @RequestBody SaasLesseeModuleUpdateReqVO updateReqVO) {
        lesseeModuleService.updateLesseeModule(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除租户拥有模块")
    @ApiImplicitParam(name = "id", value = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('saas:lessee-module:delete')")
    public CommonResult<Boolean> deleteLesseeModule(@RequestParam("id") Long id) {
        lesseeModuleService.deleteLesseeModule(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得租户拥有模块")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass =Long.class)
    @PreAuthorize("@ss.hasPermission('saas:lessee-module:query')")
    public CommonResult<SaasLesseeModuleRespVO> getLesseeModule(@RequestParam("id") Long id) {
        SaasLesseeModuleDO lesseeModule = lesseeModuleService.getLesseeModule(id);
        return success(SaasLesseeModuleConvert.INSTANCE.convert(lesseeModule));
    }

    @GetMapping("/list")
    @ApiOperation("获得租户拥有模块列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('saas:lessee-module:query')")
    public CommonResult<List<SaasLesseeModuleRespVO>> getLesseeModuleList(@RequestParam("ids") Collection<Long> ids) {
        List<SaasLesseeModuleDO> list = lesseeModuleService.getLesseeModuleList(ids);
        return success(SaasLesseeModuleConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得租户拥有模块分页")
    @PreAuthorize("@ss.hasPermission('saas:lessee-module:query')")
    public CommonResult<PageResult<SaasLesseeModuleRespVO>> getLesseeModulePage(@Valid SaasLesseeModulePageReqVO pageVO) {
        PageResult<SaasLesseeModuleDO> pageResult = lesseeModuleService.getLesseeModulePage(pageVO);
        return success(SaasLesseeModuleConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出租户拥有模块 Excel")
    @PreAuthorize("@ss.hasPermission('saas:lessee-module:export')")
    @OperateLog(type = EXPORT)
    public void exportLesseeModuleExcel(@Valid SaasLesseeModuleExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<SaasLesseeModuleDO> list = lesseeModuleService.getLesseeModuleList(exportReqVO);
        // 导出 Excel
        List<SaasLesseeModuleExcelVO> datas = SaasLesseeModuleConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "租户拥有模块.xls", "数据", SaasLesseeModuleExcelVO.class, datas);
    }

}
