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

package org.egolessness.destino.core.consistency.processor;

import org.egolessness.destino.core.exception.SnapshotException;
import org.egolessness.destino.core.model.SnapshotFiles;
import org.egolessness.destino.core.fixedness.Metadata;

import java.time.Duration;

/**
 * interface of atomic consistency processor
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public interface AtomicConsistencyProcessor extends ConsistencyProcessor {

    void onMetadata(Metadata metadata);

    default Duration snapshotInterval() {
        return  Duration.ofHours(1);
    }

    default boolean snapshotSave(final SnapshotFiles store) throws SnapshotException {
        return false;
    }

    default boolean snapshotLoad(final SnapshotFiles store) throws SnapshotException {
        return false;
    }

}
