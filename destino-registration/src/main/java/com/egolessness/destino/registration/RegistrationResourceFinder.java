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

package com.egolessness.destino.registration;

import com.egolessness.destino.registration.container.NamespaceContainer;
import com.egolessness.destino.registration.container.RegistrationContainer;
import com.egolessness.destino.registration.model.Namespace;
import com.egolessness.destino.registration.model.NamespaceInfo;
import com.egolessness.destino.registration.model.Service;
import com.egolessness.destino.registration.model.ServiceCluster;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.egolessness.destino.core.container.ContainerFactory;
import com.egolessness.destino.core.fixedness.ResourceFinder;
import com.egolessness.destino.core.message.ConsistencyDomain;
import com.egolessness.destino.core.model.Path;
import com.egolessness.destino.core.model.PathTree;

import java.util.*;
import java.util.stream.Collectors;

/**
 * resource finder of registration.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class RegistrationResourceFinder implements ResourceFinder {

    private final NamespaceContainer namespaceContainer;

    private final RegistrationContainer registrationContainer;

    @Inject
    public RegistrationResourceFinder(ContainerFactory containerFactory) {
        this.namespaceContainer = containerFactory.getContainer(NamespaceContainer.class);
        this.registrationContainer = containerFactory.getContainer(RegistrationContainer.class);
    }

    @Override
    public ConsistencyDomain domain() {
        return ConsistencyDomain.REGISTRATION;
    }

    @Override
    public List<Path> findNext(String... paths) {
        if (paths.length == 0) {
            return namespaceContainer.getNamespaces().stream().sorted().map(this::build).collect(Collectors.toList());
        }

        if (paths.length == 1) {
            Optional<Namespace> namespaceOptional = registrationContainer.findNamespace(Strings.nullToEmpty(paths[0]));
            return buildPaths(namespaceOptional.map(namespace -> namespace.getGroups().keySet()).orElse(Collections.emptySet()));
        }

        if (paths.length == 2) {
            Optional<Map<String, Service>> groupOptional = registrationContainer.findGroup(Strings.nullToEmpty(paths[0]),
                    Strings.nullToEmpty(paths[1]));
            return buildPaths(groupOptional.map(Map::keySet).orElse(Collections.emptySet()));
        }

        if (paths.length == 3) {
            Optional<Service> serviceOptional = registrationContainer.findService(Strings.nullToEmpty(paths[0]),
                    Strings.nullToEmpty(paths[1]), Strings.nullToEmpty(paths[2]));
            return buildPaths(serviceOptional.map(service -> service.getClusterStore().keySet()).orElse(Collections.emptySet()));
        }

        return Collections.emptyList();
    }

    private List<Path> buildPaths(Collection<String> resources) {
        return resources.stream().sorted().map(Path::new).collect(Collectors.toList());
    }

    private Path build(NamespaceInfo namespaceInfo) {
        return new Path(namespaceInfo.getName(), false);
    }

    private PathTree buildTree(ServiceCluster serviceCluster) {
        return new PathTree(serviceCluster.getName());
    }

    private PathTree buildTree(Service service, int level) {
        if (level <= 0) {
            return new PathTree(service.getServiceName());
        }
        return new PathTree(service.getServiceName(), buildTreesForClusters(service.getClusterStore().values()));
    }

    private PathTree buildTree(String groupName, Map<String, Service> group, int level) {
        if (level <= 0) {
            return new PathTree(groupName);
        }
        return new PathTree(groupName, buildTreesForServices(group.values(), -- level));
    }

    private PathTree buildTree(NamespaceInfo namespaceInfo, int level) {
        if (level <= 0) {
            return new PathTree(namespaceInfo.getName());
        }
        Namespace namespace = registrationContainer.getNamespace(namespaceInfo.getName());
        return new PathTree(namespaceInfo.getName(), buildTreesForGroups(namespace, -- level));
    }

    private List<PathTree> buildTreesForClusters(Collection<ServiceCluster> clusters) {
        return clusters.stream().map(this::buildTree).sorted(Comparator.comparing(PathTree::getPath)).collect(Collectors.toList());
    }

    private List<PathTree> buildTreesForServices(Collection<Service> services, int level) {
        return services.stream().map(service -> buildTree(service, level))
                .sorted(Comparator.comparing(PathTree::getPath))
                .collect(Collectors.toList());
    }

    private List<PathTree> buildTreesForGroups(Namespace namespace, int level) {
        if (namespace == null) {
            return Collections.emptyList();
        }
        List<PathTree> pathTrees = new ArrayList<>(namespace.getGroups().size());
        namespace.getGroups().forEach((name, group) -> pathTrees.add(buildTree(name, group, level - 1)));
        pathTrees.sort(Comparator.comparing(PathTree::getPath));
        return pathTrees;
    }

    @Override
    public List<PathTree> findTree(String[] paths, int level) {
        if (paths.length == 0) {
            return namespaceContainer.getNamespaces().stream().sorted()
                    .map(namespace -> buildTree(namespace, level)).collect(Collectors.toList());
        }

        if (paths.length == 1) {
            Optional<Namespace> namespaceOptional = registrationContainer.findNamespace(Strings.nullToEmpty(paths[0]));
            return namespaceOptional.map(namespace -> buildTreesForGroups(namespace, level)).orElse(Collections.emptyList());
        }

        if (paths.length == 2) {
            Optional<Map<String, Service>> groupOptional = registrationContainer.findGroup(Strings.nullToEmpty(paths[0]),
                    Strings.nullToEmpty(paths[1]));
            return groupOptional.map(group -> buildTreesForServices(group.values(), level)).orElse(Collections.emptyList());
        }

        if (paths.length == 3) {
            Optional<Service> serviceOptional = registrationContainer.findService(Strings.nullToEmpty(paths[0]),
                    Strings.nullToEmpty(paths[1]), Strings.nullToEmpty(paths[2]));
            return serviceOptional.map(service -> buildTreesForClusters(service.getClusterStore().values()))
                    .orElse(Collections.emptyList());
        }

        return Collections.emptyList();
    }
}
