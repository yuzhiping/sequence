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
package com.github.hexsmith.seq.sequence;

import com.github.hexsmith.seq.range.SequenceRangeManager;

/**
 * 序列号区间生成器接口
 *
 * @author yuzp
 * @version V1.0
 * @since 2018-06-05 14:56
 */
public interface RangeSequence extends Sequence {

    /**
     * 设置区间管理器
     *
     * @param seqRangeMgr 区间管理器
     */
    void setSeqRangeMgr(SequenceRangeManager seqRangeMgr);

    /**
     * 设置获取序列号名称
     *
     * @param rangeName 名称
     */
    void setRangeName(String rangeName);

}
