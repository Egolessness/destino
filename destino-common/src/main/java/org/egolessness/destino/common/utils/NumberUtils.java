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

package org.egolessness.destino.common.utils;

/**
 * utils of number
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class NumberUtils {

    public static int parseInt(String value, int def) {
        if (PredicateUtils.isEmpty(value)) {
            return def;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public static Long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static long parseLong(String value, long def) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return def;
        }
    }

}
