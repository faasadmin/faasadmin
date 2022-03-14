package com.faasadmin.faas.modules.admin.framework.property.resolver;

import cn.hutool.core.util.StrUtil;
import com.faasadmin.faas.modules.admin.framework.property.detector.CustomEncryptablePropertyDetector;
import com.faasadmin.framework.common.constant.ConfigConstant;
import com.faasadmin.framework.common.crypto.EnctryptTools;
import com.faasadmin.framework.common.utils.JasyptUtils;
import com.faasadmin.framework.common.utils.ToolUtils;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import org.springframework.beans.factory.annotation.Value;

/**
 * @version: V1.0
 * @author: faasadmin
 * @date: 2022-01-16 22:35
 * @description: yml 文件敏感信息处理类
 */
public class CustomEncryptablePropertyResolver implements EncryptablePropertyResolver {

    @Value("${faasadmin.encrypt.key: ''}")
    private String key;

    @Value("${faasadmin.productName: ''}")
    private String productName;

    @Override
    public String resolvePropertyValue(String str) {
        //System.out.println("未解密：" + str);
        if (ToolUtils.isNotEmpty(str)) {
            try {
                //对配置文件加密值进行解密。加密方式可以自定义
                if (StrUtil.startWith(str, CustomEncryptablePropertyDetector.ENCODED_PASSWORD_HINT_ENC, true)) {
                    str = StrUtil.removeSuffixIgnoreCase(StrUtil.removePrefixIgnoreCase(str,
                            CustomEncryptablePropertyDetector.ENCODED_PASSWORD_HINT_ENC), "}");
                    if (ToolUtils.isEmpty(key)) {
                        key = ConfigConstant.KEY;
                    }
                    str = JasyptUtils.decyptPwd(str, key);
                }
                if (StrUtil.startWith(str, CustomEncryptablePropertyDetector.ENCODED_PASSWORD_HINT_DES, true)) {
                    str = StrUtil.removeSuffixIgnoreCase(StrUtil.removePrefixIgnoreCase(str,
                            CustomEncryptablePropertyDetector.ENCODED_PASSWORD_HINT_DES), "}");
                    if (ToolUtils.isEmpty(key)) {
                        key = ConfigConstant.KEY;
                    }
                    str = EnctryptTools.DesDecode(str, key);
                }
                if (StrUtil.startWith(str, CustomEncryptablePropertyDetector.ENCODED_PASSWORD_HINT_SM4, true)) {
                    str = StrUtil.removeSuffixIgnoreCase(StrUtil.removePrefixIgnoreCase(str,
                            CustomEncryptablePropertyDetector.ENCODED_PASSWORD_HINT_SM4), "}");
                    if (ToolUtils.isEmpty(key)) {
                        key = ConfigConstant.KEY;
                    }
                    str = EnctryptTools.SM4Decode(str, key);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //System.out.println("解密：" + str);
        return str;
    }

}
