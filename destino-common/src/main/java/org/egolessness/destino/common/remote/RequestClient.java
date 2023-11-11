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

package org.egolessness.destino.common.remote;

import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.fixedness.Callback;
import org.egolessness.destino.common.fixedness.Lucermaire;
import org.egolessness.destino.common.enumeration.RequestChannel;
import org.egolessness.destino.common.model.message.Response;

import java.io.Serializable;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

/**
 * interface of request client
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public interface RequestClient extends Lucermaire {

    RequestChannel channel();

    Response request(Serializable request, Map<String, String> headers, Duration timeout) throws DestinoException, TimeoutException;

    Future<Response> request(Serializable request, Map<String, String> headers);

    void request(Serializable request, Map<String, String> headers, Callback<Response> callback);

}
