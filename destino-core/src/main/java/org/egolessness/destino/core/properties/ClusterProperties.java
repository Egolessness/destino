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

package org.egolessness.destino.core.properties;

import org.egolessness.destino.core.fixedness.PropertiesValue;

import java.util.List;

import static org.egolessness.destino.common.constant.CommonConstants.DEFAULT;

/**
 * properties with prefix:destino.cluster
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ClusterProperties implements PropertiesValue {

    private static final long serialVersionUID = 5503169768986007289L;

    private String url;

    private List<String> nodes;

    private String group = DEFAULT;

    private MulticastProperties multicast = new MulticastProperties();

    private DiscoveryProperties discovery = new DiscoveryProperties();

    public ClusterProperties() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }

    public List<String> getNodes() {
        return nodes;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public MulticastProperties getMulticast() {
        return multicast;
    }

    public void setMulticast(MulticastProperties multicast) {
        this.multicast = multicast;
    }

    public DiscoveryProperties getDiscovery() {
        return discovery;
    }

    public void setDiscovery(DiscoveryProperties discovery) {
        this.discovery = discovery;
    }

}
