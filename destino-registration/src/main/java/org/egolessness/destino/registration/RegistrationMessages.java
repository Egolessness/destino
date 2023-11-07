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

package org.egolessness.destino.registration;

import org.egolessness.destino.core.I18nMessages;

/**
 * i18n messages of registration.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public enum RegistrationMessages {

    NAMESPACE_DISPLAY("registration.namespace.display"),
    GROUP_DISPLAY("registration.group.display"),
    SERVICE_DISPLAY("registration.service.display"),
    CLUSTER_DISPLAY("registration.cluster.display"),
    TARGET_INSTANCE_DISPLAY("registration.target-instance.display"),
    NAMESPACE_ADD_DUPLICATE_NAME("namespace.add.duplicate.name"),
    SERVICE_ADD_DUPLICATE_NAME("service.add.duplicate.name"),
    SERVICE_DELETE_HAS_INSTANCE("service.delete.has.instances");

    private final String key;

    RegistrationMessages(String key) {
        this.key = key;
    }

    public String getValue() {
        return I18nMessages.getProperty(key);
    }

    @Override
    public String toString() {
        return getValue();
    }

}
