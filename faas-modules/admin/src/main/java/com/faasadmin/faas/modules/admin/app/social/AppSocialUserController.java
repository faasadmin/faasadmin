package com.faasadmin.faas.modules.admin.app.social;

import com.faasadmin.faas.business.core.module.system.service.social.SysSocialBussService;
import com.faasadmin.faas.business.member.convert.MemberBusinessConvert;
import com.faasadmin.faas.business.member.vo.social.MemberSocialUserBindReqVO;
import com.faasadmin.faas.business.member.vo.social.MemberSocialUserUnbindReqVO;
import com.faasadmin.framework.common.enums.UserTypeEnum;
import com.faasadmin.framework.common.pojo.CommonResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static com.faasadmin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;


@Api(tags = "用户 App - 社交用户")
@RestController
@RequestMapping("/system/social-user")
@Validated
public class AppSocialUserController {

    @Resource
    private SysSocialBussService sysSocialBussService;

    @PostMapping("/bind")
    @ApiOperation("社交绑定，使用 code 授权码")
    public CommonResult<Boolean> socialBind(@RequestBody @Valid MemberSocialUserBindReqVO reqVO) {
        sysSocialBussService.bindSocialUser(MemberBusinessConvert.INSTANCE.convert(getLoginUserId(), UserTypeEnum.MEMBER.getValue(), reqVO));
        return CommonResult.success(true);
    }

    @DeleteMapping("/unbind")
    @ApiOperation("取消社交绑定")
    public CommonResult<Boolean> socialUnbind(@RequestBody MemberSocialUserUnbindReqVO reqVO) {
        sysSocialBussService.unbindSocialUser(MemberBusinessConvert.INSTANCE.convert(getLoginUserId(), UserTypeEnum.MEMBER.getValue(), reqVO));
        return CommonResult.success(true);
    }

}
