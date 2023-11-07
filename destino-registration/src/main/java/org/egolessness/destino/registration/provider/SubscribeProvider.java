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

package org.egolessness.destino.registration.provider;

import org.egolessness.destino.core.model.Receiver;
import org.egolessness.destino.registration.model.Service;
import org.egolessness.destino.registration.model.ServiceSubscriber;

import java.util.List;
import java.util.function.Predicate;

/**
 * provider of service subscribe
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public interface SubscribeProvider {

    List<ServiceSubscriber> getSubscribers(Predicate<Service> predicate);

    int getSubscribeServiceCount(Receiver receiver);

}
