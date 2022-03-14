package com.faasadmin.faas.modules.admin.framework.security.provider.sms;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.faasadmin.faas.business.base.cache.RedisCache;
import com.faasadmin.faas.business.base.spirng.SpringUtils;
import com.faasadmin.faas.business.core.module.system.service.auth.SysAuthBussService;
import com.faasadmin.faas.business.core.module.system.service.logger.SysLoginLogService;
import com.faasadmin.faas.business.core.module.system.service.user.SysUserBussService;
import com.faasadmin.faas.services.system.dal.dataobject.user.SysUserDO;
import com.faasadmin.faas.services.system.enums.business.logger.SysLoginResultEnum;
import com.faasadmin.framework.common.constant.Constants;
import com.faasadmin.framework.common.exception.user.CustomizeAuthenticationException;
import com.faasadmin.framework.common.utils.ServletUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.faasadmin.framework.common.constant.SysErrorCodeConstants.AUTH_LOGIN_CAPTCHA_CODE_ERROR;
import static com.faasadmin.framework.common.constant.SysErrorCodeConstants.AUTH_LOGIN_CAPTCHA_NOT_FOUND;

/**
 * 手机验证码登陆
 **/
@Slf4j
public class SmsCodeLoginFilter extends AbstractAuthenticationProcessingFilter {

    public static final String SPRING_SECURITY_MOBILE_KEY = "mobile";
    public static final String SPRING_SECURITY_CODE_KEY = "code";
    private final String mobileParameter = SPRING_SECURITY_MOBILE_KEY;
    private final String codeParameter = SPRING_SECURITY_CODE_KEY;
    private final boolean postOnly = true;

    public SmsCodeLoginFilter(AuthenticationManager authManager,
                              AuthenticationSuccessHandler successHandler,
                              AuthenticationFailureHandler failureHandler,
                              ApplicationEventPublisher eventPublisher) {
        super(new AntPathRequestMatcher("/api/sms/login", "POST"));
        setAuthenticationManager(authManager);
        setAuthenticationSuccessHandler(successHandler);
        setAuthenticationFailureHandler(failureHandler);
        setApplicationEventPublisher(eventPublisher);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        if (this.postOnly && !request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        } else {
            String body = ServletUtils.getBody(request);
            JSONObject jsonObject = JSON.parseObject(body);
            String mobile = jsonObject.getString(mobileParameter);
            String code = jsonObject.getString(codeParameter);
            RedisCache redisCache = SpringUtils.getBean(RedisCache.class);
            SysLoginLogService loginLogService = SpringUtils.getBean(SysLoginLogService.class);
            String smsCodeMsgKey = Constants.SMS_CODE_LOGIN_KEY + mobile;
            String captcha = redisCache.get(smsCodeMsgKey);
            if (captcha == null) {
                log.error("验证码不存在或已过期");
                // 创建登陆失败日志（验证码不存在）
                loginLogService.createLoginLog(mobile, SysLoginResultEnum.CAPTCHA_NOT_FOUND);
                throw new CustomizeAuthenticationException(AUTH_LOGIN_CAPTCHA_NOT_FOUND.getMsg());
            }
            if (StringUtils.isBlank(code) || !code.equalsIgnoreCase(captcha)) {
                // 创建登陆失败日志（验证码不正确)
                loginLogService.createLoginLog(mobile, SysLoginResultEnum.CAPTCHA_CODE_ERROR);
                throw new CustomizeAuthenticationException(AUTH_LOGIN_CAPTCHA_CODE_ERROR.getMsg());
            }
            SysAuthBussService sysAuthService = SpringUtils.getBean(SysAuthBussService.class);
            SysUserBussService sysUserService = SpringUtils.getBean(SysUserBussService.class);
            SysUserDO sysUser = sysUserService.getUserByUserMobile(mobile);
            if(ObjectUtil.isEmpty(sysUser)){
                throw new CustomizeAuthenticationException("该手机号，暂未注册!");
            }
            if(!sysUserService.isAdmin(sysUser.getId())){
                //判断是否过期
                sysAuthService.checkUserExpired(sysUser);
            }
            SmsCodeAuthenticationToken authRequest = new SmsCodeAuthenticationToken(mobile, code);
            this.setDetails(request, authRequest);
            return this.getAuthenticationManager().authenticate(authRequest);
        }
    }

    protected void setDetails(HttpServletRequest request, SmsCodeAuthenticationToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }

}
