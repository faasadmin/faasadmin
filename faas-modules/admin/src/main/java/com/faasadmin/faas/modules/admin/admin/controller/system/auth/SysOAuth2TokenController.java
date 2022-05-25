package com.faasadmin.faas.modules.admin.admin.controller.system.auth;

import com.faasadmin.faas.business.core.module.system.service.auth.SysAuthBussService;
import com.faasadmin.faas.business.core.module.system.service.auth.SysOAuth2TokenService;
import com.faasadmin.faas.services.system.convert.auth.SysOAuth2TokenConvert;
import com.faasadmin.faas.services.system.dal.dataobject.auth.SysOAuth2AccessTokenDO;
import com.faasadmin.faas.services.system.enums.logger.SysLoginLogTypeEnum;
import com.faasadmin.faas.services.system.vo.auth.token.SysOAuth2AccessTokenPageReqVO;
import com.faasadmin.faas.services.system.vo.auth.token.SysOAuth2AccessTokenRespVO;
import com.faasadmin.framework.common.pojo.CommonResult;
import com.faasadmin.framework.common.pojo.PageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static com.faasadmin.framework.common.pojo.CommonResult.success;


@Api(tags = "管理后台 - OAuth2.0 令牌")
@RestController
@RequestMapping("/system/oauth2-token")
public class SysOAuth2TokenController {

    @Resource
    private SysOAuth2TokenService sysOAuth2TokenService;
    @Resource
    private SysAuthBussService sysAuthBussService;

    @GetMapping("/page")
    @ApiOperation(value = "获得访问令牌分页", notes = "只返回有效期内的")
    @PreAuthorize("@ss.hasPermission('system:oauth2-token:page')")
    public CommonResult<PageResult<SysOAuth2AccessTokenRespVO>> getAccessTokenPage(@Valid SysOAuth2AccessTokenPageReqVO reqVO) {
        PageResult<SysOAuth2AccessTokenDO> pageResult = sysOAuth2TokenService.getAccessTokenPage(reqVO);
        return success(SysOAuth2TokenConvert.INSTANCE.convert(pageResult));
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除访问令牌")
    @ApiImplicitParam(name = "accessToken", value = "访问令牌", required = true, dataTypeClass = String.class, example = "tudou")
    @PreAuthorize("@ss.hasPermission('system:oauth2-token:delete')")
    public CommonResult<Boolean> deleteAccessToken(@RequestParam("accessToken") String accessToken) {
        sysAuthBussService.logout(accessToken, SysLoginLogTypeEnum.LOGOUT_DELETE.getType());
        return success(true);
    }

}
