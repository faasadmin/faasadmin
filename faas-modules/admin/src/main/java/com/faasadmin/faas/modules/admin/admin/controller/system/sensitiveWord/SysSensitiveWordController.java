package com.faasadmin.faas.modules.admin.admin.controller.system.sensitiveWord;

import com.faasadmin.faas.business.expand.service.sensitiveWord.SysSensitiveWordBussService;
import com.faasadmin.faas.services.system.convert.sensitiveWord.SysSensitiveWordConvert;
import com.faasadmin.faas.services.system.dal.dataobject.sensitiveWord.SysSensitiveWordDO;
import com.faasadmin.faas.services.system.vo.sensitiveWord.*;
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
import java.util.List;
import java.util.Set;

import static com.faasadmin.framework.common.pojo.CommonResult.success;
import static com.faasadmin.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;


@Api(tags = "管理后台 - 敏感词")
@RestController
@RequestMapping("/system/sensitive-word")
@Validated
public class SysSensitiveWordController {

    @Resource
    private SysSensitiveWordBussService sysSensitiveWordBussService;

    @PostMapping("/create")
    @ApiOperation("创建敏感词")
    @PreAuthorize("@ss.hasPermission('system:sensitive-word:create')")
    public CommonResult<Long> createSensitiveWord(@Valid @RequestBody SysSensitiveWordCreateReqVO createReqVO) {
        return success(sysSensitiveWordBussService.createSensitiveWord(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新敏感词")
    @PreAuthorize("@ss.hasPermission('system:sensitive-word:update')")
    public CommonResult<Boolean> updateSensitiveWord(@Valid @RequestBody SysSensitiveWordUpdateReqVO updateReqVO) {
        sysSensitiveWordBussService.updateSensitiveWord(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除敏感词")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('system:sensitive-word:delete')")
    public CommonResult<Boolean> deleteSensitiveWord(@RequestParam("id") Long id) {
        sysSensitiveWordBussService.deleteSensitiveWord(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得敏感词")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('system:sensitive-word:query')")
    public CommonResult<SysSensitiveWordRespVO> getSensitiveWord(@RequestParam("id") Long id) {
        SysSensitiveWordDO sensitiveWord = sysSensitiveWordBussService.getSensitiveWord(id);
        return success(SysSensitiveWordConvert.INSTANCE.convert(sensitiveWord));
    }

    @GetMapping("/page")
    @ApiOperation("获得敏感词分页")
    @PreAuthorize("@ss.hasPermission('system:sensitive-word:query')")
    public CommonResult<PageResult<SysSensitiveWordRespVO>> getSensitiveWordPage(@Valid SysSensitiveWordPageReqVO pageVO) {
        PageResult<SysSensitiveWordDO> pageResult = sysSensitiveWordBussService.getSensitiveWordPage(pageVO);
        return success(SysSensitiveWordConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出敏感词 Excel")
    @PreAuthorize("@ss.hasPermission('system:sensitive-word:export')")
    @OperateLog(type = EXPORT)
    public void exportSensitiveWordExcel(@Valid SysSensitiveWordExportReqVO exportReqVO, HttpServletResponse response) throws IOException {
        List<SysSensitiveWordDO> list = sysSensitiveWordBussService.getSensitiveWordList(exportReqVO);
        // 导出 Excel
        List<SysSensitiveWordExcelVO> datas = SysSensitiveWordConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "敏感词.xls", "数据", SysSensitiveWordExcelVO.class, datas);
    }

    @GetMapping("/get-tags")
    @ApiOperation("获取所有敏感词的标签数组")
    @PreAuthorize("@ss.hasPermission('system:sensitive-word:query')")
    public CommonResult<Set<String>> getSensitiveWordTags() throws IOException {
        return success(sysSensitiveWordBussService.getSensitiveWordTags());
    }

    @GetMapping("/validate-text")
    @ApiOperation("获得文本所包含的不合法的敏感词数组")
    public CommonResult<List<String>> validateText(@RequestParam("text") String text, @RequestParam(value = "tags", required = false) List<String> tags) {
        return success(sysSensitiveWordBussService.validateText(text, tags));
    }

}
