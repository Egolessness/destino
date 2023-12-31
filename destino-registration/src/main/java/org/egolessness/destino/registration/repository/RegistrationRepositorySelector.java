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

package org.egolessness.destino.registration.repository;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.egolessness.destino.common.enumeration.RegisterMode;
import org.egolessness.destino.core.repository.KvRepository;
import org.egolessness.destino.core.repository.KvRepositorySelector;
import org.egolessness.destino.core.repository.AtomicKvRepository;
import org.egolessness.destino.core.repository.WeakKvRepository;
import org.egolessness.destino.registration.model.Registration;

/**
 * selector for registration repository.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class RegistrationRepositorySelector implements KvRepositorySelector<RegisterMode, Registration> {

    private final WeakKvRepository<Registration> weakKvRepository;

    private final AtomicKvRepository<Registration> atomicKvRepository;

    @Inject
    public RegistrationRepositorySelector(final RegistrationAtomicRepository atomicRepository,
                                          final RegistrationWeakRepository weakRepository) {
        this.weakKvRepository = weakRepository;
        this.atomicKvRepository = atomicRepository;
    }

    @Override
    public KvRepository<Registration> select(RegisterMode mode) {
        return RegisterMode.SAFETY == mode ? atomicKvRepository : weakKvRepository;
    }

}
