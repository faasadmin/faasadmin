package com.faasadmin.faas.modules.admin.admin.controller.system.authorizeUrl;


import com.faasadmin.faas.services.system.convert.authorizeUrl.SysAuthorizeUrlConvert;
import com.faasadmin.faas.services.system.dal.dataobject.authorizeUrl.SysAuthorizeUrlDO;
import com.faasadmin.faas.services.system.service.authorizeUrl.SysAuthorizeUrlService;
import com.faasadmin.framework.common.pojo.CommonResult;
import com.faasadmin.framework.common.pojo.PageResult;
import com.faasadmin.framework.excel.core.util.ExcelUtils;
import com.faasadmin.framework.operatelog.core.annotations.OperateLog;
import com.faasadmin.faas.services.system.vo.authorizeUrl.*;
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

@Api(tags = "security认证地址配置")
@RestController
@RequestMapping("/system/authorizeUrl")
@Validated
public class SysAuthorizeUrlController {

    @Resource
    private SysAuthorizeUrlService authorizeUrlService;

    @PostMapping("/create")
    @ApiOperation("创建security认证地址配置")
    @PreAuthorize("@ss.hasPermission('system:authorizeUrl:create')")
    public CommonResult<Long> createAuthorizeUrl(@Valid @RequestBody SysAuthorizeUrlCreateReqVO createReqVO) {
        return success(authorizeUrlService.createAuthorizeUrl(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新security认证地址配置")
    @PreAuthorize("@ss.hasPermission('system:authorizeUrl:update')")
    public CommonResult<Boolean> updateAuthorizeUrl(@Valid @RequestBody SysAuthorizeUrlUpdateReqVO updateReqVO) {
        authorizeUrlService.updateAuthorizeUrl(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除security认证地址配置")
    @ApiImplicitParam(name = "id", value = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('system:authorizeUrl:delete')")
    public CommonResult<Boolean> deleteAuthorizeUrl(@RequestParam("id") Long id) {
        authorizeUrlService.deleteAuthorizeUrl(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得security认证地址配置")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('system:authorizeUrl:query')")
    public CommonResult<SysAuthorizeUrlRespVO> getAuthorizeUrl(@RequestParam("id") Long id) {
        SysAuthorizeUrlDO authorizeUrl = authorizeUrlService.getAuthorizeUrl(id);
        return success(SysAuthorizeUrlConvert.INSTANCE.convert(authorizeUrl));
    }

    @GetMapping("/list")
    @ApiOperation("获得security认证地址配置列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('system:authorizeUrl:query')")
    public CommonResult<List<SysAuthorizeUrlRespVO>> getAuthorizeUrlList(@RequestParam("ids") Collection<Long> ids) {
        List<SysAuthorizeUrlDO> list = authorizeUrlService.getAuthorizeUrlList(ids);
        return success(SysAuthorizeUrlConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得security认证地址配置分页")
    @PreAuthorize("@ss.hasPermission('system:authorizeUrl:query')")
    public CommonResult<PageResult<SysAuthorizeUrlRespVO>> getAuthorizeUrlPage(@Valid SysAuthorizeUrlPageReqVO pageVO) {
        PageResult<SysAuthorizeUrlDO> pageResult = authorizeUrlService.getAuthorizeUrlPage(pageVO);
        return success(SysAuthorizeUrlConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出security认证地址配置 Excel")
    @PreAuthorize("@ss.hasPermission('system:authorizeUrl:export')")
    @OperateLog(type = EXPORT)
    public void exportAuthorizeUrlExcel(@Valid SysAuthorizeUrlExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<SysAuthorizeUrlDO> list = authorizeUrlService.getAuthorizeUrlList(exportReqVO);
        // 导出 Excel
        List<SysAuthorizeUrlExcelVO> datas = SysAuthorizeUrlConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "security认证地址配置.xls", "数据", SysAuthorizeUrlExcelVO.class, datas);
    }

}
