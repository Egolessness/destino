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

package org.egolessness.destino.registration.provider.impl;

import org.egolessness.destino.registration.container.RegistrationContainer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.registration.model.Namespace;
import org.egolessness.destino.registration.provider.GroupProvider;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * provider implement of service group
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class GroupProviderImpl implements GroupProvider {

    private final RegistrationContainer registrationContainer;

    @Inject
    public GroupProviderImpl(ContainerFactory containerFactory) {
        this.registrationContainer = containerFactory.getContainer(RegistrationContainer.class);
    }

    @Override
    public Collection<String> list(@Nonnull String namespace) {
        Optional<Namespace> namespaceOptional = registrationContainer.findNamespace(namespace);
        return namespaceOptional.map(n -> n.getGroups().keySet()).orElseGet(Collections::emptySet);
    }

    @Override
    public long count(@Nonnull String namespace, Predicate<String> predicate) {
        Optional<Namespace> namespaceOptional = registrationContainer.findNamespace(namespace);
        return namespaceOptional.map(n -> n.getGroups().keySet().stream().filter(predicate).count()).orElse(0L);
    }

}
