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

package org.egolessness.destino.scheduler.model.enumration;

import org.egolessness.destino.scheduler.model.Activator;
import org.egolessness.destino.scheduler.model.Contact;
import org.egolessness.destino.scheduler.model.SchedulerInfo;
import org.egolessness.destino.scheduler.model.SchedulerUpdatable;
import org.egolessness.destino.common.model.Script;

import javax.annotation.Nullable;

/**
 * schema of scheduler.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public enum SchedulerSchema {

    GET(0, SchedulerInfo.class),
    CREATE(1, SchedulerInfo.class),
    SET_CONTACT(2, Contact.class),
    EDIT_SCRIPT(3, Script.class),
    ACTIVATE(4, Activator.class),
    UPDATE(5, SchedulerUpdatable.class),
    DELETE(6, SchedulerInfo.class),
    NONE(-1, null);

    SchedulerSchema(int number, Class<?> type) {
        this.number = number;
        this.type = type;
    }

    int number;

    Class<?> type;

    public int getNumber() {
        return number;
    }

    public Class<?> getType() {
        return type;
    }


    public static @Nullable SchedulerSchema get(int number) {
        for (SchedulerSchema schema : SchedulerSchema.values()) {
            if (schema.number == number) {
                return schema;
            }
        }
        return null;
    }

}