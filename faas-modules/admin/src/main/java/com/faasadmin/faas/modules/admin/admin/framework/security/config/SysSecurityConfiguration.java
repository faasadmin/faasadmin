package com.faasadmin.faas.modules.admin.admin.framework.security.config;

import com.faasadmin.faas.business.core.support.security.processor.CustomizeSecurityMetadataSourceObjectPostProcessor;
import com.faasadmin.faas.business.core.support.security.processor.DbSecurityConfigAttributeLoader;
import com.faasadmin.faas.business.core.support.security.processor.GlobalSecurityExpressionHandlerCacheObjectPostProcessor;
import com.faasadmin.faas.services.system.service.authorizeUrl.SysAuthorizeUrlService;
import com.faasadmin.framework.security.config.AuthorizeRequestsCustomizer;
import com.faasadmin.framework.web.spring.config.WebProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;

import javax.annotation.Resource;

@Configuration
public class SysSecurityConfiguration {

    @Resource
    private SysAuthorizeUrlService sysAuthorizeUrlService;

    @Value("${spring.boot.admin.context-path:''}")
    private String adminSeverContextPath;

    @Bean("systemAuthorizeRequestsCustomizer")
    public AuthorizeRequestsCustomizer authorizeRequestsCustomizer() {
        return new AuthorizeRequestsCustomizer() {

            @Override
            public void customize(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry) {
                registry.withObjectPostProcessor(new CustomizeSecurityMetadataSourceObjectPostProcessor(new DbSecurityConfigAttributeLoader(sysAuthorizeUrlService)));
                registry.withObjectPostProcessor(new GlobalSecurityExpressionHandlerCacheObjectPostProcessor());
                // 登录的接口
                registry.antMatchers(buildAdminApi("/system/auth/login")).permitAll();
                registry.antMatchers(buildAdminApi("/system/auth/logout")).permitAll();
                registry.antMatchers(buildAdminApi("/system/auth/refresh-token")).permitAll();
                // 社交登陆的接口
                registry.antMatchers(buildAdminApi("/system/auth/social-auth-redirect")).permitAll();
                registry.antMatchers(buildAdminApi("/system/auth/social-quick-login")).permitAll();
                registry.antMatchers(buildAdminApi("/system/auth/social-bind-login")).permitAll();
                // 登录登录的接口
                registry.antMatchers(buildAdminApi("/system/auth/sms-login")).permitAll();
                registry.antMatchers(buildAdminApi("/system/auth/send-sms-code")).permitAll();
                // 验证码的接口
                registry.antMatchers(buildAdminApi("/system/captcha/**")).permitAll();
                // 获得租户编号的接口
                registry.antMatchers(buildAdminApi("/saas/lessee/get-id-by-name")).permitAll();
                // 短信回调 API
                registry.antMatchers(buildAdminApi("/system/sms/callback/**")).permitAll();
                // Swagger 接口文档
                registry.antMatchers("/swagger-ui.html").anonymous()
                        .antMatchers("/swagger-resources/**").anonymous()
                        .antMatchers("/webjars/**").anonymous()
                        .antMatchers("/*/api-docs").anonymous();
                // Spring Boot Actuator 的安全配置
                registry.antMatchers("/actuator").anonymous()
                        .antMatchers("/actuator/**").anonymous();
                // Druid 监控
                registry.antMatchers("/druid/**").anonymous();
                // Spring Boot Admin Server 的安全配置
                registry.antMatchers(adminSeverContextPath).anonymous()
                        .antMatchers(adminSeverContextPath + "/**").anonymous();
                registry.antMatchers(buildAdminApi("/member/auth/logout")).permitAll();
                // 文件的获取接口，可匿名访问
                registry.antMatchers(buildAdminApi("/system/file-upload/*/get/**"), buildAppApi("/system/file-upload/get/**")).permitAll();
            }

        };
    }

}
