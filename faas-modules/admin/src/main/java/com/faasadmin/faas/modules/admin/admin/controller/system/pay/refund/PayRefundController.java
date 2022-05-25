package com.faasadmin.faas.modules.admin.admin.controller.system.pay.refund;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.faasadmin.faas.services.pay.convert.refund.SupPayRefundConvert;
import com.faasadmin.faas.services.pay.dal.dataobject.app.SupPayAppDO;
import com.faasadmin.faas.services.pay.dal.dataobject.merchant.SupPayMerchantDO;
import com.faasadmin.faas.services.pay.dal.dataobject.order.SupPayOrderDO;
import com.faasadmin.faas.services.pay.dal.dataobject.refund.SupPayRefundDO;
import com.faasadmin.faas.services.pay.service.app.SupPayAppService;
import com.faasadmin.faas.services.pay.service.merchant.SupPayMerchantService;
import com.faasadmin.faas.services.pay.service.order.SupPayOrderService;
import com.faasadmin.faas.services.pay.service.refund.SupPayRefundService;
import com.faasadmin.faas.services.pay.vo.refund.*;
import com.faasadmin.framework.common.pojo.CommonResult;
import com.faasadmin.framework.common.pojo.PageResult;
import com.faasadmin.framework.common.utils.collection.CollectionUtils;
import com.faasadmin.framework.excel.core.util.ExcelUtils;
import com.faasadmin.framework.operatelog.core.annotations.OperateLog;
import com.faasadmin.framework.pay.core.enums.PayChannelEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.faasadmin.framework.common.pojo.CommonResult.success;
import static com.faasadmin.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Api(tags = "管理后台 - 退款订单")
@RestController
@RequestMapping("/pay/refund")
@Validated
public class PayRefundController {

    @Resource
    private SupPayRefundService refundService;
    @Resource
    private SupPayMerchantService merchantService;
    @Resource
    private SupPayAppService appService;
    @Resource
    private SupPayOrderService orderService;

    @GetMapping("/get")
    @ApiOperation("获得退款订单")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('pay:refund:query')")
    public CommonResult<SupPayRefundDetailsRespVO> getRefund(@RequestParam("id") Long id) {
        SupPayRefundDO refund = refundService.getPayRefund(id);
        if (ObjectUtil.isNull(refund)) {
            return success(new SupPayRefundDetailsRespVO());
        }
        SupPayMerchantDO merchantDO = merchantService.getMerchant(refund.getMerchantId());
        SupPayAppDO appDO = appService.getPayApp(refund.getPayAppId());
        PayChannelEnum channelEnum = PayChannelEnum.getByCode(refund.getChannelCode());
        SupPayOrderDO orderDO = orderService.getOrder(refund.getOrderId());
        SupPayRefundDetailsRespVO refundDetail = SupPayRefundConvert.INSTANCE.refundDetailConvert(refund);
        refundDetail.setMerchantName(ObjectUtil.isNotNull(merchantDO) ? merchantDO.getName() : "未知商户");
        refundDetail.setAppName(ObjectUtil.isNotNull(appDO) ? appDO.getName() : "未知应用");
        refundDetail.setChannelCodeName(ObjectUtil.isNotNull(channelEnum) ? channelEnum.getName() : "未知渠道");
        refundDetail.setSubject(orderDO.getSubject());
        return success(refundDetail);
    }

    @GetMapping("/page")
    @ApiOperation("获得退款订单分页")
    @PreAuthorize("@ss.hasPermission('pay:refund:query')")
    public CommonResult<PageResult<SupPayRefundPageItemRespVO>> getRefundPage(@Valid SupPayRefundPageReqVO pageVO) {
        PageResult<SupPayRefundDO> pageResult = refundService.getPayRefundPage(pageVO);
        if (CollectionUtil.isEmpty(pageResult.getList())) {
            return success(new PageResult<>(pageResult.getTotal()));
        }
        // 处理商户ID数据
        Map<Long, SupPayMerchantDO> merchantMap = merchantService.getMerchantMap(
                CollectionUtils.convertList(pageResult.getList(), SupPayRefundDO::getMerchantId));
        // 处理应用ID数据
        Map<Long, SupPayAppDO> appMap = appService.getPayAppMap(
                CollectionUtils.convertList(pageResult.getList(), SupPayRefundDO::getPayAppId));
        List<SupPayRefundPageItemRespVO> list = new ArrayList<>(pageResult.getList().size());
        pageResult.getList().forEach(c -> {
            SupPayMerchantDO merchantDO = merchantMap.get(c.getMerchantId());
            SupPayAppDO appDO = appMap.get(c.getAppId());
            PayChannelEnum channelEnum = PayChannelEnum.getByCode(c.getChannelCode());
            SupPayRefundPageItemRespVO item = SupPayRefundConvert.INSTANCE.pageItemConvert(c);
            item.setMerchantName(ObjectUtil.isNotNull(merchantDO) ? merchantDO.getName() : "未知商户");
            item.setAppName(ObjectUtil.isNotNull(appDO) ? appDO.getName() : "未知应用");
            item.setChannelCodeName(ObjectUtil.isNotNull(channelEnum) ? channelEnum.getName() : "未知渠道");
            list.add(item);
        });
        return success(new PageResult<>(list, pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出退款订单 Excel")
    @PreAuthorize("@ss.hasPermission('pay:refund:export')")
    @OperateLog(type = EXPORT)
    public void exportRefundExcel(@Valid SupPayRefundExportReqVO exportReqVO,
                                  HttpServletResponse response) throws IOException {
        List<SupPayRefundDO> list = refundService.getPayRefundList(exportReqVO);
        if (CollectionUtil.isEmpty(list)) {
            ExcelUtils.write(response, "退款订单.xls", "数据",
                    SupPayRefundExcelVO.class, new ArrayList<>());
        }
        // 处理商户ID数据
        Map<Long, SupPayMerchantDO> merchantMap = merchantService.getMerchantMap(
                CollectionUtils.convertList(list, SupPayRefundDO::getMerchantId));
        // 处理应用ID数据
        Map<Long, SupPayAppDO> appMap = appService.getPayAppMap(
                CollectionUtils.convertList(list, SupPayRefundDO::getPayAppId));

        List<SupPayRefundExcelVO> excelDatum = new ArrayList<>(list.size());
        // 处理商品名称数据
        Map<Long, SupPayOrderDO> orderMap = orderService.getOrderSubjectMap(
                CollectionUtils.convertList(list, SupPayRefundDO::getOrderId));
        list.forEach(c -> {
            SupPayMerchantDO merchantDO = merchantMap.get(c.getMerchantId());
            SupPayAppDO appDO = appMap.get(c.getAppId());
            PayChannelEnum channelEnum = PayChannelEnum.getByCode(c.getChannelCode());
            SupPayRefundExcelVO excelItem = SupPayRefundConvert.INSTANCE.excelConvert(c);
            excelItem.setMerchantName(ObjectUtil.isNotNull(merchantDO) ? merchantDO.getName() : "未知商户");
            excelItem.setAppName(ObjectUtil.isNotNull(appDO) ? appDO.getName() : "未知应用");
            excelItem.setChannelCodeName(ObjectUtil.isNotNull(channelEnum) ? channelEnum.getName() : "未知渠道");
            excelItem.setSubject(orderMap.get(c.getOrderId()).getSubject());
            excelDatum.add(excelItem);
        });
        // 导出 Excel
        ExcelUtils.write(response, "退款订单.xls", "数据", SupPayRefundExcelVO.class, excelDatum);
    }

}
