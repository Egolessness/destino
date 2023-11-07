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

package org.egolessness.destino.scheduler.model.response;

import org.egolessness.destino.common.model.Script;
import org.egolessness.destino.common.model.ScriptFate;

import java.io.Serializable;
import java.util.List;

/**
 * response of script view.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ScriptView implements Serializable {

    private static final long serialVersionUID = 5749394328459015188L;

    private long id;

    private Script script;

    private List<ScriptFate> histories;

    public ScriptView() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Script getScript() {
        return script;
    }

    public void setScript(Script script) {
        this.script = script;
    }

    public List<ScriptFate> getHistories() {
        return histories;
    }

    public void setHistories(List<ScriptFate> histories) {
        this.histories = histories;
    }

}
