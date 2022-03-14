package com.faasadmin.faas.modules.admin.framework.security.provider.jwt;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.faasadmin.faas.business.base.cache.RedisCache;
import com.faasadmin.faas.business.base.spirng.SpringUtils;
import com.faasadmin.faas.business.core.module.system.service.auth.SysAuthBussService;
import com.faasadmin.faas.business.core.module.system.service.auth.dto.LoginTypeDataDTO;
import com.faasadmin.faas.business.core.module.system.service.captcha.SysCaptchaService;
import com.faasadmin.faas.business.core.module.system.service.logger.SysLoginLogService;
import com.faasadmin.faas.business.core.module.system.service.user.SysUserBussService;
import com.faasadmin.faas.business.infra.service.config.InfConfigBussService;
import com.faasadmin.faas.services.system.dal.dataobject.user.SysUserDO;
import com.faasadmin.faas.services.system.enums.business.logger.SysLoginResultEnum;
import com.faasadmin.framework.common.enums.LoginTypeEnum;
import com.faasadmin.framework.common.exception.user.CustomizeAuthenticationException;
import com.faasadmin.framework.common.utils.JsonUtils;
import com.faasadmin.framework.common.utils.ServletUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.Assert;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.faasadmin.framework.common.constant.SysErrorCodeConstants.*;
import static com.faasadmin.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 用户名密码验证码登陆
 * 启动登录认证流程过滤器
 * 覆写认证方法，修改用户名、密码的获取方式
 * 覆写认证成功后的操作，移除后台跳转，添加生成令牌并返回给客户端
 **/
@Slf4j
public class JwtLoginFilter extends UsernamePasswordAuthenticationFilter {

    public JwtLoginFilter(AuthenticationManager authManager,
                          AuthenticationSuccessHandler successHandler,
                          AuthenticationFailureHandler failureHandler,
                          ApplicationEventPublisher eventPublisher) {
        setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/api/auth/login", "POST"));
        setAuthenticationManager(authManager);
        setAuthenticationSuccessHandler(successHandler);
        setAuthenticationFailureHandler(failureHandler);
        setApplicationEventPublisher(eventPublisher);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        // POST 请求 /login 登录时拦截， 由此方法触发执行登录认证流程，可以在此覆写整个登录认证逻辑
        super.doFilter(req, res, chain);
    }

    /**
     * 此过滤器的用户名密码默认从request.getParameter()获取，但是这种
     * 读取方式不能读取到如 application/json 等 post 请求数据，需要把
     * 用户名密码的读取逻辑修改为到流中读取request.getInputStream()
     * 在此做验证码的验证
     *
     * @param request
     * @param response
     * @return
     * @throws AuthenticationException
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        //获取请求实体
        String body = ServletUtils.getBody(request);
        JSONObject jsonObject = JSON.parseObject(body);
        String uuid = jsonObject.getString("uuid");
        String code = jsonObject.getString("code");
        //账户和密码
        String username = jsonObject.getString("userName").trim();
        String password = jsonObject.getString("password").trim();
        InfConfigBussService sysConfigService = SpringUtils.getBean(InfConfigBussService.class);
        RedisCache redisCache = SpringUtils.getBean(RedisCache.class);
        SysUserBussService sysUserService = SpringUtils.getBean(SysUserBussService.class);
        SysCaptchaService captchaService = SpringUtils.getBean(SysCaptchaService.class);
        SysLoginLogService loginLogService = SpringUtils.getBean(SysLoginLogService.class);
        String captchaSwitch = sysConfigService.selectConfigByKey("sys.account.captchaOnOff");
        boolean captchaOnOff;
        if (StringUtils.isEmpty(captchaSwitch)) {
            captchaOnOff = true;
        } else {
            captchaOnOff = Convert.toBool(captchaSwitch);
        }
        // 验证码开关
        if (captchaOnOff) {
            String captcha = captchaService.getCaptchaCode(uuid);
            redisCache.delete(captcha);
            if (captcha == null) {
                log.error("验证码不存在或已过期");
                // 创建登陆失败日志（验证码不存在）
                loginLogService.createLoginLog(username, SysLoginResultEnum.CAPTCHA_NOT_FOUND);
                throw new CustomizeAuthenticationException(AUTH_LOGIN_CAPTCHA_NOT_FOUND.getMsg());
            }
            if (!code.equalsIgnoreCase(captcha)) {
                // 创建登陆失败日志（验证码不正确)
                loginLogService.createLoginLog(username, SysLoginResultEnum.CAPTCHA_CODE_ERROR);
                throw new CustomizeAuthenticationException(AUTH_LOGIN_CAPTCHA_CODE_ERROR.getMsg());
            }
            // 正确，所以要删除下验证码
            captchaService.deleteCaptchaCode(uuid);
        }
        SysAuthBussService sysAuthService = SpringUtils.getBean(SysAuthBussService.class);
        SysUserDO sysUser = sysUserService.getUserByUserName(username);
        if(ObjectUtil.isEmpty(sysUser)){
            throw new CustomizeAuthenticationException("该账号，暂未注册!");
        }
        if(!sysUserService.isAdmin(sysUser.getId())){
            //判断是否过期
            sysAuthService.checkUserExpired(sysUser);
        }
        LoginTypeDataDTO loginTypeDataDTO = new LoginTypeDataDTO();
        loginTypeDataDTO.setType(LoginTypeEnum.ADMIN_ACCOUNT_PASSWORD.getType());
        loginTypeDataDTO.setNickName(username);
        loginTypeDataDTO.setPassword(password);
        JwtAuthenticatioToken authRequest = new JwtAuthenticatioToken(JsonUtils.toJsonString(loginTypeDataDTO), password);
        // Allow subclasses to set the "details" property
        setDetails(request, authRequest);
        Authentication authenticate;
        try {
            // 调用 Spring Security 的 AuthenticationManager#authenticate(...) 方法，使用账号密码进行认证
            // 在其内部，会调用到 loadUserByUsername 方法，获取 User 信息
            authenticate = this.getAuthenticationManager().authenticate(authRequest);
        } catch (BadCredentialsException badCredentialsException) {
            loginLogService.createLoginLog(username, SysLoginResultEnum.BAD_CREDENTIALS);
            throw exception(AUTH_LOGIN_BAD_CREDENTIALS);
        } catch (DisabledException disabledException) {
            loginLogService.createLoginLog(username, SysLoginResultEnum.USER_DISABLED);
            throw exception(AUTH_LOGIN_USER_DISABLED);
        } catch (AuthenticationException authenticationException) {
            log.error("[login0][username({}) 发生未知异常]", username, authenticationException);
            loginLogService.createLoginLog(username, SysLoginResultEnum.UNKNOWN_ERROR);
            throw exception(AUTH_LOGIN_FAIL_UNKNOWN);
        }
        // 登陆成功
        Assert.notNull(authenticate.getPrincipal(), "Principal 不会为空");
        loginLogService.createLoginLog(username, SysLoginResultEnum.SUCCESS);
        return authenticate;
    }


}
