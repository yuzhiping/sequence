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
import com.github.hexsmith.seq.range.SequenceRange;
import com.github.hexsmith.seq.range.SequenceRangeManager;

import javax.sql.DataSource;

/**
 * DB区间管理器实现
 *
 * @author yuzp
 * @version V1.0
 * @since 2018-06-05 14:36
 */
public class DbSequenceRange implements SequenceRangeManager {

    /**
     * 表名前缀，为防止数据库表名冲突，默认带上这个前缀
     */
    private final static String TABLE_NAME_PREFIX = "sequence_";

    /**
     * 区间步长
     */
    private int  rangeStep = 1000;
    /**
     * 区间起始位置，真实从stepStart+1开始
     */
    private long stepStart  = 0;
    /**
     * 获取区间失败重试次数
     */
    private int  retryTimes = 100;
    /**
     * DB来源
     */
    private DataSource dataSource;
    /**
     * 表名，默认range
     */
    private String tableName = "range";


    /**
     * 获取指定区间名的下一个区间
     *
     * @param rangeName 区间名
     * @return 返回区间
     * @throws com.github.hexsmith.seq.exception.SequenceException 异常
     */
    @Override
    public SequenceRange nextRange(String rangeName) throws SequenceException {
        if (isEmpty(rangeName)) {
            throw new SecurityException("[DbSeqRangeMgr-nextRange] name is empty.");
        }
        Long oldValue;
        Long newValue;
        for (int i = 0; i < getRetryTimes(); i++) {
            oldValue = AbstractDbHelper.selectRange(getDataSource(), getRealTableName(), rangeName, getStepStart());
            if (null == oldValue) {
                // 区间不存在，重试
                continue;
            }
            newValue = oldValue + getRangeStep();
            if (AbstractDbHelper.updateRange(getDataSource(), getRealTableName(), rangeName, newValue, oldValue)) {
                return new SequenceRange(oldValue + 1, newValue);
            }
            //else 失败重试
        }
        throw new SequenceException("Retried too many times, retryTimes = " + getRetryTimes());
    }

    /**
     * 初始化
     */
    @Override
    public void init() {
        checkParam();
        AbstractDbHelper.creatTable(getDataSource(), getRealTableName());
    }

    private boolean isEmpty(String str) {
        return null == str || str.trim().length() == 0;
    }

    private String getRealTableName() {
        return TABLE_NAME_PREFIX + getTableName();
    }

    private void checkParam() {
        if (rangeStep <= 0) {
            throw new SecurityException("[DbSequenceRange-checkParam] step must greater than 0.");
        }
        if (stepStart < 0) {
            throw new SecurityException("[DbSequenceRange-setStepStart] stepStart < 0.");
        }
        if (retryTimes <= 0) {
            throw new SecurityException("[DbSequenceRange-setRetryTimes] retryTimes must greater than 0.");
        }
        if (null == dataSource) {
            throw new SecurityException("[DbSequenceRange-setDataSource] dataSource is null.");
        }
        if (isEmpty(tableName)) {
            throw new SecurityException("[DbSequenceRange-setTableName] tableName is empty.");
        }
    }

    public int getRangeStep() {
        return rangeStep;
    }

    public void setRangeStep(int rangeStep) {
        this.rangeStep = rangeStep;
    }

    public long getStepStart() {
        return stepStart;
    }

    public void setStepStart(long stepStart) {
        this.stepStart = stepStart;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
