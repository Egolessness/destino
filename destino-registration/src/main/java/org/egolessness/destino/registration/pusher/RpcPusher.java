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

package org.egolessness.destino.registration.pusher;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.ManagedChannel;
import io.grpc.MethodDescriptor;
import io.grpc.stub.ClientCalls;
import org.egolessness.destino.common.enumeration.Mark;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.executor.DestinoExecutors;
import org.egolessness.destino.common.model.message.Request;
import org.egolessness.destino.common.support.CallbackSupport;
import org.egolessness.destino.common.support.RequestSupport;
import org.egolessness.destino.common.utils.NumberUtils;
import org.egolessness.destino.core.container.ChannelContainer;
import org.egolessness.destino.core.container.MemberContainer;
import org.egolessness.destino.core.enumration.Errors;
import org.egolessness.destino.core.exception.ConnectionClosedException;
import org.egolessness.destino.core.model.Connection;
import org.egolessness.destino.common.fixedness.Callback;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.core.container.ConnectionContainer;
import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.core.enumration.PushType;
import org.egolessness.destino.core.model.Member;
import org.egolessness.destino.core.model.Receiver;
import org.egolessness.destino.core.support.MemberSupport;
import org.egolessness.destino.core.support.MessageSupport;
import org.egolessness.destino.registration.message.PushRequest;
import org.egolessness.destino.registration.message.RegistrationRequestAdapterGrpc;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;

/**
 * service rpc pusher
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class RpcPusher implements Pusher {

    private final MemberContainer memberContainer;

    private final ChannelContainer channelContainer;

    private final ConnectionContainer connectionContainer;

    @Inject
    public RpcPusher(ContainerFactory containerFactory) {
        this.memberContainer = containerFactory.getContainer(MemberContainer.class);
        this.channelContainer = containerFactory.getContainer(ChannelContainer.class);
        this.connectionContainer = containerFactory.getContainer(ConnectionContainer.class);
    }

    @Override
    public void push(final Receiver receiver, final Serializable pushData, final Callback<Response> callback) {
        if (PredicateUtils.isEmpty(receiver.id())) {
            callback.onThrowable(new DestinoException(Errors.PUSH_FAIL, "Receiver is invalid."));
            return;
        }

        Connection connection = connectionContainer.getConnection(receiver.id());

        Request request = RequestSupport.build(pushData);
        if (Objects.nonNull(connection)) {
            try {
                connection.request(request, callback);
            } catch (Exception e) {
                callback.onThrowable(e);
            }
            return;
        }

        if (receiver.id().startsWith(memberContainer.getCurrent().getId() + Mark.UNDERLINE.getValue())) {
            callback.onThrowable(new ConnectionClosedException());
            return;
        }

        long memberId = NumberUtils.parseLong(Mark.UNDERLINE.split(receiver.id())[0], -1);
        Optional<Member> memberOptional = memberContainer.find(memberId);
        if (!memberOptional.isPresent()) {
            callback.onThrowable(new ConnectionClosedException());
            return;
        }

        try {
            Member member = memberOptional.get();
            ManagedChannel channel = channelContainer.get(member.getAddress());
            PushRequest pushRequest = PushRequest.newBuilder().setConnectionId(receiver.id())
                    .setRequestContent(request).setTimeout(callback.getTimeoutMillis())
                    .build();
            ClientCall<PushRequest, Response> call = channel.newCall(this.getPushToClientMethod(member), CallOptions.DEFAULT);
            ListenableFuture<Response> future = ClientCalls.futureUnaryCall(call, pushRequest);

            Executor executor = callback.getExecutor();
            if (Objects.isNull(executor)) {
                executor = DestinoExecutors.CALLBACK;
            }

            Futures.addCallback(future, new FutureCallback<Response>() {
                @Override
                public void onSuccess(Response response) {
                    CallbackSupport.triggerResponse(callback, response);
                }
                @Override
                public void onFailure(@Nullable Throwable throwable) {
                    CallbackSupport.triggerThrowable(callback, throwable);
                }
            }, executor);
        } catch (Exception e) {
            CallbackSupport.triggerThrowable(callback, e);
        }
    }

    @Override
    public PushType type() {
        return PushType.RPC;
    }

    public MethodDescriptor<PushRequest, Response> getPushToClientMethod(Member member) {
        String contextPath = MemberSupport.getContextPath(member);
        if (PredicateUtils.isBlank(contextPath)) {
            return RegistrationRequestAdapterGrpc.getPushToClientMethod();
        }
        return MessageSupport.getMethodDescriptor(RegistrationRequestAdapterGrpc.getPushToClientMethod(), contextPath);
    }

}