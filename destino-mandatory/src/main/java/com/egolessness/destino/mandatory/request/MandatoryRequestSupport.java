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

package com.egolessness.destino.mandatory.request;

import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import com.egolessness.destino.common.enumeration.Mark;
import com.egolessness.destino.core.message.Cosmos;
import com.egolessness.destino.core.support.ProtocolRequestSupport;
import com.egolessness.destino.core.support.SecuritySupport;
import com.egolessness.destino.mandatory.message.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * support of mandatory request.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class MandatoryRequestSupport extends ProtocolRequestSupport {

    public static MandatoryLoadRequest buildLoadRequest(List<Cosmos> cosmosList) {
        long timestamp = System.currentTimeMillis();
        return MandatoryLoadRequest.newBuilder().setTimestamp(timestamp)
                .setToken(SecuritySupport.createServerToken(timestamp, cosmosList))
                .addAllCosmos(cosmosList)
                .build();
    }

    public static MandatorySyncRequest buildSyncRequest(Collection<WriteInfo> writeInfos) {
        long timestamp = System.currentTimeMillis();

        MandatorySyncRequest.Builder builder = MandatorySyncRequest.newBuilder().setTimestamp(timestamp);

        List<String> encodes = Lists.newArrayList();
        for (WriteInfo writeInfo : writeInfos) {
            encodes.add(Mark.AND.join(writeInfo.getCosmos().toString(), writeInfo.getAppendCount(), writeInfo.getRemoveCount()));
            builder.addData(writeInfo);
        }

        String token = SecuritySupport.createServerToken(timestamp, encodes);
        builder.setToken(token);

        return builder.build();
    }

    public static WriteInfo buildWriteInfo(Cosmos cosmos, Map<ByteString, Message> messageMap) {
        List<VsData> appends = new ArrayList<>();
        List<VbKey> removes = new ArrayList<>();
        messageMap.forEach((key, message) -> {
            if (message instanceof VsData) {
                appends.add((VsData) message);
                return;
            }
            if (message instanceof VbKey) {
                removes.add((VbKey) message);
            }
        });
        return WriteInfo.newBuilder().setCosmos(cosmos).addAllAppend(appends).addAllRemove(removes).build();
    }

    public static MandatoryWriteRequest buildWriteRequest(Map<Cosmos, Map<ByteString, Message>> dataMap) {
        long timestamp = System.currentTimeMillis();
        MandatoryWriteRequest.Builder builder = MandatoryWriteRequest.newBuilder().setTimestamp(timestamp);

        Set<String> keys = new HashSet<>(dataMap.size());
        dataMap.forEach((cosmos, messageMap) -> {
            WriteInfo writeInfo = buildWriteInfo(cosmos, messageMap);
            for (ByteString byteString : messageMap.keySet()) {
                keys.add(byteString.toStringUtf8());
            }
            builder.addData(writeInfo);
        });

        String token = SecuritySupport.createServerToken(timestamp, keys);
        builder.setToken(token);

        return builder.build();
    }

    public static boolean validate(MandatoryLoadRequest request) {
        return System.currentTimeMillis() - request.getTimestamp() <= requestTimeValidDiff &&
                SecuritySupport.validateServerToken(request.getToken(), request.getTimestamp(), request.getCosmosList());
    }

    public static boolean validate(MandatorySyncRequest request) {
        List<String> encodes = request.getDataList().stream().map(writeInfo ->
                Mark.AND.join(writeInfo.getCosmos().toString(), writeInfo.getAppendCount(), writeInfo.getRemoveCount()))
                .collect(Collectors.toList());
        return System.currentTimeMillis() - request.getTimestamp() <= requestTimeValidDiff &&
                SecuritySupport.validateServerToken(request.getToken(), request.getTimestamp(), encodes);
    }

    public static boolean validate(MandatoryWriteRequest request) {
        Set<String> keys = new HashSet<>(request.getDataCount());
        request.getDataList().forEach(writeInfo -> {
            for (VsData vsData : writeInfo.getAppendList()) {
                keys.add(vsData.getKey().toStringUtf8());
            }
            for (VbKey vbKey : writeInfo.getRemoveList()) {
                keys.add(vbKey.getKey().toStringUtf8());
            }
        });
        return System.currentTimeMillis() - request.getTimestamp() <= requestTimeValidDiff &&
                SecuritySupport.validateServerToken(request.getToken(), request.getTimestamp(), keys);
    }

}
