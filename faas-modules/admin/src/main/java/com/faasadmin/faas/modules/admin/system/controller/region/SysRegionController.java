package com.faasadmin.faas.modules.admin.system.controller.region;

import cn.hutool.core.lang.tree.Tree;
import com.faasadmin.faas.business.core.module.system.service.region.SysRegionExtService;
import com.faasadmin.faas.services.system.convert.region.SysRegionConvert;
import com.faasadmin.faas.services.system.dal.dataobject.region.SysRegionDO;
import com.faasadmin.faas.services.system.service.region.SysRegionService;
import com.faasadmin.framework.common.pojo.CommonResult;
import com.faasadmin.framework.common.pojo.PageResult;
import com.faasadmin.framework.excel.core.util.ExcelUtils;
import com.faasadmin.framework.operatelog.core.annotations.OperateLog;
import com.faasadmin.faas.services.system.vo.region.*;
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

@Api(tags = "行政区域")
@RestController
@RequestMapping("/system/region")
@Validated
public class SysRegionController {

    @Resource
    private SysRegionService regionService;

    @Resource
    private SysRegionExtService sysRegionExtService;

    @PostMapping("/create")
    @ApiOperation("创建行政区域")
    @PreAuthorize("@ss.hasPermission('system:region:create')")
    public CommonResult<Long> createRegion(@Valid @RequestBody SysRegionCreateReqVO createReqVO) {
        return success(regionService.createRegion(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新行政区域")
    @PreAuthorize("@ss.hasPermission('system:region:update')")
    public CommonResult<Boolean> updateRegion(@Valid @RequestBody SysRegionUpdateReqVO updateReqVO) {
        regionService.updateRegion(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除行政区域")
    @ApiImplicitParam(name = "id", value = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('system:region:delete')")
    public CommonResult<Boolean> deleteRegion(@RequestParam("id") Long id) {
        regionService.deleteRegion(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得行政区域")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('system:region:query')")
    public CommonResult<SysRegionRespVO> getRegion(@RequestParam("id") Long id) {
        SysRegionDO region = regionService.getRegion(id);
        return success(SysRegionConvert.INSTANCE.convert(region));
    }

    @GetMapping("/list")
    @ApiOperation("获得行政区域列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('system:region:query')")
    public CommonResult<List<SysRegionRespVO>> getRegionList(@RequestParam("ids") Collection<Long> ids) {
        List<SysRegionDO> list = regionService.getRegionList(ids);
        return success(SysRegionConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/tree")
    @ApiOperation("获得所有行政区域")
    public CommonResult<List<Tree<String>>> getTreeRegion() {
        return success(sysRegionExtService.listRegionTree());
    }


    @GetMapping("/page")
    @ApiOperation("获得行政区域分页")
    @PreAuthorize("@ss.hasPermission('system:region:query')")
    public CommonResult<PageResult<SysRegionRespVO>> getRegionPage(@Valid SysRegionPageReqVO pageVO) {
        PageResult<SysRegionDO> pageResult = regionService.getRegionPage(pageVO);
        return success(SysRegionConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出行政区域 Excel")
    @PreAuthorize("@ss.hasPermission('system:region:export')")
    @OperateLog(type = EXPORT)
    public void exportRegionExcel(@Valid SysRegionExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<SysRegionDO> list = regionService.getRegionList(exportReqVO);
        // 导出 Excel
        List<SysRegionExcelVO> datas = SysRegionConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "行政区域.xls", "数据", SysRegionExcelVO.class, datas);
    }

}
