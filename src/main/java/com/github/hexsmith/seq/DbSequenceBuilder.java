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
package com.github.hexsmith.seq;

import com.github.hexsmith.seq.range.impl.db.DbSequenceRange;
import com.github.hexsmith.seq.sequence.Sequence;
import com.github.hexsmith.seq.sequence.impl.DefaultRangeSequence;

import javax.sql.DataSource;

/**
 * 基于DB取步长，序列号生成器构建者
 *
 * @author yuzp
 * @version V1.0
 * @since 2018-06-05 15:20
 */
public class DbSequenceBuilder implements SequenceBuilder {

    /**
     * 数据库数据源[必选]
     */
    private DataSource dataSource;

    /**
     * 业务名称[必选]
     */
    private String bizName;
    /**
     * 存放序列号步长的表[可选：默认：sequence]
     */
    private String tableName  = "sequence";
    /**
     * 并发是数据使用了乐观策略，这个是失败重试的次数[可选：默认：100]
     */
    private int    retryTimes = 100;
    /**
     * 获取range步长[可选：默认：1000]
     */
    private int    step       = 1000;

    /**
     * 构建一个序列号生成器
     *
     * @return 序列号生成器
     */
    @Override
    public Sequence build() {
        //利用DB获取区间管理器
        DbSequenceRange dbSeqRangeMgr = new DbSequenceRange();
        dbSeqRangeMgr.setDataSource(this.dataSource);
        dbSeqRangeMgr.setTableName(this.tableName);
        dbSeqRangeMgr.setRetryTimes(this.retryTimes);
        dbSeqRangeMgr.setRangeStep(this.step);
        dbSeqRangeMgr.init();
        //构建序列号生成器
        DefaultRangeSequence sequence = new DefaultRangeSequence();
        sequence.setRangeName(this.bizName);
        sequence.setSeqRangeMgr(dbSeqRangeMgr);
        return sequence;
    }

    public static DbSequenceBuilder create() {
        return new DbSequenceBuilder();
    }

    public DbSequenceBuilder dataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        return this;
    }

    public DbSequenceBuilder tableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public DbSequenceBuilder retryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
        return this;
    }

    public DbSequenceBuilder step(int step) {
        this.step = step;
        return this;
    }

    public DbSequenceBuilder bizName(String bizName) {
        this.bizName = bizName;
        return this;
    }

}
