package com.faasadmin.faas.modules.admin.admin.controller.system.sms;

import com.faasadmin.faas.business.core.module.system.service.sms.SysSmsSendBussService;
import com.faasadmin.faas.business.core.module.system.service.sms.SysSmsTemplateBussService;
import com.faasadmin.faas.services.system.convert.sms.SysSmsTemplateConvert;
import com.faasadmin.faas.services.system.dal.dataobject.sms.SysSmsTemplateDO;
import com.faasadmin.framework.common.pojo.CommonResult;
import com.faasadmin.framework.common.pojo.PageResult;
import com.faasadmin.framework.excel.core.util.ExcelUtils;
import com.faasadmin.framework.operatelog.core.annotations.OperateLog;
import com.faasadmin.faas.services.system.vo.sms.template.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

import static com.faasadmin.framework.common.pojo.CommonResult.success;
import static com.faasadmin.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Api(tags = "短信模板")
@RestController
@RequestMapping("/system/sms-template")
public class SysSmsTemplateController {

    @Resource
    private SysSmsTemplateBussService sysSmsTemplateBussService;
    @Resource
    private SysSmsSendBussService sysSmsBussService;

    @PostMapping("/create")
    @ApiOperation("创建短信模板")
    @PreAuthorize("@ss.hasPermission('system:sms-template:create')")
    public CommonResult<Long> createSmsTemplate(@Valid @RequestBody SysSmsTemplateCreateReqVO createReqVO) {
        return success(sysSmsTemplateBussService.createSmsTemplate(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新短信模板")
    @PreAuthorize("@ss.hasPermission('system:sms-template:update')")
    public CommonResult<Boolean> updateSmsTemplate(@Valid @RequestBody SysSmsTemplateUpdateReqVO updateReqVO) {
        sysSmsTemplateBussService.updateSmsTemplate(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除短信模板")
    @ApiImplicitParam(name = "id", value = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('system:sms-template:delete')")
    public CommonResult<Boolean> deleteSmsTemplate(@RequestParam("id") Long id) {
        sysSmsTemplateBussService.deleteSmsTemplate(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得短信模板")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('system:sms-template:query')")
    public CommonResult<SysSmsTemplateRespVO> getSmsTemplate(@RequestParam("id") Long id) {
        SysSmsTemplateDO smsTemplate = sysSmsTemplateBussService.getSmsTemplate(id);
        return success(SysSmsTemplateConvert.INSTANCE.convert(smsTemplate));
    }

    @GetMapping("/page")
    @ApiOperation("获得短信模板分页")
    @PreAuthorize("@ss.hasPermission('system:sms-template:query')")
    public CommonResult<PageResult<SysSmsTemplateRespVO>> getSmsTemplatePage(@Valid SysSmsTemplatePageReqVO pageVO) {
        PageResult<SysSmsTemplateDO> pageResult = sysSmsTemplateBussService.getSmsTemplatePage(pageVO);
        return success(SysSmsTemplateConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出短信模板 Excel")
    @PreAuthorize("@ss.hasPermission('system:sms-template:export')")
    @OperateLog(type = EXPORT)
    public void exportSmsTemplateExcel(@Valid SysSmsTemplateExportReqVO exportReqVO,
                                       HttpServletResponse response) throws IOException {
        List<SysSmsTemplateDO> list = sysSmsTemplateBussService.getSmsTemplateList(exportReqVO);
        // 导出 Excel
        List<SysSmsTemplateExcelVO> datas = SysSmsTemplateConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "短信模板.xls", "数据", SysSmsTemplateExcelVO.class, datas);
    }

    @PostMapping("/send-sms")
    @ApiOperation("发送短信")
    @PreAuthorize("@ss.hasPermission('system:sms-template:send-sms')")
    public CommonResult<Long> sendSms(@Valid @RequestBody SysSmsTemplateSendReqVO sendReqVO) {
        return success(sysSmsBussService.sendSingleSms(sendReqVO.getMobile(), null, null,
                sendReqVO.getTemplateCode(), sendReqVO.getTemplateParams()));
    }

}
