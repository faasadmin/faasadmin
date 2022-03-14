package com.faasadmin.faas.modules.admin.system.oss;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.faasadmin.faas.services.lessee.service.lesseeConfig.SaasLesseeConfigService;
import com.faasadmin.framework.common.constant.ConfigConstant;
import com.faasadmin.framework.common.enums.UploadStorageTypeEnums;
import com.faasadmin.framework.common.utils.ToolUtils;
import com.faasadmin.framework.oss.cloud.*;

import static com.faasadmin.framework.common.constant.SysErrorCodeConstants.*;
import static com.faasadmin.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 文件上传Factory
 */
public final class OSSFactory {

    private static SaasLesseeConfigService saasLesseeConfigService;

    static {
        OSSFactory.saasLesseeConfigService = SpringUtil.getBean(SaasLesseeConfigService.class);
    }

    public static CloudStorageService build(Long lesseeId) {
        // 获取云存储配置信息
        CloudStorageConfig config = saasLesseeConfigService.getLesseeConfigObject(ConfigConstant.CLOUD_STORAGE_CONFIG_KEY,
                CloudStorageConfig.class, lesseeId);
        if(ObjectUtil.isEmpty(config)){
            throw exception(ERROR_OSS_NOT_EXISTS);
        }
        Integer ossType = Convert.toInt(config.getType());
        if (ObjectUtil.equal(ossType, UploadStorageTypeEnums.QINIU.getId())) {
            return new QiniuCloudStorageService(config);
        } else if (ObjectUtil.equal(ossType, UploadStorageTypeEnums.ALIYUN.getId())) {
            return new AliyunCloudStorageService(config);
        } else if (ObjectUtil.equal(ossType, UploadStorageTypeEnums.QCLOUD.getId())) {
            return new QcloudCloudStorageService(config);
        }
        return null;
    }

    /**
     * 获取系统上传信息配置
     *
     * @return
     */
    public static int getOSSType(Long lesseeId) {
        // 获取云存储配置信息
        CloudStorageConfig config = saasLesseeConfigService.getLesseeConfigObject(ConfigConstant.CLOUD_STORAGE_CONFIG_KEY,
                CloudStorageConfig.class, lesseeId);
        return config.getType();
    }

    /**
     * 获取域名前缀
     *
     * @return
     */
    public static String getOSSDomain(Long lesseeId) {
        // 获取云存储配置信息
        CloudStorageConfig config = saasLesseeConfigService.getLesseeConfigObject(ConfigConstant.CLOUD_STORAGE_CONFIG_KEY,
                CloudStorageConfig.class, lesseeId);
        if(ObjectUtil.isEmpty(config)){
            return null;
        }
        String ossType = Convert.toStr(config.getType());
        if (ObjectUtil.equal(ossType, UploadStorageTypeEnums.QINIU.getId())) {
            return config.getQiniuDomain();
        } else if (ObjectUtil.equal(ossType, UploadStorageTypeEnums.ALIYUN.getId())) {
            return ToolUtils.concat(true,"https://",config.getAliyunDomain());
        } else if (ObjectUtil.equal(ossType, UploadStorageTypeEnums.QCLOUD.getId())) {
            return config.getQcloudDomain();
        }
        return null;
    }

    /**
     * 获取域名前缀
     *
     * @return
     */
    public static String getOSSDomain(Long lesseeId, String ossType) {
        // 获取云存储配置信息
        CloudStorageConfig config = saasLesseeConfigService.getLesseeConfigObject(ConfigConstant.CLOUD_STORAGE_CONFIG_KEY,
                CloudStorageConfig.class, lesseeId);
        if (ObjectUtil.equal(ossType, UploadStorageTypeEnums.QINIU.getId())) {
            return config.getQiniuDomain();
        } else if (ObjectUtil.equal(ossType, UploadStorageTypeEnums.ALIYUN.getId())) {
            return config.getAliyunDomain();
        } else if (ObjectUtil.equal(ossType, UploadStorageTypeEnums.QCLOUD.getId())) {
            return config.getQcloudDomain();
        }
        return null;
    }

}
