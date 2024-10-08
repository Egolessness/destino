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

package org.egolessness.destino.registration.healthy;

import org.egolessness.destino.registration.repository.MetaHealthyRepository;
import org.egolessness.destino.registration.repository.RegistrationRepositorySelector;
import org.egolessness.destino.registration.setting.ClientSetting;
import org.egolessness.destino.registration.storage.specifier.RegistrationKeySpecifier;
import org.egolessness.destino.registration.support.RegistrationSupport;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.egolessness.destino.common.model.ServiceInstance;
import org.egolessness.destino.common.support.InstanceSupport;
import org.egolessness.destino.core.fixedness.DomainLinker;
import org.egolessness.destino.core.message.ConsistencyDomain;
import org.egolessness.destino.core.storage.specifier.Specifier;
import org.egolessness.destino.registration.message.RegistrationKey;
import org.egolessness.destino.registration.model.MetaHealthy;
import org.egolessness.destino.registration.model.Registration;
import org.egolessness.destino.registration.model.BeatInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Objects;

/**
 * health check result handler.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class HealthCheckHandler implements DomainLinker {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckHandler.class);

    private final Specifier<RegistrationKey, String> specifier = RegistrationKeySpecifier.INSTANCE;

    private final ClientSetting clientSetting;

    private final MetaHealthyRepository metaHealthyRepository;

    private final RegistrationRepositorySelector repositorySelector;
    
    @Inject
    public HealthCheckHandler(ClientSetting clientSetting, MetaHealthyRepository metaHealthyRepository,
                              RegistrationRepositorySelector repositorySelector) {
        this.clientSetting = clientSetting;
        this.metaHealthyRepository = metaHealthyRepository;
        this.repositorySelector = repositorySelector;
    }

    public void onSuccess(final HealthCheckContext context) {
        Objects.requireNonNull(context, "Health check context require not null.");

        if (context.isCancelled()) {
            return;
        }

        Registration registration = context.getRegistration();
        ServiceInstance instance = registration.getInstance();
        BeatInfo beatInfo = context.getBeatInfo();
        beatInfo.resetFail();
        beatInfo.refreshBeat();

        if (instance.isHealthy()) {
            return;
        }

        try {
            instance.setHealthy(true);
            MetaHealthy metaHealthy = new MetaHealthy(registration.getSource(), registration.getVersion(), true);
            metaHealthyRepository.set(specifier.transfer(context.getRegistrationKey()), metaHealthy);
            logger.info("Instance {} is healthy.", getInstanceInfo(context));
        } catch (Exception e) {
            logger.error("The health checker handle has an error.", e);
        }

    }

    public void onFail(final HealthCheckContext context, boolean aloha) {
        Objects.requireNonNull(context, "Health check context require not null.");

        if (context.isCancelled()) {
            return;
        }

        Registration registration = context.getRegistration();
        ServiceInstance instance = registration.getInstance();
        BeatInfo beatInfo = context.getBeatInfo();
        Duration deathTimeout = InstanceSupport.getDeathTimeout(instance);
        int failedCount = beatInfo.failedIncrement();

        if (System.currentTimeMillis() - beatInfo.getLastBeat() > deathTimeout.toMillis()
                && beatInfo.getLastBeat() > registration.getVersion()) {
            String registrationKeyString = specifier.transfer(context.getRegistrationKey());
            MetaHealthy metaHealthy = new MetaHealthy(registration.getSource(), registration.getVersion(), false);
            repositorySelector.select(instance.getMode()).del(registrationKeyString, registration);
            metaHealthyRepository.del(registrationKeyString, metaHealthy);
            logger.info("Instance {} has removed.", getInstanceInfo(context));
            return;
        }

        if (!instance.isHealthy()) {
            return;
        }

        try {
            if (aloha || failedCount >= clientSetting.getHealthCheckRounds()) {
                instance.setHealthy(false);
                MetaHealthy metaHealthy = new MetaHealthy(registration.getSource(), registration.getVersion(), false);
                metaHealthyRepository.set(specifier.transfer(context.getRegistrationKey()), metaHealthy);
                logger.info("Instance {} is unhealthy.", getInstanceInfo(context));
            } else {
                logger.info("Instance {} is disconnected.", getInstanceInfo(context));
            }
        } catch (Exception e) {
            logger.error("The health checker handle has an error.", e);
        }

    }

    private String getInstanceInfo(final HealthCheckContext context) {
        return RegistrationSupport.getInstanceInfo(context.getRegistrationKey().getInstanceKey());
    }

    @Override
    public ConsistencyDomain domain() {
        return ConsistencyDomain.REGISTRATION;
    }
}
