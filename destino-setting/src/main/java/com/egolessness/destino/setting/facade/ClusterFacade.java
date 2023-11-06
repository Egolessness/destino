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

package com.egolessness.destino.setting.facade;

import com.egolessness.destino.setting.provider.ClusterProvider;
import com.egolessness.destino.setting.request.MemberPageRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.common.model.Address;
import com.egolessness.destino.common.model.Page;
import com.egolessness.destino.common.model.Pageable;
import com.egolessness.destino.common.model.message.Request;
import com.egolessness.destino.common.model.message.Response;
import com.egolessness.destino.common.support.RequestSupport;
import com.egolessness.destino.common.support.ResponseSupport;
import com.egolessness.destino.core.annotation.Authorize;
import com.egolessness.destino.core.enumration.Action;
import com.egolessness.destino.core.infrastructure.reader.SafetyReaderRegistry;
import com.egolessness.destino.core.message.ConsistencyDomain;
import com.egolessness.destino.core.model.Member;
import com.egolessness.destino.setting.model.DomainLeader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.egolessness.destino.core.message.ConsistencyDomain.SETTING;

/**
 * server cluster facade.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class ClusterFacade {

    private final ClusterProvider clusterProvider;

    private final SafetyReaderRegistry safetyReaderRegistry;

    @Inject
    public ClusterFacade(final ClusterProvider clusterProvider, final SafetyReaderRegistry safetyReaderRegistry) {
        this.clusterProvider = clusterProvider;
        this.safetyReaderRegistry = safetyReaderRegistry;
        this.safetyReaderRegistry.registerProcessor(MemberPageRequest.class, this::members0);
    }

    public boolean isAvailable() {
        return clusterProvider.isAvailable();
    }

    @Authorize(domain = SETTING, action = Action.READ)
    public Page<Member> pageMembers(final Pageable pageable, final String address) {
        Request request = RequestSupport.build(new MemberPageRequest(pageable, address));
        Response response = safetyReaderRegistry.execute(SETTING, request);
        return ResponseSupport.dataDeserializeWithTypeReference(response, new TypeReference<Page<Member>>() {});
    }

    @Authorize(domain = SETTING, action = Action.WRITE)
    public void register(final Address address, final List<ConsistencyDomain> excludes) throws DestinoException {
        clusterProvider.register(address, excludes);
    }

    @Authorize(domain = SETTING, action = Action.DELETE)
    public void deregister(final long id) throws DestinoException {
        clusterProvider.deregister(id);
    }

    private Response members0(final MemberPageRequest pageRequest) {
        Page<Member> memberPage = clusterProvider.pageMembers(pageRequest.toPredicate(), pageRequest);
        return ResponseSupport.success(memberPage);
    }

    @Authorize(domain = SETTING, action = Action.READ)
    public List<DomainLeader> leaders() {
        Map<ConsistencyDomain, Member> leaderMap = clusterProvider.allLeader();
        List<DomainLeader> leaders = new ArrayList<>(leaderMap.size());
        leaderMap.forEach(((domain, member) -> leaders.add(new DomainLeader(domain, member))));
        return leaders;
    }

}
