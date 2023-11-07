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

import java.util.Set;

/**
 * properties with prefix:destino.core.inet
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class InetProperties implements PropertiesValue {

    private static final long serialVersionUID = -2092473075145038798L;

    private InetRefreshProperties refresh = new InetRefreshProperties();

    private boolean hostnameFirst;

    private boolean siteLocalInterfaceOnly;

    private Set<String> ignoredInterfaces;

    private Set<String> priorityInterfaces;

    public InetProperties() {
    }

    public InetRefreshProperties getRefresh() {
        return refresh;
    }

    public void setRefresh(InetRefreshProperties refresh) {
        this.refresh = refresh;
    }

    public boolean isHostnameFirst() {
        return hostnameFirst;
    }

    public void setHostnameFirst(boolean hostnameFirst) {
        this.hostnameFirst = hostnameFirst;
    }

    public boolean isSiteLocalInterfaceOnly() {
        return siteLocalInterfaceOnly;
    }

    public void setSiteLocalInterfaceOnly(boolean siteLocalInterfaceOnly) {
        this.siteLocalInterfaceOnly = siteLocalInterfaceOnly;
    }

    public Set<String> getIgnoredInterfaces() {
        return ignoredInterfaces;
    }

    public void setIgnoredInterfaces(Set<String> ignoredInterfaces) {
        this.ignoredInterfaces = ignoredInterfaces;
    }

    public Set<String> getPriorityInterfaces() {
        return priorityInterfaces;
    }

    public void setPriorityInterfaces(Set<String> priorityInterfaces) {
        this.priorityInterfaces = priorityInterfaces;
    }
}
