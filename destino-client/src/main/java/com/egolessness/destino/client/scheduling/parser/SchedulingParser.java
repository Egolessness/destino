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

package com.egolessness.destino.client.scheduling.parser;

import com.egolessness.destino.client.scheduling.functional.Scheduled;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.List;

/**
 * interface of analyzer with standard scheduled
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public interface SchedulingParser {

    @Nullable
    Scheduled<String, String> parseForInterface(@Nonnull Object instance);

    @Nonnull
    Scheduled<String, String> parse(@Nonnull Object instance, @Nonnull Method method, @Nullable String jobName);

    List<Scheduled<String, String>> parse(Object... tasks);

}
