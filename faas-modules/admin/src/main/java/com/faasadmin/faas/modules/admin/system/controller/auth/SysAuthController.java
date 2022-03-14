package com.faasadmin.faas.modules.admin.system.controller.auth;

import com.faasadmin.faas.business.core.module.system.convert.auth.SysAuthConvert;
import com.faasadmin.faas.business.core.module.system.service.auth.SysAuthBussService;
import com.faasadmin.faas.business.core.module.system.service.permission.SysPermissionService;
import com.faasadmin.faas.business.core.module.system.service.permission.SysRoleService;
import com.faasadmin.faas.business.core.module.system.service.sms.SysSmsCodeService;
import com.faasadmin.faas.business.core.module.system.service.sms.SysSmsTemplateService;
import com.faasadmin.faas.business.core.module.system.service.social.SysSocialBussService;
import com.faasadmin.faas.services.system.dal.dataobject.permission.SysMenuDO;
import com.faasadmin.faas.services.system.dal.dataobject.permission.SysRoleDO;
import com.faasadmin.faas.services.system.dal.dataobject.user.SysUserDO;
import com.faasadmin.faas.services.system.enums.business.permission.MenuTypeEnum;
import com.faasadmin.faas.services.system.enums.business.sms.SysSmsSceneEnum;
import com.faasadmin.faas.services.system.enums.business.sms.SysSmsTemplateTagsEnum;
import com.faasadmin.faas.services.system.service.user.SysUserService;
import com.faasadmin.faas.services.system.vo.auth.mbr.MbrMobileAndPasswordLoginReqVO;
import com.faasadmin.faas.services.system.vo.auth.sms.*;
import com.faasadmin.faas.services.system.vo.auth.sys.*;
import com.faasadmin.framework.common.enums.CommonStatusEnum;
import com.faasadmin.framework.common.enums.UserTypeEnum;
import com.faasadmin.framework.common.pojo.CommonResult;
import com.faasadmin.framework.common.utils.collection.SetUtils;
import com.faasadmin.framework.security.core.annotations.PreAuthenticated;
import com.faasadmin.framework.web.core.util.WebFrameworkUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static com.faasadmin.framework.common.constant.SysErrorCodeConstants.USER_SMS_CODE_IS_EXISTS;
import static com.faasadmin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.faasadmin.framework.common.pojo.CommonResult.success;
import static com.faasadmin.framework.common.utils.ServletUtils.getClientIP;
import static com.faasadmin.framework.common.utils.ServletUtils.getUserAgent;
import static com.faasadmin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static com.faasadmin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserRoleIds;

@Api(tags = "认证")
@RestController
@RequestMapping("/")
@Validated
@Slf4j
public class SysAuthController {

    @Resource
    private SysAuthBussService sysAuthBussService;
    @Resource
    private SysUserService userService;
    @Resource
    private SysSmsCodeService smsCodeService;
    @Resource
    private SysSocialBussService socialService;
    @Resource
    private SysRoleService roleService;
    @Resource
    private SysPermissionService permissionService;
    @Resource
    private SysSmsTemplateService smsTemplateService;
    @Resource
    private SysUserService sysUserService;

    @PostMapping("/login")
    @ApiOperation("使用手机 + 密码登录")
    public CommonResult<SysSmsLoginRespVO> nickNameAndPasswordLogin(@RequestBody @Valid SysAuthNickNameAndPasswordLoginReqVO reqVO) {
        String token = sysAuthBussService.sysNickNameAndPasswordLogin(reqVO, getClientIP(), getUserAgent());
        // 返回结果
        return success(SysSmsLoginRespVO.builder().token(token).build());
    }

    @PostMapping("/mobile-login")
    @ApiOperation("使用手机 + 密码登录")
    public CommonResult<SysSmsLoginRespVO> clientMobileAndPasswordLogin(@RequestBody @Valid MbrMobileAndPasswordLoginReqVO reqVO) {
        String token = sysAuthBussService.clientMobileAndPasswordLogin(reqVO, getClientIP(), getUserAgent());
        // 返回结果
        return success(SysSmsLoginRespVO.builder().token(token).build());
    }

    @PostMapping("/sms-login")
    @ApiOperation("使用手机 + 验证码登录")
    public CommonResult<SysSmsLoginRespVO> smsLogin(@RequestBody @Valid SysSmsMobileAndCodeLoginReqVO reqVO) {
        String token = sysAuthBussService.sysSmsLogin(reqVO, getClientIP(), getUserAgent());
        // 返回结果
        return success(SysSmsLoginRespVO.builder().token(token).build());
    }


    @PostMapping("/send-sms-new-code")
    @ApiOperation(value = "发送手机验证码", notes = "检测该手机号是否已被注册，用于修改手机时使用")
    public CommonResult<Boolean> sendSmsNewCode(@RequestBody @Valid SysSendSmsReqVO reqVO) {
        // 检测手机号是否已被使用
        SysUserDO userByMobile = sysUserService.getUserByUserMobile(reqVO.getMobile());
        if (userByMobile != null) {
            throw exception(USER_SMS_CODE_IS_EXISTS);
        }
        smsCodeService.sendSmsNewCode(reqVO);
        return success(true);
    }

    @GetMapping("/send-sms-code-login")
    @ApiOperation(value = "向已登录用户发送验证码", notes = "修改手机时验证原手机号使用")
    public CommonResult<Boolean> sendSmsCodeLogin() {
        smsCodeService.sendSmsCodeLogin(getLoginUserId(),smsTemplateService.getDefSmsTemplateCodeByTag(SysSmsTemplateTagsEnum.CHANGE_PASSWORD.getTag()), SysSmsSceneEnum.CHANGE_MOBILE_BY_SMS.getScene());
        return success(true);
    }

    @PostMapping("/reset-password")
    @ApiOperation(value = "重置密码", notes = "用户忘记密码时使用")
    @PreAuthenticated
    public CommonResult<Boolean> resetPassword(@RequestBody @Valid SysSmsResetPasswordReqVO reqVO) {
        sysAuthBussService.resetPassword(reqVO);
        return success(true);
    }

    @PostMapping("/update-password")
    @ApiOperation(value = "修改用户密码", notes = "用户修改密码时使用")
    @PreAuthenticated
    public CommonResult<Boolean> updatePassword(@RequestBody @Valid SysAuthUpdatePasswordReqVO reqVO) {
        sysAuthBussService.updatePassword(getLoginUserId(), reqVO);
        return success(true);
    }

    @PostMapping("/check-sms-code")
    @ApiOperation(value = "校验验证码是否正确")
    @PreAuthenticated
    public CommonResult<Boolean> checkSmsCode(@RequestBody @Valid SysSmsMobileAndCodeCheckReqVO reqVO) {
        smsCodeService.checkSmsCode(reqVO.getMobile(), SysSmsSceneEnum.CHECK_CODE_BY_SMS.getScene(), reqVO.getCode());
        return success(true);
    }

    @GetMapping("/get-permission-info")
    @ApiOperation("获取登陆用户的权限信息")
    public CommonResult<SysAuthPermissionInfoRespVO> getPermissionInfo() {
        // 获得用户信息
        SysUserDO user = userService.getUser(WebFrameworkUtils.getLoginUserId());
        if (user == null) {
            return null;
        }
        // 获得角色列表
        List<SysRoleDO> roleList = roleService.getRolesFromCache(getLoginUserRoleIds());
        // 获得菜单列表
        List<SysMenuDO> menuList = permissionService.getRoleMenusFromCache(
                getLoginUserRoleIds(), // 注意，基于登陆的角色，因为后续的权限判断也是基于它
                SetUtils.asSet(MenuTypeEnum.DIR.getType(), MenuTypeEnum.MENU.getType(), MenuTypeEnum.BUTTON.getType()),
                SetUtils.asSet(CommonStatusEnum.ENABLE.getStatus()));
        // 拼接结果返回
        return success(SysAuthConvert.INSTANCE.convert(user, roleList, menuList));
    }

    @GetMapping("list-menus")
    @ApiOperation("获得登陆用户的菜单列表")
    public CommonResult<SysAuthSideBarMenuRespVo> getMenus(@Valid SysAuthAscriptionMenuReqVo reqVo) {
        // 获得用户拥有的菜单列表
        List<SysMenuDO> menuList = permissionService.getRoleMenusFromCache(
                getLoginUserRoleIds(), // 注意，基于登陆的角色，因为后续的权限判断也是基于它
                SetUtils.asSet(MenuTypeEnum.DIR.getType(), MenuTypeEnum.MENU.getType()), // 只要目录和菜单类型
                SetUtils.asSet(CommonStatusEnum.ENABLE.getStatus()), reqVo.getAscription()); // 只要开启的
        // 转换成 Tree 结构返回
        return success(sysAuthBussService.buildAllMenus(menuList));
    }
    // ========== 社交登录相关 ==========

    @GetMapping("/social-auth-redirect")
    @ApiOperation("社交授权的跳转")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "社交类型", required = true, dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "redirectUri", value = "回调路径", dataTypeClass = String.class)
    })
    public CommonResult<String> socialAuthRedirect(@RequestParam("type") Integer type,
                                                   @RequestParam("redirectUri") String redirectUri) {
        return success(socialService.getAuthorizeUrl(type, redirectUri));
    }

    @PostMapping("/social-login")
    @ApiOperation("社交登录，使用 code 授权码")
    public CommonResult<SysSmsLoginRespVO> socialLogin(@RequestBody @Valid SysAuthSocialLoginReqVO reqVO) {
        String token = sysAuthBussService.sysSocialCodeLogin(reqVO, getClientIP(), getUserAgent());
        return success(SysSmsLoginRespVO.builder().token(token).build());
    }

    @PostMapping("/social-login2")
    @ApiOperation("社交登录，使用 手机号 + 手机验证码")
    public CommonResult<SysSmsLoginRespVO> socialLogin2(@RequestBody @Valid SysAuthSocialLogin2ReqVO reqVO) {
        String token = sysAuthBussService.sysSocialCodeAndAccountPasswordLogin(reqVO, getClientIP(), getUserAgent());
        return success(SysSmsLoginRespVO.builder().token(token).build());
    }

    @PostMapping("/social-bind")
    @ApiOperation("社交绑定，使用 code 授权码")
    public CommonResult<Boolean> socialBind(@RequestBody @Valid SysAuthSocialBindReqVO reqVO) {
        sysAuthBussService.sysSocialBind(getLoginUserId(), reqVO);
        return success(true);
    }

    @DeleteMapping("/social-unbind")
    @ApiOperation("取消社交绑定")
    public CommonResult<Boolean> socialUnbind(@RequestBody SysAuthSocialUnbindReqVO reqVO) {
        socialService.unbindSocialUser(getLoginUserId(), reqVO.getType(), reqVO.getUnionId(), UserTypeEnum.MEMBER);
        return success(true);
    }

}
