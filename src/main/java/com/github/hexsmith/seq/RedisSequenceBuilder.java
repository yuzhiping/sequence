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

import com.github.hexsmith.seq.range.impl.redis.RedisSequenceRange;
import com.github.hexsmith.seq.sequence.Sequence;
import com.github.hexsmith.seq.sequence.impl.DefaultRangeSequence;

/**
 * 基于redis取步长，序列号生成器构建者
 *
 * @author yuzp
 * @version V1.0
 * @since 2018-06-05 15:28
 */
public class RedisSequenceBuilder implements SequenceBuilder {

    /**
     * 连接redis的IP[必选]
     */
    private String ip;
    /**
     * 连接redis的port[必选]
     */
    private int    port;
    /**
     * 业务名称[必选]
     */
    private String bizName;
    /**
     * 认证权限，看redis是否配置了需要密码auth[可选]
     */
    private String auth;
    /**
     * 获取range步长[可选，默认：1000]
     */
    private int step = 1000;

    /**
     * 构建一个序列号生成器
     *
     * @return 序列号生成器
     */
    @Override
    public Sequence build() {
        //利用Redis获取区间管理器
        RedisSequenceRange redisSeqRangeMgr = new RedisSequenceRange();
        redisSeqRangeMgr.setIp(this.ip);
        redisSeqRangeMgr.setPort(this.port);
        redisSeqRangeMgr.setAuth(this.auth);
        redisSeqRangeMgr.setStep(this.step);
        redisSeqRangeMgr.init();
        //构建序列号生成器
        DefaultRangeSequence sequence = new DefaultRangeSequence();
        sequence.setRangeName(this.bizName);
        sequence.setSeqRangeMgr(redisSeqRangeMgr);
        return sequence;
    }

    public static RedisSequenceBuilder create() {
        return new RedisSequenceBuilder();
    }

    public RedisSequenceBuilder ip(String ip) {
        this.ip = ip;
        return this;
    }

    public RedisSequenceBuilder port(int port) {
        this.port = port;
        return this;
    }

    public RedisSequenceBuilder auth(String auth) {
        this.auth = auth;
        return this;
    }

    public RedisSequenceBuilder step(int step) {
        this.step = step;
        return this;
    }

    public RedisSequenceBuilder bizName(String bizName) {
        this.bizName = bizName;
        return this;
    }

}
