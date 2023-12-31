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

package org.egolessness.destino.client.registration.message;

import java.util.Map;

/**
 * service info
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ServiceInfo {

    private int expectProvideLeast = 3;

    private Map<String, String> metadata;

    public int getExpectProvideLeast() {
        return expectProvideLeast;
    }

    public void setExpectProvideLeast(int expectProvideLeast) {
        this.expectProvideLeast = expectProvideLeast;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
}
