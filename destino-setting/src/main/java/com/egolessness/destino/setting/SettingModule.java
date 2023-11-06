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

package com.egolessness.destino.setting;

import com.egolessness.destino.core.fixedness.MemberStateListener;
import com.egolessness.destino.setting.coordinator.MemberCoordinator;
import com.egolessness.destino.setting.provider.ClusterProvider;
import com.egolessness.destino.setting.provider.SettingProvider;
import com.egolessness.destino.setting.provider.impl.ClusterProviderImpl;
import com.egolessness.destino.setting.provider.impl.SettingProviderImpl;
import com.egolessness.destino.setting.repository.MemberRepository;
import com.egolessness.destino.setting.repository.SettingRepository;
import com.egolessness.destino.setting.repository.impl.ClusteredMemberRepository;
import com.egolessness.destino.setting.repository.impl.MonolithicMemberRepository;
import com.egolessness.destino.setting.storage.SettingStorage;
import com.google.inject.*;
import com.egolessness.destino.core.enumration.ServerMode;
import com.egolessness.destino.core.repository.factory.RepositoryFactory;
import com.egolessness.destino.core.spi.DestinoModule;

/**
 * setting module.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class SettingModule extends AbstractModule implements DestinoModule {

    @Override
    protected void configure() {
        bind(MemberStateListener.class).to(MemberCoordinator.class);
        bind(SettingProvider.class).to(SettingProviderImpl.class);
        bind(ClusterProvider.class).to(ClusterProviderImpl.class);
    }

    @Provides
    @Singleton
    public MemberRepository memberRepository(Injector injector, ServerMode mode) {
        if (mode.isDistributed()) {
            return injector.getInstance(ClusteredMemberRepository.class);
        }
        return injector.getInstance(MonolithicMemberRepository.class);
    }

    @Provides
    @Singleton
    public SettingRepository settingRepository(RepositoryFactory repositoryFactory, SettingStorage storage) {
        return repositoryFactory.createRepository(SettingRepository.class, storage);
    }

}
