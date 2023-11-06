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

package com.egolessness.destino.core.infrastructure.multicast;

import com.egolessness.destino.common.fixedness.Lucermaire;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.Objects;

/**
 * multicast sender
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class MulticastSender implements Lucermaire {

    private final MulticastSocket socket;

    public MulticastSender(MulticastSocket multicastSocket) {
        this.socket = multicastSocket;
    }

    public void send(final DatagramPacket packet) throws IOException {
        if (!socket.isClosed() && Objects.nonNull(packet)) {
            socket.send(packet);
        }
    }

    @Override
    public void shutdown() {
        socket.close();
    }
}