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
package com.github.hexsmith.seq.sequence.impl;

import com.github.hexsmith.seq.exception.SequenceException;
import com.github.hexsmith.seq.range.SequenceRange;
import com.github.hexsmith.seq.range.SequenceRangeManager;
import com.github.hexsmith.seq.sequence.RangeSequence;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 序列号区间生成器接口默认实现
 *
 * @author yuzp
 * @version V1.0
 * @since 2018-06-05 14:59
 */
public class DefaultRangeSequence implements RangeSequence {

    /**
     * 获取区间是加一把独占锁防止资源冲突
     */
    private final Lock lock = new ReentrantLock();

    /**
     * 序列号区间管理器
     */
    private SequenceRangeManager sequenceRangeManager;

    /**
     * 当前序列号区间
     */
    private volatile SequenceRange currentRange;

    /**
     * 需要获取的区间名称
     */
    private String rangeName;

    /**
     * 设置区间管理器
     *
     * @param seqRangeMgr 区间管理器
     */
    @Override
    public void setSeqRangeMgr(SequenceRangeManager seqRangeMgr) {
        this.sequenceRangeManager = seqRangeMgr;
    }

    /**
     * 设置获取序列号名称
     *
     * @param rangeName 名称
     */
    @Override
    public void setRangeName(String rangeName) {
        this.rangeName = rangeName;
    }

    /**
     * 生成下一个序列号
     *
     * @return 序列号
     * @throws com.github.hexsmith.seq.exception.SequenceException 序列号生成异常
     */
    @Override
    public long nextValue() throws SequenceException {
        // 当前区间不存在，重新获取一个区间
        if (null == currentRange) {
            lock.lock();
            try {
                if (null == currentRange) {
                    currentRange = sequenceRangeManager.nextRange(rangeName);
                }
            } finally {
                lock.unlock();
            }
        }
        // 当value值为-1时，表明区间的序列号已经分配完，需要重新获取区间
        long value = currentRange.getAndIncrement();
        if (value == -1) {
            lock.lock();
            try {
                for (; ; ) {
                    if (currentRange.isRangeOver()) {
                        currentRange = sequenceRangeManager.nextRange(rangeName);
                    }
                    value = currentRange.getAndIncrement();
                    if (value == -1) {
                        continue;
                    }
                    break;
                }
            } finally {
                lock.unlock();
            }
        }
        if (value < 0) {
            throw new SequenceException("Sequence value overflow, value = " + value);
        }
        return value;
    }
}
