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

package org.egolessness.destino.client.infrastructure;

import org.egolessness.destino.client.logging.DestinoLoggers;
import org.egolessness.destino.client.properties.ReceiverProperties;
import org.egolessness.destino.common.fixedness.Handler;
import org.egolessness.destino.common.fixedness.Lucermaire;
import org.egolessness.destino.common.model.message.UdpPacket;
import org.egolessness.destino.common.remote.RequestClient;
import org.egolessness.destino.common.support.RequestSupport;
import org.egolessness.destino.common.utils.ThreadUtils;
import com.google.protobuf.ByteString;

import javax.annotation.Nullable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

/**
 * udp receiver
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class UdpReceiver implements Lucermaire {

    public final static String ACK_UNKNOWN = "ack-unknown";

    private Map<String, Function<UdpPacket, UdpPacket>> handlers;

    private Function<UdpPacket, UdpPacket> defaultHandler;

    private ExecutorService executorService;
    
    private DatagramSocket datagramSocket;
    
    private volatile boolean started;

    private int port;
    
    public UdpReceiver(final RequestClient requestClient, final ReceiverProperties properties) {
        try {
            init(requestClient, properties);
        } catch (Exception e) {
            DestinoLoggers.REGISTRATION.error("[SERVICE RECEIVER] build udp socket has error", e);
        }
    }

    private void init(final RequestClient requestClient, final ReceiverProperties properties) throws SocketException {
        int udpPort = properties.getUdpPort();
        if (!properties.isEnabled() || RequestSupport.isSupportRequestStreamReceiver(requestClient.channel()) || udpPort < 0) {
            this.port = 0;
            this.handlers = null;
        } else {
            this.datagramSocket = new DatagramSocket(new InetSocketAddress(udpPort));
            this.port = datagramSocket.getLocalPort();
            this.defaultHandler = udpPacket -> buildUdpPacket(ACK_UNKNOWN, udpPacket.getNanos());
            this.handlers = new HashMap<>();
            this.started = true;
            this.executorService = ExecutorCreator.createReceiverExecutor();
            this.executorService.execute(this::receive);
        }
    }

    public void addHandler(Class<?> requestClass, Handler<byte[], byte[]> handler)
    {
        if (handlers == null) {
            return;
        }
        this.handlers.put(requestClass.getSimpleName(), udpPacket -> {
            try {
                byte[] response = handler.handle(udpPacket.getData().toByteArray());
                return buildUdpPacket(requestClass.getSimpleName(), udpPacket.getNanos(), response);
            } catch (Exception e) {
                return buildUdpPacket(requestClass.getSimpleName(), udpPacket.getNanos());
            }
        });
    }

    public int getPort() {
        return port;
    }
    
    public void receive() {
        while (started) {
            try {
                byte[] buffer = new byte[64 * 1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(packet);
                ByteString byteString = ByteString.copyFrom(packet.getData(), 0, packet.getLength());
                UdpPacket udpPacket = UdpPacket.parseFrom(byteString);

                Function<UdpPacket, UdpPacket> handler = this.handlers.getOrDefault(udpPacket.getType(), defaultHandler);
                UdpPacket ack = handler.apply(udpPacket);
                byte[] ackBytes = ack.toByteArray();
                datagramSocket.send(new DatagramPacket(ackBytes, ackBytes.length, packet.getSocketAddress()));
            } catch (Throwable e) {
                DestinoLoggers.UDP.error("[UDP-RECEIVER] udp socket has error", e);
            }
        }
    }

    private UdpPacket buildUdpPacket(String type, long nanos) {
        return buildUdpPacket(type, nanos, null);
    }

    private UdpPacket buildUdpPacket(String type, long nanos, @Nullable byte[] data) {
        UdpPacket.Builder builder = UdpPacket.newBuilder().setType(type).setNanos(nanos);
        if (data != null) {
            builder.setData(ByteString.copyFrom(data));
        }
        return builder.build();
    }

    @Override
    public void shutdown() {
        started = false;
        datagramSocket.close();
        ThreadUtils.shutdownThreadPool(executorService);
    }

}