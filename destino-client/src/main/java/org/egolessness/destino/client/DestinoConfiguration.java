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

package org.egolessness.destino.client;

import org.egolessness.destino.client.registration.ConsultationService;
import org.egolessness.destino.client.registration.RegistrationService;
import org.egolessness.destino.client.scheduling.LocalSchedulingService;
import org.egolessness.destino.client.scheduling.impl.LocalSchedulingServiceImpl;
import org.egolessness.destino.client.infrastructure.ScriptFactory;
import org.egolessness.destino.client.properties.DestinoProperties;
import org.egolessness.destino.client.infrastructure.PropertiesInitializer;
import org.egolessness.destino.client.infrastructure.Requester;
import org.egolessness.destino.client.registration.impl.RegistrationServiceImpl;
import org.egolessness.destino.client.registration.impl.ConsultationServiceImpl;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.fixedness.Lucermaire;

import java.util.concurrent.TimeoutException;

/**
 * Destino configuration
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@SuppressWarnings("SpellCheckingInspection")
public class DestinoConfiguration implements Lucermaire {

    private final DestinoProperties properties;

    private final Requester requester;

    private final LocalSchedulingService localSchedulingService;

    private final RegistrationService registrationService;

    private final ConsultationService consultationService;

    private final ScriptFactory scriptFactory;

    public DestinoConfiguration() {
        this(new DestinoProperties());
    }

    public DestinoConfiguration(final DestinoProperties properties) {
        this(properties, new ScriptFactory());
    }

    public DestinoConfiguration(final DestinoProperties properties, final ScriptFactory scriptFactory) {
        this.properties = PropertiesInitializer.init(properties);
        this.scriptFactory = scriptFactory;
        this.requester = new Requester(properties);
        this.localSchedulingService = new LocalSchedulingServiceImpl(requester, properties, scriptFactory);
        this.registrationService = new RegistrationServiceImpl(requester);
        this.consultationService = new ConsultationServiceImpl(requester, properties);
    }

    public boolean serverCheck() throws TimeoutException {
        return requester.serverCheck();
    }

    public DestinoProperties getProperties() {
        return properties;
    }

    public LocalSchedulingService getLocalSchedulingService() {
        return localSchedulingService;
    }

    public RegistrationService getRegistrationService() {
        return registrationService;
    }

    public ConsultationService getConsultationService() {
        return consultationService;
    }

    public ScriptFactory getScriptFactory() {
        return scriptFactory;
    }

    @Override
    public void shutdown() throws DestinoException {
        localSchedulingService.shutdown();
        consultationService.shutdown();
        registrationService.shutdown();
        requester.shutdown();
    }

}
