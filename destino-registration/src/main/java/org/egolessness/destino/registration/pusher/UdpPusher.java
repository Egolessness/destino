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

import org.egolessness.destino.common.model.message.UdpPacket;
import org.egolessness.destino.core.model.Member;
import org.egolessness.destino.registration.properties.UdpPushProperties;
import org.egolessness.destino.registration.pusher.udp.SendRecord;
import org.egolessness.destino.registration.pusher.udp.UdpComponent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import org.egolessness.destino.core.model.Receiver;
import org.egolessness.destino.common.fixedness.Callback;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.core.enumration.SerializeType;
import org.egolessness.destino.core.enumration.PushType;
import org.egolessness.destino.core.utils.ThreadUtils;
import org.egolessness.destino.core.Loggers;
import org.egolessness.destino.core.infrastructure.serialize.Serializer;
import org.egolessness.destino.core.infrastructure.serialize.SerializerFactory;
import org.egolessness.destino.common.utils.ByteUtils;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * service udp pusher
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class UdpPusher implements Pusher {

    private final UdpComponent udpComponent;

    private final Serializer serializer;

    @Inject
    public UdpPusher(UdpPushProperties udpPushProperties, Member member) throws SocketException {
        this.udpComponent = new UdpComponent(udpPushProperties, member);
        this.serializer = SerializerFactory.getSerializer(SerializeType.JSON);
        ThreadUtils.addShutdownHook(this.udpComponent::shutdown);
    }

    @Override
    public void push(final Receiver receiver, final Serializable pushData, final Callback<Response> callBack) {
        if (receiver.getUdpPort() <= 0 || receiver.getUdpPort() >= 0xFFFF) {
            return;
        }

        try {
            SendRecord sendRecord = buildSenderData(receiver, pushData, callBack);
            udpComponent.sendRepeat(sendRecord);
        } catch (Exception e) {
            Loggers.PUSH.error("[UDP-PUSHER] push failed, data: {}", pushData, e);
        }
    }

    @Override
    public PushType type() {
        return PushType.UDP;
    }

    private DatagramPacket buildPacket(final Receiver receiver, final UdpPacket udpPacket) {
        InetSocketAddress socketAddress = new InetSocketAddress(receiver.getIp(), receiver.getUdpPort());
        byte[] bytes = udpPacket.toByteArray();
        return new DatagramPacket(bytes, bytes.length, socketAddress);
    }

    private SendRecord buildSenderData(final Receiver receiver, final Serializable pushData,
                                       final Callback<Response> callback) {
        long nanos = System.nanoTime();
        byte[] buffer;
        if (pushData instanceof Message) {
            buffer = ((Message) pushData).toByteArray();
        } else {
            buffer = ByteUtils.compress(serializer.serialize(pushData));
        }
        UdpPacket udpPacket = buildUdpPacket(pushData.getClass().getSimpleName(), nanos, buffer);
        DatagramPacket packet = buildPacket(receiver, udpPacket);
        return new SendRecord(packet, callback, udpPacket.getType(), nanos);
    }

    private UdpPacket buildUdpPacket(String type, long nanos, @Nullable byte[] data) {
        UdpPacket.Builder builder = UdpPacket.newBuilder().setType(type).setNanos(nanos);
        if (data != null) {
            builder.setData(ByteString.copyFrom(data));
        }
        return builder.build();
    }

}
