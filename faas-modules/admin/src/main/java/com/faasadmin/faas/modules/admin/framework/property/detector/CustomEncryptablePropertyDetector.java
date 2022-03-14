package com.faasadmin.faas.modules.admin.framework.property.detector;

import cn.hutool.core.util.StrUtil;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyDetector;

/**
 * @version: V1.0
 * @author: faasadmin
 * @date: 2022-01-16 22:34
 * @description: 分割截取配置文件加密信息
 */
public class CustomEncryptablePropertyDetector implements EncryptablePropertyDetector {

    public static final String ENCODED_PASSWORD_HINT_ENC = "ENC{";
    public static final String ENCODED_PASSWORD_HINT_DES = "DES{";
    public static final String ENCODED_PASSWORD_HINT_SM4 = "SM4{";

    //判断是否为预定的值
    @Override
    public boolean isEncrypted(String str) {
        if (null != str) {
            return StrUtil.startWith(str,ENCODED_PASSWORD_HINT_DES,true) ||
                    StrUtil.startWith(str,ENCODED_PASSWORD_HINT_SM4,true) ||
                    StrUtil.startWith(str,ENCODED_PASSWORD_HINT_ENC,true)
                    ;
        }
        return false;
    }

    //截取真正的加密串
    @Override
    public String unwrapEncryptedValue(String str) {
        if(StrUtil.startWith(str,ENCODED_PASSWORD_HINT_DES,true)){
            return StrUtil.removeSuffixIgnoreCase(StrUtil.removePrefixIgnoreCase(str,ENCODED_PASSWORD_HINT_DES),"}");
        }
        if(StrUtil.startWith(str,ENCODED_PASSWORD_HINT_SM4,true)){
            return StrUtil.removeSuffixIgnoreCase(StrUtil.removePrefixIgnoreCase(str,ENCODED_PASSWORD_HINT_SM4),"}");
        }
        if(StrUtil.startWith(str,ENCODED_PASSWORD_HINT_ENC,true)){
            return StrUtil.removeSuffixIgnoreCase(StrUtil.removePrefixIgnoreCase(str,ENCODED_PASSWORD_HINT_ENC),"}");
        }
        return str;
    }
}