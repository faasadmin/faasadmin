package com.faasadmin.faas.modules.admin.common.controller;

import cn.hutool.core.lang.Validator;
import com.faasadmin.faas.business.core.module.system.service.sms.SysSmsService;
import com.faasadmin.faas.services.system.service.user.SysUserService;
import com.faasadmin.framework.common.constant.Constants;
import com.faasadmin.framework.common.pojo.CommonResult;
import com.faasadmin.framework.common.utils.ToolUtils;
import com.faasadmin.framework.common.uuid.IdUtils;
import com.faasadmin.framework.security.core.LoginCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.faasadmin.faas.business.base.cache.RedisCache;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

import static com.faasadmin.framework.common.constant.SysErrorCodeConstants.*;
import static com.faasadmin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.faasadmin.framework.common.pojo.CommonResult.success;

@Slf4j
@RestController
@RequestMapping("/system/sms")
public class SysSmsCodeController {

    @Resource
    private RedisCache redisCache;

    @Resource
    private SysUserService sysUserService;

    @Resource
    private SysSmsService sysSmsService;

    /**
     * 登陆短信验证码
     *
     * @param mobile 手机号
     * @return
     */
    @PostMapping("login/smsCode")
    public CommonResult loginSmsCode(@RequestBody LoginCode mobile) {
        String code = IdUtils.smsCode();
        log.info("登陆短信验证码：{}", code);
        //校验手机号是否正确
        if(!Validator.isMobile(mobile.getMobile())){
            throw exception(AUTH_LOGIN_USER_MOBILE_ERROR);
        }
        //判断手机号是否注册过
        if (!sysUserService.checkMobileExist(mobile.getMobile())) {
            throw exception(AUTH_LOGIN_USER_MOBILE_NOT_REGISTER);
        }
        String smsCodeMsgKey = Constants.SMS_CODE_LOGIN_KEY + mobile.getMobile();
        //是否在一分钟之内
        String smsCodeExpiredKey = Constants.SMS_CODE_LOGIN_EXPIRED_KEY + mobile.getMobile();
        String expire = redisCache.get(smsCodeExpiredKey);
        if (ToolUtils.isNotEmpty(expire)) {
            throw exception(SMS_TEMPLATE_SEND_INTERVAL);
        }
        sysSmsService.sendLoginVerificationCode(mobile.getMobile(), code);
        redisCache.set(smsCodeMsgKey, code, Constants.CAPTCHA_EXPIRATION, TimeUnit.MINUTES);
        redisCache.set(smsCodeExpiredKey, code, Constants.CODE_EXPIRATION, TimeUnit.MINUTES);
        return success(true);
    }

    /**
     * 忘记密码短信验证码
     *
     * @param mobile 手机号
     * @return
     */
    @PostMapping("forget/smsCode")
    public CommonResult<Boolean> forgetSmsCode(@RequestBody LoginCode mobile) {
        String code = IdUtils.smsCode();
        log.info("忘记密码短信验证码：{}", code);
        //校验手机号是否正确
        if(!Validator.isMobile(mobile.getMobile())){
            throw exception(AUTH_LOGIN_USER_MOBILE_ERROR);
        }
        //判断手机号是否注册过
        if (!sysUserService.checkMobileExist(mobile.getMobile())) {
            throw exception(AUTH_LOGIN_USER_MOBILE_NOT_REGISTER);
        }
        String smsCodeMsgKey = Constants.SMS_CODE_FORGET_KEY + mobile.getMobile();
        //是否在一分钟之内
        String smsCodeExpiredKey = Constants.SMS_CODE_FORGET_EXPIRED_KEY + mobile.getMobile();
        String expire = redisCache.get(smsCodeExpiredKey);
        if (ToolUtils.isNotEmpty(expire)) {
            throw exception(SMS_TEMPLATE_SEND_INTERVAL);
        }
        sysSmsService.sendLoginVerificationCode(mobile.getMobile(), code);
        redisCache.set(smsCodeMsgKey, code, Constants.CAPTCHA_EXPIRATION, TimeUnit.MINUTES);
        redisCache.set(smsCodeExpiredKey, code, Constants.CODE_EXPIRATION, TimeUnit.MINUTES);
        return success(true);
    }

    /**
     * 换绑手机号验证码
     *
     * @param mobile 手机号
     * @return
     */
    @PostMapping("changetie/smsCode")
    public CommonResult<Boolean> changeTieSmsCode(@RequestBody LoginCode mobile) {
        String code = IdUtils.smsCode();
        log.info("换绑手机号验证码：{}", code);
        //校验手机号是否正确
        if(!Validator.isMobile(mobile.getMobile())){
            throw exception(AUTH_LOGIN_USER_MOBILE_ERROR);
        }
        //判断手机号是否注册过
        if (!sysUserService.checkMobileExist(mobile.getMobile())) {
            throw exception(AUTH_LOGIN_USER_MOBILE_NOT_REGISTER);
        }
        String smsCodeMsgKey = Constants.SMS_CODE_CHANGETIE_KEY + mobile.getMobile();
        //是否在一分钟之内
        String smsCodeExpiredKey = Constants.SMS_CODE_CHANGETIE_EXPIRED_KEY + mobile.getMobile();
        String expire = redisCache.get(smsCodeExpiredKey);
        if (ToolUtils.isNotEmpty(expire)) {
            throw exception(SMS_TEMPLATE_SEND_INTERVAL);
        }
        sysSmsService.sendLoginVerificationCode(mobile.getMobile(), code);
        redisCache.set(smsCodeMsgKey, code, Constants.CAPTCHA_EXPIRATION, TimeUnit.MINUTES);
        redisCache.set(smsCodeExpiredKey, code, Constants.CODE_EXPIRATION, TimeUnit.MINUTES);
        return success(true);
    }


    /**
     * 注册用户
     *
     * @param mobile 手机号
     * @return
     */
    @PostMapping("register/smsCode")
    public CommonResult registerSmsCode(@RequestBody LoginCode mobile) {
        if(!Validator.isMobile(mobile.getMobile())){
            throw exception(AUTH_LOGIN_USER_MOBILE_ERROR);
        }
        //判断手机号是否注册过
        sysUserService.checkMobileUnique(null,mobile.getMobile());
        String code = IdUtils.smsCode();
        log.info("注册账户验证码：{}", code);
        String smsCodeMsgKey = Constants.SMS_CODE_REGISTER_KEY + mobile.getMobile();
        //是否在一分钟之内
        String smsCodeExpiredKey = Constants.SMS_CODE_REGISTER_EXPIRED + mobile.getMobile();
        String expire = redisCache.get(smsCodeExpiredKey);
        if (ToolUtils.isNotEmpty(expire)) {
            throw exception(SMS_TEMPLATE_SEND_INTERVAL);
        }
        sysSmsService.sendLoginVerificationCode(mobile.getMobile(), code);
        redisCache.set(smsCodeMsgKey, code, Constants.CAPTCHA_EXPIRATION, TimeUnit.MINUTES);
        redisCache.set(smsCodeExpiredKey, code, Constants.CODE_EXPIRATION, TimeUnit.MINUTES);
        return success(true);
    }

}
