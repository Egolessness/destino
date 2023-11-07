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

import org.egolessness.destino.common.fixedness.Callback;
import org.egolessness.destino.common.enumeration.Mark;
import org.egolessness.destino.common.model.message.Response;

import java.net.DatagramPacket;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * udp send record
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class SendRecord {

    private final String key;

    private final DatagramPacket packet;

    private final Callback<Response> callback;

    private final AtomicInteger repeatCount = new AtomicInteger();

    private final long terminatedNanos;
    
    public SendRecord(DatagramPacket packet, Callback<Response> callback, String type, long nanos) {
        this.packet = packet;
        this.callback = callback;
        this.terminatedNanos = Duration.ofNanos(nanos).plusMillis(callback.getTimeoutMillis()).toNanos();
        this.key = buildKey(packet.getAddress().getHostAddress(), packet.getPort(), type, nanos);
    }

    public String getKey() {
        return key;
    }

    public DatagramPacket getPacket() {
        return packet;
    }

    public Callback<Response> getCallback() {
        return callback;
    }

    public int getRepeatCount() {
        return repeatCount.get();
    }

    public long getTerminatedNanos() {
        return terminatedNanos;
    }

    public void repeat() {
        repeatCount.incrementAndGet();
    }
    
    public static String buildKey(String domain, int port, String type, long time) {
        return Mark.AND.join(domain, port, type, time);
    }

}