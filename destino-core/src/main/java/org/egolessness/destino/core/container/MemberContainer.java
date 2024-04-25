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

package org.egolessness.destino.core.container;

import org.egolessness.destino.core.enumration.ElementOperation;
import org.egolessness.destino.core.enumration.NodeState;
import org.egolessness.destino.core.exception.DuplicateIdException;
import org.egolessness.destino.core.exception.OverLimitException;
import org.egolessness.destino.core.infrastructure.InetRefresher;
import org.egolessness.destino.core.infrastructure.MemberIdAssigner;
import org.egolessness.destino.core.infrastructure.PortGetter;
import org.egolessness.destino.core.infrastructure.notify.Notifier;
import org.egolessness.destino.core.properties.NotifyProperties;
import org.egolessness.destino.core.properties.ServerProperties;
import com.google.inject.Inject;
import org.egolessness.destino.common.model.Address;
import org.egolessness.destino.core.support.MemberSupport;
import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.core.event.MembersChangedEvent;
import org.egolessness.destino.core.model.Member;
import org.egolessness.destino.core.Loggers;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * container of server member
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class MemberContainer extends MemberIdAssigner implements Container {

    private final Map<Long, Member> ID_INDEXER = new ConcurrentHashMap<>();

    private Map<Address, Member> ADDRESS_INDEXER = new ConcurrentHashMap<>();

    private final Map<String, String> CONTEXT_PATH_INDEXER = new ConcurrentHashMap<>();

    private final Member current;

    private final Notifier notifier;

    @Inject
    public MemberContainer(Notifier notifier, NotifyProperties notifyProperties, PortGetter portGetter,
                           InetRefresher inetRefresher, ServerProperties serverProperties) {
        this.notifier = notifier;
        this.notifier.addPublisher(MembersChangedEvent.class, notifyProperties.getMemberBufferSize());
        this.current = initCurrent(inetRefresher, portGetter, serverProperties);
        this.ADDRESS_INDEXER.put(current.getAddress(), current);
        this.updateContextPathIndex(this.current);
    }

    public int size() {
        return ADDRESS_INDEXER.size();
    }

    public boolean isEmpty() {
        return ADDRESS_INDEXER.isEmpty();
    }

    public Set<Long> ids() {
        return ID_INDEXER.keySet();
    }

    public List<Member> members() {
        return new ArrayList<>(ID_INDEXER.values());
    }

    public @Nullable Member get(Address address) {
        return ADDRESS_INDEXER.get(address);
    }

    public @Nullable String getContextPath(String address) {
        return CONTEXT_PATH_INDEXER.get(address);
    }

    public Optional<Member> find(Address address) {
        return Optional.ofNullable(ADDRESS_INDEXER.get(address));
    }

    private Member initCurrent(InetRefresher inetRefresher, PortGetter portGetter, ServerProperties serverProperties) {
        return MemberSupport.buildCurrent(inetRefresher.getCurrentIp(), portGetter, serverProperties);
    }

    public synchronized void updateCurrentIp(String ip) {
        if (Objects.equals(ip, current.getIp())) {
            return;
        }
        ADDRESS_INDEXER.remove(current.getAddress());
        current.setIp(ip);
        ADDRESS_INDEXER.put(current.getAddress(), current);
        notifyMemberChange(current, ElementOperation.UPDATE);
    }

    public Member getCurrent() {
        return current;
    }

    public synchronized void addMember(Address address) {
        ADDRESS_INDEXER.computeIfAbsent(address, key -> {
            Member member = MemberSupport.build(address.getHost(), address.getPort(), "", NodeState.UP);
            notifyMemberChange(member, ElementOperation.ADD);
            return member;
        });
    }

    public synchronized void addMember(Member member) throws Exception {
        if (member.getId() >= 0) {
            Member existed = ID_INDEXER.get(member.getId());
            if (existed != null && !Objects.equals(existed, member)) {
                throw new DuplicateIdException("Duplicate id for member " + member.getAddress());
            }
        }

        if (member.getId() > getMaxMemberId()) {
            throw new OverLimitException("The member ID exceeds the upper limit.");
        }

        Member origin = ADDRESS_INDEXER.get(member.getAddress());

        if (member.getId() < 0) {
            if (origin != null && origin.getId() > -1) {
                member.setId(origin.getId());
            } else {
                member.setId(getNextMemberId());
            }
        }

        ID_INDEXER.put(member.getId(), member);
        ADDRESS_INDEXER.put(member.getAddress(), member);

        if (Objects.equals(member, current)) {
            current.setId(member.getId());
            return;
        }

        updateContextPathIndex(member);

        notifyMemberChange(member, ElementOperation.ADD);
    }

    private void updateContextPathIndex(Member member) {
        String val = MemberSupport.getContextPath(member);
        if (PredicateUtils.isNotBlank(val)) {
            CONTEXT_PATH_INDEXER.put(member.getAddress().toString(), val);
        } else {
            CONTEXT_PATH_INDEXER.remove(member.getAddress().toString());
        }
    }

    public Optional<Member> find(long memberId) {
        return Optional.ofNullable(ID_INDEXER.get(memberId));
    }

    public synchronized Member remove(long id) {
        Member removed = ID_INDEXER.remove(id);
        if (removed != null) {
            ADDRESS_INDEXER.remove(removed.getAddress());
            CONTEXT_PATH_INDEXER.remove(removed.getAddress().toString());
            notifyMemberChange(removed, ElementOperation.REMOVE);
        }
        return removed;
    }

    public synchronized Member remove(long id, long timestamp) {
        Member member = ID_INDEXER.get(id);
        if (member == null) {
            return null;
        }
        if (member.getLastActiveTime() < timestamp) {
            return remove(id);
        }
        return null;
    }

    @Override
    public boolean containsId(long memberId) {
        return ID_INDEXER.containsKey(memberId);
    }

    public synchronized boolean updateMember(Member newMember) {
        Loggers.CLUSTER.debug("member information update : {}", newMember);
        Address address = newMember.getAddress();
        Member oldMember = ADDRESS_INDEXER.computeIfPresent(address, (s, member) -> {
            boolean isPublishChangeEvent = MemberSupport.hasChanged(newMember, member);
            newMember.refreshLastActiveTime();
            MemberSupport.copy(newMember, member);
            updateContextPathIndex(newMember);
            if (isPublishChangeEvent) {
                notifyMemberChange(member, ElementOperation.UPDATE);
            }
            return member;
        });
        return Objects.nonNull(oldMember);
    }

    public Set<Member> loadMembers() {
        return new HashSet<>(ADDRESS_INDEXER.values());
    }

    public List<Address> otherAddresses() {
        List<Address> addresses = new ArrayList<>(ADDRESS_INDEXER.keySet());
        addresses.remove(current.getAddress());
        return addresses;
    }

    public Set<Member> loadRegisteredMembers() {
        return new HashSet<>(ID_INDEXER.values());
    }

    public List<Member> otherRegisteredMembers() {
        Map<Long, Member> members = new HashMap<>(ID_INDEXER);
        members.remove(current.getId());
        return new ArrayList<>(members.values());
    }

    public synchronized void memberChange(Collection<Member> changeMembers) {
        Set<Member> members = changeMembers.stream().filter(member -> member.getState() != NodeState.DOWN).collect(Collectors.toSet());
        if (PredicateUtils.isEmpty(members)) {
            return;
        }

        Address currentAddress = current.getAddress();
        boolean inCluster = false;
        boolean hasChange = members.size() != ADDRESS_INDEXER.size();
        Map<Address, Member> newMemberMap = new ConcurrentHashMap<>();

        for (Member member : members) {
            final Address address = member.getAddress();
            if (Objects.equals(currentAddress, address)) {
                inCluster = true;
                newMemberMap.put(currentAddress, current);
                continue;
            }
            Optional<Member> memberOptional = find(address);
            if (memberOptional.isPresent()) {
                if (member.getExtendInfo().isEmpty()) {
                    member.setExtendInfo(memberOptional.get().getExtendInfo());
                }
            } else {
                hasChange = true;
            }
            newMemberMap.put(address, memberOptional.orElse(member));
        }

        if (!inCluster) {
            Loggers.CLUSTER.info("Current node is not in cluster: {}", newMemberMap.keySet());
            newMemberMap.put(this.current.getAddress(), this.current);
        }

        if (!ADDRESS_INDEXER.keySet().equals(newMemberMap.keySet())) {
            Loggers.CLUSTER.info("Cluster nodes updated to : {}", newMemberMap.keySet());
        }

        ADDRESS_INDEXER = newMemberMap;
        newMemberMap.values().forEach(this::updateContextPathIndex);

        if (hasChange) {
            notifier.publish(new MembersChangedEvent(ADDRESS_INDEXER.values(), ElementOperation.REPLACE));
        }
    }

    public Member memberOnline(final Address address) {
        Member member = ADDRESS_INDEXER.get(address);
        if (member != null) {
            memberOnline(member);
        }
        return member;
    }

    public void memberOnline(final Member member) {
        member.setFailAccessCnt(0);
        if (member.getState() != NodeState.UP) {
            member.setState(NodeState.UP);
            notifyMemberChange(member, ElementOperation.UPDATE);
        }
    }

    public Member memberOffline(final Address address) {
        Member member = ADDRESS_INDEXER.get(address);
        if (member != null) {
            memberOffline(member);
        }
        return member;
    }

    public void memberOffline(final Member member) {
        NodeState oldState = member.getState();
        NodeState newState = NodeState.UNHEALTHY;
        member.setState(newState);
        member.setFailAccessCnt(member.getFailAccessCnt() + 1);
        if (oldState != newState) {
            notifyMemberChange(member, ElementOperation.UPDATE);
        }
    }

    private void notifyMemberChange(Member member, ElementOperation operation) {
        MembersChangedEvent event = new MembersChangedEvent(member, operation);
        notifier.publish(event);
    }

    public Member updateState(long memberId, NodeState state) {
        Member member = ID_INDEXER.get(memberId);
        if (member == null) {
            return null;
        }

        if (member.getState() != state) {
            member.setState(state);
            notifyMemberChange(member, ElementOperation.UPDATE);
        }
        return member;
    }

    @Override
    public void clear() {
        this.ID_INDEXER.clear();
    }

}
