package com.faasadmin.faas.modules.admin.framework.security.handler;

import com.faasadmin.framework.common.exception.enums.GlobalErrorCodeConstants;
import com.faasadmin.framework.common.pojo.CommonResult;
import com.faasadmin.framework.common.utils.ServletUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 自定义认证失败处理器
 **/
@Slf4j
@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    /**
     * Called when an authentication attempt fails.
     *
     * @param request   the request during which the authentication attempt occurred.
     * @param response  the response.
     * @param exception the exception which was thrown to reject the authentication
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) {
        log.debug("[commence][访问 URL({}) 时，认证失败]", request.getRequestURI(), exception);
        //ServletUtils.writeJSON(response, CommonResult.error(GlobalErrorCodeConstants.AUTHENTICATION_FAILED));
        ServletUtils.writeJSON(response, CommonResult.error(409,exception.getMessage()));
        //ServletUtils.writeJSON(response, CommonResult.error(GlobalErrorCodeConstants.AUTHENTICATION_FAILED));
    }

}
