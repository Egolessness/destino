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

package org.egolessness.destino.core.model;

import org.egolessness.destino.core.enumration.PushType;
import org.egolessness.destino.core.fixedness.ReceiverId;
import org.egolessness.destino.common.model.Address;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * client receiver
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class Receiver implements ReceiverId {

    private static final long serialVersionUID = 8323865996753268886L;

    private final PushType type;

    private final String id;

    private final Receiver next;

    private final Address address;

    private int udpPort;

    public Receiver(String ip, int port, String connectionId, int udpPort) {
        this.id = connectionId;
        this.address = Address.of(ip, port);
        this.type = PushType.RPC;
        this.next = new Receiver(ip, port, udpPort);
    }

    public Receiver(String ip, int port, String connectionId) {
        this.address = Address.of(ip, port);
        this.type = PushType.RPC;
        this.id = connectionId;
        this.next = null;
    }

    public Receiver(String ip, int port, int udpPort) {
        this.udpPort = udpPort;
        this.address = Address.of(ip, port);
        this.type = PushType.UDP;
        this.id = Address.of(ip, udpPort).toString();
        this.next = null;
    }

    public String getIp() {
        return address.getHost();
    }

    public int getUdpPort() {
        return udpPort;
    }

    public void setUdpPort(int udpPort) {
        this.udpPort = udpPort;
    }

    public Address getAddress() {
        return address;
    }

    @Override
    public PushType type() {
        return type;
    }

    @Override
    public String id() {
        return id;
    }

    @Nullable
    public Receiver next() {
        return next;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Receiver receiver = (Receiver) o;
        return type == receiver.type && Objects.equals(id, receiver.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, id);
    }

}
