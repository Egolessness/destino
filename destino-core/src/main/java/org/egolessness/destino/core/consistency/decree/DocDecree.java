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

package org.egolessness.destino.core.consistency.decree;

import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.common.support.ResponseSupport;
import org.egolessness.destino.core.exception.StorageException;
import org.egolessness.destino.core.message.*;
import org.egolessness.destino.core.storage.doc.DomainDocStorage;
import org.egolessness.destino.core.support.CosmosSupport;
import org.egolessness.destino.core.support.MessageSupport;

import java.util.List;
import java.util.Map;

/**
 * implement of document decree
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class DocDecree implements Decree {

    private final Cosmos cosmos;

    private DomainDocStorage<?> storage;

    public DocDecree(DomainDocStorage<?> docStorage) {
        this.cosmos = CosmosSupport.buildCosmos(docStorage.domain(), docStorage.type());
        this.storage = docStorage;
    }

    @Override
    public Cosmos cosmos() {
        return cosmos;
    }

    @Override
    public Response search(SearchRequest request) {
        try {
            List<Long> ids = MessageSupport.buildIdList(request.getKeyList());
            BytesList bytesList = MessageSupport.buildBytesListForDocs(storage.mGet(ids));
            return ResponseSupport.success(bytesList);
        } catch (DestinoException e) {
            return ResponseSupport.of(e.getErrCode(), e.getErrMsg());
        }
    }

    @Override
    public Response write(WriteRequest request) {
        try {
            Map<Long, byte[]> docs = MessageSupport.convertDocMap(request.getEntityList());

            List<byte[]> result;
            if (request.hasMode() && request.getMode() == WriteMode.UPDATE) {
                result = storage.mUpdate(docs);
            } else {
                result = storage.mAdd(docs);
            }

            BytesList bytesList = MessageSupport.buildBytesListForDocs(result);
            return ResponseSupport.success(bytesList);
        } catch (StorageException e) {
            return ResponseSupport.of(e.getErrCode(), e.getErrMsg());
        }
    }

    @Override
    public Response delete(DeleteRequest request) {
        try {
            List<Long> ids = MessageSupport.buildIdList(request.getKeyList());
            BytesList bytesList = MessageSupport.buildBytesListForDocs(storage.mDel(ids));
            return ResponseSupport.success(bytesList);
        } catch (StorageException e) {
            return ResponseSupport.of(e.getErrCode(), e.getErrMsg());
        }
    }

    public DomainDocStorage<?> getStorage() {
        return storage;
    }

    public void setStorage(DomainDocStorage<?> storage) {
        this.storage = storage;
    }

}
