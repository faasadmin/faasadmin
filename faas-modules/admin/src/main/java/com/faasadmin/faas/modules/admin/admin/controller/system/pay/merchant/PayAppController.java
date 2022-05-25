package com.faasadmin.faas.modules.admin.admin.controller.system.pay.merchant;

import cn.hutool.core.collection.CollUtil;
import com.faasadmin.faas.business.core.support.controller.BaseController;
import com.faasadmin.faas.services.pay.convert.app.SupPayAppConvert;
import com.faasadmin.faas.services.pay.dal.dataobject.app.SupPayAppDO;
import com.faasadmin.faas.services.pay.dal.dataobject.channel.SupPayChannelDO;
import com.faasadmin.faas.services.pay.dal.dataobject.merchant.SupPayMerchantDO;
import com.faasadmin.faas.services.pay.service.app.SupPayAppService;
import com.faasadmin.faas.services.pay.service.channel.SupPayChannelService;
import com.faasadmin.faas.services.pay.service.merchant.SupPayMerchantService;
import com.faasadmin.faas.services.pay.vo.app.*;
import com.faasadmin.framework.common.pojo.CommonResult;
import com.faasadmin.framework.common.pojo.PageResult;
import com.faasadmin.framework.common.utils.collection.CollectionUtils;
import com.faasadmin.framework.excel.core.util.ExcelUtils;
import com.faasadmin.framework.operatelog.core.annotations.OperateLog;
import com.faasadmin.framework.pay.core.enums.PayChannelEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.*;

import static com.faasadmin.framework.common.pojo.CommonResult.success;
import static com.faasadmin.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Slf4j
@Api(tags = "管理后台 - 支付应用信息")
@RestController
@RequestMapping("/pay/app")
@Validated
public class PayAppController extends BaseController {

    @Resource
    private SupPayAppService appService;
    @Resource
    private SupPayChannelService channelService;
    @Resource
    private SupPayMerchantService merchantService;

    @PostMapping("/create")
    @ApiOperation("创建支付应用信息")
    @PreAuthorize("@ss.hasPermission('pay:app:create')")
    public CommonResult<Long> createApp(@Valid @RequestBody SupPayAppCreateReqVO createReqVO) {
        createReqVO.setLesseeId(getLesseeId());
        createReqVO.setAppId(0L);
        return success(appService.createPayApp(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新支付应用信息")
    @PreAuthorize("@ss.hasPermission('pay:app:update')")
    public CommonResult<Boolean> updateApp(@Valid @RequestBody SupPayAppUpdateReqVO updateReqVO) {
        updateReqVO.setLesseeId(getLesseeId());
        updateReqVO.setAppId(0L);
        appService.updatePayApp(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @ApiOperation("更新支付应用状态")
    @PreAuthorize("@ss.hasPermission('pay:app:update')")
    public CommonResult<Boolean> updateAppStatus(@Valid @RequestBody SupPayAppUpdateStatusReqVO updateReqVO) {
        appService.updatePayAppStatus(updateReqVO.getId(), updateReqVO.getStatus());
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除支付应用信息")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('pay:app:delete')")
    public CommonResult<Boolean> deleteApp(@RequestParam("id") Long id) {
        appService.deletePayApp(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得支付应用信息")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('pay:app:query')")
    public CommonResult<SupPayAppRespVO> getApp(@RequestParam("id") Long id) {
        SupPayAppDO app = appService.getPayApp(id);
        return success(SupPayAppConvert.INSTANCE.convert(app));
    }

    @GetMapping("/list")
    @ApiOperation("获得支付应用信息列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('pay:app:query')")
    public CommonResult<List<SupPayAppRespVO>> getAppList(@RequestParam("ids") Collection<Long> ids) {
        List<SupPayAppDO> list = appService.getPayAppList(ids);
        return success(SupPayAppConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得支付应用信息分页")
    @PreAuthorize("@ss.hasPermission('pay:app:query')")
    public CommonResult<PageResult<SupPayAppPageItemRespVO>> getAppPage(@Valid SupPayAppPageReqVO pageVO) {
        // 得到应用分页列表
        PageResult<SupPayAppDO> pageResult = appService.getPayAppPage(pageVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(new PageResult<>(pageResult.getTotal()));
        }
        // 得到所有的应用编号，查出所有的渠道
        Collection<Long> payAppIds = CollectionUtils.convertList(pageResult.getList(), SupPayAppDO::getId);
        List<SupPayChannelDO> channels = channelService.getChannelListByAppIds(payAppIds);
        // TODO 可以基于 appId 建立一个 multiMap。这样下面，直接 get 到之后，CollUtil buildSet 即可
        Iterator<SupPayChannelDO> iterator = channels.iterator();
        // 得到所有的商户信息
        Collection<Long> merchantIds = CollectionUtils.convertList(pageResult.getList(), SupPayAppDO::getMerchantId);
        Map<Long, SupPayMerchantDO> deptMap = merchantService.getMerchantMap(merchantIds);
        // 利用反射将渠道数据复制到返回的数据结构中去
        List<SupPayAppPageItemRespVO> appList = new ArrayList<>(pageResult.getList().size());
        pageResult.getList().forEach(app -> {
            // 写入应用信息的数据
            SupPayAppPageItemRespVO respVO = SupPayAppConvert.INSTANCE.pageConvert(app);
            // 写入商户的数据
            respVO.setPayMerchant(SupPayAppConvert.INSTANCE.convert(deptMap.get(app.getMerchantId())));
            // 写入支付渠道信息的数据
            Set<String> channelCodes = new HashSet<>(PayChannelEnum.values().length);
            while (iterator.hasNext()) {
                SupPayChannelDO channelDO = iterator.next();
                if (channelDO.getPayAppId().equals(app.getId())) {
                    channelCodes.add(channelDO.getCode());
                    iterator.remove();
                }
            }
            respVO.setChannelCodes(channelCodes);
            appList.add(respVO);
        });
        return success(new PageResult<>(appList, pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出支付应用信息 Excel")
    @PreAuthorize("@ss.hasPermission('pay:app:export')")
    @OperateLog(type = EXPORT)
    public void exportAppExcel(@Valid SupPayAppExportReqVO exportReqVO,
                               HttpServletResponse response) throws IOException {
        List<SupPayAppDO> list = appService.getPayAppList(exportReqVO);
        // 导出 Excel
        List<SupPayAppExcelVO> datas = SupPayAppConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "支付应用信息.xls", "数据", SupPayAppExcelVO.class, datas);
    }

    @GetMapping("/list-merchant-id")
    @ApiOperation("根据商户 ID 查询支付应用信息")
    @ApiImplicitParam(name = "merchantId", value = "商户ID", required = true, example = "1", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('pay:merchant:query')")
    public CommonResult<List<SupPayAppRespVO>> getMerchantListByName(@RequestParam Long merchantId) {
        List<SupPayAppDO> appListDO = appService.getListByMerchantId(merchantId);
        return success(SupPayAppConvert.INSTANCE.convertList(appListDO));
    }

}
