package com.faasadmin.faas.modules.admin.app.member;

import com.faasadmin.faas.business.member.convert.MemberBusinessConvert;
import com.faasadmin.faas.business.member.service.MemberBussService;
import com.faasadmin.faas.business.member.vo.info.MemberInfoRespVO;
import com.faasadmin.faas.business.member.vo.info.MemberUpdateMobileReqVO;
import com.faasadmin.faas.services.member.dal.dataobject.member.SupMemberInfoDO;
import com.faasadmin.faas.services.member.service.member.SupMemberInfoService;
import com.faasadmin.framework.common.pojo.CommonResult;
import com.faasadmin.framework.security.core.annotations.PreAuthenticated;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.Valid;

import static com.faasadmin.framework.common.constant.ErrorCodeConstants.FILE_IS_EMPTY;
import static com.faasadmin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.faasadmin.framework.common.pojo.CommonResult.success;
import static com.faasadmin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Api(tags = "用户 APP - 用户个人中心")
@RestController
@RequestMapping("/member/user")
@Validated
@Slf4j
public class AppMemberController {

    @Resource
    private MemberBussService memberBussService;

    @Resource
    private SupMemberInfoService supMemberInfoService;

    @PutMapping("/update-nickname")
    @ApiOperation("修改用户昵称")
    @PreAuthenticated
    public CommonResult<Boolean> updateUserNickname(@RequestParam("nickname") String nickname) {
        supMemberInfoService.updateNickName(getLoginUserId(), nickname);
        return success(true);
    }

    @PutMapping("/update-avatar")
    @ApiOperation("修改用户头像")
    @PreAuthenticated
    public CommonResult<String> updateUserAvatar(@RequestParam("avatarFile") MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw exception(FILE_IS_EMPTY);
        }
        String avatar = memberBussService.updateAvatar(getLoginUserId(), file.getInputStream());
        return success(avatar);
    }

    @GetMapping("/get")
    @ApiOperation("获得基本信息")
    @PreAuthenticated
    public CommonResult<MemberInfoRespVO> getUserInfo() {
        SupMemberInfoDO user = supMemberInfoService.get(getLoginUserId());
        return success(MemberBusinessConvert.INSTANCE.convert(user));
    }

    @PostMapping("/update-mobile")
    @ApiOperation(value = "修改用户手机")
    @PreAuthenticated
    public CommonResult<Boolean> updateMobile(@RequestBody @Valid MemberUpdateMobileReqVO reqVO) {
        memberBussService.updateMobile(getLoginUserId(), reqVO);
        return success(true);
    }

}

