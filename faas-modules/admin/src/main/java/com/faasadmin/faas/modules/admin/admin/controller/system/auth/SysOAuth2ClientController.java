package com.faasadmin.faas.modules.admin.admin.controller.system.auth;

import com.faasadmin.faas.business.core.module.system.service.auth.SysOAuth2ClientService;
import com.faasadmin.faas.services.system.convert.auth.SysOAuth2ClientConvert;
import com.faasadmin.faas.services.system.dal.dataobject.auth.SysOAuth2ClientDO;
import com.faasadmin.faas.services.system.vo.auth.client.SysOAuth2ClientCreateReqVO;
import com.faasadmin.faas.services.system.vo.auth.client.SysOAuth2ClientPageReqVO;
import com.faasadmin.faas.services.system.vo.auth.client.SysOAuth2ClientRespVO;
import com.faasadmin.faas.services.system.vo.auth.client.SysOAuth2ClientUpdateReqVO;
import com.faasadmin.framework.common.pojo.CommonResult;
import com.faasadmin.framework.common.pojo.PageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static com.faasadmin.framework.common.pojo.CommonResult.success;


@Api(tags = "管理后台 - OAuth2 客户端")
@RestController
@RequestMapping("/system/oauth2-client")
@Validated
public class SysOAuth2ClientController {

    @Resource
    private SysOAuth2ClientService oAuth2ClientService;

    @PostMapping("/create")
    @ApiOperation("创建 OAuth2 客户端")
    @PreAuthorize("@ss.hasPermission('system:oauth2-client:create')")
    public CommonResult<Long> createOAuth2Client(@Valid @RequestBody SysOAuth2ClientCreateReqVO createReqVO) {
        return success(oAuth2ClientService.createOAuth2Client(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新 OAuth2 客户端")
    @PreAuthorize("@ss.hasPermission('system:oauth2-client:update')")
    public CommonResult<Boolean> updateOAuth2Client(@Valid @RequestBody SysOAuth2ClientUpdateReqVO updateReqVO) {
        oAuth2ClientService.updateOAuth2Client(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除 OAuth2 客户端")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('system:oauth2-client:delete')")
    public CommonResult<Boolean> deleteOAuth2Client(@RequestParam("id") Long id) {
        oAuth2ClientService.deleteOAuth2Client(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得 OAuth2 客户端")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('system:oauth2-client:query')")
    public CommonResult<SysOAuth2ClientRespVO> getOAuth2Client(@RequestParam("id") Long id) {
        SysOAuth2ClientDO oAuth2Client = oAuth2ClientService.getOAuth2Client(id);
        return success(SysOAuth2ClientConvert.INSTANCE.convert(oAuth2Client));
    }

    @GetMapping("/page")
    @ApiOperation("获得OAuth2 客户端分页")
    @PreAuthorize("@ss.hasPermission('system:oauth2-client:query')")
    public CommonResult<PageResult<SysOAuth2ClientRespVO>> getOAuth2ClientPage(@Valid SysOAuth2ClientPageReqVO pageVO) {
        PageResult<SysOAuth2ClientDO> pageResult = oAuth2ClientService.getOAuth2ClientPage(pageVO);
        return success(SysOAuth2ClientConvert.INSTANCE.convertPage(pageResult));
    }

}
