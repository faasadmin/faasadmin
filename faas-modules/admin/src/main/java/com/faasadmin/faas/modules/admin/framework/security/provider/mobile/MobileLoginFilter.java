package com.faasadmin.faas.modules.admin.framework.security.provider.mobile;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.faasadmin.faas.business.base.spirng.SpringUtils;
import com.faasadmin.faas.services.lessee.dal.dataobject.lessee.SaasLesseeDO;
import com.faasadmin.faas.services.lessee.service.lessee.SaasLesseeService;
import com.faasadmin.faas.services.member.dal.dataobject.member.SupMemberInfoDO;
import com.faasadmin.faas.services.member.service.member.SupMemberInfoService;
import com.faasadmin.framework.common.enums.CommonStatusEnum;
import com.faasadmin.framework.common.exception.user.CustomizeAuthenticationException;
import com.faasadmin.framework.common.utils.DateUtils;
import com.faasadmin.framework.common.utils.ObjectUtils;
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

import static com.faasadmin.faas.services.lessee.enums.SaasErrorCodeConstants.*;
import static com.faasadmin.framework.common.constant.SysErrorCodeConstants.AUTH_LOGIN_USER_MOBILE_NULL;
import static com.faasadmin.framework.common.constant.SysErrorCodeConstants.AUTH_LOGIN_USER_PASSWORD_NULL;

/**
 * 手机号密码登陆
 **/
@Slf4j
public class MobileLoginFilter extends AbstractAuthenticationProcessingFilter {

    public static final String SPRING_SECURITY_MOBILE_KEY = "mobile";
    public static final String SPRING_SECURITY_PASSWORD_KEY = "password";
    private final String mobileParameter = SPRING_SECURITY_MOBILE_KEY;
    private final String passwordParameter = SPRING_SECURITY_PASSWORD_KEY;
    private final boolean postOnly = true;

    public MobileLoginFilter(AuthenticationManager authManager,
                             AuthenticationSuccessHandler successHandler,
                             AuthenticationFailureHandler failureHandler,
                             ApplicationEventPublisher eventPublisher) {
        super(new AntPathRequestMatcher("/api/mobile/login", "POST"));
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
            String password = jsonObject.getString(passwordParameter);
            if (StringUtils.isEmpty(mobile)) {
                log.error("手机号不能为空");
                throw new CustomizeAuthenticationException(AUTH_LOGIN_USER_MOBILE_NULL.getMsg());
            }
            if (StringUtils.isEmpty(password)) {
                log.error("密码不能为空");
                throw new CustomizeAuthenticationException(AUTH_LOGIN_USER_PASSWORD_NULL.getMsg());
            }
            SupMemberInfoService supMemberInfoService = SpringUtils.getBean(SupMemberInfoService.class);
            SupMemberInfoDO supMemberInfoDO = supMemberInfoService.getMemberByMobile(mobile);
            if (ObjectUtil.isEmpty(supMemberInfoDO)) {
                throw new CustomizeAuthenticationException("该手机号，暂未注册!");
            }
            Long lesseeId = supMemberInfoDO.getLesseeId();
            checkLessee(lesseeId);
            MobileAuthenticationToken authRequest = new MobileAuthenticationToken(mobile, password);
            setDetails(request, authRequest);
            return this.getAuthenticationManager().authenticate(authRequest);
        }
    }

    /**
     * 校验租户
     *
     * @param lesseeId 租户ID
     * @return
     */
    public SaasLesseeDO checkLessee(Long lesseeId) {
        SaasLesseeService saasLesseeService = SpringUtils.getBean(SaasLesseeService.class);
        //判断是否过期
        SaasLesseeDO lessee = saasLesseeService.getLessee(lesseeId);
        if (ObjectUtil.isEmpty(lessee)) {
            log.error("该租户不存在,请联系客服!");
            throw new CustomizeAuthenticationException(LESSEE_NOT_EXISTS.getMsg());
        }
        if (DateUtils.compare(DateUtils.date(), lessee.getEndTime()) > 0) {
            log.error("该租户已过期,请联系客服!");
            throw new CustomizeAuthenticationException(LESSEE_EXPIRED.getMsg());
        }
        String status = lessee.getStatus();
        if (ObjectUtils.equals(CommonStatusEnum.DISABLE.getStatus(), status)) {
            log.error("该租户被停用,请联系客服!");
            throw new CustomizeAuthenticationException(LESSEE_DISABLE.getMsg());
        }
        return lessee;
    }

    protected void setDetails(HttpServletRequest request, MobileAuthenticationToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }

}
