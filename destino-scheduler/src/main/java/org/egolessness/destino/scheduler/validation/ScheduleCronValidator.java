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

package org.egolessness.destino.scheduler.validation;

import com.cronutils.parser.CronParser;
import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.scheduler.support.SchedulerSupport;
import org.egolessness.destino.scheduler.model.SchedulerCron;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * validator for {@link SchedulerCron}.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ScheduleCronValidator implements ConstraintValidator<CronValid, SchedulerCron> {

    @Override
    public boolean isValid(SchedulerCron cron, ConstraintValidatorContext context) {
        if (cron == null) {
            return true;
        }

        if (PredicateUtils.isEmpty(cron.getExpressions())) {
            return true;
        }

        if (cron.getType() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Cron type cannot be null.").addConstraintViolation();
            return false;
        }

        CronParser cronParser = SchedulerSupport.getParser(cron.getType());

        try {
            for (String expression : cron.getExpressions()) {
                cronParser.parse(expression).validate();
            }
            return true;
        } catch (IllegalArgumentException e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(e.getMessage()).addConstraintViolation();
            return false;
        }
    }

    public static void validate(SchedulerCron cron) throws InvalidExpressionException {
        CronParser cronParser = SchedulerSupport.getParser(cron.getType());
        for (String expression : cron.getExpressions()) {
            if (PredicateUtils.isEmpty(expression)) {
                throw new InvalidExpressionException("Empty expression.");
            }
            try {
                cronParser.parse(expression).validate();
            } catch (Exception e) {
                throw new InvalidExpressionException(e.getMessage());
            }
        }
    }

}
