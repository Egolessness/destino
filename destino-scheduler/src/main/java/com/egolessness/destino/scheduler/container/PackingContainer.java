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

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.egolessness.destino.common.model.ServiceInstance;
import com.egolessness.destino.common.support.RequestSupport;
import com.egolessness.destino.common.utils.PredicateUtils;
import com.egolessness.destino.core.container.Container;
import com.egolessness.destino.core.infrastructure.notify.Notifier;
import com.egolessness.destino.core.infrastructure.notify.subscriber.Subscriber;
import com.egolessness.destino.registration.message.RegistrationKey;
import com.egolessness.destino.registration.model.event.InstanceChangedEvent;
import com.egolessness.destino.registration.model.Registration;
import com.egolessness.destino.scheduler.model.InstancePacking;
import com.egolessness.destino.scheduler.model.SchedulerInfo;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Consumer;

/**
 * container of instance packing.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class PackingContainer implements Container {

    /**
     * Map(SchedulerKey, Map(jobName, Set<InstancePacking>)) for schedule relate to instance
     */
    final ConcurrentSkipListMap<SchedulerKey, ConcurrentHashMap<String, Map<RegistrationKey, InstancePacking>>> SCHEDULES;

    @Inject
    public PackingContainer(Notifier notifier) {
        this.SCHEDULES = new ConcurrentSkipListMap<>();

        notifier.subscribe((Subscriber<InstanceChangedEvent>) event -> {
            RegistrationKey registrationKey = event.getRegistrationKey();
            ServiceInstance instance = event.getInstance();
            if (!instance.isEnabled() || PredicateUtils.isEmpty(instance.getJobs()) || !instance.isHealthy()) {
                remove(new SchedulerKey(registrationKey), event);
                return;
            }
            switch (event.getOperation()) {
                case ADD:
                case UPDATE:
                    add(new SchedulerKey(registrationKey), event);
                    break;
                case REMOVE:
                    remove(new SchedulerKey(registrationKey), event);
                    break;
            }
        });
    }

    public Optional<InstancePacking> getPacking(RegistrationKey key, String jobName) {
        return Optional.ofNullable(SCHEDULES.get(new SchedulerKey(key))).map(m -> m.get(jobName)).map(m -> m.get(key));
    }

    public Optional<InstancePacking> getPacking(RegistrationKey key, Collection<String> jobNames) {
        return Optional.ofNullable(SCHEDULES.get(new SchedulerKey(key))).map(m -> {
            for (String jobName : jobNames) {
                Map<RegistrationKey, InstancePacking> packingMap = m.get(jobName);
                InstancePacking packing = packingMap.get(key);
                if (packing != null) {
                    return packing;
                }
            }
            return null;
        });
    }

    public Optional<InstancePacking> removePacking(RegistrationKey key, String jobName) {
        return Optional.ofNullable(SCHEDULES.get(new SchedulerKey(key))).map(m -> m.get(jobName)).map(m -> m.remove(key));
    }

    public void acceptInstances(SchedulerInfo schedulerInfo, Consumer<Collection<InstancePacking>> acceptor) {
        SchedulerKey searchKey = new SchedulerKey(schedulerInfo);

        if (searchKey.isSpecific()) {
            ConcurrentHashMap<String, Map<RegistrationKey, InstancePacking>> sis = SCHEDULES.get(searchKey);
            if (Objects.isNull(sis)) {
                return;
            }
            Map<RegistrationKey, InstancePacking> packingMap = sis.get(schedulerInfo.getJobName());
            if (PredicateUtils.isEmpty(packingMap)) {
                return;
            }
            acceptor.accept(packingMap.values());
            return;
        }

        boolean namespaceEmpty = PredicateUtils.isEmpty(schedulerInfo.getNamespace());
        boolean groupNameEmpty = PredicateUtils.isEmpty(schedulerInfo.getGroupName());
        boolean serviceNameEmpty = PredicateUtils.isEmpty(schedulerInfo.getServiceName());
        boolean clusterEmpty = PredicateUtils.isEmpty(schedulerInfo.getClusters());
        NavigableMap<SchedulerKey, ConcurrentHashMap<String, Map<RegistrationKey, InstancePacking>>> fromMap = SCHEDULES.tailMap(searchKey);

        for (Map.Entry<SchedulerKey, ConcurrentHashMap<String, Map<RegistrationKey, InstancePacking>>> entry : fromMap.entrySet()) {
            SchedulerKey schedulerKey = entry.getKey();
            if (namespaceEmpty) {
                acceptor.accept(getInstances(entry.getValue(), schedulerInfo));
            } else if (!Objects.equals(schedulerInfo.getNamespace(), schedulerKey.namespace)) {
                break;
            }
            if (groupNameEmpty) {
                acceptor.accept(getInstances(entry.getValue(), schedulerInfo));
            } else if (!Objects.equals(schedulerInfo.getGroupName(), schedulerKey.groupName)) {
                break;
            }
            if (serviceNameEmpty) {
                acceptor.accept(getInstances(entry.getValue(), schedulerInfo));
            } else if (!Objects.equals(schedulerInfo.getServiceName(), schedulerKey.serviceName)) {
                break;
            }
            if (clusterEmpty) {
                acceptor.accept(getInstances(entry.getValue(), schedulerInfo));
            } else if (!schedulerInfo.getClusters().contains(schedulerKey.cluster)) {
                continue;
            }
            acceptor.accept(getInstances(entry.getValue(), schedulerInfo));
        }
    }

    private Collection<InstancePacking> getInstances(Map<String, Map<RegistrationKey, InstancePacking>> map, SchedulerInfo schedulerInfo) {
        Map<RegistrationKey, InstancePacking> packingMap = map.get(schedulerInfo.getJobName());
        if (Objects.isNull(packingMap)) {
            return Collections.emptySet();
        }
        return packingMap.values();
    }

    private boolean isReachable(Registration registration) {

        ServiceInstance instance = registration.getInstance();
        if (!instance.isEnabled() || PredicateUtils.isEmpty(instance.getJobs()) || !instance.isHealthy()) {
            return false;
        }

        return RequestSupport.isSupportRequestStreamReceiver(registration.getChannel()) ||
                (instance.getUdpPort() > 0 && instance.getUdpPort() < 0xFFFF);
    }

    private void add(SchedulerKey key, InstanceChangedEvent event) {
        if (!isReachable(event.getRegistration())) {
            remove(key, event);
            return;
        }

        InstancePacking instancePacking = new InstancePacking(event);

        ConcurrentHashMap<String, Map<RegistrationKey, InstancePacking>> schedulerMap = SCHEDULES.computeIfAbsent(key,
                k -> new ConcurrentHashMap<>());

        Set<String> schedulers = new HashSet<>(event.getInstance().getJobs());
        int parallelismThreshold = 100;
        schedulerMap.forEach(parallelismThreshold, (jobName, packingMap) -> {
            boolean removed = schedulers.remove(jobName);
            if (removed) {
                packingMap.put(instancePacking.getRegistrationKey(), instancePacking);
                return packingMap;
            }
            packingMap.remove(instancePacking.getRegistrationKey());
            if (packingMap.isEmpty()) {
                return null;
            }
            return packingMap;
        }, packingList -> {});
        for (String scheduler : schedulers) {
            schedulerMap.computeIfAbsent(scheduler, k -> new ConcurrentHashMap<>())
                    .put(instancePacking.getRegistrationKey(), instancePacking);
        }
        if (schedulerMap.isEmpty()) {
            SCHEDULES.computeIfPresent(key, (k, v) -> {
                if (v.isEmpty()) {
                    return null;
                }
                return v;
            });
        }
    }

    private void remove(SchedulerKey key, InstanceChangedEvent event) {
        RegistrationKey registrationKey = event.getRegistrationKey();
        Set<String> jobs = event.getInstance().getJobs();
        if (PredicateUtils.isNotEmpty(jobs)) {
            String jobName = jobs.iterator().next();
            removePacking(registrationKey, jobName).ifPresent(packing -> packing.setRemoved(true));
        }

        ConcurrentHashMap<String, Map<RegistrationKey, InstancePacking>> schedulerMap = SCHEDULES.get(key);

        if (Objects.nonNull(schedulerMap)) {
            int parallelismThreshold = 100;
            schedulerMap.forEachValue(parallelismThreshold, packingMap -> packingMap.remove(registrationKey));
            SCHEDULES.computeIfPresent(key, (k, v) -> {
                if (v.isEmpty()) {
                    return null;
                }
                return v;
            });
        }
    }

    public ConcurrentNavigableMap<SchedulerKey, ConcurrentHashMap<String, Map<RegistrationKey, InstancePacking>>> tail(
            SchedulerKey schedulerKey) {
        return SCHEDULES.tailMap(schedulerKey, true);
    }

    @Override
    public void clear() {
        SCHEDULES.clear();
    }

    private final static Comparator<SchedulerKey> comparator = Comparator.comparing(SchedulerKey::getNamespace)
            .thenComparing(SchedulerKey::getGroupName).thenComparing(SchedulerKey::getServiceName)
            .thenComparing(SchedulerKey::getCluster);

    public static class SchedulerKey implements Comparable<SchedulerKey> {

        private final String namespace;

        private final String groupName;

        private final String serviceName;

        private final String cluster;

        public SchedulerKey(RegistrationKey key) {
            this.namespace = key.getNamespace();
            this.groupName = key.getGroupName();
            this.serviceName = key.getServiceName();
            this.cluster = key.getInstanceKey().getCluster();
        }

        public SchedulerKey(SchedulerInfo info) {
            this.namespace = Strings.nullToEmpty(info.getNamespace());
            this.groupName = Strings.nullToEmpty(info.getGroupName());
            this.serviceName = Strings.nullToEmpty(info.getServiceName());
            if (PredicateUtils.isEmpty(info.getClusters())) {
                this.cluster = PredicateUtils.emptyString();
            } else if (info.getClusters().size() == 1) {
                this.cluster = info.getClusters().get(0);
            } else {
                this.cluster = PredicateUtils.emptyString();
            }
        }

        public SchedulerKey(String... names) {
            if (names.length > 3) {
                this.cluster = names[3];
            } else {
                this.cluster = PredicateUtils.emptyString();
            }

            if (names.length > 2) {
                this.serviceName = names[2];
            } else {
                this.serviceName = PredicateUtils.emptyString();
            }

            if (names.length > 1) {
                this.groupName = names[1];
            } else {
                this.groupName = PredicateUtils.emptyString();
            }

            if (names.length > 0) {
                this.namespace = names[0];
            } else {
                this.namespace = PredicateUtils.emptyString();
            }
        }

        public String getNamespace() {
            return namespace;
        }

        public String getGroupName() {
            return groupName;
        }

        public String getServiceName() {
            return serviceName;
        }

        public String getCluster() {
            return cluster;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SchedulerKey key = (SchedulerKey) o;
            return Objects.equals(namespace, key.namespace) && Objects.equals(groupName, key.groupName)
                    && Objects.equals(serviceName, key.serviceName) && Objects.equals(cluster, key.cluster);
        }

        @Override
        public int hashCode() {
            return Objects.hash(namespace, groupName, serviceName, cluster);
        }

        @Override
        public int compareTo(@Nonnull SchedulerKey next) {
            return comparator.compare(this, next);
        }

        public boolean isSpecific() {
            return PredicateUtils.isNotEmpty(namespace) && PredicateUtils.isNotEmpty(groupName) &&
                    PredicateUtils.isNotEmpty(serviceName) && PredicateUtils.isNotEmpty(cluster);
        }

    }

}
