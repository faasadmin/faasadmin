package com.faasadmin.faas.modules.admin.common;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.HexUtil;
import com.faasadmin.faas.services.system.vo.license.SysLicenseInfoRespVO;
import com.faasadmin.framework.common.constant.ConfigConstant;
import com.faasadmin.framework.common.crypto.SM4;
import com.faasadmin.framework.common.pojo.CommonResult;
import com.faasadmin.framework.license.verify.config.LicenseProperties;
import com.faasadmin.framework.license.verify.core.LicenseVerify;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static com.faasadmin.framework.common.pojo.CommonResult.success;

/**
 * @version: V1.0
 * @author: faasadmin
 * @description: 通用Controller
 * @data: 2022-02-27 16:17
 **/
@RestController
@RequestMapping("/system/common")
public class SysCommonController {
    /**
     * 产品名称
     */
    @Value("${faasadmin.productName: faasadmin}")
    private String productName;

    @Resource
    private LicenseProperties listener;

    @GetMapping("/license-info")
    public CommonResult<SysLicenseInfoRespVO> info() {
        SysLicenseInfoRespVO sysLicenseInfoRespVO = new SysLicenseInfoRespVO();
        sysLicenseInfoRespVO.setFassSN(HexUtil.encodeHexStr(SM4.encryptData_ECB(HexUtil.decodeHex
                (ConfigConstant.FAAS_OS_SN), ConfigConstant.FAAS_KEY)));
        LicenseVerify licenseVerify = new LicenseVerify();
        int online = licenseVerify.onlineNumVerify(listener.getVerifyParam());
        sysLicenseInfoRespVO.setOnlineNum(online == -1 ? "无限制" : Convert.toStr(online));
        sysLicenseInfoRespVO.setProductName(productName);
        sysLicenseInfoRespVO.setAuthorizationTime(ConfigConstant.AUTHORIZATION_TIME);
        return success(sysLicenseInfoRespVO);
    }
}
