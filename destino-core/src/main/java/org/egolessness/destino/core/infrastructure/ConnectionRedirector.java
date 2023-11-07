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

package org.egolessness.destino.core.infrastructure;

import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.core.enumration.MemberMetadata;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.egolessness.destino.common.model.Address;
import org.egolessness.destino.common.model.request.ConnectionRedirectRequest;
import org.egolessness.destino.core.Loggers;
import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.core.container.MemberContainer;
import org.egolessness.destino.core.model.Connection;
import org.egolessness.destino.core.model.Member;

import java.util.*;

/**
 * connection redirector
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
@SuppressWarnings("SpellCheckingInspection")
public class ConnectionRedirector {

    private final MemberContainer memberContainer;

    private volatile long lastRecommendServerId;

    @Inject
    public ConnectionRedirector(ContainerFactory containerFactory) {
        this.memberContainer = containerFactory.getContainer(MemberContainer.class);
    }

    public void redirect(Connection connection) {
        getNextServer().ifPresent(member -> {
            Object path = member.getExtendInfo().get(MemberMetadata.CONTEXT_PATH.getPropertyKey());
            ConnectionRedirectRequest redirectRequest = new ConnectionRedirectRequest(member.getIp(),
                    member.getOuterPort(), Objects.toString(path, PredicateUtils.emptyString()));
            connection.request(redirectRequest, Collections.emptyMap());
            Loggers.RPC.info("Send a redirect request to connection {}, recommend server is {}",
                    connection.getId(), Address.of(redirectRequest.getIp(), redirectRequest.getPort()));
        });
    }

    private synchronized Optional<Member> getNextServer() {
        List<Member> members = memberContainer.otherRegisteredMembers();
        if (members.isEmpty()) {
            return Optional.empty();
        }

        members.sort(Comparator.comparingLong(Member::getId));
        for (Member member : members) {
            if (member.getId() > lastRecommendServerId) {
                lastRecommendServerId = member.getId();
                return Optional.of(member);
            }
        }

        Member member = members.get(0);
        lastRecommendServerId = member.getId();
        return Optional.of(member);
    }

}
