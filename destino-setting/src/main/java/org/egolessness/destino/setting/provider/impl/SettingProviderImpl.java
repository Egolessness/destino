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

package org.egolessness.destino.setting.provider.impl;

import org.egolessness.destino.common.utils.ByteUtils;
import org.egolessness.destino.core.storage.specifier.Specifier;
import org.egolessness.destino.setting.SettingKeySpecifier;
import org.egolessness.destino.setting.repository.SettingRepository;
import org.egolessness.destino.setting.SettingConsumerResolver;
import org.egolessness.destino.setting.SettingContainer;
import org.egolessness.destino.setting.SettingSupport;
import org.egolessness.destino.setting.model.SettingDomain;
import org.egolessness.destino.setting.model.SettingEntry;
import org.egolessness.destino.setting.provider.SettingProvider;
import org.egolessness.destino.setting.storage.SettingStorage;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.egolessness.destino.core.annotation.Sorted;
import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.core.enumration.SettingScope;
import org.egolessness.destino.core.exception.NotFoundException;
import org.egolessness.destino.core.setting.KeyStandard;
import org.egolessness.destino.core.spi.Setting;
import org.egolessness.destino.core.setting.SettingWriter;
import org.egolessness.destino.setting.message.SettingKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * setting provider implement.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class SettingProviderImpl implements SettingProvider {

    private final static Logger logger = LoggerFactory.getLogger(SettingProviderImpl.class);

    private final Specifier<SettingKey, String> specifier = SettingKeySpecifier.INSTANCE;

    private final SettingContainer settingContainer;

    private final SettingStorage settingStorage;

    private final SettingRepository settingRepository;

    private final Duration writeTimeout = Duration.ofSeconds(5);

    @Inject
    public SettingProviderImpl(SettingRepository settingRepository, ContainerFactory containerFactory, SettingStorage settingStorage) {
        this.settingRepository = settingRepository;
        this.settingContainer = containerFactory.getContainer(SettingContainer.class);
        this.settingStorage = settingStorage;
    }

    @Override
    public List<SettingDomain> get(SettingScope scope) throws Exception {
        return settingContainer.values().stream()
                .sorted(Comparator.comparingInt(setting -> {
                    Sorted sorted = setting.getClass().getAnnotation(Sorted.class);
                    return sorted != null ? sorted.value() : Integer.MAX_VALUE;
                }))
                .map(setting -> {
                    SettingDomain settingDomain = new SettingDomain();
                    settingDomain.setDomain(setting.subdomain());
                    settingDomain.setDesc(SettingSupport.getDomainDesc(setting.subdomain()));
                    List<SettingEntry> entries = new ArrayList<>();
                    for (KeyStandard<?> keyStandard : setting.getKeyStandards()) {
                        if (keyStandard.getScope() == scope) {
                            SettingEntry settingEntry = new SettingEntry();
                            settingEntry.setKey(keyStandard.name());
                            settingEntry.setDesc(SettingSupport.getKeyDesc(setting.subdomain(), keyStandard.name()));
                            try {
                                PropertyDescriptor propertyDescriptor = SettingConsumerResolver.getPropertyDescriptor(keyStandard.getWriter());
                                settingEntry.setValue(propertyDescriptor.getReadMethod().invoke(setting));
                                entries.add(settingEntry);
                            } catch (Exception e) {
                                logger.error("SettingConsumer resolver has an error.", e);
                            }
                        }
                    }
                    settingDomain.setDetails(entries);
                    return settingDomain;
                })
                .filter(d -> !d.getDetails().isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    public void update(String domain, String key, String value) throws Exception {

        Optional<Setting> settingOptional = settingContainer.get(domain);
        if (!settingOptional.isPresent()) {
            throw new NotFoundException("Not found for domain " + domain);
        }

        SettingWriter writer = settingOptional.get().getWriter(key);
        if (!writer.validate(value)) {
            throw new IllegalArgumentException("Invalid value.");
        }

        SettingKey settingKey = SettingSupport.buildKey(domain, key);

        switch (writer.scope()) {
            case GLOBAL:
                settingRepository.set(specifier.transfer(settingKey), ByteUtils.toBytes(value), writeTimeout);
                break;
            case LOCAL:
                settingStorage.set(specifier.transfer(settingKey), ByteUtils.toBytes(value));
        }
    }

    @Override
    public void batchUpdate(Map<SettingKey, String> settings) throws Exception {
        Map<String, byte[]> globalSettings = new HashMap<>();

        for (Map.Entry<SettingKey, String> entry : settings.entrySet()) {
            SettingKey key = entry.getKey();
            String value = entry.getValue();

            Optional<Setting> settingOptional = settingContainer.get(key.getDomain());
            if (!settingOptional.isPresent()) {
                throw new NotFoundException("Not found for domain " + key.getDomain());
            }

            SettingWriter writer = settingOptional.get().getWriter(key.getKey());
            if (!writer.validate(value)) {
                throw new IllegalArgumentException("Invalid value.");
            }

            switch (writer.scope()) {
                case GLOBAL:
                    globalSettings.put(specifier.transfer(key), ByteUtils.toBytes(value));
                    break;
                case LOCAL:
                    settingStorage.set(specifier.transfer(key), ByteUtils.toBytes(value));
                    break;
            }
        }

        if (!globalSettings.isEmpty()) {
            settingRepository.multiSet(globalSettings, writeTimeout);
        }
    }

}
