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

package com.egolessness.destino.scheduler.provider;

import com.egolessness.destino.scheduler.model.Contact;
import com.egolessness.destino.scheduler.model.SchedulerInfo;
import com.egolessness.destino.scheduler.model.SchedulerUpdatable;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.common.model.Script;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * scheduler provider
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public interface SchedulerProvider {

    List<SchedulerInfo> list(final Predicate<SchedulerInfo> predicate);

    SchedulerInfo get(final long id) throws DestinoException;

    List<SchedulerInfo> getAll(final Collection<Long> ids) throws DestinoException;

    SchedulerInfo create(final SchedulerInfo schedulerInfo) throws DestinoException;

    SchedulerInfo update(final long id, final SchedulerUpdatable updatable) throws DestinoException;

    SchedulerInfo setContact(final long id, final Contact contact) throws DestinoException;

    SchedulerInfo editScript(final long id, final Script script) throws DestinoException;

    SchedulerInfo updateEnabled(final long id, final boolean enabled) throws DestinoException;

    SchedulerInfo remove(final long id) throws DestinoException;

}
