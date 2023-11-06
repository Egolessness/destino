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

package com.egolessness.destino.registration.container;

import com.egolessness.destino.common.constant.DefaultConstants;
import com.egolessness.destino.core.container.Container;
import com.egolessness.destino.registration.model.NamespaceInfo;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * container of namespace.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class NamespaceContainer implements Container {

    private final ConcurrentHashMap<String, NamespaceInfo> namespaces = new ConcurrentHashMap<>();

    public NamespaceContainer() {
        init();
    }

    public void init() {
        create(new NamespaceInfo(DefaultConstants.REGISTRATION_NAMESPACE));
    }

    public Collection<NamespaceInfo> getNamespaces() {
        return namespaces.values();
    }

    public boolean create(final NamespaceInfo info) {
        return namespaces.putIfAbsent(info.getName(), info) == null;
    }

    public boolean update(final NamespaceInfo info) {
        return namespaces.computeIfPresent(info.getName(), (k, v) -> {
            v.setDesc(info.getDesc());
            return v;
        }) != null;
    }

    public synchronized void remove(final String name) {
        if (DefaultConstants.REGISTRATION_NAMESPACE.equals(name)) {
            throw new IllegalArgumentException("The public namespace cannot be deleted.");
        }
        namespaces.remove(name);
    }

    @Override
    public void clear() {
        namespaces.clear();
        init();
    }
}
