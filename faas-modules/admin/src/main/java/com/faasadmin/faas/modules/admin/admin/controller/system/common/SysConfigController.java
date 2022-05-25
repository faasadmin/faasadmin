package com.faasadmin.faas.modules.admin.admin.controller.system.common;

import cn.hutool.core.convert.Convert;
import com.google.gson.Gson;
import com.faasadmin.faas.business.base.spirng.SpringUtils;
import com.faasadmin.framework.common.constant.Constants;
import com.faasadmin.framework.common.utils.PropertiesUtils;
import com.faasadmin.framework.plugin.config.PluginProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @version: V1.0
 * @author: faasadmin
 * @description: 插件配置
 * @data: 2021-08-27 17:01
 **/
@RestController
@RequestMapping("/config.js")
public class SysConfigController {

    @Value("${spring.profiles.active}")
    public String active;

    private final String configJs;

    public SysConfigController(PluginProperties pluginProperties, Gson gson) {
        Map<String, Object> configJsConfig = pluginProperties.getConfig();
        configJsConfig.put("env", SpringUtils.getDevFullName(Convert.toStr(PropertiesUtils.getYmlValue(Constants.SPRING_PROFILES_ACTIVE))));
        this.configJs = "window.config = " + gson.toJson(configJsConfig);
    }


    @GetMapping(produces = "application/javascript;charset=UTF-8")
    public String getConfigJs(){
        return configJs;
    }

}
