package com.faasadmin.faas.modules.admin.admin.controller.system.common;

import com.faasadmin.faas.business.core.module.saas.service.lessee.SaasLesseeBussService;
import com.faasadmin.faas.business.core.module.saas.service.module.SaasModuleBussService;
import com.faasadmin.faas.services.lessee.convert.module.SaasModuleConvert;
import com.faasadmin.faas.services.lessee.dal.dataobject.module.SaasModuleDO;
import com.faasadmin.faas.services.lessee.vo.lessee.SaasLesseeCreateReqVO;
import com.faasadmin.faas.services.lessee.vo.module.SaasModuleExportReqVO;
import com.faasadmin.faas.services.lessee.vo.module.SaasModuleRespVO;
import com.faasadmin.framework.common.pojo.CommonResult;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

import static com.faasadmin.framework.common.pojo.CommonResult.success;

/**
 * @version: V1.0
 * @author: faasadmin
 * @description: 注册
 * @data: 2021-09-13 13:23
 **/
@Api(tags = "验证码")
@RestController
@RequestMapping("/system/register")
public class SysRegisterController {

    @Resource
    private SaasModuleBussService moduleService;
    @Resource
    private SaasLesseeBussService saasExtLesseeService;
    /**
     * 应用模块列表
     *
     * @return
     */
    @PostMapping("applys")
    public CommonResult<List<SaasModuleRespVO>> applys() {
        List<SaasModuleDO> list = moduleService.getModuleList(new SaasModuleExportReqVO());
        return success(SaasModuleConvert.INSTANCE.convertList(list));
    }

    /**
     * 注册租户
     *
     * @return
     */
    @PostMapping("lessee")
    public CommonResult<Boolean> lessee(@RequestBody SaasLesseeCreateReqVO reqVO) {
        saasExtLesseeService.registerLessee(reqVO);
        return success(true);
    }

}
