/*
 * Copyright (c) 2021-Now http://faasadmin.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 */

package com.faasadmin.faas.modules.admin.admin.service.tool.impl;

import com.faasadmin.faas.modules.admin.admin.service.tool.ToolTestDemoWebService;
import com.faasadmin.faas.services.tool.convert.test.ToolTestDemoConvert;
import com.faasadmin.faas.services.tool.dal.dataobject.test.ToolTestDemoDO;
import com.faasadmin.faas.services.tool.dal.mysql.test.ToolTestDemoMapper;
import com.faasadmin.faas.services.tool.vo.test.ToolTestDemoCreateReqVO;
import com.faasadmin.faas.services.tool.vo.test.ToolTestDemoExportReqVO;
import com.faasadmin.faas.services.tool.vo.test.ToolTestDemoPageReqVO;
import com.faasadmin.faas.services.tool.vo.test.ToolTestDemoUpdateReqVO;
import com.faasadmin.framework.common.pojo.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static com.faasadmin.framework.common.constant.ErrorCodeConstants.TEST_DEMO_NOT_EXISTS;
import static com.faasadmin.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 字典类型 Service 实现类
 *
 * @author faasadmin
 */
@Service
@Validated
public class ToolTestDemoWebServiceImpl implements ToolTestDemoWebService {

    @Resource
    private ToolTestDemoMapper testDemoMapper;

    @Override
    public Long createTestDemo(ToolTestDemoCreateReqVO createReqVO) {
        // 插入
        ToolTestDemoDO testDemo = ToolTestDemoConvert.INSTANCE.convert(createReqVO);
        testDemoMapper.insert(testDemo);
        // 返回
        return testDemo.getId();
    }

    @Override
    public void updateTestDemo(ToolTestDemoUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateTestDemoExists(updateReqVO.getId());
        // 更新
        ToolTestDemoDO updateObj = ToolTestDemoConvert.INSTANCE.convert(updateReqVO);
        testDemoMapper.updateById(updateObj);
    }

    @Override
    public void deleteTestDemo(Long id) {
        // 校验存在
        this.validateTestDemoExists(id);
        // 删除
        testDemoMapper.deleteById(id);
    }

    private void validateTestDemoExists(Long id) {
        if (testDemoMapper.selectById(id) == null) {
            throw exception(TEST_DEMO_NOT_EXISTS);
        }
    }

    @Override
    public ToolTestDemoDO getTestDemo(Long id) {
        return testDemoMapper.selectById(id);
    }

    @Override
    public List<ToolTestDemoDO> getTestDemoList(Collection<Long> ids) {
        return testDemoMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<ToolTestDemoDO> getTestDemoPage(ToolTestDemoPageReqVO pageReqVO) {
        return testDemoMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ToolTestDemoDO> getTestDemoList(ToolTestDemoExportReqVO exportReqVO) {
        return testDemoMapper.selectList(exportReqVO);
    }

}
