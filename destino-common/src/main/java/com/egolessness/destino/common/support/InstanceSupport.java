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

package com.egolessness.destino.common.support;

import com.egolessness.destino.common.constant.DefaultConstants;
import com.egolessness.destino.common.constant.InstanceMetadataKey;
import com.egolessness.destino.common.enumeration.ErrorCode;
import com.egolessness.destino.common.enumeration.Mark;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.common.model.ServiceInstance;
import com.egolessness.destino.common.model.request.InstanceRequest;
import com.egolessness.destino.common.utils.NumberUtils;
import com.egolessness.destino.common.utils.PredicateUtils;
import com.egolessness.destino.common.utils.SecurityUtils;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * support for instance
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class InstanceSupport {

    public static Optional<String> getMetadataInfo(final ServiceInstance instance, final String key) {
        Map<String, String> metadata = instance.getMetadata();
        if (PredicateUtils.isEmpty(metadata)) {
            return Optional.empty();
        }
        return Optional.ofNullable(metadata.get(key));
    }

    public static Duration getHeartbeatInterval(final ServiceInstance instance) {
        return getMetadataInfo(instance, InstanceMetadataKey.HEARTBEAT_INTERVAL)
                .map(NumberUtils::parseLong).map(Duration::ofMillis)
                .orElse(DefaultConstants.HEARTBEAT_INTERVAL);
    }

    public static Duration getHeartbeatTimeout(final ServiceInstance instance) {
        return getMetadataInfo(instance, InstanceMetadataKey.HEARTBEAT_TIMEOUT)
                .map(NumberUtils::parseLong).map(Duration::ofMillis)
                .orElse(DefaultConstants.HEARTBEAT_TIMEOUT);
    }

    public static Duration getDeathTimeout(final ServiceInstance instance) {
        return getMetadataInfo(instance, InstanceMetadataKey.DEATH_TIMEOUT)
                .map(NumberUtils::parseLong).map(Duration::ofMillis)
                .orElse(DefaultConstants.DEATH_TIMEOUT);
    }

    public static void setDefaultValue(final InstanceRequest request) {
        if (PredicateUtils.isEmpty(request.getNamespace())) {
            request.setNamespace(DefaultConstants.REGISTRATION_NAMESPACE);
        }
        if (PredicateUtils.isEmpty(request.getGroupName())) {
            request.setGroupName(DefaultConstants.REGISTRATION_GROUP);
        }
        ServiceInstance instance = request.getInstance();
        if (Objects.nonNull(instance) && PredicateUtils.isEmpty(request.getInstance().getCluster())) {
            request.getInstance().setCluster(DefaultConstants.REGISTRATION_CLUSTER);
        }
    }

    public static String buildClusterKey(String... clusters) {
        return Stream.of(clusters).filter(Objects::nonNull).sorted().map(SecurityUtils::urlEncode)
                .collect(Collectors.joining(Mark.AND.getValue()));
    }

    public static void validate(final ServiceInstance instance) throws DestinoException {
        try {
            Objects.requireNonNull(instance, "Service instance must not be null.");
            BeanValidator.validateWithException(instance);
        } catch (Exception e) {
            throw new DestinoException(ErrorCode.UNEXPECTED_PARAM, e.getMessage());
        }
        long heartbeatInterval = InstanceSupport.getHeartbeatInterval(instance).toMillis();
        if (heartbeatInterval <= 0) {
            throw new DestinoException(ErrorCode.UNEXPECTED_PARAM, "Service instance heartbeat interval must be greater than zero.");
        }
        long heartbeatTimeout = InstanceSupport.getHeartbeatTimeout(instance).toMillis();
        if (heartbeatInterval >= heartbeatTimeout) {
            throw new DestinoException(ErrorCode.UNEXPECTED_PARAM, "Service instance heartbeat interval must be less than heartbeat timeout.");
        }
        long deathTimeout = InstanceSupport.getDeathTimeout(instance).toMillis();
        if (heartbeatInterval >= deathTimeout) {
            throw new DestinoException(ErrorCode.UNEXPECTED_PARAM, "Service instance heartbeat interval must be less than death timeout.");
        }
    }

}
