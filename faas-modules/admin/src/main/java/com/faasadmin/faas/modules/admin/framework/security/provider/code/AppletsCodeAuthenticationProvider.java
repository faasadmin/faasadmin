package com.faasadmin.faas.modules.admin.framework.security.provider.code;

import com.faasadmin.faas.business.base.spirng.SpringUtils;
import com.faasadmin.faas.business.core.module.system.convert.auth.SysAuthConvert;
import com.faasadmin.faas.services.weixin.dal.dataobject.user.WxUserDO;
import com.faasadmin.faas.services.weixin.service.user.WxUserService;
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
 * @version: V1.0
 * @author: faasadmin
 * @description: 小程序身份认证组件
 * @data: 2021-10-05 8:18
 **/
@Slf4j
public class AppletsCodeAuthenticationProvider implements AuthenticationProvider {

    private final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    /**
     * 获取用户信息
     *
     * @param wxUserInfoDTO 微信用户数据
     * @return
     * @throws UsernameNotFoundException
     */
    public UserDetails loadUserByOpenId(AppletsCodeAuthenticationToken wxUserInfoDTO) throws UsernameNotFoundException {
        String openId = wxUserInfoDTO.getOpenId();
        WxUserService wxUserService = SpringUtils.getBean(WxUserService.class);
        WxUserDO wxUserDO = wxUserService.getByOpenId(openId);
        if (ToolUtils.isNull(wxUserDO)) {
            log.info("登录用户openId：{} 不存在.新注册一个", openId);
            throw new BadCredentialsException("对不起，您的账号：" + openId + " 不存在");
        } else if (UserStatus.DELETED.getCode().equals(wxUserDO.getDeleted())) {
            log.info("登录用户：{} 已被删除.", openId);
            throw new BadCredentialsException("对不起，您的账号：" + openId + " 已被删除");
        } else if (UserStatus.DISABLE.getCode().equals(wxUserDO.getStatus())) {
            log.info("登录用户：{} 已被停用.", openId);
            throw new BadCredentialsException("对不起，您的账号：" + openId + " 已停用");
        }
        return createLoginUser(wxUserDO);
    }

    public UserDetails createLoginUser(WxUserDO user) {
        // 创建 LoginUser 对象
        return SysAuthConvert.INSTANCE.convert(user);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        AppletsCodeAuthenticationToken appletsCodeAuthenticationToken = (AppletsCodeAuthenticationToken) authentication;
        //根据openId加载用户
        UserDetails user = loadUserByOpenId(appletsCodeAuthenticationToken);
        Object principalToReturn = user;
        return createSuccessAuthentication(principalToReturn, authentication, user);
    }

    @Override
    public boolean supports(Class<?> aClass) {
        //如果是AppletsAuthenticationToken该类型，则在该处理器做登录校验
        return AppletsCodeAuthenticationToken.class.isAssignableFrom(aClass);
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

}
