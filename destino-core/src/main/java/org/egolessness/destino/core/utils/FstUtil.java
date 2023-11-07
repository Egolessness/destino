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

package org.egolessness.destino.core.utils;

import org.egolessness.destino.common.utils.ByteUtils;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectSerializer;

/**
 * utils of fst serializer
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class FstUtil {

    private final static FSTConfiguration configuration = FSTConfiguration.createDefaultConfiguration();

    public static <T> T deepCopy(T target) {
        return configuration.deepCopy(target);
    }

    public static void registerSerializer(Class<?> clazz, FSTObjectSerializer ser, boolean alsoForAllSubclasses) {
        configuration.registerSerializer(clazz, ser, alsoForAllSubclasses);
    }

    @SuppressWarnings("unchecked")
    public static <T> T asObject(byte[] data) {
        if (ByteUtils.isEmpty(data)) {
            return null;
        }
        return (T) configuration.asObject(data);
    }

    public static byte[] asByteArray(Object object) {
        return configuration.asByteArray(object);
    }

    public static String asJsonString(Object object) {
        return configuration.asJsonString(object);
    }

}
