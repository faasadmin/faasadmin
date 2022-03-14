package com.faasadmin.faas.modules.admin.system.controller.file;


import com.faasadmin.faas.services.system.convert.fileRecord.SysFileRecordFolderConvert;
import com.faasadmin.faas.services.system.dal.dataobject.fileRecord.SysFileRecordFolderDO;
import com.faasadmin.faas.services.system.service.fileRecord.SysFileRecordFolderService;
import com.faasadmin.framework.common.pojo.CommonResult;
import com.faasadmin.framework.common.pojo.PageResult;
import com.faasadmin.framework.excel.core.util.ExcelUtils;
import com.faasadmin.framework.operatelog.core.annotations.OperateLog;
import com.faasadmin.framework.security.core.util.SecurityFrameworkUtils;
import com.faasadmin.faas.services.system.vo.fileRecordFolder.*;
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
import java.util.Comparator;
import java.util.List;

import static com.faasadmin.framework.common.pojo.CommonResult.success;
import static com.faasadmin.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Api(tags = "文件夹")
@RestController
@RequestMapping("/system/file/folder/record")
@Validated
public class SysFileRecordFolderController {

    @Resource
    private SysFileRecordFolderService fileFolderService;

    @PostMapping("/create")
    @ApiOperation("创建文件夹")
    @PreAuthorize("@ss.hasPermission('system:file-folder:create')")
    public CommonResult<Long> createFileFolder(@Valid @RequestBody SysFileRecordFolderCreateReqVO createReqVO) {
        createReqVO.setLesseeId(SecurityFrameworkUtils.getLesseeId());
        return success(fileFolderService.createFileFolder(createReqVO));
    }

    @PutMapping("/simple-update")
    @ApiOperation("简单更新文件夹")
    @PreAuthorize("@ss.hasPermission('system:file-folder:update')")
    public CommonResult<Boolean> updateSimpleFileFolder(@Valid @RequestBody SysFileRecordFolderSimpleUpdateReqVO simpleUpdateReqVO) {
        simpleUpdateReqVO.setLesseeId(SecurityFrameworkUtils.getLesseeId());
        fileFolderService.updateSimpleFileFolder(simpleUpdateReqVO);
        return success(true);
    }

    @PutMapping("/update")
    @ApiOperation("更新文件夹")
    @PreAuthorize("@ss.hasPermission('system:file-folder:update')")
    public CommonResult<Boolean> updateFileFolder(@Valid @RequestBody SysFileRecordFolderUpdateReqVO updateReqVO) {
        updateReqVO.setLesseeId(SecurityFrameworkUtils.getLesseeId());
        fileFolderService.updateFileFolder(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除文件夹")
    @ApiImplicitParam(name = "id", value = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('system:file-folder:delete')")
    public CommonResult<Boolean> deleteFileFolder(@RequestParam("id") Long id) {
        fileFolderService.deleteFileFolder(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得文件夹")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('system:file-folder:query')")
    public CommonResult<SysFileRecordFolderRespVO> getFileFolder(@RequestParam("id") Long id) {
        SysFileRecordFolderDO fileFolder = fileFolderService.getFileFolder(id);
        return success(SysFileRecordFolderConvert.INSTANCE.convert(fileFolder));
    }

    @GetMapping("/list")
    @ApiOperation("获得文件夹列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('system:file-folder:query')")
    public CommonResult<List<SysFileRecordFolderRespVO>> getFileFolderList(@RequestParam("ids") Collection<Long> ids) {
        List<SysFileRecordFolderDO> list = fileFolderService.getFileFolderList(ids);
        return success(SysFileRecordFolderConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得文件夹分页")
    @PreAuthorize("@ss.hasPermission('system:file-folder:query')")
    public CommonResult<PageResult<SysFileRecordFolderRespVO>> getFileFolderPage(@Valid SysFileRecordFolderPageReqVO pageVO) {
        PageResult<SysFileRecordFolderDO> pageResult = fileFolderService.getFileFolderPage(pageVO);
        return success(SysFileRecordFolderConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出文件夹 Excel")
    @PreAuthorize("@ss.hasPermission('system:file-folder:export')")
    @OperateLog(type = EXPORT)
    public void exportFileFolderExcel(@Valid SysFileRecordFolderExportReqVO exportReqVO,
                                      HttpServletResponse response) throws IOException {
        List<SysFileRecordFolderDO> list = fileFolderService.getFileFolderList(exportReqVO);
        // 导出 Excel
        List<SysFileRecordFolderExcelVO> datas = SysFileRecordFolderConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "文件夹.xls", "数据", SysFileRecordFolderExcelVO.class, datas);
    }

    @GetMapping("/list-all-simple")
    @ApiOperation(value = "获取文件夹精简信息列表", notes = "文件夹列表")
    public CommonResult<List<SysFileRecordFolderSimpleRespVO>> getSimpleFileFolder(@Valid SysFileRecordFolderListReqVO reqVO) {
        // 获得文件夹列表，只要开启状态的
        //SysFileFolderListReqVO reqVO = new SysFileFolderListReqVO();
        //reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        List<SysFileRecordFolderDO> list = fileFolderService.getSimpleFolders(reqVO);
        // 排序后，返回给前端
        list.sort(Comparator.comparing(SysFileRecordFolderDO::getSort));
        return success(SysFileRecordFolderConvert.INSTANCE.convertList03(list));
    }

    @GetMapping("/folders")
    @ApiOperation(value = "获取文件夹精简信息列表", notes = "文件夹列表")
    public CommonResult<List<SysFileRecordFolderSimpleRespVO>> folders(@Valid SysFileRecordFolderListReqVO reqVO) {
        List<SysFileRecordFolderDO> list = fileFolderService.getSimpleFolders(reqVO);
        // 排序后，返回给前端
        list.sort(Comparator.comparing(SysFileRecordFolderDO::getSort));
        return success(SysFileRecordFolderConvert.INSTANCE.convertList03(list));
    }

}
