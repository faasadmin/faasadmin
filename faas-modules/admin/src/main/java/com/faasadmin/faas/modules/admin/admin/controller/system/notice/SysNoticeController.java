package com.faasadmin.faas.modules.admin.admin.controller.system.notice;


import com.faasadmin.faas.services.system.convert.notice.SysNoticeConvert;
import com.faasadmin.faas.services.system.service.notice.SysNoticeService;
import com.faasadmin.faas.services.system.vo.notice.SysNoticeCreateReqVO;
import com.faasadmin.faas.services.system.vo.notice.SysNoticePageReqVO;
import com.faasadmin.faas.services.system.vo.notice.SysNoticeRespVO;
import com.faasadmin.faas.services.system.vo.notice.SysNoticeUpdateReqVO;
import com.faasadmin.framework.common.pojo.CommonResult;
import com.faasadmin.framework.common.pojo.PageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static com.faasadmin.framework.common.pojo.CommonResult.success;

@Api(tags = "通知公告")
@RestController
@RequestMapping("/system/notice")
@Validated
public class SysNoticeController {

    @Resource
    private SysNoticeService noticeService;

    @PostMapping("/create")
    @ApiOperation("创建通知公告")
    @PreAuthorize("@ss.hasPermission('system:notice:create')")
    public CommonResult<Long> createNotice(@Valid @RequestBody SysNoticeCreateReqVO reqVO) {
        Long noticeId = noticeService.createNotice(reqVO);
        return success(noticeId);
    }

    @PutMapping("/update")
    @ApiOperation("修改通知公告")
    @PreAuthorize("@ss.hasPermission('system:notice:update')")
    public CommonResult<Boolean> updateNotice(@Valid @RequestBody SysNoticeUpdateReqVO reqVO) {
        noticeService.updateNotice(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除通知公告")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('system:notice:delete')")
    public CommonResult<Boolean> deleteNotice(@RequestParam("id") Long id) {
        noticeService.deleteNotice(id);
        return success(true);
    }

    @GetMapping("/page")
    @ApiOperation("获取通知公告列表")
    @PreAuthorize("@ss.hasPermission('system:notice:query')")
    public CommonResult<PageResult<SysNoticeRespVO>> pageNotices(@Validated SysNoticePageReqVO reqVO) {
        return success(SysNoticeConvert.INSTANCE.convertPage(noticeService.pageNotices(reqVO)));
    }

    @GetMapping("/get")
    @ApiOperation("获得通知公告")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('system:notice:query')")
    public CommonResult<SysNoticeRespVO> getNotice(@RequestParam("id") Long id) {
        return success(SysNoticeConvert.INSTANCE.convert(noticeService.getNotice(id)));
    }

}
