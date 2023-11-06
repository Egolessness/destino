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

package com.egolessness.destino.setting.repository;

import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.core.model.Member;
import com.egolessness.destino.core.model.MemberState;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * member repository interface.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public interface MemberRepository {

    boolean isAvailable();

    Member read(long id, Duration timeout) throws DestinoException, TimeoutException;

    Member register(Member member, Duration timeout) throws DestinoException, TimeoutException;

    Member update(long memberId, Member member, Duration timeout) throws DestinoException, TimeoutException;

    void updateStateAsync(Collection<MemberState> memberStates) throws DestinoException;

    Member deregister(long memberId, Duration timeout) throws DestinoException, TimeoutException;

    void removeUnnecessary(long timestamp, List<Long> memberIds, Duration timeout) throws DestinoException, TimeoutException;

}
