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

package org.egolessness.destino.http.client;

import org.egolessness.destino.common.model.message.RequestChannel;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.fixedness.Picker;
import org.egolessness.destino.common.remote.RequestHighLevelClient;
import org.egolessness.destino.http.HttpChannel;

import java.net.URI;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import static org.egolessness.destino.common.enumeration.RequestClientState.RUNNING;

/**
 * http high-level request client.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class HttpHighLevelClient extends RequestHighLevelClient {

    private final HttpBaseClient httpBaseClient;

    public HttpHighLevelClient(Picker<URI> addressPicker, HttpChannel httpChannel) {
        super(new HttpSimpleClient(), addressPicker);
        this.httpBaseClient = new HttpBaseClient(httpChannel);
        ((HttpSimpleClient) this.SIMPLE_CLIENT).setClient(this.httpBaseClient);
    }

    @Override
    public boolean serverCheck() throws TimeoutException {
        return healthCheck();
    }

    @Override
    public synchronized boolean tryConnect(final URI address) {
        if (Objects.equals(httpBaseClient.getAddress(), address)) {
            httpBaseClient.setAddress(null);
            return false;
        }

        try {
            this.httpBaseClient.setAddress(address);
            ((HttpSimpleClient) this.SIMPLE_CLIENT).setClient(this.httpBaseClient);
            stateChange(RUNNING);
            return true;
        } catch (Exception e) {
            LOGGER.warn("The HTTP client failed to connect to server {}.", address, e);
        }

        return false;
    }

    @Override
    public void shutdown() throws DestinoException {
        super.shutdown();
    }

    @Override
    public RequestChannel channel() {
        return RequestChannel.HTTP;
    }

}
