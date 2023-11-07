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

package org.egolessness.destino.registration.publisher;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.egolessness.destino.common.fixedness.Callback;
import org.egolessness.destino.common.model.ServiceMercury;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.common.model.request.ServicePushRequest;
import org.egolessness.destino.common.support.ResponseSupport;
import org.egolessness.destino.core.Loggers;
import org.egolessness.destino.core.enumration.PushType;
import org.egolessness.destino.registration.pusher.Pusher;
import org.egolessness.destino.registration.pusher.RpcPusher;
import org.egolessness.destino.registration.pusher.UdpPusher;
import org.egolessness.destino.core.model.Receiver;
import org.egolessness.destino.registration.support.RegistrationSupport;
import org.egolessness.destino.registration.model.Service;
import org.egolessness.destino.registration.model.ServiceSubscriber;

import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/**
 * push task factory
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class PushTaskFactory {

    private final Function<PushType, Pusher> pusherGetter;

    @Inject
    public PushTaskFactory(Injector injector) {
        this.pusherGetter = buildPusherGetter(injector);
    }

    private Function<PushType, Pusher> buildPusherGetter(Injector injector) {
        Pusher[] pushers = new Pusher[PushType.values().length];
        return pushType -> {
            if (pushers[pushType.ordinal()] == null) {
                synchronized (pushers) {
                    if (pushers[pushType.ordinal()] == null) {
                        if (pushType == PushType.RPC) {
                            return pushers[pushType.ordinal()] = injector.getInstance(RpcPusher.class);
                        }
                        return pushers[pushType.ordinal()] = injector.getInstance(UdpPusher.class);
                    }
                }
            }
            return pushers[pushType.ordinal()];
        };
    }

    public Runnable newTask(SubscribeChannel channel, Service service, PushedHandler pushedHandler) {
        return () -> {
            for (ServiceSubscriber subscriber : channel.getSubscribers()) {
                if (!subscriber.isPushable()) {
                    continue;
                }
                ServiceMercury serviceMercury = RegistrationSupport.buildServiceMercury(service, subscriber);
                ServicePushRequest pushRequest = new ServicePushRequest(serviceMercury);
                push(pushRequest, service, subscriber, subscriber, pushedHandler);
            }
        };
    }

    public Runnable newTask(Service service, ServiceSubscriber subscriber, PushedHandler pushedHandler) {
        return () -> {
            ServiceMercury serviceMercury = RegistrationSupport.buildServiceMercury(service, subscriber);
            ServicePushRequest pushRequest = new ServicePushRequest(serviceMercury);
            push(pushRequest, service, subscriber, subscriber, pushedHandler);
        };
    }

    private void push(ServicePushRequest pushRequest, Service service, ServiceSubscriber subscriber,
                      Receiver receiver, PushedHandler pushedHandler) {
        Callback<Response> callback = buildCallback(pushRequest, service, subscriber, receiver, pushedHandler);
        pusherGetter.apply(subscriber.type()).push(receiver, pushRequest, callback);
    }

    public Callback<Response> buildCallback(ServicePushRequest pushRequest, Service service, ServiceSubscriber subscriber,
                                            Receiver receiver, PushedHandler pushedHandler) {
        return new Callback<Response>() {

            @Override
            public void onResponse(Response response) {
                if (ResponseSupport.isSuccess(response)) {
                    Loggers.PUSH.debug("Successful push service to subscriber:{}, namespace:{}, service:{}.",
                            subscriber.getAddress(), service.getNamespace(), service.getServiceName());
                    pushedHandler.onSuccess(subscriber);
                } else {
                    Loggers.PUSH.warn("Failed to push service to receiver:{}, namespace:{}, service:{}.",
                            subscriber.getAddress(), service.getNamespace(), service.getServiceName());
                    Receiver next = receiver.next();
                    if (next != null) {
                        push(pushRequest, service, subscriber, next, pushedHandler);
                    } else {
                        pushedHandler.onFail(subscriber);
                    }
                }
            }

            @Override
            public void onThrowable(Throwable e) {
                if (e instanceof TimeoutException) {
                    Loggers.PUSH.warn("Timed out while push service to subscriber {}", getSubscriberInfo(subscriber));
                } else {
                    Loggers.PUSH.warn("An error occurred while push service to subscriber: {}", getSubscriberInfo(subscriber), e);
                }

                Receiver next = receiver.next();
                if (next != null) {
                    push(pushRequest, service, subscriber, next, pushedHandler);
                } else {
                    pushedHandler.onFail(subscriber);
                }
            }

        };
    }

    private String getSubscriberInfo(ServiceSubscriber subscriber) {
        if (subscriber.type() == PushType.UDP) {
            return PushType.UDP + "#" + subscriber.getIp() + ":" + subscriber.getUdpPort();
        }
        return subscriber.type() + "#" + subscriber.getAddress();
    }

}
