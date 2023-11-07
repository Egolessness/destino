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

import org.egolessness.destino.core.consistency.AtomicConsistencyProtocol;
import org.egolessness.destino.core.consistency.WeakConsistencyProtocol;
import org.egolessness.destino.core.repository.AtomicDocRepository;
import org.egolessness.destino.core.repository.AtomicKvRepository;
import org.egolessness.destino.core.repository.WeakDocRepository;
import org.egolessness.destino.core.repository.WeakKvRepository;
import org.egolessness.destino.core.repository.kv.atomic.ClusteredAtomicKvRepository;
import org.egolessness.destino.core.repository.kv.atomic.MonolithicAtomicKvRepository;
import org.egolessness.destino.core.repository.kv.weak.ClusteredWeakKvRepository;
import org.egolessness.destino.core.repository.kv.weak.MonolithicWeakKvRepository;
import org.egolessness.destino.core.repository.proxy.*;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import org.egolessness.destino.core.repository.doc.atomic.ClusteredAtomicDocRepository;
import org.egolessness.destino.core.repository.doc.atomic.MonolithicAtomicDocRepository;
import org.egolessness.destino.core.repository.doc.weak.ClusteredWeakDocRepository;
import org.egolessness.destino.core.repository.doc.weak.MonolithicWeakDocRepository;
import org.egolessness.destino.core.enumration.ServerMode;
import org.egolessness.destino.core.storage.doc.EvanescentDocStorage;
import org.egolessness.destino.core.storage.doc.PersistentDocStorage;
import org.egolessness.destino.core.storage.kv.EvanescentKvStorage;
import org.egolessness.destino.core.storage.kv.PersistentKvStorage;

import java.io.Serializable;
import java.lang.reflect.Proxy;

/**
 * default implement of repository factory
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class RepositoryFactoryDefaultImpl implements RepositoryFactory {

    private final Injector injector;

    private final ServerMode serverMode;

    @Inject
    public RepositoryFactoryDefaultImpl(final Injector injector, final ServerMode mode) {
        this.injector = injector;
        this.serverMode = mode;
    }

    @SuppressWarnings("unchecked")
    protected <R> R newInstance(Class<R> type, KvRepositoryProxy<?> repository) {
        return (R) Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, repository);
    }

    @SuppressWarnings("unchecked")
    protected <R> R newInstance(Class<R> type, DocRepositoryProxy<?> repository) {
        return (R) Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, repository);
    }

    @Override
    public <T extends Serializable, R extends AtomicKvRepository<T>> R createRepository(Class<R> type,
                                                                                        PersistentKvStorage<T> storage) {
        AtomicKvRepository<byte[]> basicAtomicKvRepository = createBasicAtomicKvRepository(storage, this.serverMode);
        AtomicKvRepositoryProxy<T> proxy = new AtomicKvRepositoryProxy<>(basicAtomicKvRepository, storage);
        return newInstance(type, proxy);
    }

    @Override
    public <T extends Serializable, R extends WeakKvRepository<T>> R createRepository(Class<R> type,
                                                                                      EvanescentKvStorage<T> storage) {
        WeakKvRepository<byte[]> basicWeakKvRepository = createBasicWeakKvRepository(storage, this.serverMode);
        WeakKvRepositoryProxy<T> proxy = new WeakKvRepositoryProxy<>(basicWeakKvRepository, storage);
        return newInstance(type, proxy);
    }

    @Override
    public <T extends Serializable, R extends AtomicDocRepository<T>> R createRepository(Class<R> type, PersistentDocStorage<T> storage) {
        AtomicDocRepository<byte[]> basicAtomicDocRepository = createBasicAtomicDocRepository(storage, this.serverMode);
        AtomicDocRepositoryProxy<T> proxy = new AtomicDocRepositoryProxy<>(basicAtomicDocRepository, storage);
        return newInstance(type, proxy);
    }

    @Override
    public <T extends Serializable, R extends WeakDocRepository<T>> R createRepository(Class<R> type, EvanescentDocStorage<T> storage) {
        WeakDocRepository<byte[]> basicWeakDocRepository = createBasicWeakDocRepository(storage, this.serverMode);
        WeakDocRepositoryProxy<T> proxy = new WeakDocRepositoryProxy<>(basicWeakDocRepository, storage);
        return newInstance(type, proxy);
    }

    @Override
    public <T extends Serializable, R extends AtomicDocRepository<T>> R createRepository(Class<R> type,
                                                                                         PersistentDocStorage<T> storage,
                                                                                         ServerMode serverMode) {
        AtomicDocRepositoryProxy<T> proxy = new AtomicDocRepositoryProxy<>(createBasicAtomicDocRepository(storage, serverMode), storage);
        return newInstance(type, proxy);
    }

    private AtomicKvRepository<byte[]> createBasicAtomicKvRepository(PersistentKvStorage<?> storage, ServerMode mode) {
        if (mode.isMonolithic()) {
            return new MonolithicAtomicKvRepository(storage);
        }
        AtomicConsistencyProtocol atomicConsistencyProtocol = injector.getInstance(AtomicConsistencyProtocol.class);
        return new ClusteredAtomicKvRepository(storage, atomicConsistencyProtocol);
    }

    private WeakKvRepository<byte[]> createBasicWeakKvRepository(EvanescentKvStorage<?> storage, ServerMode mode) {
        if (mode.isMonolithic()) {
            return new MonolithicWeakKvRepository(storage);
        }
        WeakConsistencyProtocol weakConsistencyProtocol = injector.getInstance(WeakConsistencyProtocol.class);
        return new ClusteredWeakKvRepository(storage, weakConsistencyProtocol);
    }

    private AtomicDocRepository<byte[]> createBasicAtomicDocRepository(PersistentDocStorage<?> storage, ServerMode mode) {
        if (mode.isMonolithic()) {
            return new MonolithicAtomicDocRepository(storage);
        }
        AtomicConsistencyProtocol atomicConsistencyProtocol = injector.getInstance(AtomicConsistencyProtocol.class);
        return new ClusteredAtomicDocRepository(storage, atomicConsistencyProtocol);
    }

    private WeakDocRepository<byte[]> createBasicWeakDocRepository(EvanescentDocStorage<?> storage, ServerMode mode) {
        if (mode.isMonolithic()) {
            return new MonolithicWeakDocRepository(storage);
        }
        WeakConsistencyProtocol weakConsistencyProtocol = injector.getInstance(WeakConsistencyProtocol.class);
        return new ClusteredWeakDocRepository(storage, weakConsistencyProtocol);
    }

}
