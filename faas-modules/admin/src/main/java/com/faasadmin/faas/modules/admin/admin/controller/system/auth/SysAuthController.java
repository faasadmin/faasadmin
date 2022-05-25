package com.faasadmin.faas.modules.admin.admin.controller.system.auth;

import cn.hutool.core.util.StrUtil;
import com.faasadmin.faas.business.core.module.system.service.auth.SysAuthBussService;
import com.faasadmin.faas.business.core.module.system.service.permission.SysPermissionBussService;
import com.faasadmin.faas.business.core.module.system.service.permission.SysRoleBussService;
import com.faasadmin.faas.business.core.module.system.service.sms.SysSmsCodeBussService;
import com.faasadmin.faas.business.core.module.system.service.sms.SysSmsTemplateBussService;
import com.faasadmin.faas.business.core.module.system.service.social.SysSocialBussService;
import com.faasadmin.faas.business.core.module.system.service.user.SysUserBussService;
import com.faasadmin.faas.services.system.convert.auth.SysAuthConvert;
import com.faasadmin.faas.services.system.dal.dataobject.permission.SysMenuDO;
import com.faasadmin.faas.services.system.dal.dataobject.permission.SysRoleDO;
import com.faasadmin.faas.services.system.dal.dataobject.user.SysUserDO;
import com.faasadmin.faas.services.system.enums.logger.SysLoginLogTypeEnum;
import com.faasadmin.faas.services.system.enums.permission.MenuTypeEnum;
import com.faasadmin.faas.services.system.service.user.SysUserService;
import com.faasadmin.faas.services.system.vo.auth.login.*;
import com.faasadmin.faas.services.system.vo.auth.sys.SysAuthAscriptionMenuReqVO;
import com.faasadmin.faas.services.system.vo.auth.sys.SysAuthSideBarMenuRespVO;
import com.faasadmin.framework.common.enums.CommonStatusEnum;
import com.faasadmin.framework.common.pojo.CommonResult;
import com.faasadmin.framework.common.utils.collection.SetUtils;
import com.faasadmin.framework.operatelog.core.annotations.OperateLog;
import com.faasadmin.framework.security.config.SecurityProperties;
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
import java.util.List;
import java.util.Set;

import static com.faasadmin.framework.common.pojo.CommonResult.success;
import static com.faasadmin.framework.security.core.util.SecurityFrameworkUtils.*;
import static java.util.Collections.singleton;

@Api(tags = "认证")
@RestController
@RequestMapping("/system/auth")
@Validated
@Slf4j
public class SysAuthController {

    @Resource
    private SysAuthBussService sysAuthBussService;
    @Resource
    private SysUserBussService sysUserBussService;
    @Resource
    private SysRoleBussService sysRoleBussService;
    @Resource
    private SysPermissionBussService sysPermissionBussService;
    @Resource
    private SysSocialBussService sysSocialBussService;

    @Resource
    private SecurityProperties securityProperties;

    @PostMapping("/login")
    @ApiOperation("使用账号密码登录")
    @OperateLog(enable = false) // 避免 Post 请求被记录操作日志
    public CommonResult<SysAuthLoginRespVO> login(@RequestBody @Valid SysAuthLoginReqVO reqVO) {
        return success(sysAuthBussService.login(reqVO));
    }

    @PostMapping("/logout")
    @ApiOperation("登出系统")
    @OperateLog(enable = false) // 避免 Post 请求被记录操作日志
    public CommonResult<Boolean> logout(HttpServletRequest request) {
        String token = obtainAuthorization(request, securityProperties.getTokenHeader());
        if (StrUtil.isNotBlank(token)) {
            sysAuthBussService.logout(token, SysLoginLogTypeEnum.LOGOUT_SELF.getType());
        }
        return success(true);
    }

    @PostMapping("/refresh-token")
    @ApiOperation("刷新令牌")
    @ApiImplicitParam(name = "refreshToken", value = "刷新令牌", required = true, dataTypeClass = String.class)
    @OperateLog(enable = false) // 避免 Post 请求被记录操作日志
    public CommonResult<SysAuthLoginRespVO> refreshToken(@RequestParam("refreshToken") String refreshToken) {
        return success(sysAuthBussService.refreshToken(refreshToken));
    }

    @GetMapping("/get-permission-info")
    @ApiOperation("获取登录用户的权限信息")
    public CommonResult<SysAuthPermissionInfoRespVO> getPermissionInfo() {
        // 获得用户信息
        SysUserDO user = sysUserBussService.getUser(getLoginUserId());
        if (user == null) {
            return null;
        }
        // 获得角色列表
        Set<Long> roleIds = sysPermissionBussService.getUserRoleIdsFromCache(getLoginUserId(), singleton(CommonStatusEnum.ENABLE.getStatus()));
        List<SysRoleDO> roleList = sysRoleBussService.getRolesFromCache(roleIds);
        // 获得菜单列表
        List<SysMenuDO> menuList = sysPermissionBussService.getRoleMenuListFromCache(roleIds,
                SetUtils.asSet(MenuTypeEnum.DIR.getType(), MenuTypeEnum.MENU.getType(), MenuTypeEnum.BUTTON.getType()),
                singleton(CommonStatusEnum.ENABLE.getStatus())); // 只要开启的
        // 拼接结果返回
        return success(SysAuthConvert.INSTANCE.convert(user, roleList, menuList));
    }

    @GetMapping("/list-menus")
    @ApiOperation("获得登录用户的菜单列表")
    public CommonResult<SysAuthSideBarMenuRespVO> getMenus(@Valid SysAuthAscriptionMenuReqVO reqVo) {
        // 获得角色列表
        Set<Long> roleIds = sysPermissionBussService.getUserRoleIdsFromCache(getLoginUserId(), singleton(CommonStatusEnum.ENABLE.getStatus()));
        // 获得用户拥有的菜单列表
        List<SysMenuDO> menuList = sysPermissionBussService.getRoleMenusFromCache(
                roleIds, // 注意，基于登陆的角色，因为后续的权限判断也是基于它
                SetUtils.asSet(MenuTypeEnum.DIR.getType(), MenuTypeEnum.MENU.getType()), // 只要目录和菜单类型
                SetUtils.asSet(CommonStatusEnum.ENABLE.getStatus()), reqVo.getAscription()); // 只要开启的
        // 转换成 Tree 结构返回
        return success(sysAuthBussService.buildAllMenus(menuList));
    }

    // ========== 短信登录相关 ==========

    @PostMapping("/sms-login")
    @ApiOperation("使用短信验证码登录")
    @OperateLog(enable = false) // 避免 Post 请求被记录操作日志
    public CommonResult<SysAuthLoginRespVO> smsLogin(@RequestBody @Valid SysAuthSmsLoginReqVO reqVO) {
        return success(sysAuthBussService.smsLogin(reqVO));
    }

    @PostMapping("/send-sms-code")
    @ApiOperation(value = "发送手机验证码")
    @OperateLog(enable = false) // 避免 Post 请求被记录操作日志
    public CommonResult<Boolean> sendLoginSmsCode(@RequestBody @Valid SysAuthSmsSendReqVO reqVO) {
        sysAuthBussService.sendSmsCode(reqVO);
        return success(true);
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
        return CommonResult.success(sysSocialBussService.getAuthorizeUrl(type, redirectUri));
    }

    @PostMapping("/social-quick-login")
    @ApiOperation("社交快捷登录，使用 code 授权码")
    @OperateLog(enable = false) // 避免 Post 请求被记录操作日志
    public CommonResult<SysAuthLoginRespVO> socialQuickLogin(@RequestBody @Valid SysAuthSocialQuickLoginReqVO reqVO) {
        return success(sysAuthBussService.socialQuickLogin(reqVO));
    }

    @PostMapping("/social-bind-login")
    @ApiOperation("社交绑定登录，使用 code 授权码 + 账号密码")
    @OperateLog(enable = false) // 避免 Post 请求被记录操作日志
    public CommonResult<SysAuthLoginRespVO> socialBindLogin(@RequestBody @Valid SysAuthSocialBindLoginReqVO reqVO) {
        return success(sysAuthBussService.socialBindLogin(reqVO));
    }
}
