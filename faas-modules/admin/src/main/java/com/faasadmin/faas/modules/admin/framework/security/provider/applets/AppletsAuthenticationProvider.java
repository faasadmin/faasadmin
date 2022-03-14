package com.faasadmin.faas.modules.admin.framework.security.provider.applets;

import com.faasadmin.faas.business.base.spirng.SpringUtils;
import com.faasadmin.faas.business.core.module.system.convert.auth.SysAuthConvert;
import com.faasadmin.faas.services.weixin.convert.user.WxUserConvert;
import com.faasadmin.faas.services.weixin.dal.dataobject.user.WxUserDO;
import com.faasadmin.faas.services.weixin.domain.dto.WxUserInfoDTO;
import com.faasadmin.faas.services.weixin.domain.vo.user.WxUserCreateReqVO;
import com.faasadmin.faas.services.weixin.service.user.WxUserService;
import com.faasadmin.framework.common.enums.UserStatus;
import com.faasadmin.framework.common.utils.JsonUtils;
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
public class AppletsAuthenticationProvider implements AuthenticationProvider {

    private final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    /**
     * 获取用户信息
     *
     * @param wxUserInfoDTO 微信用户数据
     * @return
     * @throws UsernameNotFoundException
     */
    public UserDetails loadUserByOpenId(WxUserInfoDTO wxUserInfoDTO) throws UsernameNotFoundException {
        String openId = wxUserInfoDTO.getOpenId();
        WxUserService wxUserService = SpringUtils.getBean(WxUserService.class);
        WxUserDO wxUserDO = wxUserService.getByOpenId(openId);
        if (ToolUtils.isNull(wxUserDO)) {
            log.info("登录用户openId：{} 不存在.新注册一个", openId);
            wxUserDO = new WxUserDO();
            WxUserCreateReqVO createReqVO = WxUserConvert.INSTANCE.convert(wxUserInfoDTO);
            wxUserService.createUser(createReqVO);
        } else if (UserStatus.DELETED.getCode().equals(wxUserDO.getDeleted())) {
            log.info("登录用户：{} 已被删除.", openId);
            throw new BadCredentialsException("对不起，您的账号：" + openId + " 已被删除");
        } else if (UserStatus.DISABLE.getCode().equals(wxUserDO.getStatus())) {
            log.info("登录用户：{} 已被停用.", openId);
            throw new BadCredentialsException("对不起，您的账号：" + openId + " 已停用");
        }
        //sysUserService.recordLoginInfo(user);
        return createLoginUser(wxUserDO);
    }

    public UserDetails createLoginUser(WxUserDO user) {
        // 创建 LoginUser 对象
        return SysAuthConvert.INSTANCE.convert(user);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        AppletsAuthenticationToken appletsAuthenticationToken = (AppletsAuthenticationToken) authentication;
        //根据手机号加载用户
        WxUserInfoDTO wxUserInfoDTO = JsonUtils.parseObject(appletsAuthenticationToken.getRawData(), WxUserInfoDTO.class);
        wxUserInfoDTO.setOpenId(appletsAuthenticationToken.getOpenId());
        wxUserInfoDTO.setLesseeId(appletsAuthenticationToken.getLesseeId());
        UserDetails user = loadUserByOpenId(wxUserInfoDTO);
        Object principalToReturn = user;
        return createSuccessAuthentication(principalToReturn, authentication, user);
    }

    @Override
    public boolean supports(Class<?> aClass) {
        //如果是AppletsAuthenticationToken该类型，则在该处理器做登录校验
        return AppletsAuthenticationToken.class.isAssignableFrom(aClass);
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
