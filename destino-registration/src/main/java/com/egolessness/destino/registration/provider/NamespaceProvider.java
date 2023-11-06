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

package com.egolessness.destino.registration.provider;

import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.registration.model.NamespaceInfo;

import java.util.List;

/**
 * provider of namespace
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public interface NamespaceProvider {

    List<NamespaceInfo> list();

    void create(String name, String desc) throws DestinoException;

    void update(String name, String desc) throws DestinoException;

    void delete(String name) throws DestinoException;

}
