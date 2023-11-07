/*
 * Copyright (c) 2023 by Kang Wang. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.egolessness.destino.core.properties.factory;

import org.egolessness.destino.common.enumeration.Mark;
import org.egolessness.destino.common.utils.ByteUtils;
import org.egolessness.destino.core.infrastructure.serialize.customized.Converter;
import org.apache.commons.lang.math.IntRange;

/**
 * int range converter
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class IntRangeConverter implements Converter<IntRange> {
    @Override
    public IntRange asObj(byte[] value) {
        String rangeStr = ByteUtils.toString(value);
        String[] ranges = Mark.CROSSED.split(rangeStr);
        if (ranges.length == 1) {
            return new IntRange(Integer.parseInt(ranges[0]), Integer.parseInt(ranges[0]));
        }
        if (ranges.length == 2) {
            return new IntRange(Integer.parseInt(ranges[0]), Integer.parseInt(ranges[1]));
        }
        throw new IllegalArgumentException("Int range is invalid with value:" + rangeStr);
    }

    @Override
    public byte[] asBytes(IntRange range) {
        String rangeStr = Mark.CROSSED.join(range.getMinimumInteger(), range.getMaximumInteger());
        return ByteUtils.toBytes(rangeStr);
    }
}
