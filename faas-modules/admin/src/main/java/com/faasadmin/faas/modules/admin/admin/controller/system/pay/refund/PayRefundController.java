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

@Api(tags = "???????????? - ????????????")
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
    @ApiOperation("??????????????????")
    @ApiImplicitParam(name = "id", value = "??????", required = true, example = "1024", dataTypeClass = Long.class)
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
        refundDetail.setMerchantName(ObjectUtil.isNotNull(merchantDO) ? merchantDO.getName() : "????????????");
        refundDetail.setAppName(ObjectUtil.isNotNull(appDO) ? appDO.getName() : "????????????");
        refundDetail.setChannelCodeName(ObjectUtil.isNotNull(channelEnum) ? channelEnum.getName() : "????????????");
        refundDetail.setSubject(orderDO.getSubject());
        return success(refundDetail);
    }

    @GetMapping("/page")
    @ApiOperation("????????????????????????")
    @PreAuthorize("@ss.hasPermission('pay:refund:query')")
    public CommonResult<PageResult<SupPayRefundPageItemRespVO>> getRefundPage(@Valid SupPayRefundPageReqVO pageVO) {
        PageResult<SupPayRefundDO> pageResult = refundService.getPayRefundPage(pageVO);
        if (CollectionUtil.isEmpty(pageResult.getList())) {
            return success(new PageResult<>(pageResult.getTotal()));
        }
        // ????????????ID??????
        Map<Long, SupPayMerchantDO> merchantMap = merchantService.getMerchantMap(
                CollectionUtils.convertList(pageResult.getList(), SupPayRefundDO::getMerchantId));
        // ????????????ID??????
        Map<Long, SupPayAppDO> appMap = appService.getPayAppMap(
                CollectionUtils.convertList(pageResult.getList(), SupPayRefundDO::getPayAppId));
        List<SupPayRefundPageItemRespVO> list = new ArrayList<>(pageResult.getList().size());
        pageResult.getList().forEach(c -> {
            SupPayMerchantDO merchantDO = merchantMap.get(c.getMerchantId());
            SupPayAppDO appDO = appMap.get(c.getAppId());
            PayChannelEnum channelEnum = PayChannelEnum.getByCode(c.getChannelCode());
            SupPayRefundPageItemRespVO item = SupPayRefundConvert.INSTANCE.pageItemConvert(c);
            item.setMerchantName(ObjectUtil.isNotNull(merchantDO) ? merchantDO.getName() : "????????????");
            item.setAppName(ObjectUtil.isNotNull(appDO) ? appDO.getName() : "????????????");
            item.setChannelCodeName(ObjectUtil.isNotNull(channelEnum) ? channelEnum.getName() : "????????????");
            list.add(item);
        });
        return success(new PageResult<>(list, pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @ApiOperation("?????????????????? Excel")
    @PreAuthorize("@ss.hasPermission('pay:refund:export')")
    @OperateLog(type = EXPORT)
    public void exportRefundExcel(@Valid SupPayRefundExportReqVO exportReqVO,
                                  HttpServletResponse response) throws IOException {
        List<SupPayRefundDO> list = refundService.getPayRefundList(exportReqVO);
        if (CollectionUtil.isEmpty(list)) {
            ExcelUtils.write(response, "????????????.xls", "??????",
                    SupPayRefundExcelVO.class, new ArrayList<>());
        }
        // ????????????ID??????
        Map<Long, SupPayMerchantDO> merchantMap = merchantService.getMerchantMap(
                CollectionUtils.convertList(list, SupPayRefundDO::getMerchantId));
        // ????????????ID??????
        Map<Long, SupPayAppDO> appMap = appService.getPayAppMap(
                CollectionUtils.convertList(list, SupPayRefundDO::getPayAppId));

        List<SupPayRefundExcelVO> excelDatum = new ArrayList<>(list.size());
        // ????????????????????????
        Map<Long, SupPayOrderDO> orderMap = orderService.getOrderSubjectMap(
                CollectionUtils.convertList(list, SupPayRefundDO::getOrderId));
        list.forEach(c -> {
            SupPayMerchantDO merchantDO = merchantMap.get(c.getMerchantId());
            SupPayAppDO appDO = appMap.get(c.getAppId());
            PayChannelEnum channelEnum = PayChannelEnum.getByCode(c.getChannelCode());
            SupPayRefundExcelVO excelItem = SupPayRefundConvert.INSTANCE.excelConvert(c);
            excelItem.setMerchantName(ObjectUtil.isNotNull(merchantDO) ? merchantDO.getName() : "????????????");
            excelItem.setAppName(ObjectUtil.isNotNull(appDO) ? appDO.getName() : "????????????");
            excelItem.setChannelCodeName(ObjectUtil.isNotNull(channelEnum) ? channelEnum.getName() : "????????????");
            excelItem.setSubject(orderMap.get(c.getOrderId()).getSubject());
            excelDatum.add(excelItem);
        });
        // ?????? Excel
        ExcelUtils.write(response, "????????????.xls", "??????", SupPayRefundExcelVO.class, excelDatum);
    }

}
