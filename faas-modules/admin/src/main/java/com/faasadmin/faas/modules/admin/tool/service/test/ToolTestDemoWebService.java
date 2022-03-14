package com.faasadmin.faas.modules.admin.tool.service.test;

import com.faasadmin.faas.services.tool.dal.dataobject.test.ToolTestDemoDO;
import com.faasadmin.faas.services.tool.vo.test.ToolTestDemoCreateReqVO;
import com.faasadmin.faas.services.tool.vo.test.ToolTestDemoExportReqVO;
import com.faasadmin.faas.services.tool.vo.test.ToolTestDemoPageReqVO;
import com.faasadmin.faas.services.tool.vo.test.ToolTestDemoUpdateReqVO;
import com.faasadmin.framework.common.pojo.PageResult;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 字典类型 Service 接口
 *
 * @author faasadmin
 */
public interface ToolTestDemoWebService {

    /**
     * 创建字典类型
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTestDemo(@Valid ToolTestDemoCreateReqVO createReqVO);

    /**
     * 更新字典类型
     *
     * @param updateReqVO 更新信息
     */
    void updateTestDemo(@Valid ToolTestDemoUpdateReqVO updateReqVO);

    /**
     * 删除字典类型
     *
     * @param id 编号
     */
    void deleteTestDemo(Long id);

    /**
     * 获得字典类型
     *
     * @param id 编号
     * @return 字典类型
     */
    ToolTestDemoDO getTestDemo(Long id);

    /**
     * 获得字典类型列表
     *
     * @param ids 编号
     * @return 字典类型列表
     */
    List<ToolTestDemoDO> getTestDemoList(Collection<Long> ids);

    /**
     * 获得字典类型分页
     *
     * @param pageReqVO 分页查询
     * @return 字典类型分页
     */
    PageResult<ToolTestDemoDO> getTestDemoPage(ToolTestDemoPageReqVO pageReqVO);

    /**
     * 获得字典类型列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 字典类型列表
     */
    List<ToolTestDemoDO> getTestDemoList(ToolTestDemoExportReqVO exportReqVO);

}
