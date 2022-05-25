package com.faasadmin.faas.modules.admin.admin.controller.system.saas.lessee;

import com.faasadmin.faas.business.core.module.saas.service.lessee.SaasLesseeBussService;
import com.faasadmin.faas.business.core.support.controller.BaseController;
import com.faasadmin.faas.services.lessee.convert.lesseeConfig.SaasLesseeConfigConvert;
import com.faasadmin.faas.services.lessee.dal.dataobject.lesseeConfig.SaasLesseeConfigDO;
import com.faasadmin.faas.services.lessee.service.lesseeConfig.SaasLesseeConfigService;
import com.faasadmin.faas.services.lessee.vo.lesseeConfig.*;
import com.faasadmin.framework.common.pojo.CommonResult;
import com.faasadmin.framework.common.pojo.PageResult;
import com.faasadmin.framework.excel.core.util.ExcelUtils;
import com.faasadmin.framework.operatelog.core.annotations.OperateLog;
import com.faasadmin.framework.security.core.util.SecurityFrameworkUtils;
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

@Api(tags = "租户配置")
@RestController
@RequestMapping("/saas/lessee-config")
@Validated
public class SaasLesseeConfigController extends BaseController {

    @Resource
    private SaasLesseeConfigService lesseeConfigService;

    @Resource
    private SaasLesseeBussService saasLesseeBussService;

    @GetMapping(value = "/system")
    @ApiOperation("获得租户系统配置")
    @PreAuthorize("@ss.hasPermission('saas:lessee-config:query')")
    public CommonResult<SaasLesseeConfigAssembleRespVO> system() {
        Long lesseeId = SecurityFrameworkUtils.getLesseeId();
        Long appId = getHeaderAppId();
        SaasLesseeConfigAssembleRespVO respVO = saasLesseeBussService.selectLesseeConfig(lesseeId, appId);
        return success(respVO);
    }

    @PostMapping("/create-json")
    @ApiOperation("创建租户配置")
    @PreAuthorize("@ss.hasPermission('saas:lessee-config:create')")
    public CommonResult<Long> createLesseeJsonConfig(@Valid @RequestBody SaasLesseeJsonConfigCreateReqVO createReqVO) {
        setAppInfo(createReqVO);
        return success(lesseeConfigService.createLesseeConfig(createReqVO));
    }

    @PostMapping("/create")
    @ApiOperation("创建租户配置")
    @PreAuthorize("@ss.hasPermission('saas:lessee-config:create')")
    public CommonResult<Long> createLesseeConfig(@Valid @RequestBody SaasLesseeConfigCreateReqVO createReqVO) {
        setAppInfo(createReqVO);
        return success(lesseeConfigService.createLesseeConfig(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新租户配置")
    @PreAuthorize("@ss.hasPermission('saas:lessee-config:update')")
    public CommonResult<Boolean> updateLesseeConfig(@Valid @RequestBody SaasLesseeConfigUpdateReqVO updateReqVO) {
        setAppInfo(updateReqVO);
        lesseeConfigService.updateLesseeConfig(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-json")
    @ApiOperation("更新租户配置")
    @PreAuthorize("@ss.hasPermission('saas:lessee-config:update')")
    public CommonResult<Boolean> updateLesseeJsonConfig(@Valid @RequestBody SaasLesseeJsonConfigUpdateReqVO updateReqVO) {
        setAppInfo(updateReqVO);
        lesseeConfigService.updateLesseeJsonConfig(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除租户配置")
    @ApiImplicitParam(name = "id", value = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('saas:lessee-config:delete')")
    public CommonResult<Boolean> deleteLesseeConfig(@RequestParam("id") Long id) {
        lesseeConfigService.deleteLesseeConfig(id);
        return success(true);
    }

    @GetMapping("/get-key")
    @ApiOperation("获得租户配置")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = String.class)
    @PreAuthorize("@ss.hasPermission('saas:lessee-config:query')")
    public CommonResult<SaasLesseeConfigRespVO> getLesseeConfigByKey(@RequestParam("key") String key) {
        SaasLesseeConfigDO lesseeConfig = lesseeConfigService.getLesseeConfigByKey(SecurityFrameworkUtils.getLesseeId(), getHeaderAppId(), key);
        return success(SaasLesseeConfigConvert.INSTANCE.convert(lesseeConfig));
    }

    @GetMapping("/get-json-key")
    @ApiOperation("获得租户配置")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = String.class)
    @PreAuthorize("@ss.hasPermission('saas:lessee-config:query')")
    public CommonResult<SaasLesseeJsonConfigRespVO> getLesseeJsonConfigByKey(@RequestParam("key") String key) {
        SaasLesseeConfigDO lesseeConfig = lesseeConfigService.getLesseeConfigByKey(SecurityFrameworkUtils.getLesseeId(), getHeaderAppId(), key);
        return success(SaasLesseeConfigConvert.INSTANCE.convertToJsonConfig(lesseeConfig));
    }

    @GetMapping("/get")
    @ApiOperation("获得租户配置")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Integer.class)
    @PreAuthorize("@ss.hasPermission('saas:lessee-config:query')")
    public CommonResult<SaasLesseeConfigRespVO> getLesseeConfig(@RequestParam("id") Long id) {
        SaasLesseeConfigDO lesseeConfig = lesseeConfigService.getLesseeConfig(id);
        return success(SaasLesseeConfigConvert.INSTANCE.convert(lesseeConfig));
    }

    @GetMapping("/list")
    @ApiOperation("获得租户配置列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('saas:lessee-config:query')")
    public CommonResult<List<SaasLesseeConfigRespVO>> getLesseeConfigList(@RequestParam("ids") Collection<Long> ids) {
        List<SaasLesseeConfigDO> list = lesseeConfigService.getLesseeConfigList(ids);
        return success(SaasLesseeConfigConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得租户配置分页")
    @PreAuthorize("@ss.hasPermission('saas:lessee-config:query')")
    public CommonResult<PageResult<SaasLesseeConfigRespVO>> getLesseeConfigPage(@Valid SaasLesseeConfigPageReqVO pageVO) {
        PageResult<SaasLesseeConfigDO> pageResult = lesseeConfigService.getLesseeConfigPage(pageVO);
        return success(SaasLesseeConfigConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出租户配置 Excel")
    @PreAuthorize("@ss.hasPermission('saas:lessee-config:export')")
    @OperateLog(type = EXPORT)
    public void exportLesseeConfigExcel(@Valid SaasLesseeConfigExportReqVO exportReqVO,
                                        HttpServletResponse response) throws IOException {
        List<SaasLesseeConfigDO> list = lesseeConfigService.getLesseeConfigList(exportReqVO);
        // 导出 Excel
        List<SaasLesseeConfigExcelVO> datas = SaasLesseeConfigConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "租户配置.xls", "数据", SaasLesseeConfigExcelVO.class, datas);
    }

}
