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

package org.egolessness.destino.core.support;

import org.egolessness.destino.common.enumeration.RequestSchema;
import org.egolessness.destino.common.support.RequestSupport;
import org.egolessness.destino.core.enumration.DiscoveryType;
import org.egolessness.destino.core.enumration.MemberMetadata;
import org.egolessness.destino.core.enumration.NodeState;
import org.egolessness.destino.core.infrastructure.PortGetter;
import org.egolessness.destino.common.support.ProjectSupport;
import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.core.message.ConsistencyDomain;
import org.egolessness.destino.core.message.MulticastMemberInfo;
import org.egolessness.destino.core.properties.ServerProperties;
import org.egolessness.destino.core.model.Member;
import org.egolessness.destino.common.enumeration.Mark;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
 * support for server member.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class MemberSupport {

    public static List<ConsistencyDomain> getAvailableDomains() {
        List<ConsistencyDomain> domains = new ArrayList<>();
        for (ConsistencyDomain domain : ConsistencyDomain.values()) {
            if (domain == ConsistencyDomain.UNRECOGNIZED) {
                continue;
            }
            if (domain == ConsistencyDomain.DEFAULT) {
                continue;
            }
            domains.add(domain);
        }
        return domains;
    }

    public static String getMemberPhrase(Member member) {
        String address = member.getAddress().toString();
        String contextPath = member.getExtendVal(MemberMetadata.CONTEXT_PATH.getPropertyKey());
        if (PredicateUtils.isBlank(contextPath)) {
            return address;
        }
        return address + contextPath;
    }

    public static String getContextPath(Member member) {
        String contextPath = member.getExtendVal(MemberMetadata.CONTEXT_PATH.getPropertyKey());
        if (PredicateUtils.isNotBlank(contextPath)) {
            return contextPath;
        }
        return PredicateUtils.emptyString();
    }

    public static List<ConsistencyDomain> getAvailableDomains(Collection<ConsistencyDomain> excludes) {
        List<ConsistencyDomain> availableDomains = getAvailableDomains();
        if (PredicateUtils.isNotEmpty(excludes)) {
            availableDomains.removeAll(excludes);
        }
        return availableDomains;
    }

    public static void copy(Member source, Member dest) {
        dest.setId(source.getId());
        dest.setIp(source.getIp());
        dest.setPort(source.getPort());
        dest.setOuterPort(source.getOuterPort());
        dest.setState(source.getState());
        dest.setExtendInfo(source.getExtendInfo());
        dest.setSupportRemoteConnection(source.isSupportRemoteConnection());
    }

    public static Member build(String address) {
        URI uri = RequestSupport.parseUri(address, RequestSchema.GRPC);
        if (uri == null) {
            return null;
        }
        return build(uri.getHost(), uri.getPort(), uri.getPath());
    }

    public static Member build(MulticastMemberInfo memberInfo) {
        return build(memberInfo.getHost(), memberInfo.getPort(), memberInfo.getContextPath(), DiscoveryType.MULTICAST);
    }

    public static Member build(String ip, int port, String contextPath, DiscoveryType discoveryType) {
        Member target = build(ip, port, contextPath);
        target.setDiscoveryType(discoveryType);
        return target;
    }

    public static Member buildCurrent(String ip, PortGetter portGetter, ServerProperties serverProperties) {
        Member target = Member.newBuilder().ip(ip).port(portGetter.getInnerPort())
                .outerPort(portGetter.getOuterPort()).state(NodeState.UP).build();
        Map<String, String> extendInfo = new HashMap<>(4);
        extendInfo.put(MemberMetadata.VERSION.getPropertyKey(), ProjectSupport.getVersion());

        String contextPath = PropertiesSupport.getStandardizeContextPath(serverProperties);
        if (PredicateUtils.isNotEmpty(contextPath)) {
            extendInfo.put(MemberMetadata.CONTEXT_PATH.getPropertyKey(), contextPath);
        }

        target.setExtendInfo(extendInfo);
        return target;
    }

    public static Member build(String ip, int port, String contextPath) {
        return build(ip, port, contextPath, NodeState.STARTING);
    }

    public static Member build(String ip, int port, String contextPath, NodeState state) {
        Member member = Member.newBuilder().ip(ip).port(port).state(state).build();
        if (PredicateUtils.isNotBlank(contextPath)) {
            member.getExtendInfo().put(MemberMetadata.CONTEXT_PATH.getPropertyKey(), contextPath);
        }
        return member;
    }

    public static boolean hasChanged(Member actual, Member expected) {
        if (null == expected) {
            return null == actual;
        }
        if (!expected.getAddress().equals(actual.getAddress())) {
            return true;
        }
        if (!expected.getState().equals(actual.getState())) {
            return true;
        }
        if (expected.isSupportRemoteConnection() != actual.isSupportRemoteConnection()) {
            return true;
        }
        
        return isLargeChangedWithExtendInfo(expected, actual);
    }
    
    private static boolean isLargeChangedWithExtendInfo(Member expected, Member actual) {
        for (MemberMetadata metadata : MemberMetadata.important()) {
            String propertyKey = metadata.getPropertyKey();
            if (expected.getExtendInfo().containsKey(propertyKey) != actual.getExtendInfo().containsKey(propertyKey)) {
                return true;
            }
            if (!Objects.equals(expected.getExtendVal(propertyKey), actual.getExtendVal(propertyKey))) {
                return true;
            }
        }
        return false;
    }

    public static List<Member> parseMembersFromRemote(String configStr) {
        return parseMembersFromConfig(new StringReader(configStr), DiscoveryType.REMOTE);
    }

    public static List<Member> parseMembersFromConfig(Reader reader, DiscoveryType discoveryType) {
        BufferedReader bufferedReader = new BufferedReader(reader);

        Set<String> address = bufferedReader.lines().filter(StringUtils::isNotBlank).map(String::trim)
                .filter(line -> !line.startsWith(Mark.COMMENT.getValue()))
                .flatMap(line -> {
                    if (line.contains(Mark.COMMENT.getValue())) {
                        line = line.substring(0, line.indexOf(Mark.COMMENT.getValue()));
                        line = line.trim();
                    }
                    String[] lines = Mark.link(Mark.COMMA, Mark.BLANK).splitOne(line);
                    return Arrays.stream(lines).map(String::trim).filter(StringUtils::isNotBlank);
                }).collect(Collectors.toSet());

        return address.stream().map(MemberSupport::build)
                .filter(Objects::nonNull)
                .peek(member -> member.setDiscoveryType(discoveryType))
                .collect(Collectors.toList());
    }

}