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

package com.egolessness.destino.registration.pusher;

import com.egolessness.destino.common.fixedness.Callback;
import com.egolessness.destino.common.model.message.Response;
import com.egolessness.destino.core.enumration.PushType;
import com.egolessness.destino.core.model.Receiver;

import java.io.Serializable;

/**
 * interface of service pusher
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public interface Pusher {

    void push(Receiver receiver, Serializable pushData, Callback<Response> callback);

    PushType type();

}

