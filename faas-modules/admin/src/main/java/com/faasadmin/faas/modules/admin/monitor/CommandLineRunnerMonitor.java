package com.faasadmin.faas.modules.admin.monitor;

import com.faasadmin.framework.sms.core.client.SmsClientFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @version: V1.0
 * @author: faasadmin
 * @description: 监听
 * @data: 2022-01-12 17:29
 **/
@Component
@Order(1)
public class CommandLineRunnerMonitor implements CommandLineRunner {

    @Resource
    private SmsClientFactory smsClientFactory;

    @Override
    public void run(String... args) throws Exception {
        System.out.println(smsClientFactory.getSmsClient(1L));
    }

}
