package com.faasadmin.faas.modules.admin.admin.controller.system.pay.order;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.faasadmin.faas.services.pay.convert.order.SupPayOrderConvert;
import com.faasadmin.faas.services.pay.dal.dataobject.app.SupPayAppDO;
import com.faasadmin.faas.services.pay.dal.dataobject.merchant.SupPayMerchantDO;
import com.faasadmin.faas.services.pay.dal.dataobject.order.SupPayOrderDO;
import com.faasadmin.faas.services.pay.dal.dataobject.orderExtension.SupPayOrderExtensionDO;
import com.faasadmin.faas.services.pay.service.app.SupPayAppService;
import com.faasadmin.faas.services.pay.service.merchant.SupPayMerchantService;
import com.faasadmin.faas.services.pay.service.order.SupPayOrderService;
import com.faasadmin.faas.services.pay.service.orderExtension.SupPayOrderExtensionService;
import com.faasadmin.faas.services.pay.vo.order.*;
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
@RequestMapping("/pay/order")
@Validated
public class PayOrderController {

    @Resource
    private SupPayOrderService orderService;
    @Resource
    private SupPayOrderExtensionService orderExtensionService;
    @Resource
    private SupPayMerchantService merchantService;
    @Resource
    private SupPayAppService appService;

    @GetMapping("/get")
    @ApiOperation("??????????????????")
    @ApiImplicitParam(name = "id", value = "??????", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('pay:order:query')")
    public CommonResult<SupPayOrderDetailsRespVO> getOrder(@RequestParam("id") Long id) {
        SupPayOrderDO order = orderService.getOrder(id);
        if (ObjectUtil.isNull(order)) {
            return success(new SupPayOrderDetailsRespVO());
        }
        SupPayMerchantDO merchantDO = merchantService.getMerchant(order.getMerchantId());
        SupPayAppDO appDO = appService.getPayApp(order.getPayAppId());
        PayChannelEnum channelEnum = PayChannelEnum.getByCode(order.getPayChannelCode());
        SupPayOrderDetailsRespVO respVO = SupPayOrderConvert.INSTANCE.orderDetailConvert(order);
        respVO.setMerchantName(merchantDO.getName());
        respVO.setAppName(appDO.getName());
        respVO.setChannelCodeName(channelEnum.getName());
        SupPayOrderExtensionDO extensionDO = orderExtensionService.getOrderExtension(order.getSuccessExtensionId());
        if (ObjectUtil.isNotNull(extensionDO)) {
            respVO.setPayOrderExtension(SupPayOrderConvert.INSTANCE.orderDetailExtensionConvert(extensionDO));
        }
        return success(respVO);
    }

    @GetMapping("/page")
    @ApiOperation("????????????????????????")
    @PreAuthorize("@ss.hasPermission('pay:order:query')")
    public CommonResult<PageResult<SupPayOrderPageItemRespVO>> getOrderPage(@Valid SupPayOrderPageReqVO pageVO) {
        PageResult<SupPayOrderDO> pageResult = orderService.getOrderPage(pageVO);
        if (CollectionUtil.isEmpty(pageResult.getList())) {
            return success(new PageResult<>(pageResult.getTotal()));
        }
        // ????????????ID??????
        Map<Long, SupPayMerchantDO> merchantMap = merchantService.getMerchantMap(
                CollectionUtils.convertList(pageResult.getList(), SupPayOrderDO::getMerchantId));
        // ????????????ID??????
        Map<Long, SupPayAppDO> appMap = appService.getPayAppMap(
                CollectionUtils.convertList(pageResult.getList(), SupPayOrderDO::getPayAppId));
        List<SupPayOrderPageItemRespVO> pageList = new ArrayList<>(pageResult.getList().size());
        pageResult.getList().forEach(c -> {
            SupPayMerchantDO merchantDO = merchantMap.get(c.getMerchantId());
            SupPayAppDO appDO = appMap.get(c.getPayAppId());
            PayChannelEnum channelEnum = PayChannelEnum.getByCode(c.getPayChannelCode());
            SupPayOrderPageItemRespVO orderItem = SupPayOrderConvert.INSTANCE.pageConvertItemPage(c);
            orderItem.setMerchantName(ObjectUtil.isNotNull(merchantDO) ? merchantDO.getName() : "????????????");
            orderItem.setAppName(ObjectUtil.isNotNull(appDO) ? appDO.getName() : "????????????");
            orderItem.setChannelCodeName(ObjectUtil.isNotNull(channelEnum) ? channelEnum.getName() : "????????????");
            pageList.add(orderItem);
        });
        return success(new PageResult<>(pageList, pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @ApiOperation("??????????????????Excel")
    @PreAuthorize("@ss.hasPermission('pay:order:export')")
    @OperateLog(type = EXPORT)
    public void exportOrderExcel(@Valid SupPayOrderExportReqVO exportReqVO,
                                 HttpServletResponse response) throws IOException {
        List<SupPayOrderDO> list = orderService.getOrderList(exportReqVO);
        if (CollectionUtil.isEmpty(list)) {
            ExcelUtils.write(response, "????????????.xls", "??????",
                    SupPayOrderExcelVO.class, new ArrayList<>());
        }
        // ????????????ID??????
        Map<Long, SupPayMerchantDO> merchantMap = merchantService.getMerchantMap(
                CollectionUtils.convertList(list, SupPayOrderDO::getMerchantId));
        // ????????????ID??????
        Map<Long, SupPayAppDO> appMap = appService.getPayAppMap(
                CollectionUtils.convertList(list, SupPayOrderDO::getPayAppId));
        // ????????????????????????
        Map<Long, SupPayOrderExtensionDO> orderExtensionMap = orderExtensionService
                .getOrderExtensionMap(CollectionUtils.convertList(list, SupPayOrderDO::getSuccessExtensionId));
        List<SupPayOrderExcelVO> excelDatum = new ArrayList<>(list.size());
        list.forEach(c -> {
            SupPayMerchantDO merchantDO = merchantMap.get(c.getMerchantId());
            SupPayAppDO appDO = appMap.get(c.getPayAppId());
            PayChannelEnum channelEnum = PayChannelEnum.getByCode(c.getPayChannelCode());
            SupPayOrderExtensionDO orderExtensionDO = orderExtensionMap.get(c.getSuccessExtensionId());
            SupPayOrderExcelVO excelItem = SupPayOrderConvert.INSTANCE.excelConvert(c);
            excelItem.setMerchantName(ObjectUtil.isNotNull(merchantDO) ? merchantDO.getName() : "????????????");
            excelItem.setAppName(ObjectUtil.isNotNull(appDO) ? appDO.getName() : "????????????");
            excelItem.setChannelCodeName(ObjectUtil.isNotNull(channelEnum) ? channelEnum.getName() : "????????????");
            excelItem.setNo(ObjectUtil.isNotNull(orderExtensionDO) ? orderExtensionDO.getNo() : "");
            excelDatum.add(excelItem);
        });
        // ?????? Excel
        ExcelUtils.write(response, "????????????.xls", "??????", SupPayOrderExcelVO.class, excelDatum);
    }

}
