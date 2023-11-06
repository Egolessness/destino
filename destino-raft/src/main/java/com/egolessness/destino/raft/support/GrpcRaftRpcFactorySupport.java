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

package com.egolessness.destino.raft.support;

import com.alipay.sofa.jraft.rpc.impl.MarshallerRegistry;
import com.alipay.sofa.jraft.storage.io.ProtoBufFile;
import com.egolessness.destino.raft.JRaftTypings;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import com.egolessness.destino.raft.grpc.GrpcRaftRpcFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import static java.lang.invoke.MethodType.methodType;

/**
 * support for raft grpc factory
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class GrpcRaftRpcFactorySupport {

    public static GrpcRaftRpcFactory init(GrpcRaftRpcFactory raftRpcFactory) {
        MarshallerRegistry registry = raftRpcFactory.getMarshallerRegistry();
        for (JRaftTypings message : JRaftTypings.values()) {
            Message instance = message.getMessage();
            raftRpcFactory.registerProtobufSerializer(instance.getClass().getCanonicalName(), instance);
            registry.registerResponseInstance(instance.getClass().getCanonicalName(), JRaftTypings.RESPONSE.getMessage());
        }

        try {
            final DescriptorProtos.FileDescriptorSet descriptorSet = DescriptorProtos.FileDescriptorSet.parseFrom(ProtoBufFile.class
                    .getResourceAsStream("/raft.desc"));
            final List<Descriptors.FileDescriptor> resolveFDs = new ArrayList<>();
            for (final DescriptorProtos.FileDescriptorProto fdp : descriptorSet.getFileList()) {

                final Descriptors.FileDescriptor[] dependencies = new Descriptors.FileDescriptor[resolveFDs.size()];
                resolveFDs.toArray(dependencies);

                final Descriptors.FileDescriptor fd = Descriptors.FileDescriptor.buildFrom(fdp, dependencies);
                resolveFDs.add(fd);
                for (final Descriptors.Descriptor descriptor : fd.getMessageTypes()) {

                    final String className = fdp.getOptions().getJavaPackage() + "."
                            + fdp.getOptions().getJavaOuterClassname() + "$" + descriptor.getName();
                    final Class<?> clazz = Class.forName(className);
                    final MethodHandle getInstanceHandler = MethodHandles.lookup().findStatic(clazz,
                            "getDefaultInstance", methodType(clazz));
                    raftRpcFactory.registerProtobufSerializer(className, getInstanceHandler.invoke());
                }

            }
        } catch (final Throwable t) {
            t.printStackTrace();
        }
        return raftRpcFactory;
    }

}
