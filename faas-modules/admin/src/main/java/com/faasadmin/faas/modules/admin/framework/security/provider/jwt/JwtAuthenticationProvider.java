package com.faasadmin.faas.modules.admin.framework.security.provider.jwt;

import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @ProjectName: youliao
 * @Package: com.seahorse.youliao.util
 * @ClassName: JwtAuthenticationProvider
 * @Description: 身份验证提供者
 * 登录身份认证组件
 *
 * SecurityUtils.login登录认证是通过调用 AuthenticationManager 的 authenticate(token) 方法实现的，
 * 而 AuthenticationManager 又是通过调用 AuthenticationProvider 的 authenticate(Authentication authentication)
 * 来完成认证的，所以通过定制 AuthenticationProvider 也可以完成各种自定义的需求，我们这里只是简单的继承
 * DaoAuthenticationProvider 展示如何自定义，具体的大家可以根据各自的需求按需定制。
 * @author:songqiang
 * @Date:2020-01-10 10:02
 **/
public class JwtAuthenticationProvider extends DaoAuthenticationProvider {

    public JwtAuthenticationProvider(UserDetailsService userDetailsService) {
        setHideUserNotFoundExceptions(false);
        setUserDetailsService(userDetailsService);
        setPasswordEncoder(new BCryptPasswordEncoder());
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // 登录认证逻辑 调用父类逻辑 可以根据业务扩展
        return super.authenticate(authentication);
    }

    @Override
    public boolean supports(Class<?> aClass) {
        //如果是JwtAuthenticatioToken该类型，则在该处理器做登录校验
        return JwtAuthenticatioToken.class.isAssignableFrom(aClass);
    }

}
