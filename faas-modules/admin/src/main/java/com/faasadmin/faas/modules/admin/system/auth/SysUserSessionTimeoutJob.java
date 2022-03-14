package com.faasadmin.faas.modules.admin.system.auth;

import com.faasadmin.faas.business.core.module.system.service.auth.SysUserSessionBussService;
import com.faasadmin.framework.job.core.handler.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 用户 Session 超时 Job
 *
 * @author 願
 */
@Component
@Slf4j
public class SysUserSessionTimeoutJob implements JobHandler {

    @Resource
    private SysUserSessionBussService sysUserSessionService;

    @Override
    public String execute(String param) throws Exception {
        // 执行过期
        Long timeoutCount = sysUserSessionService.clearSessionTimeout();
        // 返回结果，记录每次的超时数量
        return String.format("移除在线会话数量为 %s 个", timeoutCount);
    }

}
