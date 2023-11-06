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

import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.common.model.message.Response;
import com.egolessness.destino.common.support.ResponseSupport;
import com.egolessness.destino.core.exception.StorageException;
import com.egolessness.destino.core.message.*;
import com.egolessness.destino.core.storage.kv.DomainKvStorage;
import com.egolessness.destino.core.support.CosmosSupport;
import com.egolessness.destino.core.support.MessageSupport;

import java.util.List;
import java.util.Map;

/**
 * implement of kv decree
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class KvDecree implements Decree {

    private final Cosmos cosmos;

    private DomainKvStorage<?> storage;

    public KvDecree(DomainKvStorage<?> storage) {
        this.cosmos = CosmosSupport.buildCosmos(storage);
        this.storage = storage;
    }

    @Override
    public Cosmos cosmos() {
        return cosmos;
    }

    @Override
    public Response search(SearchRequest request) {
        try {
            List<String> keys = MessageSupport.buildKeyList(request.getKeyList());
            MapInfo mapInfo = MessageSupport.buildMapInfo(storage.mGet(keys));
            return ResponseSupport.success(mapInfo);
        } catch (DestinoException e) {
            return ResponseSupport.of(e.getErrCode(), e.getErrMsg());
        }
    }

    @Override
    public Response write(WriteRequest request) {
        Map<String, byte[]> data = MessageSupport.convertKvMap(request.getDataList());
        try {
            storage.mSet(data);
            return ResponseSupport.success();
        } catch (StorageException e) {
            return ResponseSupport.of(e.getErrCode(), e.getErrMsg());
        }
    }

    @Override
    public Response delete(DeleteRequest request) {
        try {
            List<String> keys = MessageSupport.buildKeyList(request.getKeyList());
            storage.mDel(keys);
            return ResponseSupport.success();
        } catch (StorageException e) {
            return ResponseSupport.of(e.getErrCode(), e.getErrMsg());
        }
    }

    public DomainKvStorage<?> getStorage() {
        return storage;
    }

    public void setStorage(DomainKvStorage<?> storage) {
        this.storage = storage;
    }

}
