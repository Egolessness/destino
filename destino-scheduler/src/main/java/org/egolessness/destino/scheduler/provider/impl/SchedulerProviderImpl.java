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

package org.egolessness.destino.scheduler.provider.impl;

import org.egolessness.destino.scheduler.container.SchedulerContainer;
import org.egolessness.destino.scheduler.model.*;
import org.egolessness.destino.scheduler.repository.SchedulerRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.model.Script;
import org.egolessness.destino.core.enumration.Errors;
import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.scheduler.model.enumration.SchedulerSchema;
import org.egolessness.destino.scheduler.provider.SchedulerProvider;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * scheduler provider implement
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class SchedulerProviderImpl implements SchedulerProvider {

    private final SchedulerRepository schedulerRepository;

    private final SchedulerContainer schedulerContainer;

    private final Duration readTimeout = Duration.ofSeconds(5);

    private final Duration writeTimeout = Duration.ofSeconds(10);

    @Inject
    public SchedulerProviderImpl(final SchedulerRepository schedulerRepository, final ContainerFactory containerFactory) {
        this.schedulerRepository = schedulerRepository;
        this.schedulerContainer = containerFactory.getContainer(SchedulerContainer.class);
    }

    public List<SchedulerInfo> list(final Predicate<SchedulerInfo> predicate) {
        return schedulerContainer.loadSchedulerContexts().stream().map(SchedulerContext::getSchedulerInfo)
                .filter(predicate).collect(Collectors.toList());
    }

    @Override
    public SchedulerInfo get(final long id) throws DestinoException {
        try {
            SchedulerSeam seam = schedulerRepository.get(id, readTimeout);
            return seam != null ? (SchedulerInfo) seam.getValue() : null;
        } catch (TimeoutException e) {
            throw new DestinoException(Errors.READ_TIMEOUT, "Read timeout.");
        }
    }

    @Override
    public List<SchedulerInfo> getAll(Collection<Long> ids) throws DestinoException {
        try {
            List<SchedulerSeam> seams = schedulerRepository.getAll(ids.toArray(new Long[0]), readTimeout);
            return seams.stream().map(d -> (SchedulerInfo) d.getValue()).collect(Collectors.toList());
        } catch (TimeoutException e) {
            throw new DestinoException(Errors.READ_TIMEOUT, "Read timeout.");
        }
    }

    @Override
    public SchedulerInfo create(final SchedulerInfo schedulerInfo) throws DestinoException {
        try {
            long now = System.currentTimeMillis();
            schedulerInfo.setCreateTime(now);
            schedulerInfo.setUpdateTime(now);
            SchedulerSeam seam = new SchedulerSeam(SchedulerSchema.CREATE, schedulerInfo);
            SchedulerSeam created = schedulerRepository.add(seam, writeTimeout);
            return (SchedulerInfo) created.getValue();
        } catch (TimeoutException e) {
            throw new DestinoException(Errors.WRITE_TIMEOUT, "Write timeout.");
        }
    }

    @Override
    public SchedulerInfo update(long id, SchedulerUpdatable updatable) throws DestinoException {
        try {
            updatable.setUpdateTime(System.currentTimeMillis());
            SchedulerSeam seam = new SchedulerSeam(SchedulerSchema.UPDATE, updatable);
            SchedulerSeam updated = schedulerRepository.update(id, seam, writeTimeout);
            return updated != null ? (SchedulerInfo) updated.getValue() : null;
        } catch (TimeoutException e) {
            throw new DestinoException(Errors.WRITE_TIMEOUT, "Write timeout.");
        }
    }

    @Override
    public SchedulerInfo setContact(long id, Contact contact) throws DestinoException {
        try {
            SchedulerSeam seam = new SchedulerSeam(SchedulerSchema.SET_CONTACT, contact);
            SchedulerSeam updated = schedulerRepository.update(id, seam, writeTimeout);
            return updated != null ? (SchedulerInfo) updated.getValue() : null;
        } catch (TimeoutException e) {
            throw new DestinoException(Errors.WRITE_TIMEOUT, "Set timeout.");
        }
    }

    @Override
    public SchedulerInfo editScript(long id, Script script) throws DestinoException {
        try {
            script.setEditTime(System.currentTimeMillis());
            SchedulerSeam seam = new SchedulerSeam(SchedulerSchema.EDIT_SCRIPT, script);
            SchedulerSeam updated = schedulerRepository.update(id, seam, writeTimeout);
            return updated != null ? (SchedulerInfo) updated.getValue() : null;
        } catch (TimeoutException e) {
            throw new DestinoException(Errors.WRITE_TIMEOUT, "Save timeout.");
        }
    }

    @Override
    public SchedulerInfo updateEnabled(long id, boolean enabled) throws DestinoException {
        try {
            Activator activator = new Activator(enabled, System.currentTimeMillis());
            SchedulerSeam seam = new SchedulerSeam(SchedulerSchema.ACTIVATE, activator);
            SchedulerSeam updated = schedulerRepository.update(id, seam, writeTimeout);
            return updated != null ? (SchedulerInfo) updated.getValue() : null;
        } catch (TimeoutException e) {
            throw new DestinoException(Errors.WRITE_TIMEOUT, "Save timeout.");
        }
    }

    @Override
    public SchedulerInfo remove(final long id) throws DestinoException {
        try {
            SchedulerSeam removed = schedulerRepository.del(id, writeTimeout);
            return removed != null ? (SchedulerInfo) removed.getValue() : null;
        } catch (TimeoutException e) {
            throw new DestinoException(Errors.DELETE_TIMEOUT, "Delete timeout.");
        }
    }

}
