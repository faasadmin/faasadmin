package com.faasadmin.faas.modules.admin.admin.controller.system.appstore;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.IoUtil;
import com.faasadmin.faas.business.core.module.app.service.AppManageService;
import com.faasadmin.faas.business.core.module.app.vo.AppPageReqVo;
import com.faasadmin.faas.business.core.module.system.service.file.SysFileBussService;
import com.faasadmin.faas.business.infra.file.config.FileProperties;
import com.faasadmin.framework.common.pojo.CommonResult;
import com.faasadmin.framework.common.pojo.PageResult;
import com.gitee.starblues.integration.application.PluginApplication;
import com.gitee.starblues.integration.operator.PluginOperator;
import com.gitee.starblues.integration.operator.module.PluginInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static com.faasadmin.framework.common.constant.ErrorCodeConstants.*;
import static com.faasadmin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.faasadmin.framework.common.pojo.CommonResult.success;

/**
 * @version: V1.0
 * @author: faasadmin
 * @description: 应用控制器
 * @data: 2021-08-24 19:28
 **/
@Slf4j
@Api(tags = "应用控制器")
@RestController
@RequestMapping("/system/appstore/control")
@Validated
@AllArgsConstructor
public class SysAppControlController {

    private final PluginApplication pluginApplication;

    @Resource
    private FileProperties fileProperties;

    @Resource
    private AppManageService appManageService;

    @Resource
    private SysFileBussService sysFileBussService;

    @GetMapping("/page")
    @ApiOperation("获得插件分页列表")
    @PreAuthorize("@ss.hasPermission('sys:app-control:query')")
    public CommonResult<PageResult<PluginInfo>> getPluginPage(@Valid AppPageReqVo reqVO) {
        PluginOperator pluginOperator = pluginApplication.getPluginOperator();
        List<PluginInfo> pluginInfo = pluginOperator.getPluginInfo();
        int size = pluginInfo.size();
        PageResult<PluginInfo> pageResult = new PageResult();
        pageResult.setList(pluginInfo);
        pageResult.setTotal(Convert.toLong(size));
        return success(pageResult);
    }

    /**
     * 根据插件id启动插件
     *
     * @param id 插件id
     * @return 返回操作结果
     */
    @PostMapping("/start/{id}")
    @PreAuthorize("@ss.hasPermission('sys:app-control:start')")
    @ApiOperation("启动插件")
    @ApiImplicitParam(name = "id", value = "插件id", paramType = "path", required = true)
    public CommonResult<Boolean> start(@PathVariable("id") String id) {
        try {
            PluginOperator pluginOperator = pluginApplication.getPluginOperator();
            if (pluginOperator.start(id)) {
                return success(true);
            } else {
                throw exception(PLUGIN_START_ERROR);
            }
        } catch (Exception e) {
            log.error("启动插件 '{}' 失败.", id, e);
            throw exception(PLUGIN_START_ERROR, e.getMessage());
        }
    }

    /**
     * 根据插件id停止插件
     *
     * @param id 插件id
     * @return 返回操作结果
     */
    @PostMapping("/stop/{id}")
    @PreAuthorize("@ss.hasPermission('sys:app-control:stop')")
    @ApiOperation("停止插件")
    @ApiImplicitParam(name = "id", value = "插件id", paramType = "path", required = true)
    public CommonResult<Boolean> stop(@PathVariable("id") String id) {
        try {
            PluginOperator pluginOperator = pluginApplication.getPluginOperator();
            if (pluginOperator.stop(id)) {
                return success(true);
            } else {
                throw exception(PLUGIN_STOP_ERROR);
            }
        } catch (Exception e) {
            log.error("停止插件 '{}' 失败.", id, e);
            throw exception(PLUGIN_STOP_ERROR, e.getMessage());
        }
    }

    @PostMapping("/upload")
    @ApiOperation("上传文件")
    @PreAuthorize("@ss.hasPermission('sys:app-control:upload')")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "文件附件", required = true, dataTypeClass = MultipartFile.class),
            @ApiImplicitParam(name = "path", value = "文件路径", required = false, example = "yudaoyuanma.png", dataTypeClass = String.class)
    })
    public CommonResult<String> uploadFile(@RequestParam("file") MultipartFile file,
                                           @RequestParam("path") String path) throws Exception {
        // 存储文件
        String url = fileProperties.getBasePath() + sysFileBussService.createFile(path, IoUtil.readBytes(file.getInputStream()));
        return success(url);
    }

    @PostMapping("/offlineInstall")
    @PreAuthorize("@ss.hasPermission('sys:app-control:install')")
    @ApiOperation("离线安装插件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "插件jar包文件或配置文件", dataTypeClass = MultipartFile.class, required = true),
            @ApiImplicitParam(name = "path", value = "文件路径", required = false, example = "yudaoyuanma.png", dataTypeClass = String.class)
    })
    public CommonResult<Boolean> offlineInstall(@RequestParam("file") MultipartFile file, @RequestParam("path") String path) {
        appManageService.offlineInstall(file, path);
        return success(true);
    }

    @PostMapping("/onlineInstall")
    @PreAuthorize("@ss.hasPermission('sys:app-control:install')")
    @ApiOperation("在线安装插件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "aid", value = "应用ID", example = "yudaoyuanma.png", dataTypeClass = Long.class)
    })
    public CommonResult<Boolean> onlineInstall(@RequestParam("aid") Long aid) {
        appManageService.onlineInstall(aid);
        return success(true);
    }

    /**
     * 根据插件id卸载插件
     *
     * @param id 插件id
     * @return 返回操作结果
     */
    @PostMapping("/uninstall/{id}")
    @PreAuthorize("@ss.hasPermission('plugin:control:uninstall')")
    @ApiOperation("卸载插件")
    @ApiImplicitParam(name = "id", value = "插件id", paramType = "path", required = true)
    public CommonResult<Boolean> uninstall(@PathVariable("id") String id) {
        try {
            PluginOperator pluginOperator = pluginApplication.getPluginOperator();
            if (pluginOperator.uninstall(id, true)) {
                return success(true);
            } else {
                throw exception(PLUGIN_UNINSTALL_ERROR);
            }
        } catch (Exception e) {
            log.error("卸载插件 '{}' 失败.", id, e);
            throw exception(PLUGIN_UNINSTALL_ERROR, e.getMessage());
        }
    }

}
