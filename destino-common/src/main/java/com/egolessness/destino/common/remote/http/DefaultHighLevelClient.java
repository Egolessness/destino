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

package com.egolessness.destino.common.remote.http;

import com.egolessness.destino.common.model.message.RequestChannel;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.common.fixedness.Picker;
import com.egolessness.destino.common.remote.RequestHighLevelClient;

import java.net.URI;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import static com.egolessness.destino.common.enumeration.RequestClientState.RUNNING;

/**
 * default request high-level client
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class DefaultHighLevelClient extends RequestHighLevelClient {

    public DefaultHighLevelClient(DefaultBaseClient baseClient, Picker<URI> addressPicker) {
        super(new DefaultSimpleClient(baseClient), addressPicker);
    }

    @Override
    public boolean serverCheck() throws TimeoutException {
        return healthCheck();
    }

    @Override
    public synchronized boolean tryConnect(final URI address) {
        DefaultSimpleClient simpleClient = (DefaultSimpleClient) super.SIMPLE_CLIENT;
        if (Objects.equals(simpleClient.getAddress(), address)) {
            simpleClient.setAddress(null);
            return false;
        }

        simpleClient.setAddress(address);
        stateChange(RUNNING);
        return true;
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
