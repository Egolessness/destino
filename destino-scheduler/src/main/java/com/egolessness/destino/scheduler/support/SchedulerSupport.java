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

package com.egolessness.destino.scheduler.support;

import com.egolessness.destino.scheduler.model.SchedulerContext;
import com.egolessness.destino.scheduler.model.SchedulerInfo;
import com.cronutils.model.CompositeCron;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import com.google.common.base.Strings;
import com.egolessness.destino.common.utils.PredicateUtils;
import com.egolessness.destino.common.utils.SecurityUtils;
import com.egolessness.destino.scheduler.message.SchedulerKey;
import com.egolessness.destino.scheduler.message.SchedulerSign;
import com.egolessness.destino.scheduler.model.SchedulerCron;
import com.egolessness.destino.scheduler.model.SchedulerUpdatable;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * support fot scheduler
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class SchedulerSupport {

    private static final Map<CronType, CronParser> cronParsers;

    static {
        cronParsers = Stream.of(CronType.values()).collect(Collectors.toMap(Function.identity(),
                type -> new CronParser(CronDefinitionBuilder.instanceDefinitionFor(type))));
    }

    public static SchedulerKey buildSchedulerKey(final SchedulerInfo schedulerInfo) {
        return SchedulerKey.newBuilder()
                .setNamespace(Strings.nullToEmpty(schedulerInfo.getNamespace()))
                .setGroupName(Strings.nullToEmpty(schedulerInfo.getGroupName()))
                .setServiceName(Strings.nullToEmpty(schedulerInfo.getServiceName()))
                .setName(Strings.nullToEmpty(schedulerInfo.getName()))
                .build();
    }

    public static SchedulerKey buildSchedulerKey(final SchedulerUpdatable updatable, final String name) {
        return SchedulerKey.newBuilder()
                .setNamespace(Strings.nullToEmpty(updatable.getNamespace()))
                .setGroupName(Strings.nullToEmpty(updatable.getGroupName()))
                .setServiceName(Strings.nullToEmpty(updatable.getServiceName()))
                .setName(PredicateUtils.isNotEmpty(updatable.getName()) ? updatable.getName() : name)
                .build();
    }

    public static String buildSign(SchedulerContext context) {
        SchedulerInfo schedulerInfo = context.getSchedulerInfo();
        Cron cron = context.getCron();

        SchedulerSign.Builder builder = SchedulerSign.newBuilder()
                .setNamespace(Strings.nullToEmpty(schedulerInfo.getNamespace()))
                .setGroupName(Strings.nullToEmpty(schedulerInfo.getGroupName()))
                .setServiceName(Strings.nullToEmpty(schedulerInfo.getServiceName()))
                .setFailedRetryTimes(schedulerInfo.getFailedRetryTimes())
                .setForwardTimes(schedulerInfo.getForwardTimes())
                .setSafetyStrategy(schedulerInfo.getSafetyStrategy());

        if (PredicateUtils.isNotEmpty(schedulerInfo.getClusters())) {
            builder.addAllCluster(schedulerInfo.getClusters());
        }

        if (Objects.nonNull(cron)) {
            builder.setExpression(cron.asString());
        }

        return SecurityUtils.md5Hex(builder.build().toByteArray());
    }

    public static CronParser getParser(@Nonnull CronType type) {
        return cronParsers.get(type);
    }

    public static Optional<Cron> buildCron(SchedulerCron cron) {
        if (Objects.isNull(cron) || Objects.isNull(cron.getType()) || PredicateUtils.isEmpty(cron.getExpressions())) {
            return Optional.empty();
        }

        try {
            List<String> expressions = cron.getExpressions();
            CronParser parser = getParser(cron.getType());

            if (expressions.size() == 1) {
                Cron simpleCron = parser.parse(cron.getExpressions().get(0));
                return Optional.ofNullable(simpleCron);
            }

            List<Cron> cronList = expressions.stream().map(parser::parse).collect(Collectors.toList());
            return Optional.of(new CompositeCron(cronList));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

}
