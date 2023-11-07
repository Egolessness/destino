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

package org.egolessness.destino.registration.pusher.udp;

import org.egolessness.destino.common.model.message.UdpPacket;
import org.egolessness.destino.core.model.Member;
import org.egolessness.destino.registration.properties.UdpPushProperties;
import org.egolessness.destino.common.support.CallbackSupport;
import org.egolessness.destino.core.enumration.Errors;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.fixedness.Lucermaire;
import org.egolessness.destino.common.support.ResponseSupport;
import org.egolessness.destino.core.infrastructure.executors.UdpExecutors;
import org.egolessness.destino.core.Loggers;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.net.*;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * udp component
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class UdpComponent implements Lucermaire {
    
    private final ConcurrentMap<String, SendRecord> records;

    private final UdpSender sender;

    private final UdpReceiver receiver;

    private volatile boolean shutdown = false;

    private final long senderInterval;

    private final long receiveTimeout;

    private final int retryCount;
    
    public UdpComponent(final UdpPushProperties udpPushProperties, Member member) throws SocketException {
        DatagramSocket udpSocket = new DatagramSocket(new InetSocketAddress(member.getIp(), udpPushProperties.getPort()));
        udpSocket.setSoTimeout(30000);
        this.sender = new UdpSender(udpSocket);
        this.receiver = new UdpReceiver(udpSocket);
        this.records = new ConcurrentHashMap<>();
        this.receiveTimeout = Duration.ofMillis(udpPushProperties.getReceiveMaxTimeout()).getNano();
        this.senderInterval = udpPushProperties.getRetryInterval();
        this.retryCount = udpPushProperties.getRetryCount();
        UdpExecutors.UDP.submit(this::receive);
    }

    public void send(final DatagramPacket packet) throws IOException {
        sender.send(packet);
    }

    public void sendRepeat(final SendRecord sendRecord) {
        records.put(sendRecord.getKey(), sendRecord);
        startSend(sendRecord);
    }

    private void startSend(final SendRecord sendRecord) {
        if (!records.containsKey(sendRecord.getKey())) {
            return;
        }
        if (System.nanoTime() >= sendRecord.getTerminatedNanos()) {
            records.remove(sendRecord.getKey());
            sendRecord.getCallback().onThrowable(new TimeoutException());
            return;
        }
        if (sendRecord.getRepeatCount() > retryCount) {
            records.remove(sendRecord.getKey());
            sendRecord.getCallback().onThrowable(new DestinoException(Errors.PUSH_UDP_FAIL, "udp send repeat count too much for key:" + sendRecord));
            return;
        }
        try {
            sender.send(sendRecord.getPacket());
            sendRecord.repeat();
            long delayMillis = Long.min(sendRecord.getTerminatedNanos() - System.nanoTime(), senderInterval);
            UdpExecutors.UDP.schedule(() -> startSend(sendRecord), delayMillis, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            records.remove(sendRecord.getKey());
            sendRecord.getCallback().onThrowable(e);
        }
    }

    private void receive() {
        while (!shutdown) {
            try {
                DatagramPacket packet = receiver.receive();
                ByteString byteString = ByteString.copyFrom(packet.getData(), 0, packet.getLength());
                UdpPacket udpPacket = UdpPacket.parseFrom(byteString);
                String ip = packet.getAddress().getHostAddress();
                int port = packet.getPort();
                String senderKey = SendRecord.buildKey(ip, port, udpPacket.getType(), udpPacket.getNanos());
                if (System.nanoTime() - udpPacket.getNanos() > receiveTimeout) {
                    Loggers.PUSH.debug("[UDP-RECEIVER] timed out for sender key {}", senderKey);
                }
                SendRecord sendRecord = records.remove(senderKey);
                if (Objects.nonNull(sendRecord)) {
                    CallbackSupport.triggerResponse(sendRecord.getCallback(), ResponseSupport.success(udpPacket.getData().toByteArray()));
                }
            } catch (SocketTimeoutException ignored) {
            } catch (Exception e) {
                Loggers.PUSH.error("[UDP-RECEIVER] Receiver has error", e);
            }
        }
    }

    @Override
    public void shutdown() throws DestinoException {
        this.shutdown = true;
        this.sender.shutdown();
    }
}