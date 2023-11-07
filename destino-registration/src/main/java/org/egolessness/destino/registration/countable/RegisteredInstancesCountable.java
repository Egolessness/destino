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

package org.egolessness.destino.registration.countable;

import org.egolessness.destino.registration.RegistrationFilter;
import org.egolessness.destino.registration.container.RegistrationContainer;
import org.egolessness.destino.registration.model.Service;
import org.egolessness.destino.registration.model.ServiceCluster;
import com.google.inject.Inject;
import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.core.enumration.Action;
import org.egolessness.destino.core.fixedness.Countable;

import java.util.LinkedList;
import java.util.Map;

public class RegisteredInstancesCountable implements Countable {

    private final RegistrationContainer registrationContainer;

    private final RegistrationFilter registrationFilter;

    @Inject
    public RegisteredInstancesCountable(ContainerFactory containerFactory, RegistrationFilter registrationFilter) {
        this.registrationContainer = containerFactory.getContainer(RegistrationContainer.class);
        this.registrationFilter = registrationFilter;
    }

    @Override
    public String getKey() {
        return "registered_instances";
    }

    @Override
    public long getValue() {
        boolean hasPermission = registrationFilter.doDeepFilter(Action.READ, new LinkedList<>());
        if (!hasPermission) {
            return -1;
        }
        return registrationContainer.getNamespaces().stream().mapToInt(namespace -> {
            int count = 0;
            for (Map<String, Service> serviceMap : namespace.getGroups().values()) {
                for (Service service : serviceMap.values()) {
                    for (ServiceCluster cluster : service.getClusters()) {
                        count += cluster.getInstanceCount();
                    }
                }
            }
            return count;
        }).sum();
    }

}
