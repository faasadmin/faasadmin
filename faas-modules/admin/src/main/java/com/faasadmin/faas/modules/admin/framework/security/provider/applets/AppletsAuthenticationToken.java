package com.faasadmin.faas.modules.admin.framework.security.provider.applets;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * @version: V1.0
 * @author: faasadmin
 * @description: 小程序登陆令牌
 * @data: 2021-10-05 7:57
 **/
@Getter
@Setter
public class AppletsAuthenticationToken extends AbstractAuthenticationToken {

    private String openId;
    private String sessionKey;
    private String rawData;
    private String signature;
    private String lesseeId;

    public AppletsAuthenticationToken(String openId, String sessionKey, String rawData, String signature,String lesseeId) {
        super(null);
        this.openId = openId;
        this.sessionKey = sessionKey;
        this.rawData = rawData;
        this.signature = signature;
        this.lesseeId = lesseeId;
    }

    public AppletsAuthenticationToken(String openId, String sessionKey, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.openId = openId;
        this.sessionKey = sessionKey;
        super.setAuthenticated(true);
    }

    public AppletsAuthenticationToken(String openId, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.openId = openId;
        super.setAuthenticated(true);
    }

    public Object getCredentials() {
        return this.openId;
    }

    public Object getPrincipal() {
        return this.sessionKey;
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
