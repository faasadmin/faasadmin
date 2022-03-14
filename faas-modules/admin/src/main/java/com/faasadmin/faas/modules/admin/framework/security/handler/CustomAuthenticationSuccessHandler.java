package com.faasadmin.faas.modules.admin.framework.security.handler;

import com.faasadmin.faas.business.core.module.system.service.auth.SysUserSessionBussService;
import com.faasadmin.faas.business.core.module.system.service.permission.SysPermissionService;
import com.faasadmin.faas.services.system.vo.auth.sms.SysSmsLoginRespVO;
import com.faasadmin.framework.common.pojo.CommonResult;
import com.faasadmin.framework.common.utils.ServletUtils;
import com.faasadmin.framework.security.config.SecurityProperties;
import com.faasadmin.framework.security.core.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;

import static com.faasadmin.framework.common.pojo.CommonResult.success;
import static com.faasadmin.framework.common.utils.ServletUtils.getClientIP;
import static com.faasadmin.framework.common.utils.ServletUtils.getUserAgent;

/**
 * 自定义成功认证
 **/
@Slf4j
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Resource
    private SysUserSessionBussService userSessionService;

    @Resource
    private SysPermissionService sysPermissionService;

    @Resource
    private SecurityProperties securityProperties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.debug("[commence][访问 URL({}) 时，认证成功]", request.getRequestURI());
        Duration sessionTimeout = securityProperties.getSessionTimeout();
        // 生成并返回token给客户端，后续访问携带此token
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        loginUser.setRoleIds(sysPermissionService.getUserRoleIds(loginUser.getId())); // 获取用户角色列表
        // 缓存登陆用户到 Redis 中，返回 sessionId 编号
        String token = userSessionService.createUserSession(loginUser, getClientIP(), getUserAgent());
        CommonResult<SysSmsLoginRespVO> success = success(SysSmsLoginRespVO.builder().token(token).expiresTime(sessionTimeout.toString()).build());
        ServletUtils.writeJSON(response, success);
    }

}
