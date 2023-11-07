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

import com.alipay.sofa.jraft.Closure;
import com.alipay.sofa.jraft.Status;
import com.google.protobuf.Message;
import org.egolessness.destino.core.enumration.Errors;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.model.message.Response;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * implement raft closure.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class JRaftClosure implements Closure {
    
    private final Message message;
    
    private final CompletableFuture<Response> future;

    private Response response;

    private Throwable throwable;

    public JRaftClosure(Message message, CompletableFuture<Response> future) {
        this.message = message;
        this.future = future;
    }
    
    @Override
    public void run(Status status) {
        if (status.isOk()) {
            future.complete(response);
            return;
        }

        if (Objects.nonNull(throwable)) {
            future.completeExceptionally(throwable);
            return;
        }

        future.completeExceptionally(new DestinoException(Errors.UNKNOWN, "statemachine no response"));
    }
    
    public Message getMessage() {
        return message;
    }

    public void acceptResponse(Response response) {
        this.response = response;
        future.complete(response);
    }

    public void acceptError(Throwable throwable) {
        this.throwable = throwable;
        future.completeExceptionally(throwable);
    }
    
}