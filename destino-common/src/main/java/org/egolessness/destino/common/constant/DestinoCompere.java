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

package org.egolessness.destino.common.constant;

/**
 * destino compere
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@SuppressWarnings("SpellCheckingInspection")
public final class DestinoCompere {

    private static String name = "destino";

    public static String getName() {
        return name;
    }

    public static void setName(String name) {
        DestinoCompere.name = name;
    }

}
