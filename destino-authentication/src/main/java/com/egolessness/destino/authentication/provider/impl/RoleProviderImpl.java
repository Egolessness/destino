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

package com.egolessness.destino.authentication.provider.impl;

import com.egolessness.destino.authentication.container.RoleContainer;
import com.egolessness.destino.authentication.repository.RoleRepository;
import com.google.inject.Singleton;
import com.egolessness.destino.authentication.model.Role;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.common.model.Page;
import com.egolessness.destino.common.model.Pageable;
import com.egolessness.destino.core.container.ContainerFactory;
import com.egolessness.destino.authentication.provider.RoleProvider;
import com.google.inject.Inject;
import com.egolessness.destino.core.enumration.CommonMessages;
import com.egolessness.destino.core.enumration.Errors;
import com.egolessness.destino.core.support.PageSupport;

import java.time.Duration;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * provider implement of role.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class RoleProviderImpl implements RoleProvider {

    private final static Duration writeTimeout = Duration.ofSeconds(5);

    private final RoleRepository roleRepository;

    private final RoleContainer roleContainer;

    @Inject
    public RoleProviderImpl(final RoleRepository roleRepository, final ContainerFactory containerFactory) {
        this.roleRepository = roleRepository;
        this.roleContainer = containerFactory.getContainer(RoleContainer.class);
    }

    @Override
    public Page<Role> page(Predicate<Role> predicate, Pageable pageable) throws DestinoException {
        List<Role> accounts = roleContainer.all().stream().filter(predicate)
                .sorted(Comparator.comparingLong(Role::getCreatedTime).reversed())
                .collect(Collectors.toList());
        return PageSupport.page(accounts, pageable.getPage(), pageable.getSize());
    }

    @Override
    public Role create(Role role) throws DestinoException {
        try {
            return roleRepository.add(role, writeTimeout);
        } catch (TimeoutException e) {
            throw new DestinoException(Errors.WRITE_TIMEOUT, CommonMessages.TIP_SAVE_TIMEOUT.getValue());
        }
    }

    @Override
    public Role update(Role role) throws DestinoException {
        try {
            return roleRepository.update(role.getId(), role, writeTimeout);
        } catch (TimeoutException e) {
            throw new DestinoException(Errors.WRITE_TIMEOUT, CommonMessages.TIP_UPDATE_TIMEOUT.getValue());
        }
    }

    @Override
    public Role delete(long id) throws DestinoException {
        try {
            return roleRepository.del(id, writeTimeout);
        } catch (TimeoutException e) {
            throw new DestinoException(Errors.WRITE_TIMEOUT, CommonMessages.TIP_SAVE_TIMEOUT.getValue());
        }
    }

    @Override
    public List<Role> batchDelete(Collection<Long> ids) throws DestinoException {
        try {
            return roleRepository.delAll(ids.toArray(new Long[0]), writeTimeout);
        } catch (TimeoutException e) {
            throw new DestinoException(Errors.WRITE_TIMEOUT, CommonMessages.TIP_SAVE_TIMEOUT.getValue());
        }
    }

}
