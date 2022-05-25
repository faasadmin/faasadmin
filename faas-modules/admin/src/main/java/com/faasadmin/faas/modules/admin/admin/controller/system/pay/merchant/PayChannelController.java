package com.faasadmin.faas.modules.admin.admin.controller.system.pay.merchant;


import com.faasadmin.faas.business.core.support.controller.BaseController;
import com.faasadmin.faas.business.pay.service.channel.PayChannelCoreService;
import com.faasadmin.faas.services.pay.convert.channel.SupPayChannelConvert;
import com.faasadmin.faas.services.pay.dal.dataobject.channel.SupPayChannelDO;
import com.faasadmin.faas.services.pay.service.channel.SupPayChannelService;
import com.faasadmin.faas.services.pay.vo.channel.*;
import com.faasadmin.framework.common.pojo.CommonResult;
import com.faasadmin.framework.common.pojo.PageResult;
import com.faasadmin.framework.excel.core.util.ExcelUtils;
import com.faasadmin.framework.operatelog.core.annotations.OperateLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static com.faasadmin.framework.common.pojo.CommonResult.success;
import static com.faasadmin.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Api(tags = "管理后台 - 支付渠道")
@RestController
@RequestMapping("/pay/channel")
@Validated
public class PayChannelController extends BaseController {

    @Resource
    private SupPayChannelService channelService;

    @Resource
    private PayChannelCoreService payChannelCoreService;

    @PostMapping("/create")
    @ApiOperation("创建支付渠道 ")
    @PreAuthorize("@ss.hasPermission('pay:channel:create')")
    public CommonResult<Long> createChannel(@Valid @RequestBody SupPayChannelCreateReqVO createReqVO) {
        createReqVO.setLesseeId(getLesseeId());
        createReqVO.setAppId(0L);
        return success(payChannelCoreService.createChannel(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新支付渠道 ")
    @PreAuthorize("@ss.hasPermission('pay:channel:update')")
    public CommonResult<Boolean> updateChannel(@Valid @RequestBody SupPayChannelUpdateReqVO updateReqVO) {
        updateReqVO.setLesseeId(getLesseeId());
        updateReqVO.setAppId(0L);
        payChannelCoreService.updateChannel(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除支付渠道 ")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('pay:channel:delete')")
    public CommonResult<Boolean> deleteChannel(@RequestParam("id") Long id) {
        payChannelCoreService.deleteChannel(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得支付渠道 ")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('pay:channel:query')")
    public CommonResult<SupPayChannelRespVO> getChannel(@RequestParam("id") Long id) {
        SupPayChannelDO channel = channelService.getChannel(id);
        return success(SupPayChannelConvert.INSTANCE.convert(channel));
    }

    @GetMapping("/list")
    @ApiOperation("获得支付渠道列表")
    @ApiImplicitParam(name = "ids", value = "编号列表",
            required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('pay:channel:query')")
    public CommonResult<List<SupPayChannelRespVO>> getChannelList(@RequestParam("ids") Collection<Long> ids) {
        List<SupPayChannelDO> list = channelService.getChannelList(ids);
        return success(SupPayChannelConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得支付渠道分页")
    @PreAuthorize("@ss.hasPermission('pay:channel:query')")
    public CommonResult<PageResult<SupPayChannelRespVO>> getChannelPage(@Valid SupPayChannelPageReqVO pageVO) {
        PageResult<SupPayChannelDO> pageResult = channelService.getChannelPage(pageVO);
        return success(SupPayChannelConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出支付渠道Excel")
    @PreAuthorize("@ss.hasPermission('pay:channel:export')")
    @OperateLog(type = EXPORT)
    public void exportChannelExcel(@Valid SupPayChannelExportReqVO exportReqVO,
                                   HttpServletResponse response) throws IOException {
        List<SupPayChannelDO> list = channelService.getChannelList(exportReqVO);
        // 导出 Excel
        List<SupPayChannelExcelVO> datas = SupPayChannelConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "支付渠道.xls", "数据", SupPayChannelExcelVO.class, datas);
    }

    @GetMapping("/get-channel")
    @ApiOperation("根据条件查询微信支付渠道")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "merchantId", value = "商户编号",
                    required = true, example = "1", dataTypeClass = Long.class),
            @ApiImplicitParam(name = "appId", value = "应用编号",
                    required = true, example = "1", dataTypeClass = Long.class),
            @ApiImplicitParam(name = "code", value = "支付渠道编码",
                    required = true, example = "wx_pub", dataTypeClass = String.class)
    })
    @PreAuthorize("@ss.hasPermission('pay:channel:query')")
    public CommonResult<SupPayChannelRespVO> getChannel(@RequestParam Long merchantId, @RequestParam Long appId, @RequestParam String code) {
        // 獲取渠道
        SupPayChannelDO channel = channelService.getChannelByConditions(merchantId, appId, code);
        if (channel == null) {
            return success(new SupPayChannelRespVO());
        }
        // 拼凑数据
        SupPayChannelRespVO respVo = SupPayChannelConvert.INSTANCE.convert(channel);
        return success(respVo);
    }

}
