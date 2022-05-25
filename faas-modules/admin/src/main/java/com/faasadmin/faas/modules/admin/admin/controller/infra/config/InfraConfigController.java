package com.faasadmin.faas.modules.admin.admin.controller.infra.config;


import com.faasadmin.faas.services.infra.convert.config.InfraConfigConvert;
import com.faasadmin.faas.services.infra.dal.dataobject.config.InfraConfigDO;
import com.faasadmin.faas.services.infra.service.config.InfraConfigService;
import com.faasadmin.faas.services.infra.vo.config.*;
import com.faasadmin.framework.common.pojo.CommonResult;
import com.faasadmin.framework.common.pojo.PageResult;
import com.faasadmin.framework.excel.core.util.ExcelUtils;
import com.faasadmin.framework.operatelog.core.annotations.OperateLog;
import com.faasadmin.framework.operatelog.core.enums.OperateTypeEnum;
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

import static com.faasadmin.framework.common.constant.ErrorCodeConstants.CONFIG_GET_VALUE_ERROR_IF_SENSITIVE;
import static com.faasadmin.framework.common.exception.util.ServiceExceptionUtil.exception;

@Api(tags = "参数配置")
@RestController
@RequestMapping("/infra/config")
@Validated
public class InfraConfigController {

    @Resource
    private InfraConfigService configService;

    @PostMapping("/create-json")
    @ApiOperation("创建参数配置")
    @PreAuthorize("@ss.hasPermission('infra:config:create')")
    public CommonResult<Long> createJsonConfig(@Valid @RequestBody InfraConfigJsonCreateReqVO reqVO) {
        return CommonResult.success(configService.createConfig(reqVO));
    }

    @PostMapping("/create")
    @ApiOperation("创建参数配置")
    @PreAuthorize("@ss.hasPermission('infra:config:create')")
    public CommonResult<Long> createConfig(@Valid @RequestBody InfraConfigCreateReqVO reqVO) {
        return CommonResult.success(configService.createConfig(reqVO));
    }

    @PutMapping("/update")
    @ApiOperation("修改参数配置")
    @PreAuthorize("@ss.hasPermission('infra:config:update')")
    public CommonResult<Boolean> updateConfig(@Valid @RequestBody InfraConfigUpdateReqVO reqVO) {
        configService.updateConfig(reqVO);
        return CommonResult.success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除参数配置")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('infra:config:delete')")
    public CommonResult<Boolean> deleteConfig(@RequestParam("id") Long id) {
        configService.deleteConfig(id);
        return CommonResult.success(true);
    }

    @GetMapping(value = "/get")
    @ApiOperation("获得参数配置")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('infra:config:query')")
    public CommonResult<InfraConfigRespVO> getConfig(@RequestParam("id") Long id) {
        return CommonResult.success(InfraConfigConvert.INSTANCE.convert(configService.getConfig(id)));
    }

    @GetMapping(value = "/get-value-by-key")
    @ApiOperation(value = "根据参数键名查询参数值", notes = "敏感配置，不允许返回给前端")
    @ApiImplicitParam(name = "key", value = "参数键", required = true, example = "yunai.biz.username", dataTypeClass = String.class)
    public CommonResult<String> getConfigKey(@RequestParam("key") String key) {
        InfraConfigDO config = configService.getConfigByKey(key);
        if (config == null) {
            return null;
        }
        if (config.getSensitive()) {
            throw exception(CONFIG_GET_VALUE_ERROR_IF_SENSITIVE);
        }
        return CommonResult.success(config.getValue());
    }

    @GetMapping("/page")
    @ApiOperation("获取参数配置分页")
    @PreAuthorize("@ss.hasPermission('infra:config:query')")
    public CommonResult<PageResult<InfraConfigRespVO>> getConfigPage(@Valid InfraConfigPageReqVO reqVO) {
        PageResult<InfraConfigDO> page = configService.getConfigPage(reqVO);
        return CommonResult.success(InfraConfigConvert.INSTANCE.convertPage(page));
    }

    @GetMapping("/export")
    @ApiOperation("导出参数配置")
    @PreAuthorize("@ss.hasPermission('infra:config:export')")
    @OperateLog(type = OperateTypeEnum.EXPORT)
    public void exportSysConfig(@Valid InfraConfigExportReqVO reqVO,
                                HttpServletResponse response) throws IOException {
        List<InfraConfigDO> list = configService.getConfigList(reqVO);
        // 拼接数据
        List<InfraConfigExcelVO> datas = InfraConfigConvert.INSTANCE.convertList(list);
        // 输出
        ExcelUtils.write(response, "参数配置.xls", "数据", InfraConfigExcelVO.class, datas);
    }

}
