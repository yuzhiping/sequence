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
package com.github.hexsmith.seq.range;

import com.github.hexsmith.seq.exception.SequenceException;

/**
 * 区间管理器接口
 *
 * @author yuzp
 * @version V1.0
 * @since 2018-06-05 10:27
 */
public interface SequenceRangeManager {

    /**
     * 获取指定区间名的下一个区间
     *
     * @param name 区间名
     * @return 返回区间
     * @throws SequenceException 异常
     */
    SequenceRange nextRange(String name) throws SequenceException;

    /**
     * 初始化
     */
    void init();

}
