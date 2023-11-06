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

package com.egolessness.destino.core.consistency.decree;

import com.egolessness.destino.core.exception.SnapshotException;
import com.egolessness.destino.core.storage.kv.PersistentKvStorage;

/**
 * implement of kv atomic decree
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class KvAtomicDecree extends KvDecree implements AtomicDecree{

    private final PersistentKvStorage<?> storage;

    public KvAtomicDecree(PersistentKvStorage<?> storage) {
        super(storage);
        this.storage = storage;
    }

    @Override
    public String snapshotName() {
        return storage.snapshotName();
    }

    @Override
    public String snapshotSource() {
        return storage.snapshotSource();
    }

    @Override
    public void snapshotSave(String path) throws SnapshotException {
        storage.snapshotSave(path);
    }

    @Override
    public void snapshotLoad(String path) throws SnapshotException {
        storage.snapshotLoad(path);
    }

}
