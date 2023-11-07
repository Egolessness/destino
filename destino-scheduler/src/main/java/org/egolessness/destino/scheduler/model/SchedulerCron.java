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

import com.cronutils.model.CronType;
import jakarta.validation.constraints.Size;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.List;

/**
 * scheduler cron.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class SchedulerCron implements Serializable {

    private static final long serialVersionUID = 6422026398049227072L;

    @Nonnull
    private CronType type = CronType.QUARTZ;

    @Size(max = 200, message = "The expressions size must less than or equal to 200")
    private List<String> expressions;

    private String description;

    public List<String> getExpressions() {
        return expressions;
    }

    public void setExpressions(List<String> expressions) {
        this.expressions = expressions;
    }

    public CronType getType() {
        return type;
    }

    public void setType(CronType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
