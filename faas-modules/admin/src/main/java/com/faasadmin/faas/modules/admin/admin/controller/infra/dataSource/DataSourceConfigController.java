package com.faasadmin.faas.modules.admin.admin.controller.infra.dataSource;

import com.faasadmin.faas.services.system.convert.dataSource.SysDataSourceConfigConvert;
import com.faasadmin.faas.services.system.dal.dataobject.dataSource.SysDataSourceConfigDO;
import com.faasadmin.faas.services.system.service.dataSource.SysDataSourceConfigService;
import com.faasadmin.faas.services.system.vo.dataSource.SysDataSourceConfigCreateReqVO;
import com.faasadmin.faas.services.system.vo.dataSource.SysDataSourceConfigRespVO;
import com.faasadmin.faas.services.system.vo.dataSource.SysDataSourceConfigUpdateReqVO;
import com.faasadmin.framework.common.pojo.CommonResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static com.faasadmin.framework.common.pojo.CommonResult.success;


@Api(tags = "管理后台 - 数据源配置")
@RestController
@RequestMapping("/infra/data-source-config")
@Validated
public class DataSourceConfigController {

    @Resource
    private SysDataSourceConfigService dataSourceConfigService;

    @PostMapping("/create")
    @ApiOperation("创建数据源配置")
    @PreAuthorize("@ss.hasPermission('infra:data-source-config:create')")
    public CommonResult<Long> createDataSourceConfig(@Valid @RequestBody SysDataSourceConfigCreateReqVO createReqVO) {
        return success(dataSourceConfigService.createDataSourceConfig(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新数据源配置")
    @PreAuthorize("@ss.hasPermission('infra:data-source-config:update')")
    public CommonResult<Boolean> updateDataSourceConfig(@Valid @RequestBody SysDataSourceConfigUpdateReqVO updateReqVO) {
        dataSourceConfigService.updateDataSourceConfig(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除数据源配置")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('infra:data-source-config:delete')")
    public CommonResult<Boolean> deleteDataSourceConfig(@RequestParam("id") Long id) {
        dataSourceConfigService.deleteDataSourceConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得数据源配置")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('infra:data-source-config:query')")
    public CommonResult<SysDataSourceConfigRespVO> getDataSourceConfig(@RequestParam("id") Long id) {
        SysDataSourceConfigDO dataSourceConfig = dataSourceConfigService.getDataSourceConfig(id);
        return success(SysDataSourceConfigConvert.INSTANCE.convert(dataSourceConfig));
    }

    @GetMapping("/list")
    @ApiOperation("获得数据源配置列表")
    @PreAuthorize("@ss.hasPermission('infra:data-source-config:query')")
    public CommonResult<List<SysDataSourceConfigRespVO>> getDataSourceConfigList() {
        List<SysDataSourceConfigDO> list = dataSourceConfigService.getDataSourceConfigList();
        return success(SysDataSourceConfigConvert.INSTANCE.convertList(list));
    }

}
