package com.faasadmin.faas.modules.admin.admin.controller.system.saas.lessee;


import com.faasadmin.faas.business.core.module.saas.service.lessee.SaasLesseeBussService;
import com.faasadmin.faas.services.lessee.convert.lessee.SaasLesseeConvert;
import com.faasadmin.faas.services.lessee.dal.dataobject.lessee.SaasLesseeDO;
import com.faasadmin.faas.services.lessee.vo.lessee.*;
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

@Api(tags = "租户")
@RestController
@RequestMapping("/saas/lessee")
@Validated
public class SaasLesseeController {

    @Resource
    private SaasLesseeBussService lesseeService;

    @GetMapping("/get-id-by-name")
    @ApiOperation(value = "使用租户名，获得租户编号", notes = "登录界面，根据用户的租户名，获得租户编号")
    @ApiImplicitParam(name = "name", value = "租户名", required = true, example = "1024", dataTypeClass = Long.class)
    public CommonResult<Long> getTenantIdByName(@RequestParam("name") String name) {
        SaasLesseeDO tenantDO = lesseeService.getLesseeByName(name);
        return success(tenantDO != null ? tenantDO.getId() : null);
    }

    @PostMapping("/create")
    @ApiOperation("创建租户")
    @PreAuthorize("@ss.hasPermission('saas:lessee:create')")
    public CommonResult<Long> createLessee(@Valid @RequestBody SaasLesseeCreateReqVO createReqVO) {
        return success(lesseeService.createLessee(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新租户")
    @PreAuthorize("@ss.hasPermission('saas:lessee:update')")
    public CommonResult<Boolean> updateLessee(@Valid @RequestBody SaasLesseeUpdateReqVO updateReqVO) {
        lesseeService.updateLessee(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除租户")
    @ApiImplicitParam(name = "id", value = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('saas:lessee:delete')")
    public CommonResult<Boolean> deleteLessee(@RequestParam("id") Long id) {
        lesseeService.deleteLessee(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得租户")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('saas:lessee:query')")
    public CommonResult<SaasLesseeRespVO> getLessee(@RequestParam("id") Long id) {
        SaasLesseeDO lessee = lesseeService.getLessee(id);
        return success(SaasLesseeConvert.INSTANCE.convert(lessee));
    }

    @GetMapping("/list")
    @ApiOperation("获得租户列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('saas:lessee:query')")
    public CommonResult<List<SaasLesseeRespVO>> getLesseeList(@RequestParam("ids") Collection<Long> ids) {
        List<SaasLesseeDO> list = lesseeService.getLesseeList(ids);
        return success(SaasLesseeConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得租户分页")
    @PreAuthorize("@ss.hasPermission('saas:lessee:query')")
    public CommonResult<PageResult<SaasLesseeRespVO>> getLesseePage(@Valid SaasLesseePageReqVO pageVO) {
        PageResult<SaasLesseeDO> pageResult = lesseeService.getLesseePage(pageVO);
        return success(SaasLesseeConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出租户 Excel")
    @PreAuthorize("@ss.hasPermission('saas:lessee:export')")
    @OperateLog(type = EXPORT)
    public void exportLesseeExcel(@Valid SaasLesseeExportReqVO exportReqVO,
                                  HttpServletResponse response) throws IOException {
        List<SaasLesseeDO> list = lesseeService.getLesseeList(exportReqVO);
        // 导出 Excel
        List<SaasLesseeExcelVO> datas = SaasLesseeConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "租户.xls", "数据", SaasLesseeExcelVO.class, datas);
    }

}
