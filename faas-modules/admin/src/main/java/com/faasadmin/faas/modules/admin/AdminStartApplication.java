package com.faasadmin.faas.modules.admin;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import com.faasadmin.faas.business.core.support.listener.LocalEnvironmentPrepareEventListener;
import com.faasadmin.framework.common.constant.ConfigConstant;
import com.faasadmin.framework.common.constant.Constants;
import com.faasadmin.framework.common.crypto.SM4;
import com.faasadmin.framework.common.event.StartApplicationEvent;
import com.faasadmin.framework.common.utils.EventBusUtils;
import com.faasadmin.framework.common.utils.MachineUtils;
import com.faasadmin.framework.common.utils.ToolUtils;
import com.faasadmin.framework.common.utils.YamlUtils;
import com.faasadmin.framework.plugin.config.PluginProperties;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;

import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @version: V1.0
 * @author: faasadmin
 * @description: 服务端启动类
 * @data: 2022-01-10 21:42
 **/
@Slf4j
@SuppressWarnings("SpringComponentScan")
@SpringBootApplication(scanBasePackages = {"com.faasadmin.a.a.a.f","com.faasadmin.faas"})
public class AdminStartApplication {

    public static void main(String[] args) {
        try {
            start(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 打印信息
     */
    public static void printInfo() {
        PluginProperties pluginProperties = new PluginProperties();
        pluginProperties.setPluginPath(YamlUtils.getValueByKeyToStr(Constants.MSZF_PLUGIN_LUGINPATH));
        pluginProperties.setBackupPath(YamlUtils.getValueByKeyToStr(Constants.MSZF_PLUGIN_BACKUPPLUGINPATH));
        pluginProperties.setUploadTempPath(YamlUtils.getValueByKeyToStr(Constants.MSZF_PLUGIN_UPLOADTEMPPATH));
        log.info("插件所在目录：{}", pluginProperties.getPluginPath());
        log.info("插件备份目录：{}", pluginProperties.getBackupPath());
        log.info("插件临时目录：{}", pluginProperties.getUploadTempPath());
    }

    /**
     * 启动
     *
     * @param args
     * @throws Exception
     */
    public static void start(String[] args) throws Exception {
        printInfo();
        com.faasadmin.a.a.a.k.d.copyAdminHtml(AdminStartApplication.class);
        AdminStartApplication adminStartApplication = new AdminStartApplication();
        com.faasadmin.a.a.a.f.b installFinishEventListener = new com.faasadmin.a.a.a.f.b();
        EventBusUtils.me().register(adminStartApplication);
        EventBusUtils.me().register(installFinishEventListener);
        Boolean existFlag = com.faasadmin.a.a.a.k.d.existConfig();
        if (existFlag) {
            execute(args);
        } else {
            Integer serverPort = Convert.toInt(YamlUtils.getValueByKey(Constants.APPLICATION_SERVER_PORT));
            com.faasadmin.a.a.a.k.d.startInstallServer(serverPort);
        }
    }

    public static void checkConfig() {
    }

    /**
     * 运行
     */
    public static void execute(String[] args) throws UnknownHostException, FileNotFoundException {
        MachineUtils.init();
        System.setProperty("verify.machineCode", HexUtil.encodeHexStr(SM4.encryptData_ECB(HexUtil.decodeHex(ConfigConstant.FAAS_OS_SN), ConfigConstant.FAAS_KEY)));
        System.setProperty("verify.checkCode", HexUtil.encodeHexStr(SM4.encryptData_ECB(HexUtil.decodeHex
                (ConfigConstant.FAAS_OS_SN), ConfigConstant.FAAS_VERIFY_KEY)).substring(0, 6));
        System.setProperty("verify.pCIp", StrUtil.cleanBlank(StrUtil.join(StrUtil.COMMA, ConfigConstant.FAAS_IPS)));
        //初始化配置
        com.faasadmin.a.a.a.k.d.initConfig();
        ConfigurableApplicationContext application = new SpringApplicationBuilder(AdminStartApplication.class)
                .beanNameGenerator(new ProGuardBeanNameGenerator())
                .run(args);
        application.addApplicationListener(new LocalEnvironmentPrepareEventListener());
        System.out.println("-------------------------------------------------------------------\n"
                + "//             ┏┓   ┏┓					//\n"
                + "//            ┏┛┻━━━┛┻┓                  //\n"
                + "//            ┃   ☃   ┃				//\n"
                + "//            ┃ ┳┛ ┗┳ ┃                  //\n"
                + "//            ┃   ┻   ┃                  //\n"
                + "//            ┗━┓   ┏━┛                  //\n"
                + "//              ┃   ┗━━━┓				//\n"
                + "//              ┃神兽保佑┣┓            //\n"
                + "//              ┃启动成功!┏┛				//\n"
                + "//              ┗┓┓┏━┳┓┏┛				//\n"
                + "//               ┃┫┫  ┃┫┫				//\n"
                + "//               ┗┻┛  ┗┻┛				//\n"
                + "-------------------------------------------------------------------");
        Environment env = application.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = env.getProperty("server.port");
        String path = env.getProperty("server.servlet.context-path");
        if (ToolUtils.isEmpty(path)) {
            path = "";
        }
        log.info("\n----------------------------------------------------------\n\t" +
                "Application  is running! Access URLs:\n\t" +
                "本地访问网址: \t\thttp://localhost:" + port + path + "\n\t" +
                "外网访问网址: \thttp://" + ip + ":" + port + path + "\n" +
                "----------------------------------------------------------");
    }

    @Bean
    public WebServerFactoryCustomizer<ConfigurableWebServerFactory> webServerFactoryCustomizer() {
        return factory -> {
            ErrorPage error404Page = new ErrorPage(HttpStatus.NOT_FOUND, "/index.html");
            factory.addErrorPages(error404Page);
        };
    }

    @Subscribe
    public void startApplication(StartApplicationEvent installFinishEvent) {
        //启动
        try {
            execute(new String[]{});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static class ProGuardBeanNameGenerator extends AnnotationBeanNameGenerator {

        @Override
        public String buildDefaultBeanName(BeanDefinition definition) {
            return definition.getBeanClassName();
        }

    }

}
