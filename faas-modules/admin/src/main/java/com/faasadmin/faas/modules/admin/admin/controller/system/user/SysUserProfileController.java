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
@Api(tags = "用户个人中心")
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
    @ApiOperation("获得登录用户信息")
    public CommonResult<SysUserProfileRespVO> profile() {
        // 获得用户基本信息
        SysUserDO user = userService.getUser(getLoginUserId());
        SysUserProfileRespVO resp = SysUserConvert.INSTANCE.convert03(user);
        // 获得用户角色
        List<SysRoleDO> userRoles = sysRoleBussService.getRolesFromCache(sysPermissionBussService.listUserRoleIs(user.getId()));
        resp.setRoles(SysUserConvert.INSTANCE.convertList(userRoles));
        // 获得部门信息
        if (user.getDeptId() != null) {
            SysDeptDO dept = sysDeptBussService.getDept(user.getDeptId());
            resp.setDept(SysUserConvert.INSTANCE.convert02(dept));
        }
        // 获得岗位信息
        if (CollUtil.isNotEmpty(user.getPostIds())) {
            List<SysPostDO> posts = sysPostBussService.getPosts(user.getPostIds());
            resp.setPosts(SysUserConvert.INSTANCE.convertList02(posts));
        }
        return success(resp);
    }

    @PutMapping("/update")
    @ApiOperation("修改用户个人信息")
    public CommonResult<Boolean> updateUserProfile(@Valid @RequestBody SysUserProfileUpdateReqVO reqVO) {
        userService.updateUserProfile(getLoginUserId(), reqVO);
        return success(true);
    }

    @PutMapping("/update-password")
    @ApiOperation("修改用户个人密码")
    public CommonResult<Boolean> updateUserProfilePassword(@Valid @RequestBody SysUserProfileUpdatePasswordReqVO reqVO) {
        userService.updateUserPassword(getLoginUserId(), reqVO);
        return success(true);
    }

    @PutMapping("/upload-avatar")
    @ApiOperation("上传用户个人头像")
    public CommonResult<String> updateUserAvatar(@RequestParam("avatarFile") MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw ServiceExceptionUtil.exception(FILE_IS_EMPTY);
        }
        String avatar = sysUserBussService.updateUserAvatar(getLoginUserId(), file.getInputStream());
        return success(avatar);
    }
}
