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

package org.egolessness.destino.core.enumration;

import org.egolessness.destino.core.I18nMessages;

/**
 * some common i18n messages
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public enum CommonMessages {

    TIP_SAVE_TIMEOUT("tip.save.timeout"),
    TIP_UPDATE_TIMEOUT("tip.update.timeout"),
    TIP_DELETE_TIMEOUT("tip.delete.timeout"),
    TIP_CANNOT_BE_NULL("tip.cannot.be.null"),
    TIP_CANNOT_BE_BLANK("tip.cannot.be.blank"),
    PERMISSION_DENIED("permission.denied");

    private final String key;

    CommonMessages(String key) {
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
