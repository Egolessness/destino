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

package org.egolessness.destino.core.model;

import org.egolessness.destino.core.enumration.MetadataKey;
import org.egolessness.destino.core.message.ConsistencyDomain;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * protocol metadata
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ProtocolMetadata {
    
    private final Map<ConsistencyDomain, Map<MetadataKey, DataObservable>> metadataStore;

    public ProtocolMetadata() {
        this.metadataStore = new ConcurrentHashMap<>(ConsistencyDomain.values().length - 2);
    }

    public Optional<Map<MetadataKey, DataObservable>> get(final ConsistencyDomain domain) {
        return Optional.ofNullable(metadataStore.get(domain));
    }

    public Optional<DataObservable> get(final ConsistencyDomain domain, final MetadataKey metadataKey) {
        return get(domain).map(m -> m.get(metadataKey));
    }

    public void put(final ConsistencyDomain domain, Map<MetadataKey, Object> dataMap) {
        Map<MetadataKey, DataObservable> dataHolderMap = metadataStore.computeIfAbsent(domain,
                d -> new ConcurrentHashMap<>());
        dataMap.forEach((metadataKey, data) ->
                dataHolderMap.computeIfAbsent(metadataKey, d -> new DataObservable()).setData(data));
    }

    public void subscribe(final ConsistencyDomain domain, final MetadataKey metadataKey, final Observer observer) {
        DataObservable dataObservable = metadataStore.computeIfAbsent(domain, d -> new ConcurrentHashMap<>())
                .computeIfAbsent(metadataKey, d -> new DataObservable());
        dataObservable.addObserver(observer);
        Object data = dataObservable.getData();
        if (data != null) {
            observer.update(dataObservable, data);
        }
    }
    
    public void unsubscribe(final ConsistencyDomain domain, final MetadataKey metadataKey, final Observer observer) {
        get(domain, metadataKey).ifPresent(data -> data.deleteObserver(observer));
    }

    public static final class DataObservable extends Observable {
        
        private volatile Object data;
        
        public Object getData() {
            return data;
        }
        
        void setData(Object data) {
            this.data = data;
            setChanged();
            notifyObservers();
        }
        
    }
}