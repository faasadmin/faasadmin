package com.faasadmin.faas.modules.admin.admin.controller.system.dict;


import com.faasadmin.faas.business.core.module.system.service.dict.SysDictDataBussService;
import com.faasadmin.faas.services.system.convert.dict.SysDictDataConvert;
import com.faasadmin.faas.services.system.dal.dataobject.dict.SysDictDataDO;
import com.faasadmin.framework.common.pojo.CommonResult;
import com.faasadmin.framework.common.pojo.PageResult;
import com.faasadmin.framework.excel.core.util.ExcelUtils;
import com.faasadmin.framework.operatelog.core.annotations.OperateLog;
import com.faasadmin.faas.services.system.vo.dict.data.*;
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
import java.util.List;

import static com.faasadmin.framework.common.pojo.CommonResult.success;
import static com.faasadmin.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Api(tags = "字典数据")
@RestController
@RequestMapping("/system/dict-data")
@Validated
public class SysDictDataController {

    @Resource
    private SysDictDataBussService sysDictDataBussService;

    @PostMapping("/create")
    @ApiOperation("新增字典数据")
    @PreAuthorize("@ss.hasPermission('system:dict:create')")
    public CommonResult<Long> createDictData(@Valid @RequestBody SysDictDataCreateReqVO reqVO) {
        Long dictDataId = sysDictDataBussService.createDictData(reqVO);
        return success(dictDataId);
    }

    @PutMapping("update")
    @ApiOperation("修改字典数据")
    @PreAuthorize("@ss.hasPermission('system:dict:update')")
    public CommonResult<Boolean> updateDictData(@Valid @RequestBody SysDictDataUpdateReqVO reqVO) {
        sysDictDataBussService.updateDictData(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除字典数据")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('system:dict:delete')")
    public CommonResult<Boolean> deleteDictData(Long id) {
        sysDictDataBussService.deleteDictData(id);
        return success(true);
    }

    @GetMapping("/list-all-simple")
    @ApiOperation(value = "获得全部字典数据列表", notes = "一般用于管理后台缓存字典数据在本地")
    // 无需添加权限认证，因为前端全局都需要
    public CommonResult<List<SysDictDataSimpleRespVO>> getSimpleDictDatas() {
        List<SysDictDataDO> list = sysDictDataBussService.getDictDatas();
        return success(SysDictDataConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("/获得字典类型的分页列表")
    @PreAuthorize("@ss.hasPermission('system:dict:query')")
    public CommonResult<PageResult<SysDictDataRespVO>> getDictTypePage(@Valid SysDictDataPageReqVO reqVO) {
        return success(SysDictDataConvert.INSTANCE.convertPage(sysDictDataBussService.getDictDataPage(reqVO)));
    }

    @GetMapping(value = "/get")
    @ApiOperation("/查询字典数据详细")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('system:dict:query')")
    public CommonResult<SysDictDataRespVO> getDictData(@RequestParam("id") Long id) {
        return success(SysDictDataConvert.INSTANCE.convert(sysDictDataBussService.getDictData(id)));
    }

    @GetMapping("/export")
    @ApiOperation("导出字典数据")
    @PreAuthorize("@ss.hasPermission('system:dict:export')")
    @OperateLog(type = EXPORT)
    public void export(HttpServletResponse response, @Valid SysDictDataExportReqVO reqVO) throws IOException {
        List<SysDictDataDO> list = sysDictDataBussService.getDictDatas(reqVO);
        List<SysDictDataExcelVO> data = SysDictDataConvert.INSTANCE.convertList02(list);
        // 输出
        ExcelUtils.write(response, "字典数据.xls", "数据列表", SysDictDataExcelVO.class, data);
    }

}
