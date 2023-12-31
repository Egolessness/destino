/*
 * Copyright (c) 2023 by Kang Wang. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.egolessness.destino.core.storage.sql;

import org.egolessness.destino.common.model.Page;
import org.egolessness.destino.common.model.Pageable;
import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.core.exception.SnapshotException;
import org.egolessness.destino.core.model.Condition;
import org.egolessness.destino.core.storage.SnapshotProcessorAware;
import org.egolessness.destino.core.support.PageSupport;

import java.sql.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * local data source (H2).
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class H2Storage implements SqlStorage {

    private final static String SELECT_SQL_TEMPLATE = "SELECT * FROM {0} WHERE {1};";

    private final static String PAGE_SQL_TEMPLATE = "SELECT * FROM {0} WHERE {1} {2} LIMIT ? OFFSET ?;";

    private final static String COUNT_SQL_TEMPLATE = "SELECT COUNT(1) FROM {0} WHERE {1};";

    private final static String INSERT_SQL_TEMPLATE = "INSERT INTO {0} ({1}) VALUES ({2});";

    private final static String MERGE_SQL_TEMPLATE = "MERGE INTO {0} USING DUAL ON ({1}) " +
            "WHEN NOT MATCHED THEN INSERT ({3}) VALUES ({4}) WHEN MATCHED {2} THEN UPDATE SET {5};";

    private final static String DELETE_SQL_TEMPLATE = "DELETE FROM {0} WHERE {1};";

    private final H2Context context;

    private final String tableName;

    public H2Storage(H2Context context, String tableName) {
        this.context = context;
        this.tableName = tableName;
    }

    @Override
    public String snapshotName() {
        return tableName;
    }

    @Override
    public String snapshotSource() {
        return context.getDbDir();
    }

    @Override
    public void snapshotSave(String path) throws SnapshotException {
        context.getSnapshotOperation().snapshotSave(path);
    }

    @Override
    public void snapshotLoad(String path) throws SnapshotException {
        context.getSnapshotOperation().snapshotLoad(path);
    }

    @Override
    public ResultSet select(String where) throws SQLException {
        String sql = MessageFormat.format(SELECT_SQL_TEMPLATE, tableName, where);

        Connection connection = context.getConnection();
        Statement statement = connection.createStatement();
        statement.setEscapeProcessing(false);
        statement.execute(sql);

        return statement.getResultSet();
    }

    @Override
    public <T> Page<T> page(List<Condition> conditions, String orderBy, Pageable pageable, Function<ResultSet, T> mappingFunc) throws SQLException {
        Page<T> page = new Page<>();
        page.setPage(pageable.getPage());
        page.setSize(pageable.getSize());

        int offset = PageSupport.getStart(pageable);
        String where = buildWhereSql(conditions);
        String countSql = MessageFormat.format(COUNT_SQL_TEMPLATE, tableName, where);
        String limitSql = MessageFormat.format(PAGE_SQL_TEMPLATE, tableName, where, orderBy, pageable.getSize(), offset);

        Connection connection = context.getConnection();
        if (connection == null) {
            return Page.empty();
        }

        PreparedStatement countStatement = connection.prepareStatement(countSql);
        setWhereValue(countStatement, conditions);
        boolean executed = countStatement.execute();

        ResultSet resultSet = countStatement.getResultSet();
        if (executed && resultSet.next()) {
            page.setTotal(resultSet.getInt(1));
        }

        if (page.getTotal() <= 0) {
            return Page.empty();
        }

        PreparedStatement limitStatement = connection.prepareStatement(limitSql);
        int index =  setWhereValue(limitStatement, conditions);
        limitStatement.setInt(index + 1, pageable.getSize());
        limitStatement.setInt(index + 2, offset);
        executed = limitStatement.execute();
        List<T> records = new ArrayList<>(pageable.getSize());
        if (executed) {
            ResultSet dataResultSet = limitStatement.getResultSet();
            while (dataResultSet.next()) {
                records.add(mappingFunc.apply(dataResultSet));
            }
        }
        page.setRecords(records);

        return page;
    }

    private String buildWhereSql(List<Condition> conditions) {
        if (PredicateUtils.isEmpty(conditions)) {
            return "1=1";
        }
        return conditions.stream().map(condition -> {
                    if (condition.getValue() instanceof Object[]) {
                        String ps = Arrays.stream(((Object[]) condition.getValue())).map(v -> "?")
                                .collect(Collectors.joining(","));
                        return condition.getKey() + " " + condition.getExp() + " (" + ps + ")";
                    }
                    return condition.getKey() + " " + condition.getExp() + " ?";
                })
                .collect(Collectors.joining(" AND "));
    }

    private int setWhereValue(PreparedStatement statement, List<Condition> conditions) throws SQLException {
        int index = 0;
        for (Condition condition : conditions) {
            if (condition.getValue() instanceof Object[]) {
                Object[] value = (Object[]) condition.getValue();
                for (Object v : value) {
                    statement.setObject(++ index, v);
                }
            } else {
                statement.setObject(++ index, condition.getValue());
            }
        }
        return index;
    }

    @Override
    public long count(String where) throws SQLException {
        String sql = MessageFormat.format(COUNT_SQL_TEMPLATE, tableName, where);

        Connection connection = context.getConnection();
        if (connection == null) {
            return 0;
        }

        Statement statement = connection.createStatement();
        statement.execute(sql);

        ResultSet resultSet = statement.getResultSet();
        if (resultSet.next()) {
            return resultSet.getLong(1);
        }
        return 0;
    }

    @Override
    public void reload(List<Map<String, Object>> list) throws SQLException {
        Connection connection = context.getConnection();
        if (connection == null) {
            return;
        }

        String deleteSql = MessageFormat.format(DELETE_SQL_TEMPLATE, tableName, "1=1");
        connection.createStatement().execute(deleteSql);

        for (Map<String, Object> data : list) {
            Iterator<Map.Entry<String, Object>> iterator = data.entrySet().iterator();
            if (!iterator.hasNext()) {
                return;
            }

            StringBuilder columnsBuilder = new StringBuilder();
            StringBuilder valuesBuilder = new StringBuilder();
            List<Object> valueList = new ArrayList<>(data.size());

            Map.Entry<String, Object> entry = iterator.next();
            columnsBuilder.append(entry.getKey());
            valuesBuilder.append("?");
            valueList.add(entry.getValue());

            while (iterator.hasNext()) {
                entry = iterator.next();
                columnsBuilder.append(",").append(entry.getKey());
                valuesBuilder.append(",").append("?");
                valueList.add(entry.getValue());
            }

            String sql = MessageFormat.format(INSERT_SQL_TEMPLATE, tableName, columnsBuilder, valuesBuilder);
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            for (int i = 0; i < valueList.size(); i++) {
                preparedStatement.setObject(i + 1, valueList.get(i));
            }
            preparedStatement.execute();
        }

        connection.commit();
    }

    @Override
    public void merge(Map<String, Object> data, String matched, String where) throws SQLException {
        Iterator<Map.Entry<String, Object>> iterator = data.entrySet().iterator();
        if (!iterator.hasNext()) {
            return;
        }

        StringBuilder columnsBuilder = new StringBuilder();
        StringBuilder valuesBuilder = new StringBuilder();
        StringBuilder updateBuilder = new StringBuilder();
        List<Object> valueList = new ArrayList<>(data.size());

        Map.Entry<String, Object> entry = iterator.next();
        columnsBuilder.append(entry.getKey());
        valuesBuilder.append("?");
        valueList.add(entry.getValue());
        updateBuilder.append(entry.getKey()).append(" = ").append("?");

        while (iterator.hasNext()) {
            entry = iterator.next();
            columnsBuilder.append(",").append(entry.getKey());
            valuesBuilder.append(",").append("?");
            updateBuilder.append(", ").append(entry.getKey()).append(" = ").append("?");
            valueList.add(entry.getValue());
        }

        String sql = MessageFormat.format(MERGE_SQL_TEMPLATE, tableName, matched, where, columnsBuilder, valuesBuilder, updateBuilder);

        Connection connection = context.getConnection();
        if (connection == null) {
            return;
        }

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        for (int i = 0; i < valueList.size(); i++) {
            preparedStatement.setObject(i + 1, valueList.get(i));
            preparedStatement.setObject(i + 1 + valueList.size(), valueList.get(i));
        }
        preparedStatement.execute();
    }

    @Override
    public void delete(List<Condition> conditions) throws SQLException {
        String where = buildWhereSql(conditions);
        String sql = MessageFormat.format(DELETE_SQL_TEMPLATE, tableName, where);
        Connection connection = context.getConnection();
        if (connection == null) {
            return;
        }

        PreparedStatement statement = connection.prepareStatement(sql);
        setWhereValue(statement, conditions);
        statement.execute();
        connection.commit();
    }

    @Override
    public SnapshotProcessorAware getSnapshotProcessorAware() {
        return context.getSnapshotProcessorAware();
    }

}