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

package com.egolessness.destino.server.module;

import com.egolessness.destino.core.storage.factory.RecordStorageFactory;
import com.egolessness.destino.core.storage.factory.impl.H2StorageFactoryImpl;
import com.google.inject.*;
import com.egolessness.destino.core.container.ContainerFactory;
import com.egolessness.destino.core.container.MemberContainer;
import com.egolessness.destino.core.enumration.ServerMode;
import com.egolessness.destino.core.infrastructure.alarm.Alarm;
import com.egolessness.destino.core.infrastructure.alarm.AlarmDefaultImpl;
import com.egolessness.destino.core.infrastructure.uid.IdGenerator;
import com.egolessness.destino.core.infrastructure.uid.IdGeneratorClusteredImpl;
import com.egolessness.destino.core.infrastructure.uid.IdGeneratorMonolithicImpl;
import com.egolessness.destino.core.model.Member;
import com.egolessness.destino.core.spi.DestinoModule;
import com.egolessness.destino.core.storage.factory.EvanescentStorageFactory;
import com.egolessness.destino.core.infrastructure.undertake.ClusterUndertaker;
import com.egolessness.destino.core.infrastructure.undertake.Undertaker;
import com.egolessness.destino.core.storage.factory.impl.MemoryStorageFactoryImpl;
import com.egolessness.destino.core.storage.factory.PersistentStorageFactory;
import com.egolessness.destino.core.storage.factory.impl.RocksDBStorageFactoryImpl;
import com.egolessness.destino.core.repository.factory.RepositoryFactoryDefaultImpl;
import com.egolessness.destino.core.repository.factory.RepositoryFactory;

import java.util.ServiceLoader;

/**
 * core module.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@SuppressWarnings("SpellCheckingInspection")
public final class DestinoCoreModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(EvanescentStorageFactory.class).to(MemoryStorageFactoryImpl.class);
        bind(PersistentStorageFactory.class).to(RocksDBStorageFactoryImpl.class);
        bind(RecordStorageFactory.class).to(H2StorageFactoryImpl.class);
        bind(RepositoryFactory.class).to(RepositoryFactoryDefaultImpl.class);
        bind(Undertaker.class).to(ClusterUndertaker.class);
        bind(Alarm.class).to(AlarmDefaultImpl.class);

        ServiceLoader.load(DestinoModule.class).forEach(this::install);
    }

    @Provides
    @Singleton
    public Member currentMember(ContainerFactory containerFactory) {
        return containerFactory.getContainer(MemberContainer.class).getCurrent();
    }

    @Provides
    public IdGenerator idGenerator(ServerMode serverMode, Injector injector) {
        return serverMode.isDistributed() ?
                injector.getInstance(IdGeneratorClusteredImpl.class) : new IdGeneratorMonolithicImpl();
    }

}