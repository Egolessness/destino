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

package org.egolessness.destino.registration.storage;

import com.google.inject.Inject;
import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.core.exception.StorageException;
import org.egolessness.destino.core.message.Cosmos;
import org.egolessness.destino.core.storage.factory.EvanescentStorageFactory;
import org.egolessness.destino.core.storage.StorageOptions;
import org.egolessness.destino.core.storage.kv.EvanescentKvStorage;
import org.egolessness.destino.core.storage.kv.KvStorage;
import org.egolessness.destino.core.storage.specifier.StringSpecifier;
import org.egolessness.destino.core.support.CosmosSupport;
import org.egolessness.destino.registration.model.Registration;

/**
 * evanescent storage of registration.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RegistrationEvanescentStorage extends AbstractRegistrationStorage implements EvanescentKvStorage<Registration> {

    protected final KvStorage<String, byte[]> baseKvStorage;

    @Inject
    public RegistrationEvanescentStorage(final ContainerFactory containerFactory, final EvanescentStorageFactory evanescentStorageFactory) throws StorageException {
        super(containerFactory);
        Cosmos cosmos = CosmosSupport.buildCosmos(domain(), type());
        this.baseKvStorage = evanescentStorageFactory.create(cosmos, StringSpecifier.INSTANCE, new StorageOptions());
    }

    @Override
    protected KvStorage<String, byte[]> getBaseStorage() {
        return baseKvStorage;
    }

}
