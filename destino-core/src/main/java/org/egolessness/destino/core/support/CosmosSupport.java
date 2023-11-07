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

package org.egolessness.destino.core.support;

import org.egolessness.destino.core.storage.doc.DomainDocStorage;
import org.egolessness.destino.core.storage.kv.DomainKvStorage;
import com.google.protobuf.InvalidProtocolBufferException;
import org.egolessness.destino.core.message.ConsistencyDomain;
import org.egolessness.destino.core.message.Cosmos;

import java.util.Objects;

/**
 * support for cosmos.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class CosmosSupport {

    public static Cosmos buildCosmos(byte[] cosmosBytes) throws InvalidProtocolBufferException {
        return Cosmos.parseFrom(cosmosBytes);
    }

    public static Cosmos buildCosmos(DomainKvStorage<?> domainStorage) {
        return buildCosmos(domainStorage.domain(), domainStorage.type());
    }

    public static Cosmos buildCosmos(DomainDocStorage<?> domainStorage) {
        return buildCosmos(domainStorage.domain(), domainStorage.type());
    }

    public static Cosmos buildCosmos(ConsistencyDomain domain, Class<?> subdomainClass) {
        Cosmos.Builder builder = Cosmos.newBuilder().setDomain(domain);
        if (Objects.nonNull(subdomainClass)) {
            builder.setSubdomain(subdomainClass.getSimpleName());
        }
        return builder.build();
    }

    public static Cosmos buildCosmos(ConsistencyDomain domain, String subdomain) {
        Cosmos.Builder builder = Cosmos.newBuilder().setDomain(domain);
        if (Objects.nonNull(subdomain)) {
            builder.setSubdomain(subdomain);
        }
        return builder.build();
    }

    public static Cosmos buildCosmos(ConsistencyDomain domain) {
        return Cosmos.newBuilder().setDomain(domain).build();
    }

}
