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

package org.egolessness.destino.raft;

import com.google.protobuf.Message;
import org.egolessness.destino.core.message.DeleteRequest;
import org.egolessness.destino.core.message.MemberRequest;
import org.egolessness.destino.common.model.message.Request;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.core.message.SearchRequest;
import org.egolessness.destino.core.message.WriteRequest;

/**
 * raft protobuf messages.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public enum JRaftTypings {

    RESPONSE(Response.getDefaultInstance()),
    REQUEST(Request.getDefaultInstance()),
    SERVER_SEARCH(SearchRequest.getDefaultInstance()),
    SERVER_WRITE(WriteRequest.getDefaultInstance()),
    SERVER_DELETE(DeleteRequest.getDefaultInstance()),
    SERVER_MEMBER(MemberRequest.getDefaultInstance())
    ;

    private final Message message;

    JRaftTypings(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}
