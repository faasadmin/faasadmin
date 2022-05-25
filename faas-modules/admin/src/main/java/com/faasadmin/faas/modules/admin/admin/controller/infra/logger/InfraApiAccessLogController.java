package com.faasadmin.faas.modules.admin.admin.controller.infra.logger;

import com.faasadmin.faas.services.infra.convert.logger.InfraApiAccessLogConvert;
import com.faasadmin.faas.services.infra.dal.dataobject.logger.InfraApiAccessLogDO;
import com.faasadmin.faas.services.infra.service.logger.InfraApiAccessLogService;
import com.faasadmin.faas.services.infra.vo.logger.apiaccesslog.InfraApiAccessLogExcelVO;
import com.faasadmin.faas.services.infra.vo.logger.apiaccesslog.InfraApiAccessLogExportReqVO;
import com.faasadmin.faas.services.infra.vo.logger.apiaccesslog.InfraApiAccessLogPageReqVO;
import com.faasadmin.faas.services.infra.vo.logger.apiaccesslog.InfraApiAccessLogRespVO;
import com.faasadmin.framework.common.pojo.CommonResult;
import com.faasadmin.framework.common.pojo.PageResult;
import com.faasadmin.framework.excel.core.util.ExcelUtils;
import com.faasadmin.framework.operatelog.core.annotations.OperateLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

import static com.faasadmin.framework.common.pojo.CommonResult.success;
import static com.faasadmin.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Api(tags = "API 访问日志")
@RestController
@RequestMapping("/infra/api-access-log")
@Validated
public class InfraApiAccessLogController {

    @Resource
    private InfraApiAccessLogService apiAccessLogService;

    @GetMapping("/page")
    @ApiOperation("获得API 访问日志分页")
    @PreAuthorize("@ss.hasPermission('infra:api-access-log:query')")
    public CommonResult<PageResult<InfraApiAccessLogRespVO>> getApiAccessLogPage(@Valid InfraApiAccessLogPageReqVO pageVO) {
        PageResult<InfraApiAccessLogDO> pageResult = apiAccessLogService.getApiAccessLogPage(pageVO);
        return success(InfraApiAccessLogConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出API 访问日志 Excel")
    @PreAuthorize("@ss.hasPermission('infra:api-access-log:export')")
    @OperateLog(type = EXPORT)
    public void exportApiAccessLogExcel(@Valid InfraApiAccessLogExportReqVO exportReqVO,
                                        HttpServletResponse response) throws IOException {
        List<InfraApiAccessLogDO> list = apiAccessLogService.getApiAccessLogList(exportReqVO);
        // 导出 Excel
        List<InfraApiAccessLogExcelVO> datas = InfraApiAccessLogConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "API 访问日志.xls", "数据", InfraApiAccessLogExcelVO.class, datas);
    }

}
