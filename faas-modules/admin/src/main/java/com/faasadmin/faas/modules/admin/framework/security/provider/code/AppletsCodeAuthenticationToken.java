package com.faasadmin.faas.modules.admin.framework.security.provider.code;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * @version: V1.0
 * @author: faasadmin
 * @description: 小程序code登陆令牌
 * @data: 2021-10-05 7:57
 **/
@Getter
@Setter
public class AppletsCodeAuthenticationToken extends AbstractAuthenticationToken {

    private String openId;
    private String lesseeId;

    public AppletsCodeAuthenticationToken(String openId, String lesseeId) {
        super(null);
        this.openId = openId;
        this.lesseeId = lesseeId;
    }

    public AppletsCodeAuthenticationToken(String openId, String lesseeId, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.openId = openId;
        this.lesseeId = lesseeId;
        super.setAuthenticated(true);
    }

    public AppletsCodeAuthenticationToken(String openId, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.openId = openId;
        super.setAuthenticated(true);
    }

    public Object getCredentials() {
        return this.openId;
    }

    public Object getPrincipal() {
        return this.lesseeId;
    }

    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        if (isAuthenticated) {
            throw new IllegalArgumentException(
                    "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        }

        super.setAuthenticated(false);
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
    }


}
