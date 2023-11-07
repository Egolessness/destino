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

package org.egolessness.destino.raft.processor;

import org.egolessness.destino.common.infrastructure.Timer;
import org.egolessness.destino.core.enumration.Errors;
import org.egolessness.destino.raft.JRaftSnapshot;
import org.egolessness.destino.raft.properties.RaftProperties;
import org.egolessness.destino.common.infrastructure.ConcurrentHashSet;
import org.egolessness.destino.common.support.ResponseSupport;
import org.egolessness.destino.core.Loggers;
import org.egolessness.destino.core.consistency.decree.AtomicDecree;
import org.egolessness.destino.core.enumration.MetadataKey;
import org.egolessness.destino.core.fixedness.Metadata;
import org.egolessness.destino.core.properties.DestinoProperties;
import org.egolessness.destino.core.support.CosmosSupport;
import org.egolessness.destino.core.model.ProtocolMetadata;
import org.egolessness.destino.core.consistency.processor.AtomicConsistencyProcessor;
import org.egolessness.destino.core.model.SnapshotFiles;
import org.egolessness.destino.core.exception.SnapshotException;
import org.egolessness.destino.core.message.*;
import org.egolessness.destino.common.model.message.Response;
import org.apache.commons.lang.StringUtils;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.StampedLock;

/**
 * implement of atomic consistency processor.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class JRaftConsistencyProcessor implements AtomicConsistencyProcessor {

    private final ConsistencyDomain domain;

    private final Map<String, AtomicDecree> decrees = new ConcurrentHashMap<>();

    private final Set<JRaftSnapshot> snapshots = new ConcurrentHashSet<>();

    private final StampedLock lock = new StampedLock();

    private final ProtocolMetadata protocolMetadata;

    private final int snapshotIntervalSecs;

    private volatile String leader;

    private boolean error;

    public JRaftConsistencyProcessor(ConsistencyDomain domain, ProtocolMetadata metadata,
                                     RaftProperties raftProperties, DestinoProperties destinoProperties) {
        this.domain = domain;
        this.snapshotIntervalSecs = getSnapshotIntervalSecs(raftProperties, destinoProperties);
        this.protocolMetadata = metadata;
        this.protocolMetadata.subscribe(domain, MetadataKey.LEADER, (data, args) -> {
            if (!(data instanceof ProtocolMetadata.DataObservable)) {
                return;
            }
            Object leaderData = ((ProtocolMetadata.DataObservable) data).getData();
            String leaderIp = Objects.toString(leaderData, StringUtils.EMPTY);
            if (StringUtils.isNotBlank(leaderIp)) {
                if (!Objects.equals(leader, leaderIp)) {
                    Loggers.PROTOCOL.info("Raft group {} current leader is {}", getGroup(), leaderData);
                }
                this.leader = leaderIp;
            }
        });
    }

    private int getSnapshotIntervalSecs(RaftProperties raftProperties, DestinoProperties destinoProperties) {
        int interval = destinoProperties.getStorageProperties(domain).getSnapshotIntervalSecs();
        if (interval > 0) {
            return interval;
        }
        return raftProperties.getDefaultSnapshotIntervalSecs();
    }

    public void addAtomicDecree(String subdomain, AtomicDecree atomicDecree) {
        this.decrees.put(subdomain, atomicDecree);
        Cosmos cosmos = CosmosSupport.buildCosmos(domain, subdomain);
        this.snapshots.add(new JRaftSnapshot(cosmos, atomicDecree));
    }

    public String getGroup() {
        return domain.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public Response search(SearchRequest request) {
        long stamp = lock.readLock();
        try {
            AtomicDecree atomicDecree = decrees.get(request.getCosmos().getSubdomain());
            if (atomicDecree != null) {
                return atomicDecree.search(request);
            }
            return ResponseSupport.failed("Subdomain not exist.");
        } finally {
            lock.unlockRead(stamp);
        }
    }

    @Override
    public Response write(WriteRequest request) {
        long stamp = lock.readLock();
        try {
            AtomicDecree atomicDecree = decrees.get(request.getCosmos().getSubdomain());
            if (atomicDecree != null) {
                return atomicDecree.write(request);
            }
            return ResponseSupport.failed("Subdomain not exist.");
        } finally {
            lock.unlockRead(stamp);
        }
    }

    @Override
    public Response delete(DeleteRequest request) {
        long stamp = lock.readLock();
        try {
            AtomicDecree atomicDecree = decrees.get(request.getCosmos().getSubdomain());
            if (atomicDecree != null) {
                return atomicDecree.delete(request);
            }
            return ResponseSupport.failed("Subdomain not exist.");
        } finally {
            lock.unlockRead(stamp);
        }
    }

    @Override
    public void onMetadata(Metadata metadata) {
        protocolMetadata.put(domain, metadata.all());
    }

    @Override
    public void onError(Throwable throwable) {
        this.error = true;
        Loggers.PROTOCOL.error("Raft group {} has an error.", getGroup(), throwable);
    }

    @Override
    public boolean hasError() {
        return this.error;
    }

    @Override
    public Duration snapshotInterval() {
        return Duration.ofSeconds(snapshotIntervalSecs);
    }

    @Override
    public boolean snapshotSave(final SnapshotFiles files) throws SnapshotException {
        long stamp = lock.readLock();
        Timer timer = Timer.newInstance(true).startTiming();
        try {
            for (JRaftSnapshot snapshot : snapshots) {
                snapshot.save(files);
            }
            Loggers.PROTOCOL.info("Raft group {} snapshot save successfully in {} ms.", getGroup(), timer.getDurationMillis());
            return true;
        } catch (Throwable e) {
            Loggers.PROTOCOL.error("Raft group {} snapshot save failed, path:{} and file list:{}.", getGroup(), files.getPath(),
                    files.loadFiles(), e);
            throw new SnapshotException(Errors.SNAPSHOT_LOAD_FAIL, e);
        } finally {
            lock.unlockRead(stamp);
        }
    }

    @Override
    public boolean snapshotLoad(final SnapshotFiles files) throws SnapshotException {
        long stamp = lock.writeLock();
        Timer timer = Timer.newInstance(true).startTiming();
        try {
            for (JRaftSnapshot snapshot : snapshots) {
                snapshot.load(files);
            }
            Loggers.PROTOCOL.info("Raft group {} snapshot loaded successfully in {} ms.", getGroup(), timer.getDurationMillis());
            return true;
        } catch (Throwable e) {
            Loggers.PROTOCOL.error("Raft group {} snapshot load failed, path:{} and file list:{}.", getGroup(), files.getPath(),
                    files.loadFiles(), e);
            throw new SnapshotException(Errors.SNAPSHOT_LOAD_FAIL, e);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    @Override
    public ConsistencyDomain domain() {
        return domain;
    }

    public boolean hasLeader() {
        return StringUtils.isNotBlank(leader);
    }
}
