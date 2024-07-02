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

package org.egolessness.destino.server.discovery;

import org.egolessness.destino.common.enumeration.RequestSchema;
import org.egolessness.destino.common.utils.PredicateUtils;
import com.google.inject.Inject;
import org.egolessness.destino.common.model.Address;
import org.egolessness.destino.core.enumration.DiscoveryType;
import org.egolessness.destino.core.enumration.NodeState;
import org.egolessness.destino.core.model.Member;
import org.egolessness.destino.core.Loggers;
import org.egolessness.destino.core.properties.DiscoveryProperties;
import org.egolessness.destino.core.properties.RemoteProperties;
import org.egolessness.destino.core.support.MemberSupport;
import com.linecorp.armeria.client.WebClient;
import com.linecorp.armeria.common.AggregatedHttpResponse;
import com.linecorp.armeria.common.HttpResponse;
import org.egolessness.destino.server.spi.DiscoveryStrategy;
import org.apache.commons.lang.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * members discovery from remote address.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RemoteDiscoveryStrategy implements DiscoveryStrategy {
    
    public String address;
    
    private int addressServerFailCount = 0;
    
    private volatile boolean shutdown = false;
    
    private final Set<Member> MEMBERS_BUFFER = new HashSet<>();

    @Inject
    private RemoteProperties remoteProperties;

    @Inject
    private DiscoveryProperties discoveryProperties;

    @Override
    public DiscoveryType type() {
        return DiscoveryType.REMOTE;
    }

    @Override
    public void start() {
        address = remoteProperties.getAddress();
        if (StringUtils.isBlank(address)) {
            String domainName = remoteProperties.getDomain();
            int addressPort = remoteProperties.getPort();
            String addressUrl = remoteProperties.getUrl();
            if (PredicateUtils.isEmpty(addressUrl)) {
                addressUrl = PredicateUtils.emptyString();
            } else if (!addressUrl.startsWith("/")) {
                addressUrl = "/" + addressUrl;
            }
            address = RequestSchema.HTTP.getPrefix() + Address.of(domainName, addressPort) + addressUrl;
        }
    }

    @Override
    public Collection<Member> discoverMembers() {
        if (shutdown) {
            return Collections.emptyList();
        }
        return discoveryFromRemote();
    }

    private Collection<Member> discoveryFromRemote() {
        HttpResponse httpResponse = WebClient.of(address).get("");

        try {
            AggregatedHttpResponse response = httpResponse.aggregate().get(discoveryProperties.getTimeout(), TimeUnit.MILLISECONDS);
            String result = response.content(StandardCharsets.UTF_8);

            try {
                List<Member> members = MemberSupport.parseMembersFromRemote(result);
                members.forEach(MEMBERS_BUFFER::remove);
                MEMBERS_BUFFER.forEach(member -> member.setState(NodeState.DOWN));
                MEMBERS_BUFFER.addAll(members);
                return MEMBERS_BUFFER;
            } catch (Throwable e) {
                Loggers.DISCOVERY.error("Remote discovery has error when handle response form {}.", address, e);
            }
            addressServerFailCount = 0;

        } catch (Exception e) {
            addressServerFailCount++;
            Loggers.DISCOVERY.warn("Failed to request remote address:{}", address);
            int maxRetry = remoteProperties.getRetry();
            if (addressServerFailCount >= maxRetry) {
                shutdown = true;
                Loggers.DISCOVERY.warn("Remote discovery closed, failure count:{} >= max retry:{}", addressServerFailCount, maxRetry);
            }
        }

        return Collections.emptyList();
    }
    
    @Override
    public void destroy() {
        shutdown = true;
    }

}