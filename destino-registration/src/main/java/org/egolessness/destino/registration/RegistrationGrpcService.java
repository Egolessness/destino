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

package org.egolessness.destino.registration;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.grpc.stub.StreamObserver;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.common.support.ResponseSupport;
import org.egolessness.destino.core.container.ConnectionContainer;
import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.core.model.Connection;
import org.egolessness.destino.registration.message.PushRequest;
import org.egolessness.destino.registration.message.RegistrationRequestAdapterGrpc;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

/**
 * resource finder of registration.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class RegistrationGrpcService extends RegistrationRequestAdapterGrpc.RegistrationRequestAdapterImplBase {

    private final ConnectionContainer connectionContainer;

    @Inject
    public RegistrationGrpcService(ContainerFactory containerFactory) {
        this.connectionContainer = containerFactory.getContainer(ConnectionContainer.class);
    }

    @Override
    public void pushToClient(PushRequest pushRequest, StreamObserver<Response> responseObserver) {
        Connection connection = connectionContainer.getConnection(pushRequest.getConnectionId());
        if (Objects.isNull(connection)) {
            responseObserver.onNext(ResponseSupport.failed("Connection closed."));
            responseObserver.onCompleted();
            return;
        }

        try {
            Response response = connection.request(pushRequest.getRequestContent(), Duration.ofMillis(pushRequest.getTimeout()));
            responseObserver.onNext(response);
        } catch (DestinoException e) {
            responseObserver.onNext(ResponseSupport.failed(e.getErrMsg()));
        } catch (TimeoutException e) {
            responseObserver.onNext(ResponseSupport.failed("Push timeout."));
        }

        responseObserver.onCompleted();
    }
}
