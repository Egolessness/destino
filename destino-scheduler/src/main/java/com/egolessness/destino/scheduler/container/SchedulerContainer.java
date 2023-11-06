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

package com.egolessness.destino.scheduler.container;

import com.egolessness.destino.scheduler.model.*;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.common.exception.DestinoRuntimeException;
import com.egolessness.destino.common.model.Script;
import com.egolessness.destino.common.utils.PredicateUtils;
import com.egolessness.destino.core.container.Container;
import com.egolessness.destino.core.enumration.Errors;
import com.egolessness.destino.core.exception.DuplicateIdException;
import com.egolessness.destino.core.exception.DuplicateNameException;
import com.egolessness.destino.scheduler.support.SchedulerSupport;
import com.egolessness.destino.scheduler.message.SchedulerKey;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

/**
 * container of scheduler.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class SchedulerContainer implements Container {

    /**
     * Node(namespace, Node(groupName, Node(serviceName, schedulerIds))) for scheduler
     */
    private final Node idNodeTree = new Node();

    private final ConcurrentSkipListMap<Long, SchedulerContext> schedulers = new ConcurrentSkipListMap<>();

    private final Map<SchedulerKey, Long> keyIndexer = new ConcurrentHashMap<>();

    private volatile long latestId = -1;

    private Node getNode(@Nullable String namespace, @Nullable String groupName, @Nullable String serviceName) {
        if (PredicateUtils.isEmpty(namespace)) {
            return idNodeTree;
        }
        Node nextNode = idNodeTree.get(namespace);
        if (PredicateUtils.isEmpty(groupName)) {
            return nextNode;
        }
        nextNode = nextNode.get(groupName);
        if (PredicateUtils.isEmpty(serviceName)) {
            return nextNode;
        }
        return nextNode.get(serviceName);
    }

    private Node findNode(@Nullable String namespace, @Nullable String groupName, @Nullable String serviceName) {
        if (PredicateUtils.isEmpty(namespace)) {
            return idNodeTree;
        }
        Node nextNode = idNodeTree.find(namespace);
        if (nextNode == null || PredicateUtils.isEmpty(groupName)) {
            return nextNode;
        }
        nextNode = nextNode.find(groupName);
        if (nextNode == null || PredicateUtils.isEmpty(serviceName)) {
            return nextNode;
        }
        return nextNode.find(serviceName);
    }

    private Node getNode(@Nonnull final SchedulerInfo schedulerInfo) {
        return getNode(schedulerInfo.getNamespace(), schedulerInfo.getGroupName(), schedulerInfo.getServiceName());
    }

    public Set<Long> getSchedulerIds(@Nullable String namespace, @Nullable String groupName, @Nullable String serviceName) {
        Node node = findNode(namespace, groupName, serviceName);
        Set<Long> schedulerIds = new HashSet<>();
        setSchedulerIdsFromNode(node, schedulerIds);
        return schedulerIds;
    }

    private void setSchedulerIdsFromNode(Node node, Set<Long> schedulerIds) {
        if (node == null) {
            return;
        }
        if (node.ids != null) {
            schedulerIds.addAll(node.ids);
        }
        if (node.next != null) {
            for (Node value : node.next.values()) {
                setSchedulerIdsFromNode(value, schedulerIds);
            }
        }
    }

    public Optional<SchedulerContext> find(Long id) {
        return Optional.ofNullable(schedulers.get(id));
    }

    public List<SchedulerContext> get(Collection<Long> ids) {
        return ids.stream().map(schedulers::get).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public Collection<SchedulerContext> loadSchedulerContexts() {
        return schedulers.values();
    }

    public long getLatestId() {
        return latestId;
    }

    public synchronized SchedulerContext add(@Nonnull SchedulerInfo schedulerInfo)
            throws DuplicateNameException, DuplicateIdException {

        long id = schedulerInfo.getId();
        if (schedulers.containsKey(id)) {
            throw new DuplicateIdException("Duplicate ID: % for scheduler.", id);
        }

        SchedulerKey key = SchedulerSupport.buildSchedulerKey(schedulerInfo);
        Long oldId = keyIndexer.putIfAbsent(key, id);

        if (isDuplicateName(oldId, id)) {
            throw new DuplicateNameException("Duplicate name under the target service.");
        }

        getNode(schedulerInfo).setId(id);
        SchedulerContext context = new SchedulerContext(schedulerInfo);
        schedulers.put(id, context);

        latestId = Long.max(latestId, id);
        return context;
    }

    public synchronized SchedulerContext update(long id, @Nonnull SchedulerUpdatable updatable) throws DestinoException {
        SchedulerContext schedulerContext = schedulers.get(id);
        if (schedulerContext == null) {
            return null;
        }

        SchedulerInfo schedulerInfo = schedulerContext.getSchedulerInfo();
        SchedulerKey key = SchedulerSupport.buildSchedulerKey(updatable, schedulerInfo.getName());
        Long oldId = keyIndexer.putIfAbsent(key, id);

        if (oldId == null) {
            Node node = getNode(schedulerInfo);
            boolean updatedResult = schedulerContext.update(updatable);
            if (!updatedResult) {
                throw new DestinoException(Errors.CLOCK_NOT_SYNCHRONIZATION, "The cluster clock is not synchronized.");
            }
            node.removeId(id);
            getNode(schedulerInfo).setId(id);
        } else if (oldId == id) {
            boolean updatedResult = schedulerContext.update(updatable);
            if (!updatedResult) {
                throw new DestinoException(Errors.CLOCK_NOT_SYNCHRONIZATION, "The cluster clock is not synchronized.");
            }
        } else {
            throw new DestinoException(Errors.STORAGE_WRITE_DUPLICATE, "Duplicate name for scheduler.");
        }

        return schedulerContext;
    }

    public SchedulerContext updateEnabled(long id, Activator activator) {
        if (activator == null) {
            return null;
        }

        return schedulers.computeIfPresent(id, (k, v) -> {
            if (activator.getTimestamp() > v.getSchedulerInfo().getUpdateTime()) {
                v.getSchedulerInfo().setEnabled(activator.isEnabled());
                v.getSchedulerInfo().setUpdateTime(activator.getTimestamp());
                return v;
            }
            throw new DestinoRuntimeException(Errors.CLOCK_NOT_SYNCHRONIZATION, "The cluster clock is not synchronized.");
        });
    }

    public SchedulerContext updateContact(long id, Contact contact) {
        if (contact == null) {
            return null;
        }
        return schedulers.computeIfPresent(id, (k, v) -> {
            v.getSchedulerInfo().setContact(contact);
            return v;
        });
    }

    public SchedulerContext updateScript(long id, Script script) {
        if (script == null) {
            return null;
        }
        return schedulers.computeIfPresent(id, (k, v) -> {
            SchedulerInfo schedulerInfo = v.getSchedulerInfo();
            Script origin = schedulerInfo.getScript();
            if (origin == null) {
                script.setVersion(1);
            } else if (origin.getType() != script.getType()) {
                script.setVersion(origin.getVersion() + 1);
            } else if (PredicateUtils.isBlank(origin.getContent())) {
                script.setVersion(origin.getVersion());
            } else if (Objects.equals(origin.getContent(), script.getContent())) {
                script.setVersion(origin.getVersion());
            } else {
                script.setVersion(origin.getVersion() + 1);
            }
            schedulerInfo.setScript(script);
            return v;
        });
    }

    private boolean isDuplicateName(Long oldId, long newId) {
        return oldId != null && newId != oldId;
    }

    public SchedulerContext remove(long id) {
        SchedulerContext schedulerContext = schedulers.remove(id);
        if (Objects.isNull(schedulerContext)) {
            return null;
        }
        schedulerContext.setDeleted(true);
        SchedulerInfo schedulerInfo = schedulerContext.getSchedulerInfo();
        keyIndexer.remove(SchedulerSupport.buildSchedulerKey(schedulerInfo));
        getNode(schedulerInfo).removeId(id);
        return schedulerContext;
    }

    @Override
    public void clear() {
        idNodeTree.clear();
        schedulers.clear();
        keyIndexer.clear();
        latestId = -1;
    }

    private static class Node {

        private Node senior;

        private String key;

        private Map<String, Node> next;

        private Set<Long> ids;

        private Node() {
        }

        private Node(Node senior, String key) {
            this.senior = senior;
            this.key = key;
        }

        public synchronized void setId(Long id) {
            if (ids == null) {
                this.ids = new HashSet<>();
            }
            this.ids.add(id);
        }

        public synchronized void removeId(Long id) {
            if (ids == null) {
                return;
            }
            this.ids.remove(id);
            if (permitClean() && senior != null) {
                senior.remove(key);
            }
        }

        public synchronized void remove(String key) {
            if (next != null) {
                next.computeIfPresent(key, (k, v) -> {
                    if (v.permitClean()) {
                        return null;
                    }
                    return v;
                });
            }
        }

        public synchronized Node find(String key) {
            if (next == null) {
                return null;
            }
            return next.get(key);
        }

        public synchronized Node get(String key) {
            if (next == null) {
                next = new HashMap<>();
            }
            return next.computeIfAbsent(key, k -> new Node(this, k));
        }

        public boolean permitClean() {
            return PredicateUtils.isEmpty(ids) && PredicateUtils.isEmpty(next);
        }

        public void clear() {
            next = null;
            ids = null;
        }

    }

}
