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

package org.egolessness.destino.core.infrastructure.multicast;

import org.egolessness.destino.core.Loggers;
import org.egolessness.destino.core.message.MulticastMemberInfo;

import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.Optional;

/**
 * multicast receiver
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class MulticastReceiver {

    private static final int DATAGRAM_BUFFER_SIZE = 64 * 1024;

    private final MulticastSocket multicastSocket;


    public MulticastReceiver(MulticastSocket multicastSocket) {
        this.multicastSocket = multicastSocket;
    }

    public Optional<MulticastMemberInfo> receive() {
        try {
            DatagramPacket datagramPacketReceive = new DatagramPacket(new byte[DATAGRAM_BUFFER_SIZE], DATAGRAM_BUFFER_SIZE);
            multicastSocket.receive(datagramPacketReceive);
            MulticastMemberInfo multicastMemberInfo = MulticastMemberInfo.parser()
                    .parseFrom(datagramPacketReceive.getData(), 0, datagramPacketReceive.getLength());
            return Optional.ofNullable(multicastMemberInfo);
        } catch (SocketTimeoutException e) {
            Loggers.CLUSTER.debug("Multicast socket receive timeout.");
        } catch (Exception e) {
            Loggers.CLUSTER.debug("Multicast socket receive has error.", e);
        }
        return Optional.empty();
    }

}