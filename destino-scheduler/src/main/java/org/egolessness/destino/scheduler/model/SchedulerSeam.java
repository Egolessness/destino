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

package org.egolessness.destino.scheduler.model;

import org.egolessness.destino.scheduler.model.enumration.SchedulerSchema;

import java.io.Serializable;

/**
 * scheduler seam.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class SchedulerSeam implements Serializable {

    private static final long serialVersionUID = 8600584903899762907L;

    private SchedulerSchema schema;

    private Object value;

    public SchedulerSeam() {
    }

    public SchedulerSeam(SchedulerSchema schema, Object value) {
        this.schema = schema;
        this.value = value;
    }

    public SchedulerSchema getSchema() {
        return schema;
    }

    public void setSchema(SchedulerSchema schema) {
        this.schema = schema;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public static SchedulerSeam ofNone() {
        return new SchedulerSeam(SchedulerSchema.NONE, null);
    }

}
