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

package org.egolessness.destino.raft;

import com.alipay.sofa.jraft.*;
import com.alipay.sofa.jraft.Iterator;
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.core.StateMachineAdapter;
import com.alipay.sofa.jraft.entity.LeaderChangeContext;
import com.alipay.sofa.jraft.entity.LocalFileMetaOutter;
import com.alipay.sofa.jraft.error.RaftError;
import com.alipay.sofa.jraft.error.RaftException;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotReader;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotWriter;
import org.egolessness.destino.core.model.Member;
import org.egolessness.destino.raft.model.JRaftMetadata;
import com.google.protobuf.Any;
import com.google.protobuf.ZeroByteStringHelper;
import org.egolessness.destino.common.utils.ByteUtils;
import org.egolessness.destino.core.infrastructure.serialize.Serializer;
import org.egolessness.destino.core.infrastructure.serialize.SerializerFactory;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.core.consistency.processor.AtomicConsistencyProcessor;
import org.egolessness.destino.core.model.SnapshotFiles;
import org.egolessness.destino.core.model.LocalFileInfo;
import org.egolessness.destino.core.Loggers;
import org.egolessness.destino.core.message.DeleteRequest;
import org.egolessness.destino.core.message.SearchRequest;
import org.egolessness.destino.core.message.WriteRequest;

import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * raft state machine.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class JRaftStateMachine extends StateMachineAdapter {
    
    protected final AtomicConsistencyProcessor processor;

    private final ExecutorService snapshotExecutor;
    
    private volatile long term = -1;
    
    private volatile String leaderIp;

    private final Member current;

    public JRaftStateMachine(AtomicConsistencyProcessor processor, ExecutorService snapshotExecutor, Member current) {
        this.processor = processor;
        this.snapshotExecutor = snapshotExecutor;
        this.current = current;
    }
    
    @Override
    public void onApply(Iterator iter) {
        int index = 0;
        int applied = 0;
        Any any;
        JRaftClosure closure = null;
        try {
            while (iter.hasNext()) {
                Status status = Status.OK();
                try {
                    if (iter.done() != null) {
                        closure = (JRaftClosure) iter.done();
                        any = (Any) closure.getMessage();
                    } else {
                        any = Any.parseFrom(iter.getData().array());
                    }

                    Loggers.PROTOCOL.debug("[RAFT] Log accepted and being applied:\n {}", any);

                    Response response = null;

                    if (any.is(WriteRequest.class)) {
                        response = processor.write(any.unpack(WriteRequest.class));
                    } else if (any.is(DeleteRequest.class)) {
                        response = processor.delete(any.unpack(DeleteRequest.class));
                    } else if (any.is(SearchRequest.class)) {
                        response = processor.search(any.unpack(SearchRequest.class));
                    }

                    if (closure != null) {
                        closure.acceptResponse(response);
                    }
                } catch (Throwable e) {
                    index++;
                    status.setError(RaftError.UNKNOWN, e.toString());
                    if (Objects.nonNull(closure)) {
                        closure.acceptError(e);
                    }
                    throw e;
                } finally {
                    if (closure != null) {
                        closure.run(status);
                    }
                }
                
                applied ++;
                index ++;
                iter.next();
            }
        } catch (Throwable t) {
            Loggers.PROTOCOL.error("[RAFT] An error occurred while applying the log entry for group [{}].", processor.domain(), t);
            iter.setErrorAndRollback(index - applied, new Status(RaftError.ESTATEMACHINE,
                    "The raft state machine has an error: %s.", t.getMessage()));
        }
    }

    @Override
    public void onSnapshotSave(SnapshotWriter writer, Closure done) {
        snapshotExecutor.execute(() -> {
            SnapshotFiles snapshotFiles = new SnapshotFiles(writer.getPath());
            try {
                boolean result = processor.snapshotSave(snapshotFiles);
                boolean allAdd = true;
                Serializer serializer = SerializerFactory.getDefaultSerializer();
                for (Map.Entry<String, LocalFileInfo> fileInfoEntry : snapshotFiles.loadFiles().entrySet()) {
                    LocalFileMetaOutter.LocalFileMeta fileMeta = LocalFileMetaOutter.LocalFileMeta.newBuilder()
                            .setUserMeta(ZeroByteStringHelper.wrap(serializer.serialize(fileInfoEntry.getValue())))
                            .build();
                    allAdd = allAdd && writer.addFile(fileInfoEntry.getKey(), fileMeta);
                }
                if (result && allAdd) {
                    done.run(Status.OK());
                } else {
                    done.run(new Status(RaftError.EIO, "Failed to save the snapshot for group %s with the file path [%s].",
                            processor.domain().name().toLowerCase(Locale.ROOT), writer.getPath()));
                }
            } catch (Throwable e) {
                done.run(new Status(RaftError.EIO, "An error occurred while saving the snapshot for group %s with the file path [%s], error msg: %s",
                        processor.domain().name().toLowerCase(Locale.ROOT), writer.getPath(), e.getMessage()));
            }
        });
    }
    
    @Override
    public boolean onSnapshotLoad(SnapshotReader reader) {
        try {
            SnapshotFiles snapshotFiles = new SnapshotFiles(reader.getPath(), new HashMap<>(reader.listFiles().size()));
            Serializer serializer = SerializerFactory.getDefaultSerializer();
            for (String fileName : reader.listFiles()) {
                LocalFileMetaOutter.LocalFileMeta meta = (LocalFileMetaOutter.LocalFileMeta) reader.getFileMeta(fileName);
                byte[] bytes = meta.getUserMeta().toByteArray();
                LocalFileInfo fileInfo;
                if (ByteUtils.isEmpty(bytes)) {
                    fileInfo = new LocalFileInfo();
                } else {
                    fileInfo = serializer.deserialize(bytes, LocalFileInfo.class);
                }
                snapshotFiles.addFile(fileName, fileInfo);
            }
            return processor.snapshotLoad(snapshotFiles);
        } catch (Throwable throwable) {
            Loggers.PROTOCOL.error("An error occurred while loading raft snapshot for group {} from the file path [{}].",
                    processor.domain().name().toLowerCase(Locale.ROOT), reader.getPath(), throwable);
            return false;
        }
    }

    @Override
    public void onLeaderStart(long term) {
        this.term = term;
        this.leaderIp = current.getAddress().toString();
        this.processor.onMetadata(new JRaftMetadata(term, leaderIp));
    }

    @Override
    public void onLeaderStop(Status status) {
        super.onLeaderStop(status);
        this.leaderIp = null;
    }
    
    @Override
    public void onStartFollowing(LeaderChangeContext ctx) {
        this.term = ctx.getTerm();
        this.leaderIp = ctx.getLeaderId().getEndpoint().toString();
        this.processor.onMetadata(new JRaftMetadata(term, leaderIp));
    }
    
    @Override
    public void onConfigurationCommitted(Configuration conf) {
        this.processor.onMetadata(new JRaftMetadata(term, leaderIp));
    }
    
    @Override
    public void onError(RaftException e) {
        super.onError(e);
        this.processor.onError(e);
        this.processor.onMetadata(new JRaftMetadata(term, leaderIp, e.getMessage()));
    }

}
