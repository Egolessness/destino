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

package com.egolessness.destino.core.consistency;

import com.egolessness.destino.common.fixedness.Lucermaire;
import com.egolessness.destino.common.model.Result;
import com.egolessness.destino.common.model.message.Response;
import com.egolessness.destino.core.model.ProtocolCommand;
import com.egolessness.destino.core.model.ProtocolMetadata;
import com.egolessness.destino.core.fixedness.MembersAcceptor;
import com.egolessness.destino.core.message.DeleteRequest;
import com.egolessness.destino.core.message.SearchRequest;
import com.egolessness.destino.core.message.WriteRequest;

import java.util.concurrent.CompletableFuture;

/**
 * interface of consistency protocol
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public interface ConsistencyProtocol extends MembersAcceptor, Lucermaire {
    
    void init();

    CompletableFuture<Response> search(SearchRequest request);

    CompletableFuture<Response> write(WriteRequest request);

    CompletableFuture<Response> delete(DeleteRequest request);

    Result<String> execute(ProtocolCommand command);
    
    ProtocolMetadata protocolMetaData();
    
}