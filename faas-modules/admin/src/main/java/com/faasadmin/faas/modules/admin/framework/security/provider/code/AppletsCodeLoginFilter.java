package com.faasadmin.faas.modules.admin.framework.security.provider.code;

import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.hutool.core.convert.Convert;
import com.faasadmin.faas.business.base.spirng.SpringUtils;
import com.faasadmin.faas.services.lessee.enums.lessee.LesseeConfigEnums;
import com.faasadmin.faas.services.lessee.service.lesseeConfig.SaasLesseeConfigService;
import com.faasadmin.faas.services.weixin.config.WxMaConfiguration;
import com.faasadmin.framework.common.exception.user.CustomizeAuthenticationException;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.faasadmin.framework.common.constant.SysErrorCodeConstants.*;

/**
 * @version: V1.0
 * @author: faasadmin
 * @description: 小程序登陆
 * 用于用户认证的filter，但是真正的认证逻辑会委托给{@link AppletsAuthenticationProvider}
 * @data: 2021-10-05 8:01
 **/
@Slf4j
public class AppletsCodeLoginFilter extends AbstractAuthenticationProcessingFilter {

    public AppletsCodeLoginFilter(
            AuthenticationManager authManager,
            AuthenticationSuccessHandler successHandler,
            AuthenticationFailureHandler failureHandler,
            ApplicationEventPublisher eventPublisher) {
        super(new AntPathRequestMatcher("/api/applets/code/login", "POST"));
        setAuthenticationManager(authManager);
        setAuthenticationSuccessHandler(successHandler);
        setAuthenticationFailureHandler(failureHandler);
        setApplicationEventPublisher(eventPublisher);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse httpServletResponse) throws AuthenticationException {
        String code = request.getParameter("code");
        if (StringUtils.isBlank(code)) {
            log.error("code is null");
            throw new CustomizeAuthenticationException(AUTHORIZE_WX_AUTH_FAILED.getMsg());
        }
        String lesseeId = request.getHeader("lesseeId");
        if (StringUtils.isBlank(lesseeId)) {
            log.error("lesseeId is null");
            throw new CustomizeAuthenticationException(COMMON_LESSEE_ID_NULL.getMsg());
        }
        String appName = request.getHeader("appName");
        if (StringUtils.isBlank(appName)) {
            log.error("appName is null");
            throw new CustomizeAuthenticationException(COMMON_APP_NAME_NULL.getMsg());
        }
        SaasLesseeConfigService saasLesseeConfigService = SpringUtils.getBean(SaasLesseeConfigService.class);
        Long lid = Convert.toLong(lesseeId);
        String appId = saasLesseeConfigService.selectLesseeConfigByKeyToStr(lid, appName, LesseeConfigEnums.WX_APP_ID.getId(), StringUtils.EMPTY);
        String appSrcret = saasLesseeConfigService.selectLesseeConfigByKeyToStr(lid, appName, LesseeConfigEnums.WX_APP_SECRET.getId(), StringUtils.EMPTY);
        if (StringUtils.isEmpty(appId)) {
            log.error("appid is null");
            throw new CustomizeAuthenticationException(AUTHORIZE_WX_AUTH_APP_ID_NULL.getMsg());
        }
        if (StringUtils.isEmpty(appSrcret)) {
            log.error("appsrcret is null");
            throw new CustomizeAuthenticationException(AUTHORIZE_WX_AUTH_APP_SRCRET_NULL.getMsg());
        }
        WxMaJscode2SessionResult jscode2session;
        try {
            jscode2session = WxMaConfiguration.getMaService(appId).jsCode2SessionInfo(code);
        } catch (WxErrorException e) {
            log.error("wx code 登录凭证校验 failed, errCode is [{}], errMsg is [{}]", e.getError());
            throw new CustomizeAuthenticationException(AUTHORIZE_WX_AUTH_FAILED.getMsg());
        }
        AppletsCodeAuthenticationToken authRequest = new AppletsCodeAuthenticationToken(jscode2session.getOpenid(), lesseeId);
        this.setDetails(request, authRequest);
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    protected void setDetails(HttpServletRequest request, AppletsCodeAuthenticationToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }

}
