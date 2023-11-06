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

package com.egolessness.destino.registration.provider.impl;

import com.egolessness.destino.registration.container.NamespaceContainer;
import com.egolessness.destino.registration.repository.NamespaceRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.core.container.ContainerFactory;
import com.egolessness.destino.core.enumration.CommonMessages;
import com.egolessness.destino.core.enumration.Errors;
import com.egolessness.destino.core.message.WriteMode;
import com.egolessness.destino.registration.model.NamespaceSubject;
import com.egolessness.destino.registration.model.NamespaceInfo;
import com.egolessness.destino.registration.provider.NamespaceProvider;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * provider implement of namespace
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class NamespaceProviderImpl implements NamespaceProvider {

    private final NamespaceRepository namespaceRepository;

    private final NamespaceContainer namespaceContainer;

    private final Duration writeTimeout = Duration.ofSeconds(5);

    @Inject
    public NamespaceProviderImpl(NamespaceRepository namespaceRepository, ContainerFactory containerFactory) {
        this.namespaceRepository = namespaceRepository;
        this.namespaceContainer = containerFactory.getContainer(NamespaceContainer.class);
    }

    @Override
    public List<NamespaceInfo> list() {
        return namespaceContainer.getNamespaces().stream().sorted().collect(Collectors.toList());
    }

    @Override
    public void create(String name, String desc) throws DestinoException {
        try {
            namespaceRepository.set(name, new NamespaceSubject(desc, WriteMode.ADD), writeTimeout);
        } catch (TimeoutException e) {
            throw new DestinoException(Errors.WRITE_TIMEOUT, CommonMessages.TIP_SAVE_TIMEOUT.getValue());
        }
    }

    @Override
    public void update(String name, String desc) throws DestinoException {
        try {
            namespaceRepository.set(name, new NamespaceSubject(desc, WriteMode.UPDATE), writeTimeout);
        } catch (TimeoutException e) {
            throw new DestinoException(Errors.WRITE_TIMEOUT, CommonMessages.TIP_SAVE_TIMEOUT.getValue());
        }
    }

    @Override
    public void delete(String name) throws DestinoException {
        try {
            namespaceRepository.del(name, writeTimeout);
        } catch (TimeoutException e) {
            throw new DestinoException(Errors.DELETE_TIMEOUT, CommonMessages.TIP_DELETE_TIMEOUT.getValue());
        }
    }

}
