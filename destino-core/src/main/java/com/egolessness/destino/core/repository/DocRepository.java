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

package com.egolessness.destino.core.repository;

import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.core.fixedness.CosmosLinker;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

/**
 * interface of document repository
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public interface DocRepository<T> extends CosmosLinker {

    T get(Long id, Duration timeout) throws DestinoException, TimeoutException;

    List<T> getAll(Long[] ids, Duration timeout) throws DestinoException, TimeoutException;

    T add(T doc, Duration timeout) throws DestinoException, TimeoutException;

    List<T> addAll(Collection<T> docs, Duration timeout) throws DestinoException, TimeoutException;

    T update(Long id, T doc, Duration timeout) throws DestinoException, TimeoutException;

    List<T> updateAll(Map<Long, T> docs, Duration timeout) throws DestinoException, TimeoutException;

    T del(Long id, Duration timeout) throws DestinoException, TimeoutException;

    List<T> delAll(Long[] ids, Duration timeout) throws DestinoException, TimeoutException;

    CompletableFuture<T> get(Long id);

    CompletableFuture<List<T>> getAll(Long... ids);

    CompletableFuture<T> add(T doc);

    CompletableFuture<List<T>> addAll(Collection<T> docs);

    CompletableFuture<T> update(Long id, T doc);

    CompletableFuture<List<T>> updateAll(Map<Long, T> docs);

    CompletableFuture<T> del(Long id);

    CompletableFuture<List<T>> delAll(Long... ids);

    boolean isAvailable();

}