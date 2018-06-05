/*
 * Copyright (C) 2016-2018 The hexsmith Authors.
 *
 *	Licensed under the Apache License, Version 2.0 (the "License");
 *	you may not use this file except in compliance with the License.
 *	You may obtain a copy of the License at
 *
 *		 http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software
 *	distributed under the License is distributed on an "AS IS" BASIS,
 *	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *	See the License for the specific language governing permissions and
 *	limitations under the License.
 */
package com.github.hexsmith.seq.range.impl.db;

import com.github.hexsmith.seq.exception.SequenceException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import javax.sql.DataSource;

/**
 * 操作DB的工具类
 *
 * @author yuzp
 * @version V1.0
 * @since 2018-06-05 14:01
 */
abstract class AbstractDbHelper {

    private static final long DELTA = 100000000L;

    /**
     * 建表SQL
     */
    private static final String SQL_CREATE_TABLE =
        "CREATE TABLE IF NOT EXISTS #tableName(" + "id bigint(20) NOT NULL AUTO_INCREMENT,"
            + "value bigint(20) NOT NULL," + "name varchar(32) NOT NULL," + "gmt_create DATETIME NOT NULL,"
            + "gmt_modified DATETIME NOT NULL," + "PRIMARY KEY (`id`),UNIQUE uk_name (`name`)" + ")";
    /**
     * 新增数据SQL
     */
    private static final String SQL_INSERT_RANGE =
        "INSERT IGNORE INTO #tableName(name,value,gmt_create,gmt_modified)" + " VALUE(?,?,?,?)";
    /**
     * 更新数据SQL
     */
    private static final String SQL_UPDATE_RANGE =
        "UPDATE #tableName SET value=?,gmt_modified=? WHERE name=? AND " + "value=?";
    /**
     * 查询数据SQL
     */
    private static final String SQL_SELECT_RANGE = "SELECT value FROM #tableName WHERE name=?";


    private static void close(AutoCloseable closeable) {
        if (null != closeable) {
            try {
                closeable.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 创建表
     * @param dataSource 数据源
     * @param tableName 表名
     */
    static void creatTable(DataSource dataSource, String tableName) {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            statement.executeUpdate(SQL_CREATE_TABLE.replace("#tableName", tableName));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(statement);
            close(connection);
        }
    }

    /**
     * 新增数据区间
     * @param dataSource 数据源
     * @param tableName 表名
     * @param rangeName 区间名
     * @param stepStart 初始位置
     */
    private static void insertRange(DataSource dataSource, String tableName, String rangeName, Long stepStart) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(SQL_INSERT_RANGE.replace("#tableName", tableName));
            preparedStatement.setString(1, rangeName);
            preparedStatement.setLong(2, stepStart);
            preparedStatement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            preparedStatement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(preparedStatement);
            close(connection);
        }
    }

    /**
     * 更新数据区间
     * @param dataSource 数据源
     * @param tableName 表名
     * @param rangeName 区间名
     * @param newValue 新区间值
     * @param oldValue 旧区间值
     * @return
     */
    static boolean updateRange(DataSource dataSource, String tableName, String rangeName, Long newValue, Long oldValue) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement(SQL_UPDATE_RANGE.replace("#tableName", tableName));
            statement.setLong(1, newValue);
            statement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            statement.setString(3, rangeName);
            statement.setLong(4, oldValue);
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(statement);
            close(connection);
        }
        return false;
    }

    /**
     * 查询区间，如果区间不存在，会新增一个区间，并返回null，由上层重新执行
     *
     * @param dataSource DB来源
     * @param tableName  来源
     * @param rangeName  区间名称
     * @param stepStart  初始位置
     * @return 区间值
     */
    static Long selectRange(DataSource dataSource, String tableName, String rangeName, long stepStart) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        long oldValue;

        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement(SQL_SELECT_RANGE.replace("#tableName", tableName));
            statement.setString(1, rangeName);
            resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                // 没有此类型数据，需要初始化
                insertRange(dataSource, tableName, rangeName, stepStart);
                return null;
            }
            oldValue = resultSet.getLong(1);
            if (oldValue < 0) {
                String msg =
                    "Sequence value cannot be less than zero, value = " + oldValue + ", please check table sequence"
                        + tableName;
                throw new SequenceException(msg);
            }
            if (oldValue > Long.MAX_VALUE - DELTA) {
                String msg =
                    "Sequence value overflow, value = " + oldValue + ", please check table sequence" + tableName;
                throw new SequenceException(msg);
            }
            return oldValue;
        } catch (SQLException e) {
            throw new SequenceException(e);
        } finally {
            close(resultSet);
            close(statement);
            close(connection);
        }
    }

}
