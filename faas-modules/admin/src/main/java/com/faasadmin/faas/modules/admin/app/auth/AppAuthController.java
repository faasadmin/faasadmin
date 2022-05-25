package com.faasadmin.faas.modules.admin.app.auth;

import cn.hutool.core.util.StrUtil;
import com.faasadmin.faas.business.member.service.MemberAuthService;
import com.faasadmin.faas.business.member.vo.auth.*;
import com.faasadmin.framework.common.pojo.CommonResult;
import com.faasadmin.framework.operatelog.core.annotations.OperateLog;
import com.faasadmin.framework.security.config.SecurityProperties;
import com.faasadmin.framework.security.core.annotations.PreAuthenticated;
import com.faasadmin.framework.security.core.util.SecurityFrameworkUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static com.faasadmin.framework.common.pojo.CommonResult.success;
import static com.faasadmin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Api(tags = "用户 APP - 认证")
@RestController
@RequestMapping("/member/auth")
@Validated
@Slf4j
public class AppAuthController {

    @Resource
    private MemberAuthService authService;

    @Resource
    private SecurityProperties securityProperties;

    @PostMapping("/login")
    @ApiOperation("使用手机 + 密码登录")
    public CommonResult<MemberAuthLoginRespVO> login(@RequestBody @Valid MemberAuthLoginReqVO reqVO) {
        return success(authService.login(reqVO));
    }

    @PostMapping("/logout")
    @ApiOperation("登出系统")
    public CommonResult<Boolean> logout(HttpServletRequest request) {
        String token = SecurityFrameworkUtils.obtainAuthorization(request, securityProperties.getTokenHeader());
        if (StrUtil.isNotBlank(token)) {
            authService.logout(token);
        }
        return success(true);
    }

    @PostMapping("/refresh-token")
    @ApiOperation("刷新令牌")
    @ApiImplicitParam(name = "refreshToken", value = "刷新令牌", required = true, dataTypeClass = String.class)
    @OperateLog(enable = false) // 避免 Post 请求被记录操作日志
    public CommonResult<MemberAuthLoginRespVO> refreshToken(@RequestParam("refreshToken") String refreshToken) {
        return success(authService.refreshToken(refreshToken));
    }

    // ========== 短信登录相关 ==========

    @PostMapping("/sms-login")
    @ApiOperation("使用手机 + 验证码登录")
    public CommonResult<MemberAuthLoginRespVO> smsLogin(@RequestBody @Valid MemberAuthSmsLoginReqVO reqVO) {
        return success(authService.smsLogin(reqVO));
    }

    @PostMapping("/send-sms-code")
    @ApiOperation(value = "发送手机验证码")
    public CommonResult<Boolean> sendSmsCode(@RequestBody @Valid MemberAuthSmsSendReqVO reqVO) {
        authService.sendSmsCode(getLoginUserId(), reqVO);
        return success(true);
    }

    @PostMapping("/reset-password")
    @ApiOperation(value = "重置密码", notes = "用户忘记密码时使用")
    @PreAuthenticated
    public CommonResult<Boolean> resetPassword(@RequestBody @Valid MemberAuthResetPasswordReqVO reqVO) {
        authService.resetPassword(reqVO);
        return success(true);
    }

    @PostMapping("/update-password")
    @ApiOperation(value = "修改用户密码", notes = "用户修改密码时使用")
    @PreAuthenticated
    public CommonResult<Boolean> updatePassword(@RequestBody @Valid MemberAuthUpdatePasswordReqVO reqVO) {
        authService.updatePassword(getLoginUserId(), reqVO);
        return success(true);
    }

    // ========== 社交登录相关 ==========

    @GetMapping("/social-auth-redirect")
    @ApiOperation("社交授权的跳转")
    @ApiImplicitParams({@ApiImplicitParam(name = "type", value = "社交类型", required = true, dataTypeClass = Integer.class), @ApiImplicitParam(name = "redirectUri", value = "回调路径", dataTypeClass = String.class)})
    public CommonResult<String> socialAuthRedirect(@RequestParam("type") Integer type, @RequestParam("redirectUri") String redirectUri) {
        return success(authService.getSocialAuthorizeUrl(type, redirectUri));
    }

    @PostMapping("/social-quick-login")
    @ApiOperation(value = "社交快捷登录，使用 code 授权码", notes = "适合未登录的用户，但是社交账号已绑定用户")
    public CommonResult<MemberAuthLoginRespVO> socialQuickLogin(@RequestBody @Valid MemberAuthSocialQuickLoginReqVO reqVO) {
        return success(authService.socialQuickLogin(reqVO));
    }

    @PostMapping("/social-bind-login")
    @ApiOperation(value = "社交绑定登录，使用 手机号 + 手机验证码", notes = "适合未登录的用户，进行登录 + 绑定")
    public CommonResult<MemberAuthLoginRespVO> socialBindLogin(@RequestBody @Valid MemberAuthSocialBindLoginReqVO reqVO) {
        return success(authService.socialBindLogin(reqVO));
    }

}
