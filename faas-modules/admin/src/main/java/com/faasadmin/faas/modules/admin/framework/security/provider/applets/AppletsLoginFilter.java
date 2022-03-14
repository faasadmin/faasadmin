package com.faasadmin.faas.modules.admin.framework.security.provider.applets;

import cn.hutool.core.convert.Convert;
import com.faasadmin.faas.business.base.spirng.SpringUtils;
import com.faasadmin.faas.modules.admin.framework.dto.WxAppletsLoginResultDTO;
import com.faasadmin.faas.services.lessee.enums.lessee.LesseeConfigEnums;
import com.faasadmin.faas.services.lessee.service.lesseeConfig.SaasLesseeConfigService;
import com.faasadmin.framework.common.exception.user.CustomizeAuthenticationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.faasadmin.framework.common.constant.Constants.WX_AUTH_URL;
import static com.faasadmin.framework.common.constant.SysErrorCodeConstants.*;

/**
 * @version: V1.0
 * @author: faasadmin
 * @description: 小程序登陆
 * 用于用户认证的filter，但是真正的认证逻辑会委托给{@link AppletsAuthenticationProvider}
 * @data: 2021-10-05 8:01
 **/
@Slf4j
public class AppletsLoginFilter extends AbstractAuthenticationProcessingFilter {

    private final RestTemplate restTemplate;

    public AppletsLoginFilter(
            RestTemplate restTemplate,
            AuthenticationManager authManager,
            AuthenticationSuccessHandler successHandler,
            AuthenticationFailureHandler failureHandler,
            ApplicationEventPublisher eventPublisher) {
        super(new AntPathRequestMatcher("/api/applets/login", "POST"));
        setAuthenticationManager(authManager);
        setAuthenticationSuccessHandler(successHandler);
        setAuthenticationFailureHandler(failureHandler);
        setApplicationEventPublisher(eventPublisher);
        this.restTemplate = restTemplate;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse httpServletResponse) throws AuthenticationException, IOException, ServletException {
        String code = request.getParameter("code");
        if (StringUtils.isBlank(code)) {
            log.error("code is null");
            throw new CustomizeAuthenticationException(AUTHORIZE_WX_AUTH_FAILED.getMsg());
        }
        String rawData = request.getParameter("rawData");
        if (StringUtils.isBlank(rawData)) {
            log.error("rawData is null");
            throw new CustomizeAuthenticationException(AUTHORIZE_WX_AUTH_RAWDATA_NULL.getMsg());
        }
        String signature = request.getParameter("signature");
        if (StringUtils.isBlank(signature)) {
            log.error("signature is null");
            throw new CustomizeAuthenticationException(AUTHORIZE_WX_AUTH_SIGNATURE_NULL.getMsg());
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
        String appid = saasLesseeConfigService.selectLesseeConfigByKeyToStr(lid, appName, LesseeConfigEnums.WX_APP_ID.getId(), StringUtils.EMPTY);
        String appsrcret = saasLesseeConfigService.selectLesseeConfigByKeyToStr(lid, appName, LesseeConfigEnums.WX_APP_SECRET.getId(), StringUtils.EMPTY);
        if (StringUtils.isEmpty(appid)) {
            log.error("appid is null");
            throw new CustomizeAuthenticationException(AUTHORIZE_WX_AUTH_APP_ID_NULL.getMsg());
        }
        if (StringUtils.isEmpty(appsrcret)) {
            log.error("appsrcret is null");
            throw new CustomizeAuthenticationException(AUTHORIZE_WX_AUTH_APP_SRCRET_NULL.getMsg());
        }
        String url = String.format(WX_AUTH_URL, appid, appsrcret, code);
        log.debug("wx auth url: [{}]", url);
        //String s = HttpUtils.sendGet(url, null);
        WxAppletsLoginResultDTO wxLoginResult = restTemplate.getForObject(url, WxAppletsLoginResultDTO.class);
        if (wxLoginResult.getErrcode() != null && !wxLoginResult.getErrcode().equals(0)) {
            log.error("wx auth failed, errCode is [{}], errMsg is [{}]", wxLoginResult.getErrcode(), wxLoginResult.getErrmsg());
            throw new CustomizeAuthenticationException(AUTHORIZE_WX_AUTH_FAILED.getMsg());
        }
        log.info("wx login result: [{}]", wxLoginResult);
        AppletsAuthenticationToken authRequest = new AppletsAuthenticationToken(wxLoginResult.getOpenid(), wxLoginResult.getSession_key(), rawData, signature, lesseeId);
        setDetails(request, authRequest);
        return this.getAuthenticationManager().authenticate(authRequest);
        //return this.getAuthenticationManager().authenticate(new AppletsAuthenticationToken(wxLoginResult.getOpenid(), wxLoginResult.getSession_key(), rawData, signature));
    }

    protected void setDetails(HttpServletRequest request, AppletsAuthenticationToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }

}
