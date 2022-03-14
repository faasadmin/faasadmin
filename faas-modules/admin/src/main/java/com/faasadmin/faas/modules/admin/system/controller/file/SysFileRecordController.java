package com.faasadmin.faas.modules.admin.system.controller.file;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import com.faasadmin.faas.modules.admin.system.oss.OSSFactory;
import com.faasadmin.faas.business.core.Global;
import com.faasadmin.faas.business.core.support.controller.BaseController;
import com.faasadmin.faas.business.infra.file.config.FileProperties;
import com.faasadmin.faas.services.system.convert.fileRecord.SysFileRecordConvert;
import com.faasadmin.faas.services.system.dal.dataobject.fileRecord.SysFileRecordDO;
import com.faasadmin.faas.services.system.service.fileRecord.SysFileRecordService;
import com.faasadmin.framework.common.constant.Constants;
import com.faasadmin.framework.common.enums.UploadStorageTypeEnums;
import com.faasadmin.framework.common.pojo.CommonResult;
import com.faasadmin.framework.common.pojo.PageResult;
import com.faasadmin.framework.excel.core.util.ExcelUtils;
import com.faasadmin.framework.operatelog.core.annotations.OperateLog;
import com.faasadmin.framework.oss.cloud.CloudStorageService;
import com.faasadmin.framework.security.core.util.SecurityFrameworkUtils;
import com.faasadmin.faas.services.system.vo.fileRecord.*;
import com.faasadmin.framework.common.utils.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static com.faasadmin.framework.common.constant.SysErrorCodeConstants.*;
import static com.faasadmin.framework.common.pojo.CommonResult.error;
import static com.faasadmin.framework.common.pojo.CommonResult.success;
import static com.faasadmin.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Slf4j
@Api(tags = "文件")
@RestController
@RequestMapping("/system/file/record")
@Validated
public class SysFileRecordController extends BaseController {

    @Resource
    private SysFileRecordService fileService;

    @Resource
    private FileProperties fileProperties;

    /**
     * 系统上传组件上传文件接口
     * 支持上传OSS
     *
     * @param reqVO
     * @return
     */
    @ApiOperation("上传文件")
    @RequestMapping(value = "upload-base64-file", method = RequestMethod.POST)
    @ResponseBody
    @PreAuthorize("@ss.hasPermission('system:file:upload')")
    public CommonResult<SysFileUploadRespVo> uploadBase64File(@Valid @RequestBody SysUploadBase64ReqVO reqVO) {
        try {
            Long lesseeId = SecurityFrameworkUtils.getLesseeId();
            InputStream inputStream = IoUtils.baseToInputStream(reqVO.getBase64().replace("data:image/jpeg;base64,", ""));
            SysFileUploadRespVo result = new SysFileUploadRespVo();
            String attachPath = Global.getAttachPath();
            String previewUrl = null;
            //String fileName = super.getPara("name");
            //文件名称
            String fileMd5 = ToolUtils.encodingFilename(reqVO.getFileName());
            //文件相对路径
            String relativePath = "file/" + lesseeId + "/" +
                    DateFormatUtils.format(new Date(), "yyyy/MM/dd") + "/" + fileMd5 + "." + FileUploadUtils.getFileExtension(reqVO.getFileName());
            int ossType = OSSFactory.getOSSType(lesseeId);
            File file0 = null;
            SysFileRecordCreateReqVO sysFile = new SysFileRecordCreateReqVO();
            attachPath = attachPath + relativePath;
            file0 = FileUtil.touch(attachPath);
            //配置的是本地
            if (ObjectUtil.equal(ossType, UploadStorageTypeEnums.LOCAL.getId())) {
                attachPath = attachPath + relativePath;
                file0 = FileUtil.touch(attachPath);
                IoUtils.copyInputStreamToFile(inputStream, file0);
                attachPath = FileUtil.getAbsolutePath(file0);
                previewUrl = fileProperties.getBasePath();
            } else if (ObjectUtil.equal(ossType, UploadStorageTypeEnums.ALIYUN.getId())) {
                //阿里云
                previewUrl = OSSFactory.build(lesseeId).upload(inputStream, relativePath);
            }
            sysFile.setFolderId(Convert.toLong(reqVO.getFid()));
            sysFile.setFileMd5(fileMd5);
            sysFile.setFileName(reqVO.getFileName());
            sysFile.setFilePath(relativePath);
            sysFile.setClassify("0");
            sysFile.setLesseeId(lesseeId);
            sysFile.setOssType(ossType);
            double size = Base64Utils.base64FileSize(reqVO.getBase64());
            sysFile.setFileSize(new BigDecimal(size));
            Long fileId = fileService.createFile(sysFile);
            if (fileId > 0) {
                result.setId(fileId);
                result.setPath(attachPath);
                result.setFileName(reqVO.getFileName());
                result.setFileMd5(fileMd5);
                result.setFileSize(sysFile.getFileSize());
                result.setPreviewUrl(previewUrl);
                return success(result);
            } else {
                if (ObjectUtil.equal(ossType, UploadStorageTypeEnums.LOCAL.getId())) {
                    FileUtil.del(file0);
                }
                return error(FILE_UPLOAD_FAILED);
            }
        } catch (Exception e) {
            log.error("上传异常:", e);
            return error(FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 文件上传到本地
     *
     * @param file
     * @return
     */
    @ApiOperation("上传文件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "文件附件", required = true, dataTypeClass = MultipartFile.class),
            @ApiImplicitParam(name = "name", value = "文件名", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "fid", value = "文件夹", required = true, dataTypeClass = String.class),
    })
    @RequestMapping(value = "uploadLocal", method = RequestMethod.POST)
    @ResponseBody
    @PreAuthorize("@ss.hasPermission('system:file:upload')")
    public CommonResult<SysFileUploadRespVo> uploadLocal(@RequestParam("file") MultipartFile file,
                                                         @RequestParam("name") String fileName,
                                                         @RequestParam("fid") String fid) {
        try {
            Long lesseeId = SecurityFrameworkUtils.getLesseeId();
            String suffix = FileUtils.getSuffix(fileName);
            SysFileUploadRespVo result = new SysFileUploadRespVo();
            String attachPath = Global.getAttachPath();
            //文件名称
            String fileMd5 = ToolUtils.encodingFilename(fileName);
            //文件相对路径
            String relativePath = File.separator + "file" + File.separator + lesseeId + File.separator +
                    DateFormatUtils.format(new Date(), "yyyy/MM/dd") + File.separator + fileMd5 + "." + FileUploadUtils.getExtension(file);
            SysFileRecordCreateReqVO sysFile = new SysFileRecordCreateReqVO();
            attachPath = attachPath + relativePath;
            File file0 = FileUtil.touch(attachPath);
            file.transferTo(file0);
            attachPath = FileUtil.getAbsolutePath(file0);
            String previewUrl = fileProperties.getBasePath();
            sysFile.setFolderId(Convert.toLong(fid));
            sysFile.setFileMd5(fileMd5);
            sysFile.setFileName(fileName);
            sysFile.setSuffixName(suffix);
            sysFile.setFilePath(relativePath);
            sysFile.setClassify("0");
            sysFile.setFileType(sysFile.getResourceTypes());
            sysFile.setOssType(UploadStorageTypeEnums.LOCAL.getId());
            sysFile.setFileSize(new BigDecimal(file.getSize()));
            sysFile.setLesseeId(lesseeId);
            Long fileId = fileService.createFile(sysFile);
            if (fileId > 0) {
                result.setId(fileId);
                result.setPath(attachPath);
                result.setFileName(fileName);
                result.setFileMd5(fileMd5);
                result.setFileSize(sysFile.getFileSize());
                result.setPreviewUrl(previewUrl);
                return success(result);
            } else {
                FileUtil.del(file0);
                return error(FILE_UPLOAD_FAILED);
            }
        } catch (Exception e) {
            log.error("上传异常:", e);
            return error(FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 文件上传到OSS
     *
     * @param file
     * @return
     */
    @ApiOperation("上传文件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "文件附件", required = true, dataTypeClass = MultipartFile.class),
            @ApiImplicitParam(name = "name", value = "文件名", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "fid", value = "文件夹ID",  dataTypeClass = String.class),
            @ApiImplicitParam(name = "bizId", value = "业务ID",  dataTypeClass = String.class),
    })
    @RequestMapping(value = "uploadOss", method = RequestMethod.POST)
    @ResponseBody
    @PreAuthorize("@ss.hasPermission('system:file:upload')")
    public CommonResult<SysFileUploadRespVo> uploadOss(@RequestParam("file") MultipartFile file,
                                                       @RequestParam("name") String fileName,
                                                       @RequestParam("fid") String fid) {
        try {
            Long lesseeId = SecurityFrameworkUtils.getLesseeId();
            SysFileUploadRespVo result = new SysFileUploadRespVo();
            String suffix = FileUtils.getSuffix(fileName);
            //文件名称
            String fileMd5 = ToolUtils.encodingFilename(fileName);
            //文件相对路径
            String relativePath = "file/" + lesseeId + "/" +
                    DateFormatUtils.format(new Date(), "yyyy/MM/dd") + "/" + fileMd5 + "." + FileUploadUtils.getExtension(file);
            int ossType = OSSFactory.getOSSType(lesseeId);
            File file0 = null;
            SysFileRecordCreateReqVO sysFile = new SysFileRecordCreateReqVO();
            //阿里云
            String previewUrl = OSSFactory.build(lesseeId).upload(file.getInputStream(), relativePath);
            sysFile.setFolderId(Convert.toLong(fid));
            sysFile.setFileMd5(fileMd5);
            sysFile.setFileName(fileName);
            sysFile.setFilePath(relativePath);
            sysFile.setClassify("0");
            sysFile.setSuffixName(suffix);
            sysFile.setFileType(sysFile.getResourceTypes());
            sysFile.setOssType(ossType);
            sysFile.setFileSize(new BigDecimal(file.getSize()));
            sysFile.setLesseeId(lesseeId);
            Long fileId = fileService.createFile(sysFile);
            if (fileId > 0) {
                result.setId(fileId);
                result.setPath(null);
                result.setFileName(fileName);
                result.setFileMd5(fileMd5);
                result.setFileSize(sysFile.getFileSize());
                result.setPreviewUrl(previewUrl);
                return success(result);
            } else {
                if (ObjectUtil.equal(ossType, UploadStorageTypeEnums.LOCAL.getId())) {
                    FileUtil.del(file0);
                }
                return error(FILE_UPLOAD_FAILED);
            }
        } catch (Exception e) {
            log.error("上传异常:", e);
            return error(FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 系统上传组件上传文件接口
     * 支持上传OSS
     *
     * @param file
     * @return
     */
    @ApiOperation("上传文件")
    @PostMapping(value = "uploadFile")
    @ResponseBody
    @PreAuthorize("@ss.hasPermission('system:file:upload')")
    public CommonResult<SysFileUploadRespVo> uploadFile(MultipartFile file) {
        try {
            Long lesseeId = SecurityFrameworkUtils.getLesseeId();
            String fileName = file.getOriginalFilename();
            String suffix = FileUtils.getSuffix(fileName);
            SysFileUploadRespVo result = new SysFileUploadRespVo();
            String attachPath = null;
            String previewUrl = null;
            //文件名称
            String fileMd5 = ToolUtils.encodingFilename(fileName);
            int ossType = OSSFactory.getOSSType(lesseeId);
            File file0 = null;
            //文件相对路径
            String relativePath = null;
            SysFileRecordCreateReqVO sysFile = new SysFileRecordCreateReqVO();
            //配置的是本地
            if (ObjectUtil.equal(ossType, UploadStorageTypeEnums.LOCAL.getId())) {
                relativePath = File.separator + "file" + File.separator + lesseeId + File.separator +
                        DateFormatUtils.format(new Date(), "yyyy/MM/dd") + File.separator + fileMd5 + "." + FileUploadUtils.getExtension(file);
                attachPath = Global.getAttachPath() + relativePath;
                file0 = FileUtil.touch(attachPath);
                file.transferTo(file0);
                attachPath = FileUtil.getAbsolutePath(file0);
                previewUrl = fileProperties.getBasePath();
            } else if (ObjectUtil.equal(ossType, UploadStorageTypeEnums.ALIYUN.getId())) {
                relativePath = "file/" + lesseeId + "/" +
                        DateFormatUtils.format(new Date(), "yyyy/MM/dd") + "/" + fileMd5 + "." + FileUploadUtils.getExtension(file);
                //阿里云
                previewUrl = Constants.HTTPS + OSSFactory.build(lesseeId).upload(file.getInputStream(), relativePath);
            }
            sysFile.setFileMd5(fileMd5);
            sysFile.setSuffixName(suffix);
            sysFile.setFileName(fileName);
            sysFile.setFilePath(relativePath);
            sysFile.setFileType(sysFile.getResourceTypes());
            sysFile.setClassify("0");
            sysFile.setOssType(ossType);
            sysFile.setFileSize(new BigDecimal(file.getSize()));
            sysFile.setLesseeId(lesseeId);
            Long fileId = fileService.createFile(sysFile);
            if (fileId > 0) {
                result.setId(fileId);
                result.setPath(attachPath);
                result.setFileName(fileName);
                result.setFileMd5(fileMd5);
                result.setFileSize(sysFile.getFileSize());
                result.setPreviewUrl(previewUrl);
                return success(result);
            } else {
                if (ObjectUtil.equal(ossType, UploadStorageTypeEnums.LOCAL.getId())) {
                    FileUtil.del(file0);
                }
                return error(FILE_UPLOAD_FAILED);
            }
        } catch (Exception e) {
            log.error("上传异常:", e);
            return error(FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 系统上传组件上传文件接口
     * 支持上传OSS
     *
     * @param file
     * @return
     */
    @ApiOperation("上传文件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "文件附件", required = true, dataTypeClass = MultipartFile.class),
            @ApiImplicitParam(name = "name", value = "文件名", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "fid", value = "文件夹", required = true, dataTypeClass = String.class),
    })
    @RequestMapping(value = "upload", method = RequestMethod.POST)
    @ResponseBody
    @PreAuthorize("@ss.hasPermission('system:file:upload')")
    public CommonResult<SysFileUploadRespVo> upload(@RequestParam("file") MultipartFile file,
                                                    @RequestParam("name") String fileName,
                                                    @RequestParam("fid") String fid) {
        try {
            Long lesseeId = SecurityFrameworkUtils.getLesseeId();
            String suffix = FileUtils.getSuffix(fileName);
            SysFileUploadRespVo result = new SysFileUploadRespVo();
            String attachPath = null;
            String previewUrl = null;
            //文件名称
            String fileMd5 = ToolUtils.encodingFilename(fileName);
            int ossType = OSSFactory.getOSSType(lesseeId);
            File file0 = null;
            //文件相对路径
            String relativePath = null;
            SysFileRecordCreateReqVO sysFile = new SysFileRecordCreateReqVO();
            //配置的是本地
            if (ObjectUtil.equal(ossType, UploadStorageTypeEnums.LOCAL.getId())) {
                relativePath = File.separator + "file" + File.separator + lesseeId + File.separator +
                        DateFormatUtils.format(new Date(), "yyyy/MM/dd") + File.separator + fileMd5 + "." + FileUploadUtils.getExtension(file);
                attachPath = Global.getAttachPath() + relativePath;
                file0 = FileUtil.touch(attachPath);
                file.transferTo(file0);
                attachPath = FileUtil.getAbsolutePath(file0);
                previewUrl = fileProperties.getBasePath();
            } else if (ObjectUtil.equal(ossType, UploadStorageTypeEnums.ALIYUN.getId())) {
                relativePath = "file/" + lesseeId + "/" +
                        DateFormatUtils.format(new Date(), "yyyy/MM/dd") + "/" + fileMd5 + "." + FileUploadUtils.getExtension(file);
                //阿里云
                previewUrl = OSSFactory.build(lesseeId).upload(file.getInputStream(), relativePath);
            }
            if (StringUtils.isNotEmpty(fid)) {
                sysFile.setFolderId(Convert.toLong(fid));
            }
            sysFile.setFileMd5(fileMd5);
            sysFile.setSuffixName(suffix);
            sysFile.setFileName(fileName);
            sysFile.setFilePath(relativePath);
            sysFile.setLesseeId(lesseeId);
            sysFile.setFileType(sysFile.getResourceTypes());
            sysFile.setClassify("0");
            sysFile.setOssType(ossType);
            sysFile.setFileSize(new BigDecimal(file.getSize()));
            Long fileId = fileService.createFile(sysFile);
            if (fileId > 0) {
                result.setId(fileId);
                result.setPath(attachPath);
                result.setFileName(fileName);
                result.setFileMd5(fileMd5);
                result.setFileSize(sysFile.getFileSize());
                result.setPreviewUrl(previewUrl);
                return success(result);
            } else {
                if (ObjectUtil.equal(ossType, UploadStorageTypeEnums.LOCAL.getId())) {
                    FileUtil.del(file0);
                }
                return error(FILE_UPLOAD_FAILED);
            }
        } catch (Exception e) {
            log.error("上传异常:", e);
            return error(FILE_UPLOAD_FAILED);
        }
    }

    @ApiOperation("下载文件")
    @RequestMapping("download/bizId/{bizId}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "bizId", value = "业务ID", required = true, dataTypeClass = MultipartFile.class)
    })
    @PreAuthorize("@ss.hasPermission('system:file:download')")
    public void downloadBizId(HttpServletRequest request,
                              HttpServletResponse response, @PathVariable("bizId") Long bizId) {
        try {
            List<SysFileRecordDO> files = fileService.getSysFileRecordByBizId(bizId);
            if (ToolUtils.isEmpty(files)) {
                log.error("文件不存在");
                return;
            }
            SysFileRecordDO file = files.get(0);
            Integer ossType = file.getOssType();
            //获取数据库文件信息
            String relativePath = file.getFilePath();
            String fileName = file.getFileName();
            String filePath = Global.getAttachPath() + relativePath;
            InputStream inputStream = null;
            CloudStorageService cloud = null;
            //配置的是本地
            if (ObjectUtil.equal(ossType, UploadStorageTypeEnums.LOCAL.getId())) {
                if (!FileUtil.exist(filePath)) {
                    log.error("文件不存在");
                    return;
                }
                inputStream = FileUtil.getInputStream(filePath);
            } else if (ObjectUtil.equal(ossType, UploadStorageTypeEnums.ALIYUN.getId())) {
                cloud = OSSFactory.build(file.getLesseeId());
                //阿里云
                inputStream = cloud.download(relativePath);
            }
            //下载文件
            ToolUtils.fileDownload(request, response, fileName, inputStream);
            if (ObjectUtil.notEqual(ossType, UploadStorageTypeEnums.LOCAL.getId())) {
                cloud.shutdown();
            }
            return;
        } catch (Exception e) {
            log.error("下载文件异常");
            return;
        }
    }

    @ApiOperation("下载文件")
    @RequestMapping("download/fileId/{fileId}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "fileId", value = "文件ID", required = true, dataTypeClass = MultipartFile.class)
    })
    @PreAuthorize("@ss.hasPermission('system:file:download')")
    public void downloadFileId(HttpServletRequest request,
                               HttpServletResponse response, @PathVariable("fileId") Long fileId) {
        try {
            SysFileRecordDO file = fileService.getFile(fileId);
            if (ToolUtils.isEmpty(file)) {
                log.error("文件不存在");
                return;
            }
            Integer ossType = file.getOssType();
            //获取数据库文件信息
            String relativePath = file.getFilePath();
            String fileName = file.getFileName();
            String filePath = Global.getAttachPath() + relativePath;
            InputStream inputStream = null;
            CloudStorageService cloud = null;
            //配置的是本地
            if (ObjectUtil.equal(ossType, UploadStorageTypeEnums.LOCAL.getId())) {
                if (!FileUtil.exist(filePath)) {
                    log.error("文件不存在");
                    return;
                }
                inputStream = FileUtil.getInputStream(filePath);
            } else if (ObjectUtil.equal(ossType, UploadStorageTypeEnums.ALIYUN.getId())) {
                cloud = OSSFactory.build(file.getLesseeId());
                //阿里云
                inputStream = cloud.download(relativePath);
            }
            //下载文件
            ToolUtils.fileDownload(request, response, fileName, inputStream);
            if (ObjectUtil.notEqual(ossType, UploadStorageTypeEnums.LOCAL.getId())) {
                cloud.shutdown();
            }
            return;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("下载文件异常");
            return;
        }
    }

    @PostMapping("/create")
    @ApiOperation("创建文件")
    @PreAuthorize("@ss.hasPermission('system:local:file:create')")
    public CommonResult<Long> createFile(@Valid @RequestBody SysFileRecordCreateReqVO createReqVO) {
        return success(fileService.createFile(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新文件")
    @PreAuthorize("@ss.hasPermission('system:file:update')")
    public CommonResult<Boolean> updateFile(@Valid @RequestBody SysFileRecordUpdateReqVO updateReqVO) {
        fileService.updateFile(updateReqVO);
        return success(true);
    }

    @PutMapping("/simple-update")
    @ApiOperation("简单更新文件")
    @PreAuthorize("@ss.hasPermission('system:file:update')")
    public CommonResult<Boolean> simpleUpdateFile(@Valid @RequestBody SysFileRecordSimpleUpdateReqVO updateReqVO) {
        fileService.simpleUpdateFile(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除文件")
    @ApiImplicitParam(name = "id", value = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('system:local:file:delete')")
    public CommonResult<Boolean> deleteFile(@RequestParam("id") Long id) {
        fileService.deleteFile(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得文件")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('system:local:file:query')")
    public CommonResult<SysFileRecordRespVO> getFile(@RequestParam("id") Long id) {
        SysFileRecordDO file = fileService.getFile(id);
        return success(SysFileRecordConvert.INSTANCE.convert(file));
    }

    @GetMapping("/list")
    @ApiOperation("获得文件列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('system:local:file:query')")
    public CommonResult<List<SysFileRecordRespVO>> getFileList(@RequestParam("ids") Collection<Long> ids) {
        List<SysFileRecordDO> list = fileService.getFileList(ids);
        return success(SysFileRecordConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得文件分页")
    @PreAuthorize("@ss.hasPermission('system:local:file:query')")
    public CommonResult<PageResult<SysFileRecordRespVO>> getFilePage(@Valid SysFileRecordPageReqVO pageVO) {
        PageResult<SysFileRecordDO> pageResult = fileService.getFilePage(pageVO);
        return success(SysFileRecordConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出文件 Excel")
    @PreAuthorize("@ss.hasPermission('system:local:file:export')")
    @OperateLog(type = EXPORT)
    public void exportFileExcel(@Valid SysFileRecordExportReqVO exportReqVO,
                                HttpServletResponse response) throws IOException {
        List<SysFileRecordDO> list = fileService.getFileList(exportReqVO);
        // 导出 Excel
        List<SysFileRecordExcelVO> datas = SysFileRecordConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "文件.xls", "数据", SysFileRecordExcelVO.class, datas);
    }

    @PostMapping("/folder-files")
    @ApiOperation(value = "获取文件夹下的文件", notes = "获取文件夹下的文件")
    public CommonResult<PageResult<SysFolderFileListRespVo>> folderFiles(@Valid SysFolderFileListReqVo reqVO) {
        PageResult<SysFolderFileListRespVo> list = fileService.getFolderFiles(reqVO);
        return success(list);
    }

    @PostMapping("/move-files")
    @ApiOperation(value = "移动文件", notes = "移动文件")
    public CommonResult<Boolean> moveFiles(@Valid SysFolderFileMoveReqVo reqVO) {
        setLesseeId(reqVO);
        fileService.moveFiles(reqVO);
        return success(true);
    }

}
