package com.faasadmin.faas.modules.admin.admin.controller.infra.job;

import com.faasadmin.faas.services.infra.convert.job.InfraJobConvert;
import com.faasadmin.faas.services.infra.dal.dataobject.job.InfraJobDO;
import com.faasadmin.faas.services.infra.service.job.InfraJobService;
import com.faasadmin.faas.services.infra.vo.job.job.*;
import com.faasadmin.framework.common.pojo.CommonResult;
import com.faasadmin.framework.common.pojo.PageResult;
import com.faasadmin.framework.excel.core.util.ExcelUtils;
import com.faasadmin.framework.job.core.util.CronUtils;
import com.faasadmin.framework.operatelog.core.annotations.OperateLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.quartz.SchedulerException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.faasadmin.framework.common.pojo.CommonResult.success;
import static com.faasadmin.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Api(tags = "定时任务")
@RestController
@RequestMapping("/infra/job")
@Validated
public class InfraJobController {

    @Resource
    private InfraJobService infraJobService;

    @PostMapping("/create")
    @ApiOperation("创建定时任务")
    @PreAuthorize("@ss.hasPermission('infra:job:create')")
    public CommonResult<Long> createJob(@Valid @RequestBody InfraJobCreateReqVO createReqVO) throws SchedulerException {
        return success(infraJobService.createJob(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新定时任务")
    @PreAuthorize("@ss.hasPermission('infra:job:update')")
    public CommonResult<Boolean> updateJob(@Valid @RequestBody InfraJobUpdateReqVO updateReqVO) throws SchedulerException {
        infraJobService.updateJob(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @ApiOperation("更新定时任务的状态")
    @ApiImplicitParams({@ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class), @ApiImplicitParam(name = "status", value = "状态", required = true, example = "1", dataTypeClass = Integer.class),})
    @PreAuthorize("@ss.hasPermission('infra:job:update')")
    public CommonResult<Boolean> updateJobStatus(@RequestParam(value = "id") Long id, @RequestParam("status") Integer status) throws SchedulerException {
        infraJobService.updateJobStatus(id, status);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除定时任务")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('infra:job:delete')")
    public CommonResult<Boolean> deleteJob(@RequestParam("id") Long id) throws SchedulerException {
        infraJobService.deleteJob(id);
        return success(true);
    }

    @PutMapping("/trigger")
    @ApiOperation("触发定时任务")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('infra:job:trigger')")
    public CommonResult<Boolean> triggerJob(@RequestParam("id") Long id) throws SchedulerException {
        infraJobService.triggerJob(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得定时任务")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('infra:job:query')")
    public CommonResult<InfraJobRespVO> getJob(@RequestParam("id") Long id) {
        InfraJobDO job = infraJobService.getJob(id);
        return success(InfraJobConvert.INSTANCE.convert(job));
    }

    @GetMapping("/list")
    @ApiOperation("获得定时任务列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('infra:job:query')")
    public CommonResult<List<InfraJobRespVO>> getJobList(@RequestParam("ids") Collection<Long> ids) {
        List<InfraJobDO> list = infraJobService.getJobList(ids);
        return success(InfraJobConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得定时任务分页")
    @PreAuthorize("@ss.hasPermission('infra:job:query')")
    public CommonResult<PageResult<InfraJobRespVO>> getJobPage(@Valid InfraJobPageReqVO pageVO) {
        PageResult<InfraJobDO> pageResult = infraJobService.getJobPage(pageVO);
        return success(InfraJobConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出定时任务 Excel")
    @PreAuthorize("@ss.hasPermission('infra:job:export')")
    @OperateLog(type = EXPORT)
    public void exportJobExcel(@Valid InfraJobExportReqVO exportReqVO, HttpServletResponse response) throws IOException {
        List<InfraJobDO> list = infraJobService.getJobList(exportReqVO);
        // 导出 Excel
        List<InfraJobExcelVO> datas = InfraJobConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "定时任务.xls", "数据", InfraJobExcelVO.class, datas);
    }

    @GetMapping("/get_next_times")
    @ApiOperation("获得定时任务的下 n 次执行时间")
    @ApiImplicitParams({@ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class), @ApiImplicitParam(name = "count", value = "数量", example = "5", dataTypeClass = Long.class)})
    @PreAuthorize("@ss.hasPermission('infra:job:query')")
    public CommonResult<List<Date>> getJobNextTimes(@RequestParam("id") Long id, @RequestParam(value = "count", required = false, defaultValue = "5") Integer count) {
        InfraJobDO job = infraJobService.getJob(id);
        if (job == null) {
            return success(Collections.emptyList());
        }
        return success(CronUtils.getNextTimes(job.getCronExpression(), count));
    }

}
