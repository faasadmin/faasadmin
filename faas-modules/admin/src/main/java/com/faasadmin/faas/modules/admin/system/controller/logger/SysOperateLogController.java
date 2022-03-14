package com.faasadmin.faas.modules.admin.system.controller.logger;

import com.faasadmin.faas.business.core.module.system.service.logger.SysOperateLogService;
import com.faasadmin.faas.services.system.convert.logger.SysOperateLogConvert;
import com.faasadmin.faas.services.system.dal.dataobject.logger.SysOperateLogDO;
import com.faasadmin.faas.services.system.dal.dataobject.user.SysUserDO;
import com.faasadmin.faas.services.system.service.user.SysUserService;
import com.faasadmin.faas.services.system.vo.logger.operatelog.SysOperateLogExcelVO;
import com.faasadmin.faas.services.system.vo.logger.operatelog.SysOperateLogExportReqVO;
import com.faasadmin.faas.services.system.vo.logger.operatelog.SysOperateLogPageReqVO;
import com.faasadmin.faas.services.system.vo.logger.operatelog.SysOperateLogRespVO;
import com.faasadmin.framework.common.pojo.CommonResult;
import com.faasadmin.framework.common.pojo.PageResult;
import com.faasadmin.framework.common.utils.collection.CollectionUtils;
import com.faasadmin.framework.common.utils.collection.MapUtils;
import com.faasadmin.framework.excel.core.util.ExcelUtils;
import com.faasadmin.framework.operatelog.core.annotations.OperateLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.faasadmin.framework.common.pojo.CommonResult.success;
import static com.faasadmin.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Api(tags = "操作日志")
@RestController
@RequestMapping("/system/operate-log")
@Validated
public class SysOperateLogController {

    @Resource
    private SysOperateLogService operateLogService;
    @Resource
    private SysUserService userService;

    @GetMapping("/page")
    @ApiOperation("查看操作日志分页列表")
    @PreAuthorize("@ss.hasPermission('system:operate-log:query')")
    public CommonResult<PageResult<SysOperateLogRespVO>> pageOperateLog(@Valid SysOperateLogPageReqVO reqVO) {
        PageResult<SysOperateLogDO> pageResult = operateLogService.getOperateLogPage(reqVO);

        // 获得拼接需要的数据
        Collection<Long> userIds = CollectionUtils.convertList(pageResult.getList(), SysOperateLogDO::getUserId);
        Map<Long, SysUserDO> userMap = userService.getUserMap(userIds);
        // 拼接数据
        List<SysOperateLogRespVO> list = new ArrayList<>(pageResult.getList().size());
        pageResult.getList().forEach(operateLog -> {
            SysOperateLogRespVO respVO = SysOperateLogConvert.INSTANCE.convert(operateLog);
            list.add(respVO);
            // 拼接用户信息
            MapUtils.findAndThen(userMap, operateLog.getUserId(), user -> respVO.setUserNickname(user.getNickName()));
        });
        return success(new PageResult<>(list, pageResult.getTotal()));
    }

    @ApiOperation("导出操作日志")
    @GetMapping("/export")
    @PreAuthorize("@ss.hasPermission('system:operate-log:export')")
    @OperateLog(type = EXPORT)
    public void exportOperateLog(HttpServletResponse response, @Valid SysOperateLogExportReqVO reqVO) throws IOException {
        List<SysOperateLogDO> list = operateLogService.getOperateLogs(reqVO);

        // 获得拼接需要的数据
        Collection<Long> userIds = CollectionUtils.convertList(list, SysOperateLogDO::getUserId);
        Map<Long, SysUserDO> userMap = userService.getUserMap(userIds);
        // 拼接数据
        List<SysOperateLogExcelVO> excelDataList = SysOperateLogConvert.INSTANCE.convertList(list, userMap);
        // 输出
        ExcelUtils.write(response, "操作日志.xls", "数据列表", SysOperateLogExcelVO.class, excelDataList);
    }

}
