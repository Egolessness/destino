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

package com.egolessness.destino.core.fixedness;

import com.egolessness.destino.core.enumration.NodeState;
import com.egolessness.destino.common.model.Address;
import com.egolessness.destino.core.model.Member;

/**
 * listener of member state changed
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public interface MemberStateListener {

    void onCreated(Address address);

    void onError(Address address, String errMsg);

    void onRemoved(Address address);

    Member onChanged(Address address, NodeState state);

}
