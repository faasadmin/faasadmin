package com.faasadmin.faas.modules.admin.framework.security.config;

import com.faasadmin.faas.modules.admin.framework.security.provider.applets.AppletsAuthenticationProvider;
import com.faasadmin.faas.modules.admin.framework.security.provider.applets.AppletsLoginFilter;
import com.faasadmin.faas.modules.admin.framework.security.provider.code.AppletsCodeAuthenticationProvider;
import com.faasadmin.faas.modules.admin.framework.security.provider.code.AppletsCodeLoginFilter;
import com.faasadmin.faas.modules.admin.framework.security.provider.jwt.JwtAuthenticationProvider;
import com.faasadmin.faas.modules.admin.framework.security.provider.mobile.MobileAuthenticationProvider;
import com.faasadmin.faas.modules.admin.framework.security.provider.mobile.MobileLoginFilter;
import com.faasadmin.faas.modules.admin.framework.security.provider.sms.SmsCodeAuthenticationProvider;
import com.faasadmin.faas.modules.admin.framework.security.provider.sms.SmsCodeLoginFilter;
import com.faasadmin.faas.modules.admin.framework.security.handler.CustomAuthenticationFailureHandler;
import com.faasadmin.faas.modules.admin.framework.security.handler.CustomAuthenticationSuccessHandler;
import com.faasadmin.faas.modules.admin.framework.security.provider.jwt.JwtLoginFilter;
import com.faasadmin.framework.security.core.configurer.ServiceWebSecurityConfigurer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

/**
 * @version: V1.0
 * @author: faasadmin
 * @description: 自定义
 * @data: 2022-01-19 11:37
 **/
@Service
public class CustomWebSecurityConfigurer implements ServiceWebSecurityConfigurer {

    /**
     * 自定义用户【认证】逻辑
     */
    @Resource
    private UserDetailsService userDetailsService;

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    @Resource
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Resource
    private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    @Override
    public void configure(HttpSecurity httpSecurity, AuthenticationManager authenticationManager) throws Exception {
        // 开启短信登录认证过滤器
        httpSecurity.addFilterBefore(new SmsCodeLoginFilter(authenticationManager, customAuthenticationSuccessHandler, customAuthenticationFailureHandler, applicationEventPublisher), UsernamePasswordAuthenticationFilter.class);
        // 开启账号登录认证流程过滤器
        httpSecurity.addFilterBefore(new JwtLoginFilter(authenticationManager, customAuthenticationSuccessHandler, customAuthenticationFailureHandler, applicationEventPublisher), UsernamePasswordAuthenticationFilter.class);
        // 开启手机号登陆认证流程过滤器
        httpSecurity.addFilterBefore(new MobileLoginFilter(authenticationManager, customAuthenticationSuccessHandler, customAuthenticationFailureHandler, applicationEventPublisher), UsernamePasswordAuthenticationFilter.class);
        // 开启微信小程序授权认证流程过滤器
        httpSecurity.addFilterBefore(new AppletsLoginFilter(restTemplate, authenticationManager, customAuthenticationSuccessHandler, customAuthenticationFailureHandler, applicationEventPublisher), UsernamePasswordAuthenticationFilter.class);
        // 开启微信小程序code授权认证流程过滤器
        httpSecurity.addFilterBefore(new AppletsCodeLoginFilter(authenticationManager, customAuthenticationSuccessHandler, customAuthenticationFailureHandler, applicationEventPublisher), UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 自定义 账号登录身份认证组件
        auth.authenticationProvider(new JwtAuthenticationProvider(userDetailsService));
        // 自定义 短信登录身get-permission-info份认证组件
        auth.authenticationProvider(new SmsCodeAuthenticationProvider());
        // 自定义 手机号登录身份认证组件
        auth.authenticationProvider(new MobileAuthenticationProvider());
        // 自定义 微信小程序登录身份认证组件
        auth.authenticationProvider(new AppletsAuthenticationProvider());
        // 自定义 微信小程序登录身份认证组件
        auth.authenticationProvider(new AppletsCodeAuthenticationProvider());
    }

}
