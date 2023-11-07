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

package org.egolessness.destino.server.module;

import org.egolessness.destino.core.storage.factory.RecordStorageFactory;
import org.egolessness.destino.core.storage.factory.impl.H2StorageFactoryImpl;
import com.google.inject.*;
import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.core.container.MemberContainer;
import org.egolessness.destino.core.enumration.ServerMode;
import org.egolessness.destino.core.infrastructure.alarm.Alarm;
import org.egolessness.destino.core.infrastructure.alarm.AlarmDefaultImpl;
import org.egolessness.destino.core.infrastructure.uid.IdGenerator;
import org.egolessness.destino.core.infrastructure.uid.IdGeneratorClusteredImpl;
import org.egolessness.destino.core.infrastructure.uid.IdGeneratorMonolithicImpl;
import org.egolessness.destino.core.model.Member;
import org.egolessness.destino.core.spi.DestinoModule;
import org.egolessness.destino.core.storage.factory.EvanescentStorageFactory;
import org.egolessness.destino.core.infrastructure.undertake.ClusterUndertaker;
import org.egolessness.destino.core.infrastructure.undertake.Undertaker;
import org.egolessness.destino.core.storage.factory.impl.MemoryStorageFactoryImpl;
import org.egolessness.destino.core.storage.factory.PersistentStorageFactory;
import org.egolessness.destino.core.storage.factory.impl.RocksDBStorageFactoryImpl;
import org.egolessness.destino.core.repository.factory.RepositoryFactoryDefaultImpl;
import org.egolessness.destino.core.repository.factory.RepositoryFactory;

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