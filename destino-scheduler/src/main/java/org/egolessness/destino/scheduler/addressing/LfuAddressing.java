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

package org.egolessness.destino.scheduler.addressing;

import org.egolessness.destino.common.infrastructure.FifoCache;
import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.registration.message.RegistrationKey;
import org.egolessness.destino.scheduler.message.AddressingStrategy;
import org.egolessness.destino.scheduler.model.InstancePacking;
import org.egolessness.destino.scheduler.model.SchedulerInfo;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * addressing of lfu.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class LfuAddressing extends AbstractAddressing {

    private final Node head = new Node();

    private final Map<RegistrationKey, Node> nodeMap = new HashMap<>();

    private final FifoCache<String, Boolean> cache = new FifoCache<>(32);

    protected LfuAddressing(ContainerFactory containerFactory, SchedulerInfo schedulerInfo) {
        super(containerFactory, schedulerInfo);
    }

    @Override
    public synchronized void accept(Collection<InstancePacking> values) {
        for (InstancePacking packing : values) {
            Node node = nodeMap.get(packing.getRegistrationKey());
            if (node == null) {
                node = new Node(packing);
                setFirst(node);
            } else {
                node.instancePacking = packing;
            }
        }
    }

    @Override
    public synchronized void lastDest(RegistrationKey registrationKey, long executionTime) {
        Boolean origin = cache.putIfAbsent(executionTime + registrationKey.toString(), Boolean.TRUE);
        if (origin != null) {
            return;
        }
        Node node = nodeMap.get(registrationKey);
        if (node == null) {
            node = new Node(registrationKey);
            setFirst(node);
            return;
        }
        selected(node);
    }

    @Override
    boolean isEmpty() {
        return nodeMap.isEmpty();
    }

    @Override
    public synchronized InstancePacking get() {

        Node node = head;
        while ((node = node.next) != null) {
            if (node.instancePacking == null) {
                node.pre.next = node.next;
                Node next = node.next;
                if (next != null) {
                    next.pre = node.pre;
                }
                nodeMap.remove(node.registrationKey);
                continue;
            }
            if (isAvailable(node.instancePacking)) {
                selected(node);
                return node.instancePacking;
            }
        }
        node = head;
        while ((node = node.next) != null) {
            if (!node.instancePacking.isRemoved()) {
                selected(node);
                return node.instancePacking;
            }
        }
        return null;
    }

    @Override
    public Collection<InstancePacking> all() {
        return nodeMap.values().stream().map(node -> node.instancePacking)
                .filter(Objects::nonNull).collect(Collectors.toList());
    }

    private void selected(Node node) {
        node.count += 1;
        while ((node.next) != null) {
            if (node.compareTo(node.next) > 0) {
                swapNext(node);
            } else {
                break;
            }
        }
    }

    private void setFirst(Node node) {
        node.pre = head;
        node.next = head.next;
        if (head.next != null) head.next.pre = node;
        head.next = node;
        nodeMap.put(node.registrationKey, node);
    }

    private void swapNext(Node node) {
        node.pre.next = node.next;
        node.next.pre = node.pre;
        node.pre = node.next;
        node.next = node.next.next;
    }

    @Override
    public void clear() {
        Node node = head;
        while ((node = node.next) != null) {
            if (node.instancePacking == null) {
                node.pre.next = node.next;
                node.next.pre = node.pre;
                nodeMap.remove(node.registrationKey);
            }
            node.instancePacking = null;
        }
    }

    @Override
    public AddressingStrategy strategy() {
        return AddressingStrategy.LFU;
    }

    private static class Node implements Comparable<Node> {

        private RegistrationKey registrationKey;

        private InstancePacking instancePacking;

        private long count;

        private Node pre;

        private Node next;

        public Node() {
        }

        public Node(RegistrationKey registrationKey) {
            this.registrationKey = registrationKey;
            this.count = 1;
        }

        public Node(InstancePacking instancePacking) {
            this.registrationKey = instancePacking.getRegistrationKey();
            this.instancePacking = instancePacking;
            this.count = 1;
        }

        @Override
        public int compareTo(@Nonnull Node node) {
            return Long.compare(count, node.count);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node frequency = (Node) o;
            return Objects.equals(registrationKey, frequency.registrationKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(registrationKey);
        }
    }

}
