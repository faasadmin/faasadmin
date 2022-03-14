package com.faasadmin.faas.modules.admin.system.controller.file;

import cn.hutool.core.io.IoUtil;
import com.faasadmin.faas.business.infra.file.config.FileProperties;
import com.faasadmin.faas.services.system.convert.dbFile.SysDbFileConvert;
import com.faasadmin.faas.services.system.dal.dataobject.dbFile.SysDbFileDO;
import com.faasadmin.faas.services.system.service.dbFile.SysDbFileService;
import com.faasadmin.faas.services.system.vo.dbFile.SysDbFilePageReqVO;
import com.faasadmin.faas.services.system.vo.dbFile.SysDbFileRespVO;
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
import java.io.IOException;

import static com.faasadmin.framework.common.pojo.CommonResult.success;

@Api(tags = "文件存储")
@RestController
@RequestMapping("/system/db/file")
@Validated
@Slf4j
public class SysDbFileController {

    @Resource
    private SysDbFileService fileService;

    @Resource
    private FileProperties fileProperties;

    @PostMapping("/upload")
    @ApiOperation("上传文件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "文件附件", required = true, dataTypeClass = MultipartFile.class),
            @ApiImplicitParam(name = "path", value = "文件路径", dataTypeClass = String.class)
    })
    public CommonResult<String> uploadFile(@RequestParam("file") MultipartFile file,
                                           @RequestParam("path") String path) throws IOException {
        // 存储文件
        String url = fileProperties.getBasePath() + fileService.createFile(path, IoUtil.readBytes(file.getInputStream()));
        return success(url);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除文件")
    @ApiImplicitParam(name = "id", value = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('system:db:file:delete')")
    public CommonResult<Boolean> deleteFile(@RequestParam("id") String id) {
        fileService.deleteFile(id);
        return success(true);
    }

    @GetMapping("/download/{path}")
    @ApiOperation("下载文件")
    @ApiImplicitParam(name = "path", value = "文件附件", required = true, dataTypeClass = MultipartFile.class)
    public void download(HttpServletResponse response, @PathVariable("path") String path) throws IOException {
        SysDbFileDO file = fileService.getFile(path);
        if (file == null) {
            log.warn("[getFile][path({}) 文件不存在]", path);
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }
        ServletUtils.writeAttachment(response, path, file.getContent());
    }

    @GetMapping("/page")
    @ApiOperation("获得文件分页")
    @PreAuthorize("@ss.hasPermission('system:db:file:query')")
    public CommonResult<PageResult<SysDbFileRespVO>> getFilePage(@Valid SysDbFilePageReqVO pageVO) {
        PageResult<SysDbFileDO> pageResult = fileService.getFilePage(pageVO);
        return success(SysDbFileConvert.INSTANCE.convertPage(pageResult));
    }

}
