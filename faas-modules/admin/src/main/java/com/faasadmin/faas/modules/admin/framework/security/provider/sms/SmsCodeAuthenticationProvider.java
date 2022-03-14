package com.faasadmin.faas.modules.admin.framework.security.provider.sms;

import com.faasadmin.faas.business.base.spirng.SpringUtils;
import com.faasadmin.faas.business.core.module.system.convert.auth.SysAuthConvert;
import com.faasadmin.faas.services.system.convert.user.SysUserConvert;
import com.faasadmin.faas.services.system.dal.dataobject.user.SysUserDO;
import com.faasadmin.faas.services.system.service.user.SysUserService;
import com.faasadmin.faas.services.system.vo.user.SysUserCreateReqVO;
import com.faasadmin.framework.common.enums.UserStatus;
import com.faasadmin.framework.common.utils.ToolUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * @description: 短信登录身份认证组件
 **/
@Slf4j
public class SmsCodeAuthenticationProvider implements AuthenticationProvider {

    private final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String mobile = (String) authentication.getPrincipal();
        //根据手机号加载用户
        UserDetails user = loadUserByPhone(mobile);
        Object principalToReturn = user;
        return createSuccessAuthentication(principalToReturn, authentication, user);
    }

    @Override
    public boolean supports(Class<?> aClass) {
        //如果是SmsCodeAuthenticationToken该类型，则在该处理器做登录校验
        return SmsCodeAuthenticationToken.class.isAssignableFrom(aClass);
    }

    /**
     * 跟账号登录保持一致
     *
     * @param principal
     * @param authentication
     * @param user
     * @return
     */
    protected Authentication createSuccessAuthentication(Object principal,
                                                         Authentication authentication, UserDetails user) {
        // Ensure we return the original credentials the user supplied,
        // so subsequent attempts are successful even with encoded passwords.
        // Also ensure we return the original getDetails(), so that future
        // authentication events after cache expiry contain the details
        UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(
                principal, authentication.getCredentials(),
                authoritiesMapper.mapAuthorities(user.getAuthorities()));
        result.setDetails(authentication.getDetails());
        return result;
    }

    /**
     * 获取用户信息
     *
     * @param phone
     * @return
     * @throws UsernameNotFoundException
     */
    public UserDetails loadUserByPhone(String phone) throws UsernameNotFoundException {
        SysUserService sysUserService = SpringUtils.getBean(SysUserService.class);
        SysUserDO user = sysUserService.getUserByUserMobile(phone);
        if (ToolUtils.isNull(user)) {
            log.info("登录用户：{} 不存在.新注册一个", phone);
            user = new SysUserDO();
            String phoneNumber = phone.replaceAll("(\\d{3})\\d{4}(\\d{4})","$1****$2");
            user.setMobile(phone);
            user.setUserName(phoneNumber);
            user.setNickName(phoneNumber);
            SysUserCreateReqVO createReqVO = SysUserConvert.INSTANCE.convertCreate(user);
            sysUserService.createUser(createReqVO);
        } else if (UserStatus.DELETED.getCode().equals(user.getDeleted())) {
            log.info("登录用户：{} 已被删除.", phone);
            throw new BadCredentialsException("对不起，您的账号：" + phone + " 已被删除");
        } else if (UserStatus.DISABLE.getCode().equals(user.getStatus())) {
            log.info("登录用户：{} 已被停用.", phone);
            throw new BadCredentialsException("对不起，您的账号：" + phone + " 已停用");
        }
        //sysUserService.recordLoginInfo(user);
        return createLoginUser(user);
    }

    public UserDetails createLoginUser(SysUserDO user) {
        // 创建 LoginUser 对象
        return SysAuthConvert.INSTANCE.convert(user);
    }

}
