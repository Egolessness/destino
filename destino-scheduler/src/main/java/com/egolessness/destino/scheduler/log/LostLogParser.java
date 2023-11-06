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

package com.egolessness.destino.scheduler.log;

import com.egolessness.destino.scheduler.message.Process;

/**
 * log parser for lost.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class LostLogParser implements LogParser {

    public final static LostLogParser INSTANCE = new LostLogParser();

    @Override
    public String getProcess() {
        return Process.LOST.name();
    }

    @Override
    public String getMessage() {
        return "The execution plan has lost its target instance.";
    }

}
