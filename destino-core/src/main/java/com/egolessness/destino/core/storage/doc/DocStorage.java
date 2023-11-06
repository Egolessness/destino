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

package com.egolessness.destino.core.storage.doc;

import com.egolessness.destino.core.exception.StorageException;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * interface of document storage
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public interface DocStorage<T> {

    T get(long id) throws StorageException;

    default @Nonnull List<T> mGet(@Nonnull Collection<Long> ids) throws StorageException {
        List<T> selectList = new ArrayList<>(ids.size());
        for (Long id : ids) {
            T val = get(id);
            if (Objects.nonNull(val)) {
                selectList.add(val);
            }
        }
        return selectList;
    }

    T add(long id, @Nonnull T doc) throws StorageException;

    default List<T> mAdd(Map<Long, T> docs) throws StorageException {
        List<T> addedList = new ArrayList<>(docs.size());
        for (Map.Entry<Long, T> entry : docs.entrySet()) {
            T added = add(entry.getKey(), entry.getValue());
            if (added != null) {
                addedList.add(added);
            }
        }
        return addedList;
    }

    T update(long id, @Nonnull T doc) throws StorageException;

    default List<T> mUpdate(@Nonnull Map<Long, T> docs) throws StorageException {
        List<T> updatedList = new ArrayList<>(docs.size());
        for (Map.Entry<Long, T> entry : docs.entrySet()) {
            T updated = update(entry.getKey(), entry.getValue());
            if (updated != null) {
                updatedList.add(updated);
            }
        }
        return updatedList;
    }

    T del(long id) throws StorageException;

    default List<T> mDel(@Nonnull Collection<Long> ids) throws StorageException {
        List<T> delList = new ArrayList<>(ids.size());
        for (Long id : ids) {
            T deleted = del(id);
            if (deleted != null) {
                delList.add(deleted);
            }
        }
        return delList;
    }

    @Nonnull List<Long> ids() throws StorageException;

    @Nonnull List<T> all() throws StorageException;

}
