/*
 * Copyright (c) 2021-Now http://faasadmin.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 */

package com.faasadmin.faas.modules.admin.admin.service.tool.impl;

import cn.hutool.core.collection.CollUtil;
import com.faasadmin.faas.modules.admin.admin.framework.config.codegen.CodegenProperties;
import com.faasadmin.faas.modules.admin.admin.service.tool.ToolCodegenWebService;
import com.faasadmin.faas.modules.admin.admin.service.tool.codegen.ToolCodegenBuilder;
import com.faasadmin.faas.modules.admin.admin.service.tool.codegen.ToolCodegenEngine;
import com.faasadmin.faas.modules.admin.admin.service.tool.codegen.ToolCodegenSQLParser;
import com.faasadmin.faas.services.tool.convert.codegen.ToolCodegenConvert;
import com.faasadmin.faas.services.tool.dal.dataobject.codegen.ToolCodegenColumnDO;
import com.faasadmin.faas.services.tool.dal.dataobject.codegen.ToolCodegenTableDO;
import com.faasadmin.faas.services.tool.dal.dataobject.codegen.ToolSchemaColumnDO;
import com.faasadmin.faas.services.tool.dal.dataobject.codegen.ToolSchemaTableDO;
import com.faasadmin.faas.services.tool.dal.mysql.codegen.ToolCodegenColumnMapper;
import com.faasadmin.faas.services.tool.dal.mysql.codegen.ToolCodegenTableMapper;
import com.faasadmin.faas.services.tool.dal.mysql.codegen.ToolSchemaColumnMapper;
import com.faasadmin.faas.services.tool.dal.mysql.codegen.ToolSchemaTableMapper;
import com.faasadmin.faas.services.tool.enums.codegen.ToolCodegenImportTypeEnum;
import com.faasadmin.faas.services.tool.vo.codegen.ToolCodegenUpdateReqVO;
import com.faasadmin.faas.services.tool.vo.codegen.table.ToolCodegenTablePageReqVO;
import com.faasadmin.framework.common.pojo.PageResult;
import com.faasadmin.framework.common.utils.collection.CollectionUtils;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.KeyValue;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.faasadmin.framework.common.constant.ErrorCodeConstants.*;
import static com.faasadmin.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * ???????????? Service ?????????
 *
 * @author faasadmin
 */
@Service
public class ToolCodegenWebServiceImpl implements ToolCodegenWebService {

    @Resource
    private ToolSchemaTableMapper schemaTableMapper;
    @Resource
    private ToolSchemaColumnMapper schemaColumnMapper;
    @Resource
    private ToolCodegenTableMapper codegenTableMapper;
    @Resource
    private ToolCodegenColumnMapper codegenColumnMapper;
    @Resource
    private ToolCodegenBuilder codegenBuilder;
    @Resource
    private ToolCodegenEngine codegenEngine;
    @Resource
    private CodegenProperties codegenProperties;

    private Long createCodegen0(ToolCodegenImportTypeEnum importType,
                                ToolSchemaTableDO schemaTable, List<ToolSchemaColumnDO> schemaColumns) {
        // ?????????????????????????????????
        if (schemaTable == null) {
            throw exception(CODEGEN_IMPORT_TABLE_NULL);
        }
        if (CollUtil.isEmpty(schemaColumns)) {
            throw exception(CODEGEN_IMPORT_COLUMNS_NULL);
        }
        // ????????????????????????
        if (codegenTableMapper.selectByTableName(schemaTable.getTableName()) != null) {
            throw exception(CODEGEN_TABLE_EXISTS);
        }
        // ?????? ToolCodegenTableDO ?????????????????? DB ???
        ToolCodegenTableDO table = codegenBuilder.buildTable(schemaTable);
        table.setImportType(importType.getType());
        codegenTableMapper.insert(table);
        // ?????? ToolCodegenColumnDO ?????????????????? DB ???
        List<ToolCodegenColumnDO> columns = codegenBuilder.buildColumns(schemaColumns);
        columns.forEach(column -> {
            column.setTableId(table.getId());
            codegenColumnMapper.insert(column); // TODO ????????????
        });
        return table.getId();
    }

    @Override
    public Long createCodegenListFromSQL(String sql) {
        // ??? SQL ??????????????????????????????
        ToolSchemaTableDO schemaTable;
        List<ToolSchemaColumnDO> schemaColumns;
        try {
            KeyValue<ToolSchemaTableDO, List<ToolSchemaColumnDO>> result = ToolCodegenSQLParser.parse(sql);
            schemaTable = result.getKey();
            schemaColumns = result.getValue();
        } catch (Exception ex) {
            throw exception(CODEGEN_PARSE_SQL_ERROR);
        }
        // ??????
        return this.createCodegen0(ToolCodegenImportTypeEnum.SQL, schemaTable, schemaColumns);
    }

    @Override
    public Long createCodegen(String tableName) {
        // ????????????schema
        String tableSchema = codegenProperties.getDbSchemas().iterator().next();
        // ??????????????????????????????????????????
        ToolSchemaTableDO schemaTable = schemaTableMapper.selectByTableSchemaAndTableName(tableSchema, tableName);
        List<ToolSchemaColumnDO> schemaColumns = schemaColumnMapper.selectListByTableName(tableSchema, tableName);
        // ??????
        return this.createCodegen0(ToolCodegenImportTypeEnum.DB, schemaTable, schemaColumns);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> createCodegenListFromDB(List<String> tableNames) {
        List<Long> ids = new ArrayList<>(tableNames.size());
        // ??????????????????????????????????????????????????????????????????????????????????????????????????????
        tableNames.forEach(tableName -> ids.add(createCodegen(tableName)));
        return ids;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCodegen(ToolCodegenUpdateReqVO updateReqVO) {
        // ????????????????????????
        if (codegenTableMapper.selectById(updateReqVO.getTable().getId()) == null) {
            throw exception(CODEGEN_TABLE_NOT_EXISTS);
        }
        // ?????? table ?????????
        ToolCodegenTableDO updateTableObj = ToolCodegenConvert.INSTANCE.convert(updateReqVO.getTable());
        codegenTableMapper.updateById(updateTableObj);
        // ?????? column ????????????
        List<ToolCodegenColumnDO> updateColumnObjs = ToolCodegenConvert.INSTANCE.convertList03(updateReqVO.getColumns());
        updateColumnObjs.forEach(updateColumnObj -> codegenColumnMapper.updateById(updateColumnObj));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncCodegenFromDB(Long tableId) {
        // ????????????????????????
        ToolCodegenTableDO table = codegenTableMapper.selectById(tableId);
        if (table == null) {
            throw exception(CODEGEN_TABLE_NOT_EXISTS);
        }
        String tableSchema = codegenProperties.getDbSchemas().iterator().next();
        // ??????????????????????????????????????????
        List<ToolSchemaColumnDO> schemaColumns = schemaColumnMapper.selectListByTableName(tableSchema, table.getTableName());
        // ????????????
        this.syncCodegen0(tableId, schemaColumns);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncCodegenFromSQL(Long tableId, String sql) {
        // ????????????????????????
        ToolCodegenTableDO table = codegenTableMapper.selectById(tableId);
        if (table == null) {
            throw exception(CODEGEN_TABLE_NOT_EXISTS);
        }
        // ??? SQL ??????????????????????????????
        List<ToolSchemaColumnDO> schemaColumns;
        try {
            KeyValue<ToolSchemaTableDO, List<ToolSchemaColumnDO>> result = ToolCodegenSQLParser.parse(sql);
            schemaColumns = result.getValue();
        } catch (Exception ex) {
            throw exception(CODEGEN_PARSE_SQL_ERROR);
        }
        // ????????????
        this.syncCodegen0(tableId, schemaColumns);
    }

    private void syncCodegen0(Long tableId, List<ToolSchemaColumnDO> schemaColumns) {
        // ??????????????????????????????
        if (CollUtil.isEmpty(schemaColumns)) {
            throw exception(CODEGEN_SYNC_COLUMNS_NULL);
        }
        Set<String> schemaColumnNames = CollectionUtils.convertSet(schemaColumns, ToolSchemaColumnDO::getColumnName);
        // ?????? ToolCodegenColumnDO ?????????????????????????????????
        List<ToolCodegenColumnDO> codegenColumns = codegenColumnMapper.selectListByTableId(tableId);
        Set<String> codegenColumnNames = CollectionUtils.convertSet(codegenColumns, ToolCodegenColumnDO::getColumnName);
        // ???????????????????????????
        schemaColumns.removeIf(column -> codegenColumnNames.contains(column.getColumnName()));
        // ???????????????????????????
        Set<Long> deleteColumnIds = codegenColumns.stream().filter(column -> !schemaColumnNames.contains(column.getColumnName()))
                .map(ToolCodegenColumnDO::getId).collect(Collectors.toSet());
        if (CollUtil.isEmpty(schemaColumns) && CollUtil.isEmpty(deleteColumnIds)) {
            throw exception(CODEGEN_SYNC_NONE_CHANGE);
        }
        // ?????????????????????
        List<ToolCodegenColumnDO> columns = codegenBuilder.buildColumns(schemaColumns);
        columns.forEach(column -> {
            column.setTableId(tableId);
            codegenColumnMapper.insert(column); // TODO ????????????
        });
        // ????????????????????????
        if (CollUtil.isNotEmpty(deleteColumnIds)) {
            codegenColumnMapper.deleteBatchIds(deleteColumnIds);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCodegen(Long tableId) {
        // ????????????????????????
        if (codegenTableMapper.selectById(tableId) == null) {
            throw exception(CODEGEN_TABLE_NOT_EXISTS);
        }
        // ?????? table ?????????
        codegenTableMapper.deleteById(tableId);
        // ?????? column ????????????
        codegenColumnMapper.deleteListByTableId(tableId);
    }

    @Override
    public PageResult<ToolCodegenTableDO> getCodegenTablePage(ToolCodegenTablePageReqVO pageReqVO) {
        return codegenTableMapper.selectPage(pageReqVO);
    }

    @Override
    public ToolCodegenTableDO getCodegenTablePage(Long id) {
        return codegenTableMapper.selectById(id);
    }

    @Override
    public List<ToolCodegenTableDO> getCodeGenTableList() {
        return codegenTableMapper.selectList();
    }

    @Override
    public List<ToolCodegenColumnDO> getCodegenColumnListByTableId(Long tableId) {
        return codegenColumnMapper.selectListByTableId(tableId);
    }

    @Override
    public Map<String, String> generationCodes(Long tableId) {
        // ????????????????????????
        ToolCodegenTableDO table = codegenTableMapper.selectById(tableId);
        if (codegenTableMapper.selectById(tableId) == null) {
            throw exception(CODEGEN_TABLE_NOT_EXISTS);
        }
        List<ToolCodegenColumnDO> columns = codegenColumnMapper.selectListByTableId(tableId);
        if (CollUtil.isEmpty(columns)) {
            throw exception(CODEGEN_COLUMN_NOT_EXISTS);
        }
        // ????????????
        return codegenEngine.execute(table, columns);
    }

    @Override
    public Map<String, String> batchGenerationCodes(String[] tableIds) {
        Map<String, String> result = Maps.newHashMap();
        for (String tableName : tableIds) {
            // ????????????????????????
            ToolCodegenTableDO table = codegenTableMapper.selectByTableName(tableName);
            if (codegenTableMapper.selectByTableName(tableName) == null) {
                throw exception(CODEGEN_TABLE_NOT_EXISTS);
            }
            List<ToolCodegenColumnDO> columns = codegenColumnMapper.selectListByTableId(table.getId());
            if (CollUtil.isEmpty(columns)) {
                throw exception(CODEGEN_COLUMN_NOT_EXISTS);
            }
            // ????????????
            result.putAll(codegenEngine.execute(table, columns));
        }
        return result;
    }

    @Override
    public List<ToolSchemaTableDO> getSchemaTableList(String tableName, String tableComment) {
        List<ToolSchemaTableDO> tables = schemaTableMapper.selectList(codegenProperties.getDbSchemas(), tableName, tableComment);
        // TODO ???????????? Quartz ??????????????????????????????
        tables.removeIf(table -> table.getTableName().startsWith("QRTZ_"));
        return tables;
    }
//    /**
//     * ????????????????????????
//     *
//     * @param genTable ????????????
//     */
//    @Override
//    public void validateEdit(GenTable genTable) {
//        if (GenConstants.TPL_TREE.equals(genTable.getTplCategory())) {
//            String options = JSON.toJSONString(genTable.getParams());
//            JSONObject paramsObj = JSONObject.parseObject(options);
//            if (StringUtils.isEmpty(paramsObj.getString(GenConstants.TREE_CODE))) {
//                throw new CustomException("???????????????????????????");
//            } else if (StringUtils.isEmpty(paramsObj.getString(GenConstants.TREE_PARENT_CODE))) {
//                throw new CustomException("??????????????????????????????");
//            } else if (StringUtils.isEmpty(paramsObj.getString(GenConstants.TREE_NAME))) {
//                throw new CustomException("???????????????????????????");
//            }
//        }
//    }
}
