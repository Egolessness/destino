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

package org.egolessness.destino.core.repository.factory;

import org.egolessness.destino.core.enumration.ServerMode;
import org.egolessness.destino.core.repository.AtomicDocRepository;
import org.egolessness.destino.core.repository.AtomicKvRepository;
import org.egolessness.destino.core.repository.WeakDocRepository;
import org.egolessness.destino.core.repository.WeakKvRepository;
import org.egolessness.destino.core.storage.doc.EvanescentDocStorage;
import org.egolessness.destino.core.storage.doc.PersistentDocStorage;
import org.egolessness.destino.core.storage.kv.EvanescentKvStorage;
import org.egolessness.destino.core.storage.kv.PersistentKvStorage;

import java.io.Serializable;

/**
 * interface of repository factory
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public interface RepositoryFactory {

    <T extends Serializable, R extends AtomicKvRepository<T>> R createRepository(Class<R> type, PersistentKvStorage<T> storage);

    <T extends Serializable, R extends WeakKvRepository<T>> R createRepository(Class<R> type, EvanescentKvStorage<T> storage);

    <T extends Serializable, R extends AtomicDocRepository<T>> R createRepository(Class<R> type, PersistentDocStorage<T> storage);

    <T extends Serializable, R extends WeakDocRepository<T>> R createRepository(Class<R> type, EvanescentDocStorage<T> storage);

    <T extends Serializable, R extends AtomicDocRepository<T>> R createRepository(Class<R> type, PersistentDocStorage<T> storage,
                                                                                  ServerMode serverMode);

}
