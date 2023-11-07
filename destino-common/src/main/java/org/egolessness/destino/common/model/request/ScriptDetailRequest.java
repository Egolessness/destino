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

package org.egolessness.destino.common.model.request;

import org.egolessness.destino.common.annotation.Http;
import org.egolessness.destino.common.annotation.Path;
import org.egolessness.destino.common.enumeration.HttpMethod;

import java.io.Serializable;

/**
 * request of read script detail
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Http(value = "/api/script/{id}", method = HttpMethod.GET)
public class ScriptDetailRequest implements Serializable {

    private static final long serialVersionUID = -8127414554601221600L;

    @Path(value = "id")
    private long id;

    @Path(value = "version")
    private long version;

    public ScriptDetailRequest() {
    }

    public ScriptDetailRequest(long id, long version) {
        this.id = id;
        this.version = version;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

}
