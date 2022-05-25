package com.faasadmin.faas.modules.admin.admin.controller.system.file;

import cn.hutool.core.io.IoUtil;
import com.faasadmin.faas.business.core.module.system.service.file.SysFileBussService;
import com.faasadmin.faas.business.core.support.controller.BaseController;
import com.faasadmin.faas.business.infra.file.config.FileProperties;
import com.faasadmin.faas.services.system.convert.file.SysFileUploadConvert;
import com.faasadmin.faas.services.system.dal.dataobject.file.SysFileUploadDO;
import com.faasadmin.faas.services.system.service.file.SysFileUploadService;
import com.faasadmin.faas.services.system.vo.fileUpload.*;
import com.faasadmin.framework.common.pojo.CommonResult;
import com.faasadmin.framework.common.pojo.PageResult;
import com.faasadmin.framework.common.utils.ServletUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import static com.faasadmin.framework.common.pojo.CommonResult.success;

@Slf4j
@Api(tags = "文件")
@RestController
@RequestMapping("/system/file-upload")
@Validated
public class SysFileUploadController extends BaseController {

    @Resource
    private SysFileBussService sysFileBussService;

    @Resource
    private SysFileUploadService sysFileUploadService;

    @PostMapping("/upload")
    @ApiOperation("上传文件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "文件附件", required = true, dataTypeClass = MultipartFile.class),
            @ApiImplicitParam(name = "path", value = "文件路径", example = "yudaoyuanma.png", dataTypeClass = String.class)
    })
    public CommonResult<String> uploadFile(@RequestParam("file") MultipartFile file,
                                           @RequestParam("path") String path) throws Exception {
        return success(sysFileBussService.createFile(path, IoUtil.readBytes(file.getInputStream())));
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除文件")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('system:file:delete')")
    public CommonResult<Boolean> deleteFile(@RequestParam("id") Long id) throws Exception {
        sysFileBussService.deleteFile(id);
        return success(true);
    }

    @GetMapping("/{configId}/get/{path}")
    @ApiOperation("下载文件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "configId", value = "配置编号", required = true, dataTypeClass = Long.class),
            @ApiImplicitParam(name = "path", value = "文件路径", required = true, dataTypeClass = String.class)
    })
    public void getFileContent(HttpServletResponse response,
                               @PathVariable("configId") Long configId,
                               @PathVariable("path") String path) throws Exception {
        byte[] content = sysFileBussService.getFileContent(configId, path);
        if (content == null) {
            log.warn("[getFileContent][configId({}) path({}) 文件不存在]", configId, path);
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }
        ServletUtils.writeAttachment(response, path, content);
    }

    @GetMapping("/page")
    @ApiOperation("获得文件分页")
    @PreAuthorize("@ss.hasPermission('system:file:query')")
    public CommonResult<PageResult<SysFileUploadRespVO>> getFilePage(@Valid SysFileUploadPageReqVO pageVO) {
        PageResult<SysFileUploadDO> pageResult = sysFileUploadService.getFilePage(pageVO);
        return success(SysFileUploadConvert.INSTANCE.convertPage(pageResult));
    }


    @PostMapping("/folder-files")
    @ApiOperation(value = "获取文件夹下的文件", notes = "获取文件夹下的文件")
    public CommonResult<PageResult<SysFolderFileListRespVO>> folderFiles(@Valid SysFolderFileListReqVO reqVO) {
        PageResult<SysFolderFileListRespVO> list = sysFileUploadService.getFolderFiles(reqVO);
        return success(list);
    }

    @PostMapping("/move-files")
    @ApiOperation(value = "移动文件", notes = "移动文件")
    public CommonResult<Boolean> moveFiles(@Valid SysFolderFileMoveReqVO reqVO) {
        setLesseeId(reqVO);
        sysFileBussService.moveFiles(reqVO);
        return success(true);
    }

}
