package com.faasadmin.faas.modules.admin.admin.framework.property.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@ConfigurationProperties(prefix = "faasadmin.encrypt")
@Validated
@Data
public class PropertyEncryptProperties {

    /**
     * 配置文件yml文件中敏感数据加密key
     */
    @NotEmpty(message = "加密key不能为空")
    private String key;

}
