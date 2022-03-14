package com.faasadmin.faas.modules.admin.framework.property.config;

import com.faasadmin.faas.modules.admin.framework.property.detector.CustomEncryptablePropertyDetector;
import com.faasadmin.faas.modules.admin.framework.property.resolver.CustomEncryptablePropertyResolver;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyDetector;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @version: V1.0
 * @author: faasadmin
 * @description: 属性加解密配置
 * @data: 2022-01-16 22:24
 **/
@Configuration
public class PropertyEncryptAutoConfiguration {

    @Bean(name = "encryptablePropertyDetector")
    public EncryptablePropertyDetector encryptablePropertyDetector() {
        return new CustomEncryptablePropertyDetector();
    }

    @Bean(name = "encryptablePropertyResolver")
    public EncryptablePropertyResolver encryptablePropertyResolver() {
        return new CustomEncryptablePropertyResolver();
    }
}
