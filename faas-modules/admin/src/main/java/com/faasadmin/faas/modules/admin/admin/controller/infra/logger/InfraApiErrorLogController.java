package com.faasadmin.faas.modules.admin.admin.controller.infra.logger;

import com.faasadmin.faas.services.infra.convert.logger.InfraApiErrorLogConvert;
import com.faasadmin.faas.services.infra.dal.dataobject.logger.InfraApiErrorLogDO;
import com.faasadmin.faas.services.infra.service.logger.InfraApiErrorLogService;
import com.faasadmin.faas.services.infra.vo.logger.apierrorlog.InfraApiErrorLogExcelVO;
import com.faasadmin.faas.services.infra.vo.logger.apierrorlog.InfraApiErrorLogExportReqVO;
import com.faasadmin.faas.services.infra.vo.logger.apierrorlog.InfraApiErrorLogPageReqVO;
import com.faasadmin.faas.services.infra.vo.logger.apierrorlog.InfraApiErrorLogRespVO;
import com.faasadmin.framework.common.pojo.CommonResult;
import com.faasadmin.framework.common.pojo.PageResult;
import com.faasadmin.framework.excel.core.util.ExcelUtils;
import com.faasadmin.framework.operatelog.core.annotations.OperateLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
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
import static com.faasadmin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Api(tags = "API 错误日志")
@RestController
@RequestMapping("/infra/api-error-log")
@Validated
public class InfraApiErrorLogController {

    @Resource
    private InfraApiErrorLogService apiErrorLogService;

    @PutMapping("/update-status")
    @ApiOperation("更新 API 错误日志的状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class),
            @ApiImplicitParam(name = "processStatus", value = "处理状态", required = true, example = "1", dataTypeClass = Integer.class)
    })
    @PreAuthorize("@ss.hasPermission('infra:api-error-log:update-status')")
    public CommonResult<Boolean> updateApiErrorLogProcess(@RequestParam("id") Long id, @RequestParam("processStatus") Integer processStatus) {
        apiErrorLogService.updateApiErrorLogProcess(id, processStatus, getLoginUserId());
        return success(true);
    }

    @GetMapping("/page")
    @ApiOperation("获得 API 错误日志分页")
    @PreAuthorize("@ss.hasPermission('infra:api-error-log:query')")
    public CommonResult<PageResult<InfraApiErrorLogRespVO>> getApiErrorLogPage(@Valid InfraApiErrorLogPageReqVO pageVO) {
        PageResult<InfraApiErrorLogDO> pageResult = apiErrorLogService.getApiErrorLogPage(pageVO);
        return success(InfraApiErrorLogConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出 API 错误日志 Excel")
    @PreAuthorize("@ss.hasPermission('infra:api-error-log:export')")
    @OperateLog(type = EXPORT)
    public void exportApiErrorLogExcel(@Valid InfraApiErrorLogExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<InfraApiErrorLogDO> list = apiErrorLogService.getApiErrorLogList(exportReqVO);
        // 导出 Excel
        List<InfraApiErrorLogExcelVO> datas = InfraApiErrorLogConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "API 错误日志.xls", "数据", InfraApiErrorLogExcelVO.class, datas);
    }

}
