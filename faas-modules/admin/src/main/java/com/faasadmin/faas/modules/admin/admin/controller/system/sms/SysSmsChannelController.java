package com.faasadmin.faas.modules.admin.admin.controller.system.sms;


import com.faasadmin.faas.business.core.module.system.service.sms.SysSmsChannelBussService;
import com.faasadmin.faas.services.system.convert.sms.SysSmsChannelConvert;
import com.faasadmin.faas.services.system.dal.dataobject.sms.SysSmsChannelDO;
import com.faasadmin.framework.common.pojo.CommonResult;
import com.faasadmin.framework.common.pojo.PageResult;
import com.faasadmin.faas.services.system.vo.sms.channel.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Comparator;
import java.util.List;

import static com.faasadmin.framework.common.pojo.CommonResult.success;

@Api(tags = "短信渠道")
@RestController
@RequestMapping("system/sms-channel")
public class SysSmsChannelController {

    @Resource
    private SysSmsChannelBussService sysSmsChannelBussService;

    @PostMapping("/create")
    @ApiOperation("创建短信渠道")
    @PreAuthorize("@ss.hasPermission('system:sms-channel:create')")
    public CommonResult<Long> createSmsChannel(@Valid @RequestBody SysSmsChannelCreateReqVO createReqVO) {
        return success(sysSmsChannelBussService.createSmsChannel(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新短信渠道")
    @PreAuthorize("@ss.hasPermission('system:sms-channel:update')")
    public CommonResult<Boolean> updateSmsChannel(@Valid @RequestBody SysSmsChannelUpdateReqVO updateReqVO) {
        sysSmsChannelBussService.updateSmsChannel(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除短信渠道")
    @ApiImplicitParam(name = "id", value = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('system:sms-channel:delete')")
    public CommonResult<Boolean> deleteSmsChannel(@RequestParam("id") Long id) {
        sysSmsChannelBussService.deleteSmsChannel(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得短信渠道")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('system:sms-channel:query')")
    public CommonResult<SysSmsChannelRespVO> getSmsChannel(@RequestParam("id") Long id) {
        SysSmsChannelDO smsChannel = sysSmsChannelBussService.getSmsChannel(id);
        return success(SysSmsChannelConvert.INSTANCE.convert(smsChannel));
    }

    @GetMapping("/page")
    @ApiOperation("获得短信渠道分页")
    @PreAuthorize("@ss.hasPermission('system:sms-channel:query')")
    public CommonResult<PageResult<SysSmsChannelRespVO>> getSmsChannelPage(@Valid SysSmsChannelPageReqVO pageVO) {
        PageResult<SysSmsChannelDO> pageResult = sysSmsChannelBussService.getSmsChannelPage(pageVO);
        return success(SysSmsChannelConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/list-all-simple")
    @ApiOperation(value = "获得短信渠道精简列表", notes = "包含被禁用的短信渠道")
    public CommonResult<List<SysSmsChannelSimpleRespVO>> getSimpleSmsChannels() {
        List<SysSmsChannelDO> list = sysSmsChannelBussService.getSmsChannelList();
        // 排序后，返回给前端
        list.sort(Comparator.comparing(SysSmsChannelDO::getId));
        return success(SysSmsChannelConvert.INSTANCE.convertList03(list));
    }

}
