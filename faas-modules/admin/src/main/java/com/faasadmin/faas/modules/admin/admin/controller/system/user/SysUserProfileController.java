package com.faasadmin.faas.modules.admin.admin.controller.system.user;

import cn.hutool.core.collection.CollUtil;
import com.faasadmin.faas.business.core.module.system.service.dept.SysDeptBussService;
import com.faasadmin.faas.business.core.module.system.service.dept.SysPostBussService;
import com.faasadmin.faas.business.core.module.system.service.permission.SysPermissionBussService;
import com.faasadmin.faas.business.core.module.system.service.permission.SysRoleBussService;
import com.faasadmin.faas.business.core.module.system.service.sms.SysSmsCodeBussService;
import com.faasadmin.faas.business.core.module.system.service.user.SysUserBussService;
import com.faasadmin.faas.services.system.convert.user.SysUserConvert;
import com.faasadmin.faas.services.system.dal.dataobject.dept.SysDeptDO;
import com.faasadmin.faas.services.system.dal.dataobject.dept.SysPostDO;
import com.faasadmin.faas.services.system.dal.dataobject.permission.SysRoleDO;
import com.faasadmin.faas.services.system.dal.dataobject.user.SysUserDO;
import com.faasadmin.faas.services.system.enums.sms.SysSmsSceneEnum;
import com.faasadmin.faas.services.system.service.user.SysUserService;
import com.faasadmin.faas.services.system.vo.user.SysUserUpdateMobileReqVO;
import com.faasadmin.faas.services.system.vo.user.profile.SysUserProfileRespVO;
import com.faasadmin.faas.services.system.vo.user.profile.SysUserProfileUpdatePasswordReqVO;
import com.faasadmin.faas.services.system.vo.user.profile.SysUserProfileUpdateReqVO;
import com.faasadmin.framework.common.exception.util.ServiceExceptionUtil;
import com.faasadmin.framework.common.pojo.CommonResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static com.faasadmin.framework.common.constant.ErrorCodeConstants.FILE_IS_EMPTY;
import static com.faasadmin.framework.common.pojo.CommonResult.success;
import static com.faasadmin.framework.common.utils.ServletUtils.getClientIP;
import static com.faasadmin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * @author niudehua
 */
@Api(tags = "??????????????????")
@RestController
@RequestMapping("/system/user/profile")
@Validated
@Slf4j
public class SysUserProfileController {

    @Resource
    private SysUserService userService;
    @Resource
    private SysUserBussService sysUserBussService;
    @Resource
    private SysDeptBussService sysDeptBussService;
    @Resource
    private SysPostBussService sysPostBussService;
    @Resource
    private SysPermissionBussService sysPermissionBussService;
    @Resource
    private SysRoleBussService sysRoleBussService;
    @Resource
    private SysSmsCodeBussService sysSmsCodeBussService;

    @GetMapping("/get")
    @ApiOperation("????????????????????????")
    public CommonResult<SysUserProfileRespVO> profile() {
        // ????????????????????????
        SysUserDO user = userService.getUser(getLoginUserId());
        SysUserProfileRespVO resp = SysUserConvert.INSTANCE.convert03(user);
        // ??????????????????
        List<SysRoleDO> userRoles = sysRoleBussService.getRolesFromCache(sysPermissionBussService.listUserRoleIs(user.getId()));
        resp.setRoles(SysUserConvert.INSTANCE.convertList(userRoles));
        // ??????????????????
        if (user.getDeptId() != null) {
            SysDeptDO dept = sysDeptBussService.getDept(user.getDeptId());
            resp.setDept(SysUserConvert.INSTANCE.convert02(dept));
        }
        // ??????????????????
        if (CollUtil.isNotEmpty(user.getPostIds())) {
            List<SysPostDO> posts = sysPostBussService.getPosts(user.getPostIds());
            resp.setPosts(SysUserConvert.INSTANCE.convertList02(posts));
        }
        return success(resp);
    }

    @PutMapping("/update")
    @ApiOperation("????????????????????????")
    public CommonResult<Boolean> updateUserProfile(@Valid @RequestBody SysUserProfileUpdateReqVO reqVO) {
        userService.updateUserProfile(getLoginUserId(), reqVO);
        return success(true);
    }

    @PutMapping("/update-password")
    @ApiOperation("????????????????????????")
    public CommonResult<Boolean> updateUserProfilePassword(@Valid @RequestBody SysUserProfileUpdatePasswordReqVO reqVO) {
        userService.updateUserPassword(getLoginUserId(), reqVO);
        return success(true);
    }

    @PutMapping("/upload-avatar")
    @ApiOperation("????????????????????????")
    public CommonResult<String> updateUserAvatar(@RequestParam("avatarFile") MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw ServiceExceptionUtil.exception(FILE_IS_EMPTY);
        }
        String avatar = sysUserBussService.updateUserAvatar(getLoginUserId(), file.getInputStream());
        return success(avatar);
    }
}
