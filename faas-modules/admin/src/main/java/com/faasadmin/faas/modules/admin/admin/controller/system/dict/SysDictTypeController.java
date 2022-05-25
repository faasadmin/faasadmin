package com.faasadmin.faas.modules.admin.admin.controller.system.dict;

import com.faasadmin.faas.business.core.module.system.service.dict.SysDictTypeBussService;
import com.faasadmin.faas.services.system.convert.dict.SysDictTypeConvert;
import com.faasadmin.faas.services.system.dal.dataobject.dict.SysDictTypeDO;
import com.faasadmin.framework.common.pojo.CommonResult;
import com.faasadmin.framework.common.pojo.PageResult;
import com.faasadmin.framework.excel.core.util.ExcelUtils;
import com.faasadmin.framework.operatelog.core.annotations.OperateLog;
import com.faasadmin.faas.services.system.vo.dict.type.*;
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

@Api(tags = "字典类型")
@RestController
@RequestMapping("/system/dict-type")
@Validated
public class SysDictTypeController {

    @Resource
    private SysDictTypeBussService sysDictTypeBussService;

    @PostMapping("/create")
    @ApiOperation("创建字典类型")
    @PreAuthorize("@ss.hasPermission('system:dict:create')")
    public CommonResult<Long> createDictType(@Valid @RequestBody SysDictTypeCreateReqVO reqVO) {
        Long dictTypeId = sysDictTypeBussService.createDictType(reqVO);
        return success(dictTypeId);
    }

    @PutMapping("/update")
    @ApiOperation("修改字典类型")
    @PreAuthorize("@ss.hasPermission('system:dict:update')")
    public CommonResult<Boolean> updateDictType(@Valid @RequestBody SysDictTypeUpdateReqVO reqVO) {
        sysDictTypeBussService.updateDictType(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除字典类型")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('system:dict:delete')")
    public CommonResult<Boolean> deleteDictType(Long id) {
        sysDictTypeBussService.deleteDictType(id);
        return success(true);
    }

    @ApiOperation("/获得字典类型的分页列表")
    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('system:dict:query')")
    public CommonResult<PageResult<SysDictTypeRespVO>> pageDictTypes(@Valid SysDictTypePageReqVO reqVO) {
        return success(SysDictTypeConvert.INSTANCE.convertPage(sysDictTypeBussService.getDictTypePage(reqVO)));
    }

    @ApiOperation("/查询字典类型详细")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @GetMapping(value = "/get")
    @PreAuthorize("@ss.hasPermission('system:dict:quey')")
    public CommonResult<SysDictTypeRespVO> getDictType(@RequestParam("id") Long id) {
        return success(SysDictTypeConvert.INSTANCE.convert(sysDictTypeBussService.getDictType(id)));
    }

    @GetMapping("/list-all-simple")
    @ApiOperation(value = "获得全部字典类型列表", notes = "包括开启 + 禁用的字典类型，主要用于前端的下拉选项")
    // 无需添加权限认证，因为前端全局都需要
    public CommonResult<List<SysDictTypeSimpleRespVO>> listSimpleDictTypes() {
        List<SysDictTypeDO> list = sysDictTypeBussService.getDictTypeList();
        return success(SysDictTypeConvert.INSTANCE.convertList(list));
    }

    @ApiOperation("导出数据类型")
    @GetMapping("/export")
    @PreAuthorize("@ss.hasPermission('system:dict:query')")
    @OperateLog(type = EXPORT)
    public void export(HttpServletResponse response, @Valid SysDictTypeExportReqVO reqVO) throws IOException {
        List<SysDictTypeDO> list = sysDictTypeBussService.getDictTypeList(reqVO);
        List<SysDictTypeExcelVO> data = SysDictTypeConvert.INSTANCE.convertList02(list);
        // 输出
        ExcelUtils.write(response, "字典类型.xls", "类型列表", SysDictTypeExcelVO.class, data);
    }

}
