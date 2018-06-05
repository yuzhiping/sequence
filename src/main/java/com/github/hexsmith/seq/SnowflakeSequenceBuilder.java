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

import com.github.hexsmith.seq.sequence.Sequence;
import com.github.hexsmith.seq.sequence.impl.SnowflakeSequence;

/**
 * 基于雪花算法，序列号生成器构建者
 *
 * @author yuzp
 * @version V1.0
 * @since 2018-06-05 15:32
 */
public class SnowflakeSequenceBuilder implements SequenceBuilder {

    /**
     * 数据中心ID，值的范围在[0,31]之间，一般可以设置机房的IDC[必选]
     */
    private long dataCenterId;
    /**
     * 工作机器ID，值的范围在[0,31]之间，一般可以设置机器编号[必选]
     */
    private long workerId;

    /**
     * 构建一个序列号生成器
     *
     * @return 序列号生成器
     */
    @Override
    public Sequence build() {
        SnowflakeSequence sequence = new SnowflakeSequence();
        sequence.setDataCenterId(this.dataCenterId);
        sequence.setWorkerId(this.workerId);
        return sequence;
    }

    public static SnowflakeSequenceBuilder create() {
        return new SnowflakeSequenceBuilder();
    }

    public SnowflakeSequenceBuilder dataCenterId(long dataCenterId) {
        this.dataCenterId = dataCenterId;
        return this;
    }

    public SnowflakeSequenceBuilder workerId(long workerId) {
        this.workerId = workerId;
        return this;
    }

}
