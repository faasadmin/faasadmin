package com.faasadmin.faas.modules.admin.framework.security.provider.mobile;


import com.faasadmin.faas.business.base.spirng.SpringUtils;
import com.faasadmin.faas.business.core.module.system.convert.auth.SysAuthConvert;
import com.faasadmin.faas.services.member.dal.dataobject.member.SupMemberInfoDO;
import com.faasadmin.faas.services.member.service.member.SupMemberInfoService;
import com.faasadmin.framework.common.enums.UserStatus;
import com.faasadmin.framework.common.utils.ToolUtils;
import com.faasadmin.framework.security.core.util.SecurityFrameworkUtils;
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
 * @description: 手机号密码登录身份认证组件
 **/
@Slf4j
public class MobileAuthenticationProvider implements AuthenticationProvider {

    private final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String mobile = (String) authentication.getPrincipal();
        String password = (String) authentication.getCredentials();
        //根据手机号加载用户
        UserDetails user = loadUserByMobile(mobile, password);
        Object principalToReturn = user;
        return createSuccessAuthentication(principalToReturn, authentication, user);
    }

    @Override
    public boolean supports(Class<?> aClass) {
        //如果是MobileAuthenticationToken该类型，则在该处理器做登录校验
        return MobileAuthenticationToken.class.isAssignableFrom(aClass);
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
     * @param mobile 手机号
     * @return
     * @throws UsernameNotFoundException
     */
    public UserDetails loadUserByMobile(String mobile, String password) throws UsernameNotFoundException {
        SupMemberInfoService supMemberInfoService = SpringUtils.getBean(SupMemberInfoService.class);
        //SysLoginLogService sysLoginService = SpringUtils.getBean(SysLoginLogService.class);
        SupMemberInfoDO user = supMemberInfoService.getMemberByMobile(mobile);
        //UserDetails userDeatils = new User(mobile, user.getPassword(), AuthorityUtils.commaSeparatedStringToAuthorityList("USER"));
        //BCryptPasswordEncoder bCryptPasswordEncoder = SpringUtils.getBean(BCryptPasswordEncoder.class);
        // 自定义的加密规则，用户名、输的密码和数据库保存的盐值进行加密
        //String encodedPassword = SecurityUtils.encryptPassword(password);
        //if (!bCryptPasswordEncoder.matches(encodedPassword, userDeatils.getPassword())) {
        //    throw new BadCredentialsException("登录名或密码错误");
        //}
        //BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        //if (!passwordEncoder.matches(user.getPassword(), password)) {
        //    log.debug("登录名或密码错误");
        //    throw new BadCredentialsException("登录名或密码错误");
        //}
        if (ToolUtils.isNull(user)) {
            log.info("登录用户：{} 不存在", mobile);
            throw new BadCredentialsException("对不起，您的账号：" + mobile + " 未注册");
        }
        if (!SecurityFrameworkUtils.matchesPassword(password, user.getPassword())) {
            log.info("登录用户：{} 密码错误：{}", mobile, password);
            throw new BadCredentialsException("对不起，您的账号：" + mobile + " 密码错误");
        }
        if (UserStatus.DELETED.getCode().equals(user.getDeleted())) {
            log.info("登录用户：{} 已被删除.", mobile);
            throw new BadCredentialsException("对不起，您的账号：" + mobile + " 已被删除");
        } else if (UserStatus.DISABLE.getCode().equals(user.getStatus())) {
            log.info("登录用户：{} 已被停用.", mobile);
            throw new BadCredentialsException("对不起，您的账号：" + mobile + " 已停用");
        }
        //sysLoginService.recordLoginInfo(user);
        return createLoginMember(user);
    }

    public UserDetails createLoginMember(SupMemberInfoDO user) {
        // 创建 LoginUser 对象
        return SysAuthConvert.INSTANCE.convert(user);
    }

}
