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

package com.egolessness.destino.server.discovery;

import com.egolessness.destino.core.container.ContainerFactory;
import com.egolessness.destino.core.container.MemberContainer;
import com.egolessness.destino.server.application.ApplicationHome;
import com.google.inject.Inject;
import com.egolessness.destino.common.enumeration.Mark;
import com.egolessness.destino.common.utils.ByteUtils;
import com.egolessness.destino.common.utils.PredicateUtils;
import com.egolessness.destino.core.enumration.DiscoveryType;
import com.egolessness.destino.core.enumration.NodeState;
import com.egolessness.destino.core.model.Member;
import com.egolessness.destino.core.properties.DestinoProperties;
import com.egolessness.destino.core.support.MemberSupport;
import com.egolessness.destino.core.Loggers;
import com.egolessness.destino.core.utils.FileUtils;
import com.egolessness.destino.server.spi.DiscoveryStrategy;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * members discovery from local file.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class FileConfigDiscoveryStrategy implements DiscoveryStrategy {

    private Set<Member> MEMBERS_BUFFER = new HashSet<>();
    
    private final MemberContainer memberContainer;

    private final Path confPath;

    @Inject
    public FileConfigDiscoveryStrategy(DestinoProperties destinoProperties, ContainerFactory containerFactory) {
        String configLocation = destinoProperties.getConfig().getLocation();
        if (PredicateUtils.isNotBlank(configLocation)) {
            confPath = Paths.get(configLocation, "cluster.conf");
        } else {
            ApplicationHome applicationHome = new ApplicationHome(getClass());
            String path = applicationHome.getSource().getParentFile().getPath();
            confPath = Paths.get(path, "cluster.conf");
        }
        this.memberContainer = containerFactory.getContainer(MemberContainer.class);
    }

    @Override
    public DiscoveryType type() {
        return DiscoveryType.CONFIG;
    }

    @Override
    public void start() {
    }

    @Override
    public Collection<Member> discoverMembers() {
        Collection<Member> members = Collections.emptyList();
        try (Reader reader = new InputStreamReader(new FileInputStream(confPath.toFile()), StandardCharsets.UTF_8)) {
            members = MemberSupport.parseMembersFromConfig(reader, DiscoveryType.CONFIG);
            if (members.isEmpty()) {
                MEMBERS_BUFFER.forEach(member -> member.setState(NodeState.DOWN));
                return MEMBERS_BUFFER;
            }
        } catch (FileNotFoundException ignore) {
        } catch (Exception e) {
            Loggers.CLUSTER.warn("Cluster discovery have exception when read config", e);
        }

        members.forEach(MEMBERS_BUFFER::remove);
        MEMBERS_BUFFER.forEach(member -> member.setState(NodeState.DOWN));
        MEMBERS_BUFFER.addAll(members);
        return MEMBERS_BUFFER;
    }

    @Override
    public void sync() {
        try {
            Set<Member> newBuffer = new HashSet<>(MEMBERS_BUFFER.size());
            boolean hasChanged = false;

            for (Member val : MEMBERS_BUFFER) {
                Member member = this.memberContainer.get(val.getAddress());
                if (member == null || member.getState() == NodeState.DOWN) {
                    hasChanged = true;
                } else {
                    newBuffer.add(member);
                }
            }
            MEMBERS_BUFFER = newBuffer;

            if (hasChanged) {
                String prefix = Mark.COMMENT.getValue() + " " + LocalDateTime.now();
                List<String> addressList = newBuffer.stream().map(MemberSupport::getMemberPhrase)
                        .sorted().collect(Collectors.toList());
                addressList.add(0, prefix);
                byte[] bytes = ByteUtils.toBytes(Mark.LINE_FEED.join(addressList));
                FileUtils.writeFile(confPath.toFile(), bytes, false);
            }

        } catch (Throwable throwable) {
            Loggers.CLUSTER.error("sync members write config file failed", throwable);
        }
    }

    @Override
    public void destroy() {}

}