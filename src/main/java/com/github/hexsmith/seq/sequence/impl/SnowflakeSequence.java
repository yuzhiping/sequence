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
import com.github.hexsmith.seq.sequence.Sequence;

/**
 * 基于雪花算法的序列号生成器实现
 * 一个long类型的数据，64位。以下是每位的具体含义。
 * <br>
 * snowflake的结构如下(每部分用-分开):
 * <br>
 * 0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000
 * <br>
 * （1）第一位为未使用
 * （2）接下来的41位为毫秒级时间(41位的长度可以使用69年)
 * （3）然后是5位dataCenterId
 * （4）5位workerId
 * （5）最后12位是毫秒内的计数（12位的计数顺序号支持每个节点每毫秒产生4096个ID序号）
 * <br>
 * 一共加起来刚好64位，为一个Long型。(转换成字符串长度为18)
 * @author yuzp
 * @version V1.0
 * @since 2018-06-05 15:09
 */
public class SnowflakeSequence implements Sequence {

    /**
     * 开始时间截 (2018-01-01)
     */
    private final long twepoch = 1514736000000L;

    /**
     * 机器id所占的位数
     */
    private final long workerIdBits = 5L;

    /**
     * 数据标识id所占的位数
     */
    private final long dataCenterIdBits = 5L;

    /**
     * 支持的最大机器id，结果是31 (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数)
     */
    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);

    /**
     * 支持的最大数据标识id，结果是31
     */
    private final long maxDataCenterId = -1L ^ (-1L << dataCenterIdBits);

    /**
     * 序列在id中占的位数
     */
    private final long sequenceBits = 12L;

    /**
     * 机器ID向左移12位
     */
    private final long workerIdShift = sequenceBits;

    /**
     * 数据标识id向左移17位(12+5)
     */
    private final long dataCenterIdShift = sequenceBits + workerIdBits;

    /**
     * 时间截向左移22位(5+5+12)
     */
    private final long timestampLeftShift = sequenceBits + workerIdBits + dataCenterIdBits;

    /**
     * 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095)
     */
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    /**
     * 工作机器ID(0~31)
     */
    private long workerId;

    /**
     * 数据中心ID(0~31)
     */
    private long dataCenterId;

    /**
     * 毫秒内序列(0~4095)
     */
    private long sequence = 0L;

    /**
     * 上次生成ID的时间截
     */
    private long lastTimestamp = -1L;

    /**
     * 生成下一个序列号
     *
     * @return 序列号
     * @throws com.github.hexsmith.seq.exception.SequenceException 序列号生成异常
     */
    @Override
    public synchronized long nextValue() throws SequenceException {
        long timestamp = timeGen();
        // 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
        if (timestamp < lastTimestamp) {
            throw new SequenceException("[SnowflakeSequence-nextValue] 当前时间小于上次生成序列号的时间，时间被回退了，请确认服务器时间的设置.");
        }
        // 如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            // 毫秒内序列溢出
            if (sequence == 0) {
                // 阻塞到下一个毫秒,获得新的时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 时间戳改变，毫秒内序列重置
            sequence = 0L;
        }

        // 上次生成ID的时间截
        lastTimestamp = timestamp;

        // 移位并通过或运算拼到一起组成64位的ID
        return ((timestamp - twepoch) << timestampLeftShift)
            | (dataCenterId << dataCenterIdShift)
            | (workerId << workerIdShift)
            | sequence;
    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     *
     * @param lastTimestamp 上次生成ID的时间截
     * @return 当前时间戳
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 返回以毫秒为单位的当前时间
     *
     * @return 当前时间(毫秒)
     */
    private long timeGen() {
        return System.currentTimeMillis();
    }

    public void setWorkerId(long workerId) {
        if (workerId > maxWorkerId) {
            throw new SequenceException("[SnowflakeSequence-setWorkerId] workerId 不能大于31.");
        }

        this.workerId = workerId;
    }

    public void setDataCenterId(long dataCenterId) {
        if (dataCenterId > maxDataCenterId) {
            throw new SequenceException("[SnowflakeSequence-setDataCenterId] dataCenterId 不能大于31.");
        }
        this.dataCenterId = dataCenterId;
    }

}
