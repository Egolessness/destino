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

package org.egolessness.destino.core.repository;

import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.core.fixedness.CosmosLinker;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

/**
 * interface of key-value repository
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public interface KvRepository<T> extends CosmosLinker {

    T get(String key, Duration timeout) throws DestinoException, TimeoutException;

    Map<String, T> multiGet(String[] keys, Duration timeout) throws DestinoException, TimeoutException;

    void set(String key, T value, Duration timeout) throws DestinoException, TimeoutException;

    void multiSet(Map<String, T> data, Duration timeout) throws DestinoException, TimeoutException;

    void del(String key, Duration timeout) throws DestinoException, TimeoutException;

    void multiDel(String[] keys, Duration timeout) throws DestinoException, TimeoutException;

    CompletableFuture<T> get(String key);

    CompletableFuture<Map<String, T>> multiGet(String... keys);

    CompletableFuture<Void> set(String key, T value);

    CompletableFuture<Void> multiSet(Map<String, T> data);

    CompletableFuture<Void> del(String key);

    CompletableFuture<Void> multiDel(String... keys);

    boolean isAvailable();

}