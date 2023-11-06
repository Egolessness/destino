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

package com.egolessness.destino.scheduler.provider.impl;

import com.egolessness.destino.scheduler.container.SchedulerContainer;
import com.egolessness.destino.scheduler.model.SchedulerContext;
import com.egolessness.destino.scheduler.model.ScriptKey;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.common.model.Script;
import com.egolessness.destino.common.model.ScriptFate;
import com.egolessness.destino.core.container.ContainerFactory;
import com.egolessness.destino.scheduler.model.response.ScriptView;
import com.egolessness.destino.scheduler.provider.ScriptProvider;
import com.egolessness.destino.scheduler.repository.storage.SchedulerStorage;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * script provider implement
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class ScriptProviderImpl implements ScriptProvider {

    private final SchedulerContainer schedulerContainer;

    private final SchedulerStorage schedulerStorage;

    @Inject
    public ScriptProviderImpl(ContainerFactory containerFactory, SchedulerStorage schedulerStorage) {
        this.schedulerContainer = containerFactory.getContainer(SchedulerContainer.class);
        this.schedulerStorage = schedulerStorage;
    }

    @Override
    public Optional<Script> find(long id, long version) throws DestinoException {
        Optional<SchedulerContext> standardOptional = schedulerContainer.find(id);

        if (standardOptional.isPresent()) {
            Script script = standardOptional.get().getSchedulerInfo().getScript();
            if (script == null) {
                return Optional.empty();
            }
            if (script.getVersion() == version) {
                return Optional.of(script);
            }
            if (script.getVersion() < version) {
                return Optional.empty();
            }
        }

        Script script = schedulerStorage.getScript(id, version);
        return Optional.ofNullable(script);
    }

    @Override
    public ScriptView view(long id, @Nullable Long version) throws DestinoException {
        Map<ScriptKey, byte[]> scriptHistories = schedulerStorage.getScriptHistories(id);

        ScriptView view = new ScriptView();
        view.setId(id);

        List<Script> scripts = scriptHistories.values().stream().map(schedulerStorage::deserializeScript)
                .sorted().collect(Collectors.toList());

        Script current = null;
        List<ScriptFate> scriptFates = new ArrayList<>(scripts.size());

        if (version == null) {
            for (Script script : scripts) {
                current = script;
                scriptFates.add(ScriptFate.copy(script));
            }
        } else {
            for (Script script : scripts) {
                scriptFates.add(ScriptFate.copy(script));
                if (script.getVersion() == version) {
                    current = script;
                }
            }
        }

        view.setScript(current);
        view.setHistories(scriptFates);

        return view;
    }

}
