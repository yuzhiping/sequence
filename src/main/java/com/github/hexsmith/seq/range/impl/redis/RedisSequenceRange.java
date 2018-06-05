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
package com.github.hexsmith.seq.range.impl.redis;

import com.github.hexsmith.seq.exception.SequenceException;
import com.github.hexsmith.seq.range.SequenceRange;
import com.github.hexsmith.seq.range.SequenceRangeManager;

import redis.clients.jedis.Jedis;

/**
 * Redis区间管理器
 *
 * @author yuzp
 * @version V1.0
 * @since 2018-06-05 10:33
 */
public class RedisSequenceRange implements SequenceRangeManager {

    /**
     * 前缀防止key重复
     */
    private static final String KEY_PREFIX = "sequence_";

    /**
     * redis客户端
     */
    private Jedis jedis;

    /**
     * IP
     */
    private String ip;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 验证权限
     */
    private String auth;

    /**
     * 区间步长
     */
    private Integer step = 1000;


    /**
     * 获取指定区间名的下一个区间
     *
     * @param name 区间名
     * @return 返回区间
     * @throws com.github.hexsmith.seq.exception.SequenceException 异常
     */
    @Override
    public SequenceRange nextRange(String name) throws SequenceException {
        long max = jedis.incrBy(getRealKey(name), step);
        long min = max - step + 1;
        return new SequenceRange(min, max);
    }

    /**
     * 初始化
     */
    @Override
    public void init() {
        checkParam();
        jedis = new Jedis(ip, port);
        if (null != auth) {
            jedis.auth(auth);
        }
    }

    private void checkParam() {
        if (isEmpty(ip)) {
            throw new SecurityException("[RedisSequenceRange-checkParam] ip is empty.");
        }
        if (null == port) {
            throw new SecurityException("[RedisSequenceRange-checkParam] port is null.");
        }
    }

    private boolean isEmpty(String str) {
        return null == str || str.length() == 0;
    }

    /**
     * 获取key值
     * @param name 原始key
     * @return 真实的key
     */
    private String getRealKey(String name) {
        return KEY_PREFIX + name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }
}
