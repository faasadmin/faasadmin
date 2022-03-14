package com.faasadmin.faas.modules.admin.framework.dto;

import lombok.Data;

/**
 * 微信小程序登陆返回
 */
@Data
public class WxAppletsLoginResultDTO {
    private String openid;
    private String session_key;
    private String errcode;
    private String errmsg;
}
