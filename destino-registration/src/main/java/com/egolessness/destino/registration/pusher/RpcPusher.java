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

package com.egolessness.destino.registration.pusher;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.core.enumration.Errors;
import com.egolessness.destino.core.exception.ConnectionClosedException;
import com.egolessness.destino.core.model.Connection;
import com.egolessness.destino.common.fixedness.Callback;
import com.egolessness.destino.common.model.message.Response;
import com.egolessness.destino.common.utils.PredicateUtils;
import com.egolessness.destino.core.container.ConnectionContainer;
import com.egolessness.destino.core.container.ContainerFactory;
import com.egolessness.destino.core.enumration.PushType;
import com.egolessness.destino.core.model.Receiver;

import java.io.Serializable;
import java.util.Collections;
import java.util.Objects;

/**
 * service rpc pusher
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class RpcPusher implements Pusher {

    private final ConnectionContainer connectionContainer;

    @Inject
    public RpcPusher(ContainerFactory containerFactory) {
        this.connectionContainer = containerFactory.getContainer(ConnectionContainer.class);
    }

    @Override
    public void push(final Receiver receiver, final Serializable pushData, final Callback<Response> callback) {

        if (PredicateUtils.isEmpty(receiver.id())) {
            callback.onThrowable(new DestinoException(Errors.PUSH_FAIL, "Receiver is invalid."));
            return;
        }

        Connection connection = connectionContainer.getConnection(receiver.id());

        if (Objects.isNull(connection)) {
            callback.onThrowable(new ConnectionClosedException());
            return;
        }

        try {
            connection.request(pushData, Collections.emptyMap(), callback);
        }  catch (Exception e) {
            callback.onThrowable(e);
        }

    }

    @Override
    public PushType type() {
        return PushType.RPC;
    }


}