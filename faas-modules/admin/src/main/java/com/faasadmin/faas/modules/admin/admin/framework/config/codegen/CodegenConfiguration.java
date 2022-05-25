package com.faasadmin.faas.modules.admin.admin.framework.config.codegen;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CodegenProperties.class)
public class CodegenConfiguration {
}
