package com.faasadmin.faas.modules.admin.admin.controller.system.pay.merchant;

import com.faasadmin.faas.business.core.support.controller.BaseController;
import com.faasadmin.faas.business.pay.service.merchant.PayMerchantCoreService;
import com.faasadmin.faas.services.pay.convert.merchant.SupPayMerchantConvert;
import com.faasadmin.faas.services.pay.dal.dataobject.merchant.SupPayMerchantDO;
import com.faasadmin.faas.services.pay.enums.merchant.MerchantkeyTypeEnum;
import com.faasadmin.faas.services.pay.service.merchant.SupPayMerchantService;
import com.faasadmin.faas.services.pay.vo.merchant.*;
import com.faasadmin.framework.common.pojo.CommonResult;
import com.faasadmin.framework.common.pojo.PageResult;
import com.faasadmin.framework.excel.core.util.ExcelUtils;
import com.faasadmin.framework.operatelog.core.annotations.OperateLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
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

@Api(tags = "支付商户信息")
@RestController
@RequestMapping("/pay/merchant")
@Validated
public class PayMerchantController extends BaseController {

    @Resource
    private SupPayMerchantService merchantService;

    @Resource
    private PayMerchantCoreService payMerchantCoreService;

    @PostMapping("/create")
    @ApiOperation("创建支付商户信息")
    @PreAuthorize("@ss.hasPermission('pay:merchant:create')")
    public CommonResult<Long> createMerchant(@Valid @RequestBody SupPayMerchantCreateReqVO createReqVO) {
        createReqVO.setKeyType(MerchantkeyTypeEnum.SM2.getType());
        createReqVO.setLesseeId(getLesseeId());
        createReqVO.setAppId(0L);
        return success(payMerchantCoreService.createMerchant(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新支付商户信息")
    @PreAuthorize("@ss.hasPermission('pay:merchant:update')")
    public CommonResult<Boolean> updateMerchant(@Valid @RequestBody SupPayMerchantUpdateReqVO updateReqVO) {
        updateReqVO.setKeyType(MerchantkeyTypeEnum.SM2.getType());
        updateReqVO.setLesseeId(getLesseeId());
        updateReqVO.setAppId(0L);
        merchantService.updateMerchant(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @ApiOperation("修改支付商户状态")
    @PreAuthorize("@ss.hasPermission('pay:merchant:update')")
    public CommonResult<Boolean> updateMerchantStatus(@Valid @RequestBody SupPayMerchantUpdateStatusReqVO reqVO) {
        merchantService.updateMerchantStatus(reqVO.getId(), reqVO.getStatus());
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除支付商户信息")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('pay:merchant:delete')")
    public CommonResult<Boolean> deleteMerchant(@RequestParam("id") Long id) {
        merchantService.deleteMerchant(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得支付商户信息")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('pay:merchant:query')")
    public CommonResult<SupPayMerchantRespVO> getMerchant(@RequestParam("id") Long id) {
        SupPayMerchantDO merchant = merchantService.getMerchant(id);
        return success(SupPayMerchantConvert.INSTANCE.convert(merchant));
    }

    @GetMapping("/list-by-name")
    @ApiOperation("根据商户名称获得支付商户信息列表")
    @ApiImplicitParam(name = "name", value = "商户名称", example = "芋道", dataTypeClass = String.class)
    @PreAuthorize("@ss.hasPermission('pay:merchant:query')")
    public CommonResult<List<SupPayMerchantRespVO>> getMerchantListByName(@RequestParam(required = false) String name) {
        List<SupPayMerchantDO> merchantListDO = merchantService.getMerchantListByName(name);
        return success(SupPayMerchantConvert.INSTANCE.convertList(merchantListDO));
    }

    @GetMapping("/list")
    @ApiOperation("获得支付商户信息列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('pay:merchant:query')")
    public CommonResult<List<SupPayMerchantRespVO>> getMerchantList(@RequestParam("ids") Collection<Long> ids) {
        List<SupPayMerchantDO> list = merchantService.getMerchantList(ids);
        return success(SupPayMerchantConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得支付商户信息分页")
    @PreAuthorize("@ss.hasPermission('pay:merchant:query')")
    public CommonResult<PageResult<SupPayMerchantRespVO>> getMerchantPage(@Valid SupPayMerchantPageReqVO pageVO) {
        PageResult<SupPayMerchantDO> pageResult = merchantService.getMerchantPage(pageVO);
        return success(SupPayMerchantConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出支付商户信息 Excel")
    @PreAuthorize("@ss.hasPermission('pay:merchant:export')")
    @OperateLog(type = EXPORT)
    public void exportMerchantExcel(@Valid SupPayMerchantExportReqVO exportReqVO,
                                    HttpServletResponse response) throws IOException {
        List<SupPayMerchantDO> list = merchantService.getMerchantList(exportReqVO);
        // 导出 Excel
        List<SupPayMerchantExcelVO> datas = SupPayMerchantConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "支付商户信息.xls", "数据", SupPayMerchantExcelVO.class, datas);
    }

}
