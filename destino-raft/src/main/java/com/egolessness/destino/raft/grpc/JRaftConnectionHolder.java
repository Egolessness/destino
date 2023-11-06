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

package com.egolessness.destino.raft.grpc;

import java.util.List;
import com.alipay.sofa.jraft.rpc.Connection;
import com.alipay.sofa.jraft.rpc.impl.ConnectionClosedEventListener;
import com.egolessness.destino.core.support.ConnectionSupport;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

/**
 * Get netty channel.
 */
public class JRaftConnectionHolder {

    private static final AttributeKey<NettyConnection> NETTY_CONN_KEY = AttributeKey.valueOf("netty.conn");

    public static Connection getConnection(final List<ConnectionClosedEventListener> listeners) {
        final Channel channel = ConnectionSupport.getChannel();
        final Attribute<NettyConnection> attr = channel.attr(NETTY_CONN_KEY);
        NettyConnection connection = attr.get();
        if (connection == null) {
            final NettyConnection nettyConnection = new NettyConnection(channel);
            connection = attr.setIfAbsent(nettyConnection);
            if (connection == null) {
                connection = nettyConnection;
                for (final ConnectionClosedEventListener l : listeners) {
                    connection.addClosedEventListener(l);
                }
            }
        }
        return connection;
    }
}

class NettyConnection implements Connection {

    private final Channel channel;

    NettyConnection(final Channel ch) {
        this.channel = ch;
    }

    @Override
    public Object setAttributeIfAbsent(final String key, final Object value) {
        return this.channel.attr(AttributeKey.valueOf(key)).setIfAbsent(value);
    }

    @Override
    public Object getAttribute(final String key) {
        return this.channel.attr(AttributeKey.valueOf(key)).get();
    }

    @Override
    public void setAttribute(final String key, final Object value) {
        this.channel.attr(AttributeKey.valueOf(key)).set(value);
    }

    @Override
    public void close() {
        this.channel.close();
    }

    void addClosedEventListener(final ConnectionClosedEventListener listener) {
      this.channel.closeFuture().addListener(future ->
              listener.onClosed(this.channel.remoteAddress().toString(), NettyConnection.this));
    }
}